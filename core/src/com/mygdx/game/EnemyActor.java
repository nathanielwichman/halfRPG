package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

/**
 * Defines an enemy character, i.e. one that the computer controls and 
 * is generally hostile (or at least antagonistic) 
 */
public class EnemyActor extends CharacterActor {
	public EnemyActor(Texture t, String name, int speed) {
		super(t, name, speed);
	}
}
