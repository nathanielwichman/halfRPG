package com.mygdx.game;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes what tiles an action can select  
 * 
 * TODO: Add ally/enemy so that actions can be applied to players
 * and npcs easier
 */
public class ActionProperties {
	public interface Properties {};
	
	/*
	 * Describes what spaces can be targeted for the action
	 */
	public enum CanSelect implements Properties {
		WALLS, CHARACTER, ENEMY, PLAYER, SELF, TILE;
	}
	
	/*
	 * Describes if path finds through the selected objects
	 */
	public enum CanMoveThrough implements Properties {
		WALLS, CHARACTER, ENEMY, PLAYER;
	}
	
	/*
	 * Describes if the range of the selection respects difficult terrain 
	 */
	public enum EffectedByTerrain implements Properties {
		IGNORE_TERRAIN, RESPECT_TERRAIN;
	}
	
	public Set<Properties> s;
	
	public ActionProperties(Properties... properties) {
		s = new HashSet<>();
		for (Properties p : properties) {
			s.add(p);
		}
	}
	
	public boolean isOneOf(Properties...properties) {
		for (Properties p : properties) {
			if (s.contains(p)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean is(Properties... properties) {
		for (Properties p : properties) {
			if (!s.contains(p)) {
				return false;
			}
		}
		return true;
	}
	
	
}
