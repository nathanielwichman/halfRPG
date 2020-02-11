package com.mygdx.game;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class MapLoader {
	public static void loadEnemies(RPGStage stage, TiledMap tiled) {
		TiledMapTileLayer enemyLayer = (TiledMapTileLayer) tiled.getLayers().get("Markers");
		enemyLayer.setVisible(false);
		for (int x = 0; x < enemyLayer.getWidth(); x++) {
			for (int y = 0; y < enemyLayer.getHeight(); y++) {
				Cell c = enemyLayer.getCell(x, y);
				if (c != null)
					stage.addCharacter(new EnemyActor(stage, CharacterInfo.getCharacterInfo("SkeletonPunchingBag")),
						false, x, y);
			}
		}	
	}
}
