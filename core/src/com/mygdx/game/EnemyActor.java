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
	private List<PlayerActor> alertedTo;
	
	public EnemyActor(Texture t, String name, int speed) {
		super(t, name, speed);
		alertedTo = new ArrayList<>();
	}
	
	public EnemyActor(RPGStage parent, CharacterInfo c) {
		super(parent, c);
		alertedTo = new ArrayList<>();
	}
	
	public boolean alerted() {
		return alertedTo.size() == 0;
	}
	
	public List<PlayerActor> alertedTo() {
		return alertedTo;
	}
	
	public boolean isAlertedTo(PlayerActor p) {
		return alertedTo.contains(p);
	}
	
	public void alertedBy(PlayerActor p) {
		alertedTo.add(p);
	}
	
	public PlayerActor firstTargetAlertedTo() {
		if (alertedTo.size() == 0) {
			return null;
		} else {
			return alertedTo.get(0);
		}
	}
}
