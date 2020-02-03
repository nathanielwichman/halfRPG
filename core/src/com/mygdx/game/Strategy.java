package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

/**
 * Describes an ordered set of moves to be executed by a character
 */
public class Strategy {
	public enum StepType {
		MOVE, ATTACK;
	}
	
	/**
	 * A generic step that is one move in a strategy 
	 */
	interface Step { 
		public StepType getType();
	}
	
	/**
	 * A step that describes moving to a new tile with an associated cost
	 */
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
	
	/**
	 * A step representing an attack
	 */
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
	
	/**
	 * Creates a new strategy
	 */
	public Strategy() {
		steps = new ArrayList<>();
		finished = false;
		index = 0;
	}
	
	/**
	 * Adds a new step to this strategy
	 * Does not work if strategy is finished
	 * @param s Step to add
	 */
	public void addStep(Step s) {
		if (!finished)
			steps.add(s);
	}
	
	/**
	 * Sets this strategy in stone, preventing it from being modified.
	 * To be called after strategy is finished and ready to be iterated over
	 */
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
	
	/**
	 * @return If there is another step in this strategy
	 */
	public boolean hasNextStep() {
		return index < steps.size();
	}
	
	/**
	 * Requires that the strategy is finished via a finishPlanning call
	 * @return the next step in the strategy if it exists
	 * @throws OutOfBoundsException if hasNextStep() is not properly used
	 */
	public Step getNextStep() {
		if (!finished) { return null; }
		
		index++;
		return steps.get(index-1); 
	}
	
	/**
	 * Sets up this iterator, must be called every time you want to start
	 * a clean iteration
	 */
	public void setup() {
		index = 0;
	}
	
	
	
}
