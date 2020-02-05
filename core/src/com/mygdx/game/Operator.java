package com.mygdx.game;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedMap;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Strategy.MoveStep;

/**
 * Operator handles logic and organization for a given team (AI or Player).
 */
public abstract class Operator {
	private RPGStage stage;
	private Set<CharacterActor> actors;	
	
	/**
	 * Helper method - given a sorted map describing a series of moves in the format
	 * cost -> move location returns a Strategy object describing the same set of moves
	 * @param moves Series of moves to describe
	 * @return Strategy describing moves
	 */
	public static Strategy getMovePlan(SortedMap<Integer, Vector2> moves) {
		int lastCost = 0;
		Strategy plan = new Strategy();
		for (Integer cost : moves.keySet()) {
			assert cost > lastCost;
			if (cost == 0) { continue; }  // assume at origin
			Vector2 v = moves.get(cost);
			plan.addStep(new MoveStep(v,cost-lastCost));
			lastCost = cost;
		}
		plan.finishPlanning();
		return plan;
	}
	
	public Operator(RPGStage stage) {
		this.stage = stage;
		this.actors = new HashSet<>();
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
