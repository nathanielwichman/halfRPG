package com.mygdx.game;

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
	
	/**
	 * Generates a 2D array of tiles with all relevant game info based off a given TiledMap
	 * @param base The TileMap being used
	 */
	public MapInfo(TiledMap base) {
		tiledMap = base;
		setupTiles();
		infoMap = MapInfo.getInfoForLayer((TiledMapTileLayer) tiledMap.getLayers().get(0));
	}
	
	/**
	 * Getter
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
		TileInfo.setupTile("floor", 1, tiledMap.getTileSets().getTile(3));
		TileInfo.setupTile("wall", 99999, tiledMap.getTileSets().getTile(1));
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
