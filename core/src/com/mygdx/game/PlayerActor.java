package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * An actor representing a character the player can control.
 * Holds info about the characters abilities and display.
 */
public class PlayerActor extends Actor {
	private Texture texture;
	private final String name;
	private int speed;  // how many tiles can be moved each turn
	
	/**
	 * @param form The texture to represent this character
	 * @param name The name of this character
	 */
	public PlayerActor(Texture form, String name) {
		this.texture = form;
		this.name = name;
		this.setBounds(getX(), getY(), texture.getWidth(), texture.getHeight());
		this.speed = 5;
	}
	
	@Override
	public void draw(Batch batch, float alpha) {
		batch.draw(texture,  getX(),  getY());
	}
	
	/**
	 * @return this character's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return this character's tile location on the stage as a tile
	 */
	public Vector2 getCell() {
		return new Vector2(((int) getX()) / 64, ((int) getY()) / 64);
	}
	
	/**
	 * @return the number of tiles this character can move in a turn
	 */
	public int getSpeed() {
		return speed;
	}
	
	
}
