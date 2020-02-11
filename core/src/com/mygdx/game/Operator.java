package com.mygdx.game;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedMap;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Strategy.MoveStep;
import com.mygdx.game.Strategy.Step;

/**
 * Operator handles logic and organization for a given team (AI or Player).
 */
public abstract class Operator {
	protected Operator otherOp;
	protected RPGStage stage;
	protected Set<CharacterActor> actors;	
	
	public Operator(RPGStage stage) {
		this.stage = stage;
		this.actors = new HashSet<>();
	}
	
	public void setOtherOp(Operator other) {
		otherOp = other;
	}
	
	public abstract void beginTurn();
	
	public abstract void actorDeath(CharacterActor a);
	
	// return interupt
	public boolean executingStep(CharacterActor actor, Step s) { return false; }
	
	public int handleAttack(CharacterActor a, AttackAction p) {
		if (actors.contains(a)) {
			int dmg = a.handleAttack(p);
			if (a.getHealth() == 0) {
				System.out.println("enemy dead");
				this.actorDeath(a);
				otherOp.actorDeath(a);
			}
			return dmg;
		}
		return -1;
	}
	
	/**
	 * Add a CharacterActor to this Operator's team
	 * Note: Make sure to add the correct actor type for each operator
	 * (player or enemy)
	 * @param a Actor to add
	 */
	public void addActor(CharacterActor a) {
		RPG.getCurrentMapInfo().addCharacter(a);
		actors.add(a);
	}
	
	public boolean removeActor(CharacterActor a) {
		if (actors.contains(a)) {
			actors.remove(a);
			return true;
		}
		return false;
	}
	
	/**
	 * Refreshes the moves and abilities of all actors this operator controls
	 * i.e. at the start of a new turn
	 */
	public void refreshAll() {
		for (CharacterActor a : actors) {
			a.refresh();
		}
	}
	
	/**
	 * @return All actors this operator controls
	 */
	public Set<CharacterActor> getActors() {
		return actors; 
	}
	
	public CharacterActor getActorAtCell(Vector2 cell) {
		for (CharacterActor a : actors) {
			if (a.getCell().epsilonEquals(cell)) {
				return a;
			}
		}
		return null;
	}
}
