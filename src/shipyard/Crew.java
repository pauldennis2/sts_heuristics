/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

public class Crew {

	private int level; //Level of experience of crew
	/**
	 * 0 - 70%  "Rookie"
	 * 1 - 100% "Capable"
	 * 2 - 115% "Veteran"
	 * 3 - 125% "Expert"
	 * 4 - 130% "Mastery"
	 */
	
	public static final double[] LEVEL_EFFICIENCY = {0.7, 1.0, 1.15, 1.25, 1.3};
	public static final String[] LEVEL_NAMES = {"Rookie", "Capable", "Veteran", "Expert", "Mastery"};
	private double morale;
	private int size; //Amount of officers in the crew
	
	private ShipClass shipClass;
	
	/**
	 * @param level
	 * @param morale
	 * @param size
	 * @param shipClass
	 */
	public Crew(int level, double morale, int size, ShipClass shipClass) {
		super();
		this.level = level;
		this.morale = morale;
		this.size = size;
		this.shipClass = shipClass;
	}
	
	@Override
	public String toString () {
		return LEVEL_NAMES[level] + " Crew, Size: " + size + ", Morale: " + morale;
	}
	
	public void levelUp () {
		if (level < 4) {
			level++;
			System.out.println("Crew leveled up to " + LEVEL_NAMES[level] + " status.");
		}
	}

	public double getEffectiveness () {
		double crewFactor = (double) size / (double) shipClass.getCrewRequired();
		
		if (crewFactor > 1.0) {
			crewFactor = 1.0;
		}
		
		return crewFactor * morale * LEVEL_EFFICIENCY[level];
	}
	
}
