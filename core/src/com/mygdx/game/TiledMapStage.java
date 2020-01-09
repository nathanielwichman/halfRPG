package com.mygdx.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class TiledMapStage extends Stage {
	private TiledMap map;
		
	public TiledMapStage(TiledMap map) {
		this.map = map;
		
		for (MapLayer layer : map.getLayers()) {
			TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
			createActorsForLayer(tileLayer);
		}
	}
	
	public void createActorsForLayer(TiledMapTileLayer tileLayer) {
		for (int x = 0; x < tileLayer.getWidth(); x++) {
			for (int y = 0; y < tileLayer.getHeight(); y++) {
				TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
				TiledMapActor actor = new TiledMapActor(map, tileLayer, cell);
				actor.setBounds(x * tileLayer.getTileWidth(), y * tileLayer.getTileHeight(),
						tileLayer.getTileWidth(), tileLayer());
				actor.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						System.out.println(actor.getCell() + " has been clicked");
					}
				});
			}
		}
	}
}
