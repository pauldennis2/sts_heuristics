/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PowerCrystalTrait {

	//Preferences
	LIKES_SCIENCE(1),
	LIKES_WAR(1),
	LIKES_EXPLORATION(1),
	DISLIKES_SCIENCE(1),
	DISLIKES_WAR(1),
	DISLIKES_EXPLORATION(1),
	
	//Basic traits
	ACQUISITIVE(1),
	KLEPTO(1),
	COWARDLY(1),
	BULLHEADED(1),
	WANTS_GREAT_LEADER(1),
	
	//Level 2 Basic traits/prefs
	LOVES_SCIENCE(2, LIKES_SCIENCE),
	LOVES_WAR(2, LIKES_WAR),
	LOVES_EXPLORATION(2, LIKES_EXPLORATION),
	HATES_SCIENCE(2, DISLIKES_SCIENCE),
	HATES_WAR(2, DISLIKES_WAR),
	HATES_EXPLORATION(2, DISLIKES_EXPLORATION),
	
	
	BANKER(2, ACQUISITIVE),
	KLEPTOCHARGED(2, KLEPTO),
	COWARDLY_2(2, COWARDLY),
	BULLHEADED_2(2, BULLHEADED),
	NEEDS_GREAT_LEADER(2, WANTS_GREAT_LEADER),
	
	//Elemental Aspects - extra damage
    FIRE_ASPECT(2, ElementalDevotion.FIRE), //Weapon damage/dot
    WATER_ASPECT(2, ElementalDevotion.WATER), //Shield effectiveness
    EARTH_ASPECT(2, ElementalDevotion.EARTH), //Hull sturdiness/armor
    AIR_ASPECT(2, ElementalDevotion.AIR), //Dodge attacks
    
    //Fundamental Aspects - extra damage of type, vulnerable to opposite type. Counters opposite ability if used after.
    LIGHT_ASPECT(2, FundamentalDevotion.LIGHT),//Lets out a pulse of light, healing nearby allies and damaging enemies.
    DARK_ASPECT(2, FundamentalDevotion.DARK),//Cloaks ship in shadows for 1 round, regening health.
    ORDER_ASPECT(2, FundamentalDevotion.ORDER),//Ship can never randomly malfunction; attacks that cause system malfunctions don't work. Shield bonus.
    CHAOS_ASPECT(2, FundamentalDevotion.CHAOS),//All ships in combat nearby have a 5% chance of random malfunction, extra chaos damage.
    
    //Secret Elemental Combo Aspects (all mutually exclusive)
    //Deals both types of bonus damage.
    //Only vulnerable to the double opposite type (i.e. chaoswind vulnerable to orderstone)
    CHAOSWIND(3, CHAOS_ASPECT, AIR_ASPECT), //All enemy ships 10% chance malfunction. Higher dodge chance
    DARKWATER(3, DARK_ASPECT, WATER_ASPECT), //Additional overshield that prevents any scanning. Better shield regen
    FIRELIGHT(3, LIGHT_ASPECT, FIRE_ASPECT), //Continuous Radiance aura, dealing damage to enemies and blinding. Enemy systems DOT'd cant be repaired.
    ORDERSTONE(3, ORDER_ASPECT, EARTH_ASPECT), //Armor thing? No malfunction aura? Strong damage reduction
	
    //Negative perks
    HOMEBODY(-3), //Can't travel out of home system
    INACCURATE(-2), //Chance for attacks to miss
    NO_SHIELDS(-2), //Can't equip shields
    
	//Special
	TRAUMATIZED(0);
	
	private int cost;
	private boolean possibleRandom = false;
	private PowerCrystalTrait prereq;
	private PowerCrystalTrait prereq2;
	
	private PowerCrystalTrait exclusiveWith;
	
	private ElementalDevotion elementalPrereq;
	private FundamentalDevotion fundamentalPrereq;
	
	private TraitEffect effect;
	
	private int requiredLevel;
	
	public static final int REQUIRED_LEVEL_ELEMENTAL = 3;
	public static final int REQUIRED_LEVEL_FUNDAMENTAL = 3;
	public static final int REQUIRED_LEVEL_COMBO = 7;
	
	private PowerCrystalTrait (int cost) {
		this.cost = cost;
	}
	
	private PowerCrystalTrait (int cost, ElementalDevotion elementalDevotion) {
		this.cost = cost;
		this.elementalPrereq = elementalDevotion;
	}
	
	private PowerCrystalTrait (int cost, FundamentalDevotion fundamentalDevotion) {
		this.cost = cost;
		this.fundamentalPrereq = fundamentalDevotion;
	}
	
	private PowerCrystalTrait (int cost, boolean possibleRandom) {
		this.cost = cost;
		this.possibleRandom = possibleRandom;
	}
	
	private PowerCrystalTrait (int cost, PowerCrystalTrait prereq) {
		this.cost = cost;
		this.prereq = prereq;
	}
	
	private PowerCrystalTrait (int cost, PowerCrystalTrait prereq, PowerCrystalTrait prereq2) {
		this.cost = cost;
		this.prereq = prereq;
		this.prereq2 = prereq2;
	}
	
	public static final int LIKES_BONUS = 10;
	public static final int DISLIKES_PENALTY = 10;
	public static final int HATES_PENALTY = 20;
	public static final int HATES_BONUS = 5; //Bonus for NOT doing the thing the ship hates
	public static void setEffects () {
		
		LIKES_SCIENCE.effect = (ship) -> {
			if (ship.getMission() == MissionType.SCIENCE) {
				ship.setEffectiveness(ship.getEffectiveness() + LIKES_BONUS);
				ship.setMalfunctionChance(0.0);
			}
		};
		
		DISLIKES_SCIENCE.effect = (ship) -> {
			if (ship.getMission() == MissionType.SCIENCE) {
				ship.setEffectiveness(ship.getEffectiveness() - DISLIKES_PENALTY);
			}
		};
		
		HATES_SCIENCE.effect = (ship) -> {
			if (ship.getMission() == MissionType.SCIENCE) {
				ship.setEffectiveness(ship.getEffectiveness() - HATES_PENALTY);
			} else {
				ship.setEffectiveness(ship.getEffectiveness() + HATES_BONUS);
			}
		};
	}
	
	public static void initialize () {
		setRequiredLevels();
		setExclusions();
	}
	
	private static void setRequiredLevels () {
		FIRE_ASPECT.requiredLevel = REQUIRED_LEVEL_ELEMENTAL;
		WATER_ASPECT.requiredLevel = REQUIRED_LEVEL_ELEMENTAL;
		AIR_ASPECT.requiredLevel = REQUIRED_LEVEL_ELEMENTAL;
		EARTH_ASPECT.requiredLevel = REQUIRED_LEVEL_ELEMENTAL;
		
		LIGHT_ASPECT.requiredLevel = REQUIRED_LEVEL_FUNDAMENTAL;
		DARK_ASPECT.requiredLevel = REQUIRED_LEVEL_FUNDAMENTAL;
		CHAOS_ASPECT.requiredLevel = REQUIRED_LEVEL_FUNDAMENTAL;
		ORDER_ASPECT.requiredLevel = REQUIRED_LEVEL_FUNDAMENTAL;
		
		CHAOSWIND.requiredLevel = REQUIRED_LEVEL_COMBO;
		DARKWATER.requiredLevel = REQUIRED_LEVEL_COMBO;
		FIRELIGHT.requiredLevel = REQUIRED_LEVEL_COMBO;
		ORDERSTONE.requiredLevel = REQUIRED_LEVEL_COMBO;
		
		WANTS_GREAT_LEADER.requiredLevel = 3;
		NEEDS_GREAT_LEADER.requiredLevel = 5;
	}
	
	private static void setExclusions () {
		//Consider the problem of exclusions with higher tiers.
		//For example shouldn't be possible to have LOVES_SCIENCE and DISLIKES_SCIENCE
		//Could solve this by adding code to review prereqs of all existing traits - sort of ugly
		FIRE_ASPECT.exclusiveWith = WATER_ASPECT;
		WATER_ASPECT.exclusiveWith = FIRE_ASPECT;
		AIR_ASPECT.exclusiveWith = EARTH_ASPECT;
		EARTH_ASPECT.exclusiveWith = AIR_ASPECT;
		
		LIGHT_ASPECT.exclusiveWith = DARK_ASPECT;
		DARK_ASPECT.exclusiveWith = LIGHT_ASPECT;
		CHAOS_ASPECT.exclusiveWith = ORDER_ASPECT;
		ORDER_ASPECT.exclusiveWith = CHAOS_ASPECT;
		
		LIKES_SCIENCE.exclusiveWith = DISLIKES_SCIENCE;
		DISLIKES_SCIENCE.exclusiveWith = LIKES_SCIENCE;
		
		LIKES_WAR.exclusiveWith = DISLIKES_WAR;
		DISLIKES_WAR.exclusiveWith = LIKES_WAR;
		
		LIKES_EXPLORATION.exclusiveWith = DISLIKES_EXPLORATION;
		DISLIKES_EXPLORATION.exclusiveWith = LIKES_EXPLORATION;
		
		//Should the likes/hates be exclusive? i.e. a ship can't dislike science AND war
		//initial conclusion: no. doesn't fit with current impl, and isn't needed (ships can be grumpy and not like anything)
	}
	
	public static List<PowerCrystalTrait> getPossibleRandoms () {
		List<PowerCrystalTrait> traits = Arrays.asList(PowerCrystalTrait.values());
		return traits.stream().filter(trait -> trait.cost == 1 || trait.possibleRandom).collect(Collectors.toList());
	}
	
	public List<PowerCrystalTrait> getPrereqs () {
		List<PowerCrystalTrait> prereqs = new ArrayList<>();
		if (prereq != null) {
			prereqs.add(prereq);
		}
		if (prereq2 != null) {
			prereqs.add(prereq2);
		}
		return prereqs;
	}
	
	public int getRequiredLevel () {
		return requiredLevel;
	}
	
	//Evaluate the Power Crystal to see if it meets the prereqs for a given trait
	public boolean evaluatePrereqs (PowerCrystal powerCrystal) {
		//Check to see if the crystal already has this trait
		if (powerCrystal.hasTrait(this)) {
			return false;
		}
		if (this.prereq != null && !powerCrystal.hasTrait(prereq)) {
			return false;
		}
		if (this.prereq2 != null && !powerCrystal.hasTrait(prereq2)) {
			return false;
		}
		//Check exclusions
		if (exclusiveWith != null && powerCrystal.hasTrait(exclusiveWith)) {
			return false;
		}
		//Check if devotion is present
		if (elementalPrereq != null && powerCrystal.getElementalDevotion() != elementalPrereq) {
			return false;
		}
		if (fundamentalPrereq != null && powerCrystal.getFundamentalDevotion() != fundamentalPrereq) {
			return false;
		}
		//Check if square is activated
		if (elementalPrereq != null) {
			int[][] overMatrix = powerCrystal.getOverMatrix();
			if (overMatrix[elementalPrereq.getRequiredX()][elementalPrereq.getRequiredY()] < 1) {
				return false;
			}
		}
		if (fundamentalPrereq != null) {
			int[][] overMatrix = powerCrystal.getOverMatrix();
			if (overMatrix[fundamentalPrereq.getRequiredX()][fundamentalPrereq.getRequiredY()] < 1) {
				return false;
			}
		}
		if (powerCrystal.getShipLevel() < requiredLevel) {
			return false;
		}
		return true;
	}
	
	public String evaluatePrereqsAndReturnReason (PowerCrystal powerCrystal) {
		if (powerCrystal.hasTrait(this)) {
			return "Already present";
		}
		if ((this.prereq != null && !powerCrystal.hasTrait(prereq) ||
				(this.prereq2 != null && !powerCrystal.hasTrait(prereq2)))) {
			return "Missing " + prereq;
		}
		//Check exclusions
		if (exclusiveWith != null && powerCrystal.hasTrait(exclusiveWith)) {
			return "Exclusive with " + exclusiveWith;
		}
		//Check if devotion is present
		if (elementalPrereq != null && powerCrystal.getElementalDevotion() != elementalPrereq) {
			return "Missing " + elementalPrereq + " devotion";
		}
		if (fundamentalPrereq != null && powerCrystal.getFundamentalDevotion() != fundamentalPrereq) {
			return "Missing " + fundamentalPrereq + " devotion";
		}
		//TODO - figure out why this code isn't collectively working properly (error could be elsewhere)
		//Check if square is activated
		if (elementalPrereq != null) {
			int[][] overMatrix = powerCrystal.getOverMatrix();
			if (overMatrix[elementalPrereq.getRequiredX()][elementalPrereq.getRequiredY()] < 1) {
				return "Required region not activated (" + elementalPrereq.getRequiredX() + ", " + elementalPrereq.getRequiredY() + ")";
			}
		}
		if (fundamentalPrereq != null) {
			int[][] overMatrix = powerCrystal.getOverMatrix();
			if (overMatrix[fundamentalPrereq.getRequiredX()][fundamentalPrereq.getRequiredY()] < 1) {
				return "Required region not activated (" + fundamentalPrereq.getRequiredX() + ", " + fundamentalPrereq.getRequiredY() + ")";
			}
		}
		if (powerCrystal.getShipLevel() < requiredLevel) {
			return "Required level of " + requiredLevel + " not met";
		}
		return "";
	}
	
	@Override
	public String toString () {
		return MyStringUtils.capitalizeWords(super.toString());
	}
	
	public TraitEffect getEffect () {
		return effect;
	}
	
	public int getCost () {
		return cost;
	}
	
	public PowerCrystalTrait getExclusiveWith () {
		return exclusiveWith;
	}
}
