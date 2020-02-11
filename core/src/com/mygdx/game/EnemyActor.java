package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;

/**
 * Defines an enemy character, i.e. one that the computer controls and 
 * is generally hostile (or at least antagonistic) 
 */
public class EnemyActor extends CharacterActor {
	public RPGAi brain;
	
	
	public EnemyActor(Texture t, String name, int speed) {
		super(t, name, speed);
		brain = new RPGAiBasic(this);
	}
	
	public EnemyActor(RPGStage parent, CharacterInfo c) {
		super(parent, c);
		brain = new RPGAiBasic(this);
	}
	
	
}
