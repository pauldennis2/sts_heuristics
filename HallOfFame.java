/**
 * @author Paul Dennis (pd236m)
 * May 16, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class HallOfFame {

	//Used to keep track of the 20 most successful strategies on average
	//that have been run at least 500 times (hopefully)
	
	static final String ROOT_DIR = "data/hall_of_fame/";
	static final String FAMERS_FILE_LOC = ROOT_DIR + "hall_of_fame.txt";
	static final String POTENTIALS_FILE_LOC = ROOT_DIR + "potentials.txt";
	static final String FORMER_HOF = ROOT_DIR + "old/former_hof_";
	static final String HIGH_WATER = ROOT_DIR + "high_water.txt";
	static final String TIMES_SINCE_IMPROVEMENT = ROOT_DIR + "times_since_improvement.txt";
	static final int MAX_NUM_FAMERS = 20;
	static final int MAX_NUM_POTENTIALS = 200;
	
	private double famersMinAttainmentNeeded;
	private double potentialsMinAttainmentNeeded;
	
	private List<AdaptiveStrategy> famers;
	private List<AdaptiveStrategy> potentials;
	
	static boolean TEST_MODE = false;
	static boolean SAVE_OLD_HOF = true;
	
	private int timesSinceImprovement;
	
	private final double bestHof;
	
	public HallOfFame () {
		new File("data/hall_of_fame/old/").mkdirs();
		famers = AdaptiveStrategy.readStrategiesFromFile(FAMERS_FILE_LOC);
		Collections.sort(famers);
		bestHof = famers.get(0).averageLevelAttained;
		potentials = AdaptiveStrategy.readStrategiesFromFile(POTENTIALS_FILE_LOC);
		if (potentials.size() == 0) {
			potentials = new ArrayList<>(famers);
		}
		try (Scanner fileScanner = new Scanner(new File(TIMES_SINCE_IMPROVEMENT))) {
			timesSinceImprovement = Integer.parseInt(fileScanner.nextLine());
		} catch (FileNotFoundException ex) {
			System.err.println("!!Couldn't find times_since_improvement.txt. Setting to 0.");
			timesSinceImprovement = 0;
		} catch (NumberFormatException ex) {
			System.err.println("!!Bad file format for times since improvement. Setting it to 0.");
			timesSinceImprovement = 0;
		}
		System.out.println("Times since improvement = " + timesSinceImprovement);
		determineMinAttainmentNeeded();
		if (SAVE_OLD_HOF) {
			writeOldFamers();
		} else {
			System.out.println("In Test Mode - not making any file changes.");
		}
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
	
	//Write out the top 20 and top 200
	public void close (long millis) {
		System.out.println("Hall of Fame closing down for the day...");
		truncateLists();
		
		if (TEST_MODE) {
			System.out.println("Not making any changes to file.");
		} else {
			System.out.println("Writing " + famers.size() + " famers to file.");
			AdaptiveStrategy.writeStrategiesToFile(famers, FAMERS_FILE_LOC);
			
			System.out.println("Writing " + potentials.size() + " potentials to file.");
			AdaptiveStrategy.writeStrategiesToFile(potentials, POTENTIALS_FILE_LOC);
			
			writeHighWaterAndTimesSinceImprovement(millis);
		}
		//Close should only be called when we're done.
		//So if someone tries to access these after close, we'll fail loudly
		famers = null;
		potentials = null;
	}
	
	private void writeHighWaterAndTimesSinceImprovement (long millis) {
		AdaptiveStrategy topDog = famers.get(0);
		if (topDog != null) {
			double highest = topDog.averageLevelAttained;
			double lowest = famers.get(famers.size() - 1).averageLevelAttained;
			System.out.println("Writing highest achievement of " + highest + " to file.");
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String data = "\n" + now.format(formatter) + ", " + highest + ", " + lowest + ", " + millis;
			if (highest > bestHof) {
				timesSinceImprovement = 0;
			} else {
				timesSinceImprovement++;
			}
			String times = "" + timesSinceImprovement;
			try {
				Files.write(Paths.get(HIGH_WATER), data.getBytes(), StandardOpenOption.APPEND);
				Files.write(Paths.get(TIMES_SINCE_IMPROVEMENT), times.getBytes(), StandardOpenOption.WRITE);
			} catch (IOException ex) {
				System.err.println("!!Error writing high water mark data.");
				ex.printStackTrace();
			}
		} else {
			System.out.println("Have no top dog, can't record high water.");
		}
	}
	
	private void writeOldFamers () {
		if (famers.size() > 0) {
			System.out.println("Saving old famers to file.");
			AdaptiveStrategy.writeStrategiesToFile(famers, FORMER_HOF + System.currentTimeMillis() + ".txt");
		} else {
			System.out.println("Famers empty (not saving to file).");
		}
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
	
	public double getFamersMinAttainmentNeeded () {
		return famersMinAttainmentNeeded;
	}
	
	public double getPotentialsMinAttainmentNeeded () {
		return potentialsMinAttainmentNeeded;
	}
	
	public int getTimesSinceImprovement () {
		return timesSinceImprovement;
	}
}
