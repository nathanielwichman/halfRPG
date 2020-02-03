package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class Strategy {
	public enum StepType {
		MOVE, ATTACK;
	}
	
	interface Step { 
		public StepType getType();
	}
	
	public static class MoveStep implements Step {
		public Vector2 stepLocation;
		public int cost;
		
		public MoveStep(Vector2 location, int cost) {
			stepLocation = location;
			this.cost = cost;
		}
		
		public MoveStep(int x, int y, int cost) {
			stepLocation = new Vector2(x,y);
			this.cost = cost;
		}
		
		public StepType getType() {
			return StepType.MOVE;
		}
		
		public String toString() {
			return "move to " + stepLocation + " [" + cost + "]";
		}
	}
	
	public static class ActionStep implements Step {
		public Vector2 actionLocation;
		public AttackAction action;
		
		public ActionStep(Vector2 location, AttackAction a) {
			actionLocation = location;
			action = a;
		}
		
		public ActionStep(int x, int y, AttackAction a) {
			this(new Vector2(x,y), a);
		}
		
		public StepType getType() {
			return StepType.ATTACK;
		}
		
		public String toString() {
			return "use attack " + action.attackName + " at " + actionLocation;
		}
	}
	
	private List<Step> steps;
	private boolean finished;
	private int index;
	
	public Strategy() {
		steps = new ArrayList<>();
		finished = false;
		index = 0;
	}
	
	public void addStep(Step s) {
		if (!finished)
			steps.add(s);
	}
	
	public void finishPlanning() {
		finished = true;
	}
	
	public String toString() {
		String combined = "";
		if (steps.size() == 0) {
			return "empty strategy";
		}
		
		for (int i = 0; i < steps.size() - 1; i++) {
			combined += steps.get(i).toString() + ", ";
		}
		combined += steps.get(steps.size()-1).toString();		
		return combined;
	}
	
	// iterates when finished
	public boolean hasNextStep() {
		return index < steps.size();
	}
	
	public Step getNextStep() {
		if (!finished) { return null; }
		
		index++;
		return steps.get(index-1); 
	}
	
	public void reset() {
		index = 0;
	}
	
	
	
}
