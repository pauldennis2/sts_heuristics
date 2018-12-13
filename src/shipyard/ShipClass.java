/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import java.util.Arrays;
import java.util.List;

public enum ShipClass {
	
	FIGHTER 		(1, 5, 2, 0),
	DESTROYER 		(15, 50, 5, 1),
	CRUISER 		(20, 55, 7, 2),
	BATTLESHIP 		(40, 100, 15, 4),
	MOTHERSHIP 		(60, 200, 25, 8),
	
	//Upgraded form
	
	STARFIGHTER		(1, 10, 3, 1, true),
	FRIGATE			(15, 60, 7, 2, true),
	BATTLECRUISER	(20, 75, 10, 3, true),
	DREADNOUGHT		(40, 175, 20, 5, true),
	MOBILE_FORTRESS	(60, 250, 40, 12, true);
	//;
	
	ShipClass (int crewRequired, int size, int slots, int amenitySlots) {
		this.crewRequired = crewRequired;
		this.size = size;
		this.slots = slots;
		this.amenitySlots = amenitySlots;
		upgrade = false;
	}
	
	ShipClass (int crewRequired, int size, int slots, int amenitySlots, boolean upgrade) {
		this(crewRequired, size, slots, amenitySlots);
		
		this.upgrade = upgrade;
	}
	
	public static final double FIGHTER_DODGE_CHANCE = 0.2;
	
	//$Design
	//These fields aren't final because technology might allow them to be altered
	//Player discovers a tech that allows fighters to have an amenity, for example
	private int crewRequired;
	private int size;
	private int slots;
	private int amenitySlots;
	
	private boolean upgrade;
	
	public int getCrewRequired () {
		return crewRequired;
	}
	
	public int getSize () {
		return size;
	}
	
	public int getSlots () {
		return slots;
	}
	
	public int getAmenitySlots() {
		return amenitySlots;
	}
	
	public static List<ShipClass> getRegularClasses () {
		return Arrays.asList(FIGHTER, DESTROYER, CRUISER, BATTLESHIP, MOTHERSHIP);
	}
	
	public static List<ShipClass> getUpgradedClasses () {
		return Arrays.asList(STARFIGHTER, FRIGATE, BATTLECRUISER, DREADNOUGHT, MOBILE_FORTRESS);
	}
	
	@Override
	public String toString () {
		return MyStringUtils.capitalizeWords(super.toString());
	}
	
	public boolean isUpgrade () {
		return upgrade;
	}

	
	//Returns the upgraded form of this chassis if applicable
	//If already an upgrade returns null
	public ShipClass getUpgradedForm () {
		if (upgrade) {
			return null;
		}
		switch (this) {
		case FIGHTER:
			return STARFIGHTER;
		case DESTROYER:
			return FRIGATE;
		case CRUISER:
			return BATTLECRUISER;
		case BATTLESHIP:
			return DREADNOUGHT;
		case MOTHERSHIP:
			return MOBILE_FORTRESS;
		default:
			return null;
		}
	}
}
