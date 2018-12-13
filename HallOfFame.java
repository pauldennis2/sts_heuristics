/**
 * @author Paul Dennis (pd236m)
 * May 16, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HallOfFame {

	//Used to keep track of the 20 most successful strategies on average
	//that have been run at least 500 times (hopefully)
	
	static final String ROOT_DIR = "data/hall_of_fame/";
	static final String FAMERS_FILE_LOC = ROOT_DIR + "hall_of_fame.txt";
	static final String POTENTIALS_FILE_LOC = ROOT_DIR + "potentials.txt";
	static final String FORMER_HOF = ROOT_DIR + "old/former_hof_";
	static final int MAX_NUM_FAMERS = 20;
	static final int MAX_NUM_POTENTIALS = 200;
	
	private double famersMinAttainmentNeeded;
	private double potentialsMinAttainmentNeeded;
	
	private List<AdaptiveStrategy> famers;
	private List<AdaptiveStrategy> potentials;
	
	static boolean TEST_MODE = false;
	static boolean SAVE_OLD_HOF = true;
	
	private String detailedDirectory;
	
	public HallOfFame () {
		new File("data/hall_of_fame/old/").mkdirs();
		new File("data/hall_of_fame/details/").mkdirs();
		famers = AdaptiveStrategy.readStrategiesFromFile(FAMERS_FILE_LOC);
		
		potentials = AdaptiveStrategy.readStrategiesFromFile(POTENTIALS_FILE_LOC);
		if (potentials.size() == 0) {
			potentials = new ArrayList<>(famers);
		}

		determineMinAttainmentNeeded();
		if (SAVE_OLD_HOF) {
			writeOldFamers();
		} else {
			if (GameRunner.OUTPUT_LEVEL >= 0) {
				System.out.println("In Test Mode - not making any file changes.");
			}
		}
	}
	
	public int addPotentialMembers (List<AdaptiveStrategy> newPotentials) {
		detailedDirectory = ROOT_DIR + "details/" + System.currentTimeMillis() + "/";
		
		System.out.println("\tEvaluating " + newPotentials.size() + " new potentials.");
		System.out.println("\tFamers require a minimum of: " + famersMinAttainmentNeeded);
		System.out.println("\tPotentials require a minimum of: " + potentialsMinAttainmentNeeded);
		int numFamersAdded = 0;
		for (AdaptiveStrategy newStrat : newPotentials) {
			//If either the list has room or the new one is better, and it's not already in, add it.
			if ((newStrat.averageLevelAttained > potentialsMinAttainmentNeeded || potentials.size() < MAX_NUM_POTENTIALS)
					&& !potentials.contains(newStrat)) {
				potentials.add(newStrat);
			}
			if ((newStrat.averageLevelAttained > famersMinAttainmentNeeded || famers.size() < MAX_NUM_FAMERS) 
					&& !famers.contains(newStrat)) {
				famers.add(newStrat);
				numFamersAdded++;
				writeAttainmentData(newStrat);
			}
		}
		
		truncateLists();
		determineMinAttainmentNeeded();
		
		return numFamersAdded;
	}
	
	public void setFamers (List<AdaptiveStrategy> strategies) {
		System.err.println("!!Warning!! This method should only be called after the purgeConditions operation.");
		if (strategies.size() != 20) {
			throw new AssertionError("Wrong input list size");
		}
		famers = strategies;
	}
	
	private void writeAttainmentData (AdaptiveStrategy strategy) {
		File detailedFileDirectory = new File(detailedDirectory);
		detailedFileDirectory.mkdirs();
		List<Integer> attainment = strategy.getAttainment();
		if (attainment == null || attainment.size() == 0) {
			System.err.println("Attainment data is null/empty for strategy " + strategy.getName());
			return;
		}
		String data = attainment.toString();
		data = data.substring(1, data.length() - 1);
		try {
			String thisDirectory = detailedDirectory + strategy.getName() + "_details.txt";
			File detailFile = new File(thisDirectory);
			boolean created = detailFile.createNewFile();
			if (!created) {
				System.err.println("Expected no file to be here: " + thisDirectory);
			}
			Files.write(Paths.get(thisDirectory), data.getBytes(), StandardOpenOption.WRITE);
		} catch (IOException ex) {
			System.err.println("Problem writing detailed attainment data to file.");
			ex.printStackTrace();
		}
	}
	
	//Write out the top 20 and top 200
	public void close () {
		System.out.println("Hall of Fame closing down for the day...");
		truncateLists();
		
		if (TEST_MODE) {
			System.out.println("Not making any changes to file.");
		} else {
			System.out.println("Writing " + famers.size() + " famers to file.");
			AdaptiveStrategy.writeStrategiesToFile(famers, FAMERS_FILE_LOC);
			
			System.out.println("Writing " + potentials.size() + " potentials to file.");
			AdaptiveStrategy.writeStrategiesToFile(potentials, POTENTIALS_FILE_LOC);
		}
		//Close should only be called when we're done.
		//So if someone tries to access these after close, we'll fail loudly
		famers = null;
		potentials = null;
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
}
