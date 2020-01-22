package com.mygdx.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.game.ActionProperties.CanMoveThrough;
import com.mygdx.game.ActionProperties.CanSelect;
import com.mygdx.game.ActionProperties.EffectedByDarkness;
import com.mygdx.game.ActionProperties.EffectedByTerrain;
import com.mygdx.game.ActionProperties.Properties;

/**
 * Static class for pathfinding tasks, may be replaced with more efficient/sophisticated
 *  code at some point 
 */
public class Wayfinder {
	
	/**
	 * Inner class for custom sorting using Dijkstra's algorithm
	 */
	static class DijkstraNode implements Comparable {
		public final Vector2 node;
		public int movesLeft;
		
		public DijkstraNode(Vector2 n, int m) {
			node = n;
			movesLeft = m;
		}

		@Override
		// return element with most moves left
		public int compareTo(Object arg0) {
			return ((DijkstraNode) arg0).movesLeft - this.movesLeft;
		}
		
		@Override
		public String toString() {
			return "(" + node.toString() + ", " + movesLeft + ")";
		}
	}
	
	
	/**
	 * Explores and finds all tiles a CharacterActor can reach given a speefic range and selection
	 * properties. Used for getting all possible tiles for movement, abilities, attacks, etc.
	 * @param actor The CharacterActor initiating the search
	 * @param origin Origin tile of the search
	 * @param reach Number of tiles the search can reach
	 * @param map mapInfo defining the current state of the map
	 * @param p ActionProperties defining the nature of selectable tiles (what can be selected,
	 * 		explored, etc).
	 * @return A map of selectable tiles to the number of moves left after moving there
	 * 		(speedLeft).
	 */
	public static Map<Vector2, Integer> getAllSelectableTiles2(CharacterActor actor, Vector2 origin,
			int reach, MapInfo map, ActionProperties p) {
		Set<Vector2> exploredNodes = new HashSet<>();
		PriorityQueue<DijkstraNode> tilesToCheck = new PriorityQueue<>();
		Map<Vector2, Integer> selectableTiles = new HashMap<>();
		
		// for reference
		Map<Vector2, CharacterActor> charMap = map.getCharacters();		
	
		// first iteration
		addIfSelectable(origin, reach, map.getTileInfo(origin), charMap, p, selectableTiles, actor);
		DijkstraNode start = new DijkstraNode(origin, reach);
		exploredNodes.add(start.node);
		tilesToCheck.add(start);
		
		while (!tilesToCheck.isEmpty()) {
			DijkstraNode nodeToCheck = tilesToCheck.poll();
			//System.out.println("exploring: " + (nodeToCheck.node.x - 5) + ", " + (nodeToCheck.node.y - 14) + ": " + nodeToCheck.movesLeft);
			
			// Check all adjacent nodes to see if they're selectable or ignorable
			for (Vector2 v : getAdjacentUncheckedTiles(exploredNodes, nodeToCheck.node, map)) {
				exploredNodes.add(v);
				// Can't select or explore darkness normally
				if (map.isDarkness(v) && p.isNot(EffectedByDarkness.IGNORE)) { continue; }
				
				// Ignoring movement restriction, are there enough moves left to reach tile?
				int movesLeftAfterEntering = reachable(v, nodeToCheck.movesLeft, map.getTileInfo(v), p);
				if (movesLeftAfterEntering >= 0) {
					// add to selectable/moveable queue if possible
					addIfSelectable(v, movesLeftAfterEntering, map.getTileInfo(v), charMap, p, selectableTiles, actor);
					if (movesLeftAfterEntering > 0 && canMoveInto(v, map.getTileInfo(v), charMap, p)) {
						tilesToCheck.add(new DijkstraNode(v, movesLeftAfterEntering));
					}
				}
			}
			
		}
		return selectableTiles;
		
	}
	
	/**
	 * Can the given tile be explored
	 * @param position Position of tile to explore
	 * @param t TileInfo about said tile
	 * @param charMap List of all characters on the map on their location
	 * @param p ActionProperties defining the movement
	 * @return true if explorable, false otherwise
	 */
	private static boolean canMoveInto(Vector2 position, TileInfo t,
			Map<Vector2, CharacterActor> charMap, ActionProperties p) {
		boolean notBlocked = !charMap.containsKey(position) ||
							 p.is(CanMoveThrough.CHARACTER) ||
							 (p.is(CanMoveThrough.PLAYER) && (charMap.get(position) instanceof PlayerActor)) ||
							 (p.is(CanMoveThrough.ENEMY) && (charMap.get(position) instanceof EnemyActor));
		return notBlocked && (!t.isWall() || p.is(CanMoveThrough.WALLS));
	}
	
	/**
	 * Can the given tile be reached given the remaining number of moves
	 * Note: Ignores movement restrictions - check with canMoveInto
	 * @param position Position to check
	 * @param movesLeft How many moves are left
	 * @param t TileInfo about the tile to be moved into
	 * @param p ActionProperties defining the movement
	 * @return The number of moves left after moving into said tile
	 *    (returns negative if not reachable)
	 */
	private static int reachable(Vector2 position, int movesLeft, TileInfo t, ActionProperties p) {
		if (p.is(EffectedByTerrain.RESPECT_TERRAIN)) {
			return movesLeft - t.getSpeedToCross();
		} else  {
			return movesLeft - 1;
		}
	}
	
	/**
	 * Given information about a tile, adds it to a map of selectable tiles with
	 * the moves remaining after selecting it if it's selectable given the tile 
	 * and movement properties
	 * @param position Position of a tile
	 * @param movesLeft Number of moves left
	 * @param t TileInfo describing tile to be examined
	 * @param charMap Map of all characters and their locations on map
	 * @param p ActionProperties defining the movement 
	 * @param m Map of tile locations to the number of moves left after reaching them
	 *      for all selectable tiles in the movement. Will add the given position and
	 *      moves left if the given tile is selectable
	 * @param actor Actor initiating the movement
	 */
	private static void addIfSelectable(Vector2 position, int movesLeft, TileInfo t,
			Map<Vector2, CharacterActor> charMap, ActionProperties p, Map<Vector2, Integer> m, CharacterActor actor) {
		
		if (p.is(CanSelect.WALLS) && t.isWall()) {
			m.put(position, movesLeft);
		} else if (p.is(CanSelect.CHARACTER) && charMap.containsKey(position)) {
			m.put(position, movesLeft);
		} else if (p.is(CanSelect.ENEMY) && charMap.containsKey(position) && charMap.get(position) instanceof EnemyActor) {
			m.put(position, movesLeft);
		} else if (p.is(CanSelect.PLAYER) && charMap.containsKey(position) && charMap.get(position) instanceof PlayerActor) {
			m.put(position, movesLeft);
		} else if (actor != null && p.is(CanSelect.SELF) && position.equals(actor.getCell())) {
			m.put(position, movesLeft);
		} else if (p.is(CanSelect.TILE) && !t.isWall() && !charMap.containsKey(position)) {
			m.put(position, movesLeft);
		}
	}
	
	/**
	 * Helper method to get all valid adjacent tiles to explore 
	 * 
	 * @param checkedTiles set of tiles already checked over
	 * @param baseTile the tile to look around
	 * @return a set of vectors reflecting all adjacent tiles to explore
	 */
	private static Set<Vector2> getAdjacentUncheckedTiles(Set<Vector2> checkedTiles, Vector2 baseTile, MapInfo map) {
		Set<Vector2> tilesToCheck = new HashSet<>();
		Vector2 bounds = map.getMapSize();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Vector2 tileToCheck = new Vector2(baseTile.x + x, baseTile.y + y);
				if (tileToCheck.x >= 0 && tileToCheck.y >= 0 && tileToCheck.x < bounds.x && tileToCheck.y < bounds.y) {
					if (!checkedTiles.contains(tileToCheck))  // should mean we don't add the base tile to list
						tilesToCheck.add(tileToCheck);
				}
			}
		}
		return tilesToCheck;
	}
	
	/**
	 * Gets all tiles in a list that are within line of sight of origin and returns them
	 * 
	 * @param origin Origin to check line of sight with
	 * @param points List of points to check
	 * @param m MapInfo describing the map
	 * @return A new set containing all elements in points that are also in line of sight
	 *     of origin
	 */
	public static Set<Vector2> getAllInSight(Vector2 origin, Set<Vector2> points, MapInfo m) {
		Set<Vector2> seeAble = new HashSet<>();
		for (Vector2 point : points) {
			if (traceLine(point, origin, m)) {
				seeAble.add(point);
			}
			
		}
		return seeAble;
	}
	
	/**
	 * Gets all tiles in a list that are not within line of sight of origin and returns them
	 * 
	 * @param origin Origin to check line of sight with
	 * @param points List of points to check
	 * @param m MapInfo describing the map
	 * @return A new set containing all elements in points that are not in line of sight
	 */
	public static Set<Vector2> getAllOutOfSight(Vector2 origin, Set<Vector2> points, MapInfo m) {
		Set<Vector2> seeAble = new HashSet<>();
		
		for (Vector2 point : points) {
			System.out.println(point);
			if (!traceLine(point, origin, m)) {
				seeAble.add(point);
			}
		}
		return seeAble;
	}
	
	/**
	 * Checks line of sight between two tiles by drawing a line between their centers
	 * and checking if any pixels in the line belong to wall tiles
	 * @param a A tile in the map
	 * @param b A tile in the map
	 * @param map MapInfo describing the map
	 * @return true if line of sight is unobstructed, false otherwise
	 */
	private static boolean traceLine(Vector2 a, Vector2 b, MapInfo map) {
		Set<Vector2> checkedTiles = new HashSet<>();  // to prevent unnecessary recompute, idk if worth
		
		// get true center tile positions
		int x0 = ((int) a.x) * 64 + 32; 
		int x1 = ((int) b.x) * 64 + 32;
		int y0 = ((int) a.y) * 64 + 32;
		int y1 = ((int) b.y) * 64 + 32;

		// Do line drawing algorithm
		int dx = (int) Math.abs(x1 - x0);
		int sx = x0 < x1 ? 1 : -1;
		
		int dy = (int) -Math.abs(y1 - y0);
		int sy = y0 < y1 ? 1 : -1;
		int err = dx + dy;

		while (x0 != x1 || y0 != y1) {
			// Found new pixel in line, check if intersects wall tile
			if (!checkPixel(x0,y0, checkedTiles, map, a, b)) {
				return false;
			}
			int e2 = 2*err;
			if (e2 >= dy) {
				err += dy;
				x0 += sx;
			}
			
			if (e2 <= dx) {
				err += dx;
				y0 += sy;
			}
		}
		return true;
	}
	
	/**
	 * Checks if a pixel corresponds to a wall tile. Does some fuzzing so if the pixel
	 * just barely intersects a wall still returns true
	 * @param x Pixel x location
	 * @param y Pixel y location
	 * @param checkedTiles Set of tiles we've already checked and confirmed are non-wall
	 * 		to save multiple lookups
	 * @param map MapInfo describing all tiles in map
	 * @param origin The start point of line - ignores if this tile is wall
	 * @param target The end point of the line - ignores if this tile is wall
	 * @return True if pixel is not in a wall tile, false otherwise
	 */
	private static boolean checkPixel(int x, int y, Set<Vector2> checkedTiles,
			MapInfo map, Vector2 origin, Vector2 target) {
		int gridx = x / 64;
		int gridy = y / 64;
		
		Vector2 v = new Vector2(gridx, gridy);
		if (v.equals(origin) || v.equals(target)) {
			return true;
		}
		
		// If nearby pixel not wall we're close enough to still return true
		for (int i = -2; i <= 2; i += 2) {
			for (int j = -2; j <= 2; j += 2) {
				if ((x + i) / 64 != gridx || (y+j) / 64 != gridy) {
					Vector2 altV = new Vector2((x+i)/64, (y+j)/64);
					if (checkedTiles.contains(altV)) {
						return true;
					} else {
						TileInfo t = map.getTileInfo(altV);
						if (!t.isWall()) {
							checkedTiles.add(altV);
							return true;
						}
					}
				}
			}
		}
		
		if (!checkedTiles.contains(v)) {
			TileInfo t = map.getTileInfo(v);
			
			checkedTiles.add(v);
			return !t.isWall();
		}
		return true;
	}
		
}
