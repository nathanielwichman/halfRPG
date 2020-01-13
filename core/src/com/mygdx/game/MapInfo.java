package com.mygdx.game;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

/**
 * Stores all relevant info for a map and provides an API for getting
 * relevant information about a given tile. Loads based off of a tiledMap.
 * 
 * TODO: Add multi-layer support, make setup run off xml etc., allow for temp
 * effects
 *
 */
public class MapInfo {
	private TiledMap tiledMap;  
	private TileInfo[][] infoMap;  // 2D map array of tiles reflecting the tiledMap
	private Set<CharacterActor> characters;
	
	/**
	 * Generates a 2D array of tiles with all relevant game info based off a given TiledMap
	 * @param base The TileMap being used
	 */
	public MapInfo(TiledMap base) {
		characters = new HashSet<>();
		tiledMap = base;
		setupTiles();
		infoMap = MapInfo.getInfoForLayer((TiledMapTileLayer) tiledMap.getLayers().get(0));
	}
	
	/**
	 * Adds an actor to the map - important for tracking collisions, etc. 
	 * @param a the character to add
	 * @return true if added, false if already added
	 */
	public boolean addCharacter(CharacterActor a) {
		return characters.add(a);
	}
	
	/**
	 * Removes an actor from the map, i.e. if they died
	 * @param a the actor to remove
	 * @return true if removed, false if not found
	 */
	public boolean removeCharacter(CharacterActor a) {
		return characters.remove(a);
	}
	
	public Map<Vector2, CharacterActor> getCharacters() {
		Map<Vector2, CharacterActor> characterLocations = new HashMap<>();
		for (CharacterActor c : characters) {
			characterLocations.put(c.getCell(), c);
		}
		return characterLocations;
	}
	
	public Set<Vector2> getSelectedCharacterPositions(Class type) {
		Set<Vector2> characterPositions = new HashSet<>();
		for (CharacterActor c : characters) {
			if (type.isInstance(c))
				characterPositions.add(c.getCell());
		}
		return characterPositions;
	}
	
	public Set<Vector2> getCharacterPositions() {
		return getSelectedCharacterPositions(CharacterActor.class);
	}
	
	public Set<Vector2> getEnemyPositions() {
		return getSelectedCharacterPositions(EnemyActor.class);
	}
	
	public Set<Vector2> getPlayerPositions() {
		return getSelectedCharacterPositions(PlayerActor.class);
	}
	
	
	/**
	 * @return the TiledMap this class is based off of
	 */
	public TiledMap getTiledMap() {
		return tiledMap;
	}
	
	/**
	 * Defines the attributes of each TiledMap tile based on it's id. 
	 * 
	 * TODO: Make this load from some document which defines tile info / tileId
	 */
	private void setupTiles() {
		TileInfo.setupTile("floor", 1, false, tiledMap.getTileSets().getTile(3));
		TileInfo.setupTile("wall", 1, true, tiledMap.getTileSets().getTile(1));
	}
	
	public boolean inMapBounds(Vector2 v) {
		return (v.x >= 0 && v.x < infoMap.length) && (v.y >= 0 && v.y < infoMap[0].length);
	}
	
	public Vector2 getMapSize() {
		return new Vector2(infoMap.length, infoMap[0].length);
	}
	
	/**
	 * @param v A vector representing a tile position on the map
	 * @return null if given location not in current map, else the tile at that
	 *  location's TileInfo
	 */
	public TileInfo getTileInfo(Vector2 v) {
		return getTileInfo((int) v.x,(int) v.y);
	}
	
	/**
	 * @param v A vector representing a tile position on the map
	 * @return -1 if given location not in current map, else the tile at that
	 * 	location's speed to cross
	 */
	public int getTileSpeedToCross(Vector2 v) {
		return getTileSpeedToCross((int) v.x, (int) v.y);
	}
	
	/**
	 * @param x The x value of a tile position on the map
	 * @param y The y value of a tile position on the map
	 * @return null if given location not in current map, else the tile at that
	 *  location's TileInfo
	 */
	public TileInfo getTileInfo(int x, int y) {
		if (x < 0 || x > infoMap.length || y < 0 || y > infoMap[x].length) {
			return null;
		} else {
			return infoMap[x][y];
		}
	}
	
	/**
	 * @param x The x value of a tile position on the map
	 * @param y The y value of a tile position on the map
	 * @return -1 if given location not in current map, else the tile at that
	 * 	location's speed to cross
	 */
	public int getTileSpeedToCross(int x, int y) {
		TileInfo t = this.getTileInfo(x, y);
		if (t == null) {
			return -1;
		} else {
			return t.getSpeedToCross();
		}
	}
	
	/**
	 * Returns a 2D array of TileInfo reflecting a layer in a TiledMap
	 * @param layer A TiledMap's layer you want to load 
	 * @return A 2D array of TileInfo for that layer
	 */
	public static TileInfo[][] getInfoForLayer(TiledMapTileLayer layer) {
		TileInfo[][] newMap = new TileInfo[layer.getWidth()][layer.getHeight()];
		for (int x = 0; x < layer.getWidth(); x++) {
			for (int y = 0; y < layer.getHeight(); y++) {
				TileInfo t = TileInfo.getTile(layer.getCell(x, y).getTile().getId());
				assert t != null : "Tile ID not loaded before being placed";
				newMap[x][y] = t;
			}
		}
		return newMap;
	}
	
}
