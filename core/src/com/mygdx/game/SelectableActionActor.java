package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class SelectableActionActor extends Actor {
	enum ActionType {
		MOVE, ATTACK;
	}
	
	private CharacterActor origin;
	private int cost;
	private Texture texture;
	private AttackAction a;
	
	// for attacks
	public SelectableActionActor(CharacterActor origin, Texture t, AttackAction a) {
		this.texture = t;
		this.a = a;
		this.origin = origin;
	}
	
	// for movement
	public SelectableActionActor(CharacterActor origin, Texture t, int cost) {
		this.texture = t;
		this.cost = cost;
		this.origin = origin;
	}
	
	@Override
	public void draw(Batch batch, float alpha) {
		batch.draw(texture,  getX(),  getY());
	}
	
	public int getCost() {
		return cost;
	}
	
	public CharacterActor getOrigin() {
		return origin;
	}
	
	public AttackAction getAttack() {
		return a;
	}
}
