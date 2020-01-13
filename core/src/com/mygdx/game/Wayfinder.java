package com.mygdx.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.game.ActionProperties.CanMoveThrough;
import com.mygdx.game.ActionProperties.CanSelect;
import com.mygdx.game.ActionProperties.EffectedByTerrain;
import com.mygdx.game.ActionProperties.Properties;

/**
 * Static class for pathfinding tasks, may be replaced with more efficient/sophisticated
 *  code at some point 
 */
public class Wayfinder {
	static class DijkstraNode implements Comparable {
		public final Vector2 node;
		public int movesLeft;
		
		public DijkstraNode(Vector2 n, int m) {
			node = n;
			movesLeft = m;
		}

		@Override
		public int compareTo(Object arg0) {
			return this.movesLeft - ((DijkstraNode) arg0).movesLeft;
		}
		
		@Override
		public String toString() {
			return "(" + node.toString() + ", " + movesLeft + ")";
		}
	}
	
	
	/**
	 * Given a character actor and a map finds all locations they can move to in one turn
	 * based on the actor's speed, the terrain, etc.
	 *
	 * TODO: Make it work for non-player actors, filter out unreachable states, make efficient  
	 *
	 * @param actor the PlayerActor looking to move
	 * @param map the map the PlayerActor is in
	 * @return a map of locations to the amount of moves the actor will use to reach it
	 *  		May contain some unreachable tiles (the moves left will be negative)
	 */
	public static Map<Vector2, Integer> getAllMoveableTiles(CharacterActor actor, MapInfo map) {
		Map<Vector2, Integer> checkedTiles = new HashMap<>();  // all tiles explored and the moves left after exploring
		Queue<Vector2> tilesToCheck = new Queue<>();  // queue of tiles to explore around
		Vector2 startingPosition = actor.getCell();
		int actorSpeed = actor.getMaxSpeed();
		//System.out.println(actorSpeed);
		// setup starting position as first explored position
		tilesToCheck.addLast(startingPosition);
		checkedTiles.put(startingPosition, actorSpeed);
		
		while (!tilesToCheck.isEmpty()) {  // while we have moves left and unexplored tiles
			Vector2 tileToCheck = tilesToCheck.removeFirst();
			
			int speedLeft = checkedTiles.get(tileToCheck);
			// explore all adjacent tiles and add them to queue if the character will still have
			// moves left after reaching that space
			for (Vector2 adjacentTile : getAdjacentUncheckedTiles(checkedTiles.keySet(), tileToCheck)) {
				int newSpeedLeft = speedLeft - map.getTileSpeedToCross(adjacentTile);
				assert !checkedTiles.containsKey(adjacentTile) : "error in pathfinding alg";
				checkedTiles.put(adjacentTile, newSpeedLeft);
				if (newSpeedLeft > 0) {
					tilesToCheck.addLast(adjacentTile);
				}
			}
		}
	
		Set<Vector2> blocked = map.getCharacterPositions();  // get position of
	
		// remove unreachable tiles or tiles blocked by other tiles
		Iterator<Map.Entry<Vector2, Integer>> deleterator = checkedTiles.entrySet().iterator();
		while (deleterator.hasNext()) {
			Map.Entry<Vector2, Integer> pair = deleterator.next();
			if (pair.getValue() < 0 || blocked.contains(pair.getKey())) {
				//System.out.println(pair.getValue());
				//System.out.println(blocked.contains(pair.getKey()));
				deleterator.remove();
			}
		}
		
		return checkedTiles;
	}
	
	// can pass walls, can pass character, can pass ally, can pass enemy
	public static Set<Vector2> getAllSelectableTiles(CharacterActor actor, Vector2 startPosition, int reach, MapInfo map, ActionProperties p) {
		// modified in operation
		//Set<Vector2> checkedTiles = new HashSet<>();
		Map<Vector2, Integer> checkedTiles = new HashMap<>();
		Set<Vector2> selectableTiles = new HashSet<>();
		Queue<DijkstraNode> tilesToCheck = new Queue<>();  // TODO:CHANGE TO PRIORIT
		// for reference
		Map<Vector2, CharacterActor> charMap = map.getCharacters();
		
		// NOTE: We don't check if origin is moveable, this should be done before calling this method
		addIfSelectable(startPosition, map.getTileInfo(startPosition), charMap, p, selectableTiles, actor);
		tilesToCheck.addFirst(new DijkstraNode(startPosition, reach));
		
		while (tilesToCheck.notEmpty()) {
			
			DijkstraNode nodeToCheck = tilesToCheck.removeFirst();
			System.out.println(nodeToCheck);
			Vector2 tileToCheck = nodeToCheck.node;
			//addIfSelectable(tileToCheck, map.getTileInfo(tileToCheck), charMap, p, selectableTiles, actor);
			
			for (Vector2 v : getAdjacentUncheckedTiles(checkedTiles.keySet(), tileToCheck)) {
				if (map.inMapBounds(v)) {
					// TODO: this gives +1 move, consider how to hanle moveable vs selectable
					addIfSelectable(v, map.getTileInfo(v), charMap, p, selectableTiles, actor);
					if (addIfMoveable(v, map.getTileInfo(v), charMap, p, checkedTiles, nodeToCheck.movesLeft)) {
						tilesToCheck.addLast(new DijkstraNode(v, checkedTiles.get(v)));
					}
				}
			}
		}
		return selectableTiles;		
	}
	
	private static boolean addIfMoveable(Vector2 position, TileInfo t,
			Map<Vector2, CharacterActor> charMap, ActionProperties p, Map<Vector2, Integer> m, int movesLeft) {
		int newMovesLeft = -1;
		if (movesLeft > 0) {
			if (canMoveInto(position, t, charMap, p)) {
				if (p.is(EffectedByTerrain.RESPECT_TERRAIN)) {  // ignore terrain is default
					if (t.getSpeedToCross() <= movesLeft) {
						newMovesLeft = movesLeft - t.getSpeedToCross();
						//m.put(position, movesLeft-t.getSpeedToCross());
						//return true;
					}					
				} else {  // ignore terrain, we know moves > 0
					newMovesLeft = movesLeft - 1;
					//m.put(position, movesLeft-1);
					//return true;
				}
			}
		}
		if (!m.containsKey(position) || m.get(position) < newMovesLeft) {
			m.put(position, newMovesLeft);
			return  newMovesLeft > 0;
		}
		return false;
	}
	
	private static boolean canMoveInto(Vector2 position, TileInfo t,
			Map<Vector2, CharacterActor> charMap, ActionProperties p) {
		boolean notBlocked = !charMap.containsKey(position) ||
							 p.is(CanMoveThrough.CHARACTER) ||
							 (p.is(CanMoveThrough.PLAYER) && (charMap.get(position) instanceof PlayerActor)) ||
							 (p.is(CanMoveThrough.ENEMY) && (charMap.get(position) instanceof EnemyActor));
		return notBlocked && (!t.isWall() || p.is(CanMoveThrough.WALLS));
	}
	
	private static void addIfSelectable(Vector2 position, TileInfo t,
			Map<Vector2, CharacterActor> charMap, ActionProperties p, Set<Vector2> s, CharacterActor actor) {
		
		if (p.is(CanSelect.WALLS) && t.isWall()) {
			s.add(position);
		} else if (p.is(CanSelect.CHARACTER) && charMap.containsKey(position)) {
			s.add(position);
		} else if (p.is(CanSelect.ENEMY) && charMap.containsKey(position) && charMap.get(position) instanceof EnemyActor) {
			s.add(position);
		} else if (p.is(CanSelect.PLAYER) && charMap.containsKey(position) && charMap.get(position) instanceof PlayerActor) {
			s.add(position);
		} else if (actor != null && p.is(CanSelect.SELF) && position.equals(actor.getCell())) {
			s.add(position);
		} else if (p.is(CanSelect.TILE) && !t.isWall() && !charMap.containsKey(position)) {
			s.add(position);
		}
	}
	
	/**
	 * Helper method to get all valid adjacent tiles to explore 
	 * 
	 * @param checkedTiles set of tiles already checked over
	 * @param baseTile the tile to look around
	 * @return a set of vectors reflecting all adjacent tiles to explore
	 */
	private static Set<Vector2> getAdjacentUncheckedTiles(Set<Vector2> checkedTiles, Vector2 baseTile) {
		Set<Vector2> tilesToCheck = new HashSet<>();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Vector2 tileToCheck = new Vector2(baseTile.x + x, baseTile.y + y);
				//if (!checkedTiles.contains(tileToCheck))  // should mean we don't add the base tile to list
				tilesToCheck.add(tileToCheck);
			}
		}
		return tilesToCheck;
	}
}
