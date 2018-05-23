/**
 * @author Paul Dennis (pd236m)
 * May 16, 2018
 */
package sts_heuristics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HallOfFame {

	//Used to keep track of the 20 most successful strategies on average
	//that have been run at least 500 times (hopefully)
	
	static final String FAMERS_FILE_LOC = "data/hall_of_fame/hall_of_fame.txt";
	static final String POTENTIALS_FILE_LOC = "data/hall_of_fame/potentials.txt";
	static final String FORMER_HOF = "data/hall_of_fame/old/former_hof_";
	static final int MAX_NUM_FAMERS = 20;
	static final int MAX_NUM_POTENTIALS = 200;
	
	double famersMinAttainmentNeeded;
	double potentialsMinAttainmentNeeded;
	
	private List<AdaptiveStrategy> famers;
	private List<AdaptiveStrategy> potentials;
	
	public HallOfFame () {
		//String directory = "data/gritty/" + System.currentTimeMillis();
		new File("data/hall_of_fame/old/").mkdirs();
		famers = AdaptiveStrategy.readStrategiesFromFile(FAMERS_FILE_LOC);
		potentials = AdaptiveStrategy.readStrategiesFromFile(POTENTIALS_FILE_LOC);
		if (potentials.size() == 0) {
			potentials = new ArrayList<>(famers);
		}
		determineMinAttainmentNeeded();
		writeOldFamers();
	}
	
	private HallOfFame (String reason) {
		System.out.println("Being constructed for reason: " + reason);
		famers = AdaptiveStrategy.readStrategiesFromFile(FAMERS_FILE_LOC);
		potentials = AdaptiveStrategy.readStrategiesFromFile(POTENTIALS_FILE_LOC);
		if (potentials.size() == 0) {
			potentials = new ArrayList<>(famers);
		}
		determineMinAttainmentNeeded();
	}
	
	public void addPotentialMembers (List<AdaptiveStrategy> newPotentials) {
		System.out.println("\tEvaluating " + newPotentials.size() + " new potentials.");
		System.out.println("\tFamers require a minimum of: " + famersMinAttainmentNeeded);
		System.out.println("\tPotentials require a minimum of: " + potentialsMinAttainmentNeeded);
		for (AdaptiveStrategy newStrat : newPotentials) {
			//If either the list has room or the new one is better, and it's not already in, add it.
			if ((newStrat.averageLevelAttained > potentialsMinAttainmentNeeded || potentials.size() < MAX_NUM_POTENTIALS)
					&& !potentials.contains(newStrat)) {
				potentials.add(newStrat);
			}
			if ((newStrat.averageLevelAttained > famersMinAttainmentNeeded || famers.size() < MAX_NUM_FAMERS) 
					&& !famers.contains(newStrat)) {
				famers.add(newStrat);
			}
		}
		
		truncateLists();
		determineMinAttainmentNeeded();
	}
	
	public void recertifyHallOfFame () {
		System.out.println("\"Recertifying\" hall of fame by running each strategy 10,000 times.");
		for (AdaptiveStrategy strategy : famers) {
			for (int i = 0; i < 10000; i++) {
				new ClimbingGame(strategy).playGame();
			}
		}
		//TODO: implement
	}
	
	public static void main(String[] args) {
		testAddPotentialMembers();
	}
	
	public static void testAddPotentialMembers () {
		HallOfFame hof = new HallOfFame("test");
		double min = hof.famersMinAttainmentNeeded;
		
		List<AdaptiveStrategy> unworthyStrategies = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			AdaptiveStrategy strategy = new AdaptiveStrategy(new Hero());
			strategy.setAverageLevelAttained(min - 0.1);
			unworthyStrategies.add(strategy);
		}
		
		hof.addPotentialMembers(unworthyStrategies);
		
		for (AdaptiveStrategy strategy : unworthyStrategies) {
			if (hof.famers.contains(strategy)) {
				throw new AssertionError("Strategy should not have been added.");
			}
		}
		System.out.println("Test completed successfully.");
		
	}
	
	//Write out the top 20 and push any formers to formers
	//formers nyi
	public void close () {
		System.out.println("Hall of Fame closing down for the day...");
		truncateLists();
		
		System.out.println("Writing " + famers.size() + " famers to file.");
		AdaptiveStrategy.writeStrategiesToFile(famers, FAMERS_FILE_LOC);
		
		System.out.println("Writing " + potentials.size() + " potentials to file.");
		AdaptiveStrategy.writeStrategiesToFile(potentials, POTENTIALS_FILE_LOC);
		
		//Close should only be called when we're done.
		//So if someone tries to access these after close, we'll fail loudly
		famers = null;
		potentials = null;
	}
	
	private void writeOldFamers () {
		System.out.println("Saving old famers to file.");
		AdaptiveStrategy.writeStrategiesToFile(famers, FORMER_HOF + System.currentTimeMillis() + ".txt");
	}
	
	private void truncateLists () {
		Collections.sort(famers);
		Collections.sort(potentials);
		if (famers.size() > MAX_NUM_FAMERS) {
			famers = famers.subList(0, MAX_NUM_FAMERS);
		}
		if (potentials.size() > MAX_NUM_POTENTIALS) {
			potentials = potentials.subList(0, MAX_NUM_POTENTIALS);
		}
		determineMinAttainmentNeeded();
	}
	
	private void determineMinAttainmentNeeded () {
		if (potentials.size() > 0) {
			potentialsMinAttainmentNeeded = potentials.get(potentials.size() - 1).averageLevelAttained;
		} else {
			potentialsMinAttainmentNeeded = 0.0;
		}
		if (famers.size() > 0) {
			famersMinAttainmentNeeded = famers.get(famers.size() - 1).averageLevelAttained;
		} else {
			famersMinAttainmentNeeded = 0.0;
		}
	}
	
	public List<AdaptiveStrategy> getFamers () {
		List<AdaptiveStrategy> response = new ArrayList<>();
		for (AdaptiveStrategy strategy : famers) {
			response.add(strategy.getCopy());
		}
		return response;
	}
	
	public List<AdaptiveStrategy> getPotentials () {
		List<AdaptiveStrategy> response = new ArrayList<>();
		for (AdaptiveStrategy strategy : potentials) {
			response.add(strategy.getCopy());
		}
		return response;
	}
}
