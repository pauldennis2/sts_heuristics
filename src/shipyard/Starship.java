/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Starship {

	private GreatLeader captain;
	private Crew crew;
	private ShipClass shipClass;
	
	private Set<Amenity> amenities;
	private List<ShipPart> parts;
	private PowerCrystal powerCrystal;
	private String name;
	
	private Storage cargo;
	
	private BoundedInt hullHp;
	
	private MissionType mission;
	private int effectiveness = 100;
	
	private double malfunctionChance;
	private double dodgeChance;
	
	//Table describing how different materials multiply hull strength
	public static final int[] HULL_MULTS = {1, 2, 4, 8};
	
	public Starship () {//Mine S6E18
		this.name = "Enterprise";
		this.powerCrystal = new PowerCrystal("Jane");
		this.shipClass = ShipClass.BATTLESHIP;
		hullHp = new BoundedInt(1000);
		
		parts = new ArrayList<>();
		amenities = new HashSet<>();
		amenities.add(Amenity.SECURITY);
		amenities.add(Amenity.MEDICAL_BAY);
		amenities.add(Amenity.HOLODECK);
		
		cargo = new Storage(500);
		
		powerCrystal.levelUpMatrix(50);
		crew = new Crew(3, 1.5, 60, ShipClass.BATTLESHIP);
		dodgeChance = 0.05;
		captain = new GreatLeader("Captain Picard", MissionType.EXPLORATION);
	}
	
	public Starship (String name, PowerCrystal powerCrystal, ShipPart hull) {
		this.name = name;
		this.powerCrystal = powerCrystal;
		this.shipClass = hull.getSize();
		int hp = shipClass.getSize() * HULL_MULTS[hull.getMaterial().getLevel()];
		hullHp = new BoundedInt(hp);
		malfunctionChance = 0.01;
		
		parts = new ArrayList<>();
		amenities = new HashSet<>();
		
		if (shipClass == ShipClass.FIGHTER) {
			dodgeChance = ShipClass.FIGHTER_DODGE_CHANCE;
		} else {
			dodgeChance = 0.0;
		}
		
		cargo = new Storage(shipClass.getSize());
	}
	
	public void upgradeHull () {
		if (shipClass.isUpgrade()) {
			System.err.println("!Can't upgrade, already upgraded");
			return;
		}
		shipClass = shipClass.getUpgradedForm();
		hullHp = new BoundedInt(shipClass.getSize());
	}
	
	public boolean addPart (ShipPart part) {
		if (shipClass.getSlots() > parts.size()) {
			System.out.println("Added " + part + " to " + name + ".");
			parts.add(part);
			return true;
		} else {
			System.out.println("Ship has no empty part slots.");
			return false;
		}
	}
	
	public void removePart (ShipPart part) {
		parts.remove(part);
	}
	
	public boolean addAmenity (Amenity amenity) {
		if (shipClass.getAmenitySlots() > amenities.size()) {
			System.out.println("Added " + amenity + " to " + name + ".");
			amenities.add(amenity);
			return true;
		} else {
			System.out.println("Ship has no empty amenity slots.");
			return false;
		}
	}
	
	public boolean removeAmenity (Amenity amenity) {
		return amenities.remove(amenity);
	}
	
	public String toString () {
		return name + "\nPower Crystal: " + powerCrystal + "\nHP: " + hullHp + " (" + shipClass + ")";
	}
	
	public void printDetails () {
		System.out.println(this);
		
		//Print captain and crew if not null
		System.out.println(crew != null ? "Crew: " + crew : "Crew: none");
		System.out.println(captain != null ? "Captain: " + captain : "Captain: none");
		powerCrystal.printAllInfo();
	}
	
	public Crew addCrew (Crew newCrew) {
		Crew removed = crew;
		crew = newCrew;
		return removed;
	}
	
	public PowerCrystal addPowerCrystal (PowerCrystal newPowerCrystal) {
		PowerCrystal removed = powerCrystal;
		powerCrystal = newPowerCrystal;
		return removed;
	}
	
	//These methods are technically redundant since we could always call addCrew(null)
	public Crew removeCrew () {
		Crew removed = crew;
		crew = null;
		return removed;
	}
	
	public PowerCrystal removePowerCrystal () {
		PowerCrystal removed = powerCrystal;
		powerCrystal = null;
		return removed;
	}
	
	public boolean isOperational () {
		return crew != null && powerCrystal != null;
	}
	
	public MissionType getMission () {
		return mission;
	}
	
	public void setMission (MissionType mission) {
		this.mission = mission;
	}
	
	public int getEffectiveness () {
		return effectiveness;
	}
	
	public void setEffectiveness (int effectiveness) {
		this.effectiveness = effectiveness;
	}
	
	public Storage getCargo () {
		return cargo;
	}
	
	public Crew getCrew () {
		return crew;
	}
	
	public PowerCrystal getPowerCrystal () {
		return powerCrystal;
	}
	
	public void setMalfunctionChance (double malfunctionChance) {
		this.malfunctionChance = malfunctionChance;
	}
	
	public double getMalfunctionChance () {
		return malfunctionChance;
	}
	
	public double getDodgeChance () {
		return dodgeChance;
	}
	
}
