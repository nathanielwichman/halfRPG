package com.mygdx.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class TiledMapActor extends Actor {
	private TiledMap tiledMap;
	
	private TiledMapTileLayer layer;

	private TiledMapTileLayer.Cell cell;
	
	public TiledMapActor(TiledMap tiledMap, TiledMapTileLayer layer, TiledMapTileLayer.Cell cell) {
		this.tiledMap = tiledMap;
		this.layer = layer;
		this.cell = cell;
	}
	
	public TiledMapTileLayer.Cell getCell() {
		return this.cell;
	}
}
