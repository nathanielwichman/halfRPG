package com.mygdx.game;

import java.util.*;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/**
 * This class stores information about each type of tile in a map,
 * including references to the base TiledMap tile. There should only
 * ever be one instantiation of this class per tile type done via this
 * class's static factory methods
 */
public class TileInfo {
	private final String name;  // name of the tile
	private final int speedToCross;  // amount of movement it takes to cross this tile
	private final TiledMapTile tile;  // reference to the TiledMap tile
	private final int tiledId;  // Id of the Tiled tile
	private final boolean isWall;
	
	// Factory methods
	private static Map<String, TileInfo> instances;  // tracks which tile's have been setup
	private static Map<Integer, TileInfo> ids;  // for ease of use
	
	/**
	 * Factory method - do not call outside this class.
	 * Initializes a new type of time, which should be done
	 * only once per type. Name and Id must be unique.
	 */
	private TileInfo(String name, int speedToCross, boolean isWall,
			TiledMapTile tile) {
		this.name = name;
		this.isWall = isWall;
		this.speedToCross = speedToCross;
		this.tile = tile;
		this.tiledId = tile.getId();
		
		// setup static fields if not already created
		if (TileInfo.instances == null) {
			TileInfo.instances = new HashMap<>();
			TileInfo.ids = new HashMap<>();
		}
		
		TileInfo.instances.put(this.name, this);
		TileInfo.ids.put(this.tiledId, this);
	}
	
	/**
	 * Creates a new type of tile with the given information. name and the TileMap
	 * Tile's id must be unique 
	 * 
	 * @param name name of the tile
	 * @param speedToCross movement required for a character to cross this tile
	 * @param tile reference to the TileMapTile this class is describing
	 * @return true if properly setup, false if already created
	 */
	public static boolean setupTile(String name, int speedToCross, boolean isWall,
						  TiledMapTile tile) {
		if (TileInfo.instances == null || !TileInfo.instances.containsKey(name)) {
			new TileInfo(name, speedToCross, isWall, tile);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Factory method used to get a reference to a type of tile
	 * 
	 * @param name name of the tile to get
	 * @return a reference to the requested tile, or null if it hasn't yet been setup
	 */
	public static TileInfo getTile(String name) {
		if (!TileInfo.containsTile(name)) {
			return null;
		} else {
			return TileInfo.instances.get(name);
		}
	}
	
	/**
	 * Factory method used to get a reference to a type of tile
	 * 
	 * @param id Tiled id for the tile to get
	 * @return a reference to the requested tile, or null if it hasn't yet been setup
	 */
	public static TileInfo getTile(int id) {
		if (!TileInfo.containsTile(id)) {
			return null;
		} else {
			return TileInfo.ids.get(id);
		}
	}
	
	/**
	 * @param name name of a tile type
	 * @return true if tile type has been created, false otherwise
	 */
	public static boolean containsTile(String name) {
		if (TileInfo.instances == null || !TileInfo.instances.containsKey(name)) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * @param id Tiled id of a tile type
	 * @return true if tile type has been created, false otherwise
	 */
	public static boolean containsTile(int id) {
		if (TileInfo.ids == null || !TileInfo.ids.containsKey(id)) {
			return false;
		} else {
			return true;
		}
	}
	
	// instance methods
	
	/**
	 * @return name of tile
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return moves required for a character to cross this tile
	 */
	public int getSpeedToCross() {
		return this.speedToCross;
	}
	
	/**
	 * @return reference to the TileMapTile this class describes
	 */
	public TiledMapTile getTileTile() {
		return this.tile;
	}
	
	/**
	 * @return id of the Tiled tile this class describes
	 */
	public int getTiledId() {
		return this.tiledId;
	}
	
	public boolean isWall() {
		return this.isWall;
	}
}
