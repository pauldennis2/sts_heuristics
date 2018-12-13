/**
 * @author Paul Dennis
 * Jul 11, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceAcademy extends Building {
	
	double progressTowardsGreatLeader;
	Random random;
	
	List<GreatLeader> availableLeaders;
	int availableRookies;
	int availableCapable; //Unused for now
	int availableVeterans; //""
	
	public static final int[] CREW_CAPACITY = {25, 50, 100};
	public static final int[] GREAT_LEADER_CAPACITY = {3, 5, 9};
	
	public static final String[] LEVEL_NAMES = {"Basic Academy", "Advanced Academy", "Elite Academy"};
	
	public SpaceAcademy () {
		super(2);
		random = new Random();
		availableLeaders = new ArrayList<>();
		progressTowardsGreatLeader = 0.0;
	}
	
	@Override
	public void doTurn () {
		int level = this.getLevel();
		//Great leader stuff
		for (int i = 0; i <= level; i++) {
			progressTowardsGreatLeader += (random.nextDouble() / 5.0);
		}
		if (progressTowardsGreatLeader >= 2.0) {
			progressTowardsGreatLeader = 2.0;
		}
		if (progressTowardsGreatLeader >= 1.0 && 
			availableLeaders.size() < GREAT_LEADER_CAPACITY[level]) {
			availableLeaders.add(new GreatLeader());
			progressTowardsGreatLeader -= 1.0;
		}
		//Regular Crew stuff
		availableRookies += getProductionRate();
		if (availableRookies > CREW_CAPACITY[level]) {
			availableRookies = CREW_CAPACITY[level];
		}
	}
	
	@Override
	public void upgrade () {
		super.upgrade();
		setProductionRate(getProductionRate() * 2);
	}
	
	@Override
	public String toString () {
		String response = LEVEL_NAMES[getLevel()] + ", Available Crew: " + availableRookies;
		response += "\nGreat Leaders:" + availableLeaders;
		return response;
	}

}
