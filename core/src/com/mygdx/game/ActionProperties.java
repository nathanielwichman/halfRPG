package com.mygdx.game;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes what tiles an action can select and explore.  
 * Used to make actions players and NPCs make generic and
 * easily describable 
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
	
	/*
	 * Normally actions cannot target or move through tiles covered by darkness.
	 * If IGNORE, this restriction is lifted.
	 * Note if the player sees this info they may get info about which
	 * tiles are covered by darkness
	 */
	public enum EffectedByDarkness implements Properties {
		IGNORE;
	}
	
	// Instance list of properties
	public Set<Properties> s;
	
	/**
	 * Instantiates a new set of properties
	 * @param properties List of various action properties
	 */
	public ActionProperties(Properties... properties) {
		s = new HashSet<>();
		for (Properties p : properties) {
			s.add(p);
		}
	}
	
	private void addProperty(Properties p) {
		s.add(p);
	}
	
	/**
	 * @param properties A list of properties to query this instance about
	 * @return True if this instance possesses one or more of the 
	 * 		passed properties, false otherwise
	 */
	public boolean isOneOf(Properties...properties) {
		for (Properties p : properties) {
			if (s.contains(p)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param properties A list of properties to query this instance about
	 * @return True if this instance contains none of the passed properties, false otherwise
	 */
	public boolean isNot(Properties...properties) {
		for (Properties p : properties) {
			if (s.contains(p)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param properties A list of properties to query this instance about
	 * @return True if this instance contains all of the passed properties, false otherwise
	 */
	public boolean is(Properties... properties) {
		for (Properties p : properties) {
			if (!s.contains(p)) {
				return false;
			}
		}
		return true;
	}
	
	public static ActionProperties getDefaultMoveProperty() {
		return new ActionProperties(CanSelect.TILE, EffectedByTerrain.RESPECT_TERRAIN);
				
	}
	
	public static ActionProperties getDefaultAttackProperties(boolean player) {
		
		if (player) {
			return new ActionProperties(CanSelect.ENEMY, EffectedByTerrain.IGNORE_TERRAIN);
		} else {
			return new ActionProperties(CanSelect.PLAYER, EffectedByTerrain.IGNORE_TERRAIN);
		}
	}
}
