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
 * 
 * TODO: Make this all loadable in some way
 */
public class PlayerActor extends CharacterActor {
	
	/**
	 * @param form The texture to represent this character
	 * @param name The name of this character
	 */
	public PlayerActor(Texture form, String name, int speed) {
		super(form, name, speed);
	}
	
	public PlayerActor(CharacterInfo t) {
		super(t);
	}
}