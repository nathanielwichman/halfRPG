package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.game.SelectableActionActor.ActionType;

public class UiActionActor extends Actor {
	public enum SpecialAction {
		END_TURN, MOVE;
	}
	
	public Texture texture;
	public PlayerActor base;
	public AttackAction attack;
	public SpecialAction other;
	
	public UiActionActor(Texture t, PlayerActor base, AttackAction action) {
		this.texture = t;
		this.base = base;
		this.attack = action;
		this.other = null;
	}
	
	public UiActionActor(Texture t, PlayerActor base, SpecialAction action) {
		this.base = base;
		texture = t;
		other = action;
	}
	
	public boolean isSpecial() {
		return other != null;
	}
	
	 @Override
	 public void draw(Batch batch, float alpha) {
		 batch.draw(texture,  getX(),  getY());
	 }
	
}
