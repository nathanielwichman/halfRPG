package com.mygdx.game;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * An actor representing a character, either NPC or player controlled.
 * Holds info about the characters abilities and display.
 */
public class CharacterActor extends Actor {
	private RPGStage parent;
	
	private Texture texture;  // texture for this actor
	private final String name;  // unique name for actor
	private String className;  // type name of actor (i.e. orc), may be same as name
	private int maxSpeed;  // how many tiles can be moved each turn
	private int maxHealth; // how much dmg it can take
	private int vision;
	
	private int speedRemaining;  // number of moves left this turn
	private int healthRemaining;  // health remaining
	
	private List<AttackAction> actions;  // actions a character can make
	private AttackAction basicAttack; 
	
	private int maxActions;
	private int actionsLeft;
	
	/**
	 * @param form The texture to represent this character
	 * @param name The name of this character
	 */
	public CharacterActor(Texture form, String name, int speed) {
		this.texture = form;
		this.name = name;
		this.maxSpeed = speed;
		this.setBounds(getX(), getY(), texture.getWidth(), texture.getHeight());
	}
	
	/**
	 * Loads a new character actor from a CharacterInfo instance
	 * @param i characterInfo object describing this actor
	 */
	public CharacterActor(RPGStage parent, CharacterInfo i) {
		this.parent = parent;
		this.texture = i.t;
		this.className = i.className;
		this.name = i.name;
		this.maxSpeed = i.maxSpeed;
		this.maxHealth = i.maxHealth;
		this.actions = i.actions;
		this.basicAttack = i.basicAttack;
		this.maxActions = 1;
		this.vision = i.vision;
		this.setBounds(getX(), getY(), i.t.getWidth(), i.t.getHeight());
		
		this.actionsLeft = 1;
		this.healthRemaining = this.maxHealth;
		this.speedRemaining = this.maxSpeed;
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
	
	public AttackAction getBasicAttack() {
		return basicAttack;
	}
	
	/**
	 * @return the number of tiles this character can move in a turn
	 */
	public int getMaxSpeed() {
		return maxSpeed;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public int getSpeedRemaining() {
		return speedRemaining;
	}
	
	public void setSpeedRemaining(int moves) {
		speedRemaining = moves;
	}
	
	public boolean hasSpeedRemaining() {
		return speedRemaining > 0;
	}
	
	public boolean canMove(int spaces) {
		return spaces <= speedRemaining;
	}
	
	public int moveSpaces(int spaces) {
		speedRemaining -= spaces;
		return speedRemaining;	
	}
	
	public boolean isExhausted() {
		return actionsLeft == 0;
	}
	
	public int getVisionDistance() {
		return vision;
	}
	
	public boolean exhaustAction() {
		actionsLeft -= 1;
		return actionsLeft == 0;
	}
	
	public int regainMoves(int spaces, boolean overflow) {
		int newspeedRemaining = speedRemaining + spaces;
		if (overflow || newspeedRemaining < maxSpeed) {
			speedRemaining = newspeedRemaining;
		} else {
			speedRemaining = maxSpeed;
		}
		
		return speedRemaining;
	}
	
	public void refresh() {
		speedRemaining = maxSpeed;
		actionsLeft = maxActions;
	}
	
	public int getHealth() {
		return healthRemaining;
	}
	
	public void setHealth(int health) {
		healthRemaining = health;
	}
	
	public int takeDamage(int damage) {
		healthRemaining -= damage;
		if (healthRemaining < 0)
			healthRemaining = 0;
		return healthRemaining; 
	}
	
	public int heal(int health, boolean overheal) {
		int newHealth = healthRemaining + health;
		if (overheal || newHealth < maxHealth) {
			healthRemaining = newHealth;
		} else {
			healthRemaining = maxHealth;
		}
		return healthRemaining;
	}
	
	public void setMovesLeft(int movesLeft) {
		this.speedRemaining = movesLeft;
	}
	
	public int handleAttack(AttackAction a) {
		System.out.println(name + ": ouch! Took " + a.damage + " dmg / " + this.healthRemaining);
		if (this.takeDamage(a.damage) == 0) {
			System.out.println(name + ": zoinks! I'm dead");
			parent.removeCharacter(this);
		}
		return a.damage;
		
	}
	
}
