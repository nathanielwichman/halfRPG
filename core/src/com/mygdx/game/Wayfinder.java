package com.mygdx.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;

/**
 * Static class for pathfinding tasks, may be replaced with more efficient/sophisticated
 *  code at some point 
 */
public class Wayfinder {
	
	/**
	 * Given a playable actor and a map finds all locations they can move to in one turn
	 * based on the actor's speed, the terrain, etc.
	 *
	 * TODO: Make it work for non-player actors, filter out unreachable states, make efficient  
	 *
	 * @param actor the PlayerActor looking to move
	 * @param map the map the PlayerActor is in
	 * @return a map of locations to the amount of moves the actor will use to reach it
	 *  		May contain some unreachable tiles (the moves left will be negative)
	 */
	public static Map<Vector2, Integer> getAllMoveableTiles(PlayerActor actor, MapInfo map) {
		Map<Vector2, Integer> checkedTiles = new HashMap<>();  // all tiles explored and the moves left after exploring
		Queue<Vector2> tilesToCheck = new Queue<>();  // queue of tiles to explore around
		Vector2 startingPosition = actor.getCell();
		int actorSpeed = actor.getSpeed();
		
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
	
		return checkedTiles;
		
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
				if (!checkedTiles.contains(tileToCheck))  // should mean we don't add the base tile to list
					tilesToCheck.add(tileToCheck);
			}
		}
		return tilesToCheck;
	}
}
