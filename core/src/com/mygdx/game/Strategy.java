package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

/**
 * Describes an ordered set of moves to be executed by a character
 */
public class Strategy implements Comparable {
	public static class Origin {
		public Strategy strat;
		public CharacterActor origin;
		
		public Origin(Strategy s, CharacterActor origin) {
			this.origin = origin;
			this.strat = s;
		}
	}
	
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
		public Vector2 attackLocation;
		public CharacterActor target;
		public AttackAction action;
		
		public ActionStep(CharacterActor target, AttackAction a) {
			this.attackLocation = target.getCell();
			this.target = target;
			action = a;
		}
		
		public ActionStep(Vector2 targetLocation, AttackAction a) {
			this.attackLocation = targetLocation;
			this.action = a;
		}
		
		public StepType getType() {
			return StepType.ATTACK;
		}
		
		public String toString() {
			if (target != null) {
				return "use attack " + action.attackName + " on " + target;
			} else {
				return "use attack " + action.attackName + " at " + attackLocation;
			}
		}
	}
	
	private List<Step> steps;
	private boolean finished;
	private int index;
	private int points;
	
	/**
	 * Creates a new strategy
	 */
	public Strategy() {
		steps = new ArrayList<>();
		finished = false;
		index = 0;
		points = 0;
	}
	
	/**
	 * Creates a new strategy with the given steps
	 * @param steps = List of ordered steps
	 */
	public Strategy(List<Step> steps) {
		steps = new ArrayList<>(steps);
		finished = false;
		index = 0;
		points = 0;
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
	 * @return Number of steps currently in strategy
	 */
	public int getNumSteps() {
		return steps.size();
	}
	
	public int getPoints() {
		return points;
	}
	
	public void addCost(int v) {
		if (!finished) {
			points -= v;
		}
	}
	
	public void addPoints(int v) {
		if (!finished) {
			points += v;
		}
	}
	
	public void setPoints(int v) {
		if (!finished) {
			points = v;
		}
	}
	
	/**
	 * @return Total cost of all move steps in this strategy
	 */
	public int getTotalMoveCost() {
		int cost = 0;
		for (Step s : steps) {
			if (s instanceof MoveStep) {
				cost += ((MoveStep) s).cost;
			}
		}
		return cost;
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
		return combined + " <" + points + ">";
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
		finished = true;
	}
	
	public Step getLastStep() {
		if (steps.isEmpty()) {
			return null;
		} else {
			return steps.get(steps.size()-1);
		}
	}
	
	public boolean cullToSpeed(int remainingSpeed) {
		int totalCost = 0;
		
		if (!finished) {
			for (int i = 0; i < steps.size(); i++) {
				Step s = steps.get(i);
				if (s instanceof MoveStep) {
					totalCost += ((MoveStep) s).cost;
					if (totalCost > remainingSpeed) {
						while (steps.size() > i) {
							steps.remove(i);
						}	
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof Strategy) {
			Strategy other = (Strategy) o;
			return other.points - this.points;
		}
		return 0;
	}
	
	
	
}
