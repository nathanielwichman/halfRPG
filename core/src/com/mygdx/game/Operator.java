package com.mygdx.game;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Strategy.MoveStep;

public abstract class Operator {
	private RPGStage stage;
	private Set<CharacterActor> actors;	
	
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
	
	public void addActor(CharacterActor a) {
		RPG.getCurrentMapInfo().addCharacter(a);
		actors.add((PlayerActor) a);
	}
	
	public void refreshAll() {
		for (CharacterActor a : actors) {
			a.refresh();
		}
	}
	
	public Set<CharacterActor> getActors() {
		return actors; 
	}
}
