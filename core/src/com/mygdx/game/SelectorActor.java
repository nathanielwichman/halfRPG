package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Simple actor for the selector sprite which highlights the tile the user
 * is mousing over when an action will occur based on the clicked tile. 
 */
public class SelectorActor extends Actor {
	private Texture texture;
	
	/**
	 * Loads its own texture from assets and adds bounds
	 */
	public SelectorActor() {
		texture = new Texture(Gdx.files.internal("data/MiscSprites/selector.png"));
		setBounds(getX(), getY(), texture.getWidth(), texture.getHeight());
	}

	@Override
	public void draw(Batch batch, float alpha) {
		batch.draw(texture, getX(), getY());
	}
}
