/**
 * @author Paul Dennis (pd236m)
 * May 25, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


public class GameRunner {

	private HallOfFame hallOfFame;
	private ExecutorService threadService;
	private AtomicLong optimizedCount;
	
	private int timesSinceImprovement;
	
	//Family of variables to help keep track of which meta-strategies produce the most results
	private long seedStratCount = 0;
	private long breedStratCount = 0;
	private long deepBreedStratCount = 0;
	private long crossBreedStratCount = 0;
	
	private int seedStratHelpedCount = 0;
	private int breedStratHelpedCount = 0;
	private int deepBreedStratHelpedCount = 0;
	private int crossBreedStratHelpedCount = 0;
	
	public static final String ROOT_DIR = "data/hall_of_fame/";
	public static final String METADATA_FILE = ROOT_DIR + "metadata.txt";

	public static final String TIMES_SINCE_IMPROVEMENT = ROOT_DIR + "times_since_improvement.txt";
	//private static int OUTPUT_LEVEL = 0;
	
	private double bestHof;
	
	public GameRunner () {
		optimizedCount = new AtomicLong(0);
		hallOfFame = new HallOfFame();
		List<AdaptiveStrategy> famers = hallOfFame.getFamers();
		if (famers.size() > 0 && famers.get(0) != null) {
			bestHof = famers.get(0).averageLevelAttained;
		} else {
			bestHof = 0.0;
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
	}
	
	public static void main(String[] args) {
		//new GameRunner().run();
		new GameRunner().getGrittyHallOfFameData();
		//new GameRunner().testHallOfFameVariability();
	}
	
	public void smallTest () {
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			strategies.add(new AdaptiveStrategy(new Hero()));
		}
		System.out.println("Created ten strategies.");
		for (AdaptiveStrategy strategy : strategies) {
			System.out.println("Running " + strategy.getName() + " ten times:");
			int sum = 0;
			for (int i = 0; i < 10; i++) {
				int result = new ClimbingGame(strategy).playGame();
				sum += result;
				System.out.println("\tLost on: " + result);
				strategy.addAttainment(result);
			}
			double average = (double) sum / 10.0;
			System.out.println("Average: " + average);
		}
		calcAverageAttainment(strategies);
		
		System.out.println("Adding to HOF...");
		hallOfFame.addPotentialMembers(strategies);
		hallOfFame.close();
	}
	
	public void run () {
		long startTime = System.currentTimeMillis();
		breedingMethod1();
		//getGrittyHallOfFameData();
		boolean breeding = true;
		if (timesSinceImprovement >= 3 && breeding) {
			System.out.println("Times since improvement = " + timesSinceImprovement);
			breedingMethodNewBlood();
			System.out.println("Re-running normal breeding method.");
			breedingMethod1();
		}
		if (timesSinceImprovement >= 9 && breeding) {
			breedingMethodTrueDesparation();
		}
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		writeMetadata(duration);
		hallOfFame.close();
		System.out.println("Whole process took: " + duration + " milliseconds.");
		System.out.println("Optimization helped " + optimizedCount + " times.");
	}
	
	private void writeMetadata (long millis) {
		File metadataFile = new File(METADATA_FILE);
		try {
			List<AdaptiveStrategy> famers = hallOfFame.getFamers();
			AdaptiveStrategy topDog = famers.get(0);
			String highwaterData;
			if (topDog != null) {
				double highest = topDog.averageLevelAttained;
				double lowest = famers.get(famers.size() - 1).averageLevelAttained;
				System.out.println("Writing highest achievement of " + highest + " to file.");
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				highwaterData = now.format(formatter) + ", " + highest + ", " + lowest + ", " + millis;
				if (highest > bestHof) {
					timesSinceImprovement = 0;
				} else {
					timesSinceImprovement++;
				}
				String times = "" + timesSinceImprovement;
				new File(TIMES_SINCE_IMPROVEMENT).createNewFile();
				Files.write(Paths.get(TIMES_SINCE_IMPROVEMENT), times.getBytes(), StandardOpenOption.WRITE);
			} else {
				System.out.println("Have no top dog, can't record high water.");
				highwaterData = "MISSING!";
			}
			boolean created = metadataFile.createNewFile();
			if (created) {
				String headers = "Date/Time, Highest, Lowest, Duration, " + 
						"Seed, Breed, Deep, Cross, Seed Helped, Breed Helped, Deep Helped, Cross Helped\n";
				Files.write(Paths.get(METADATA_FILE), headers.getBytes(), StandardOpenOption.APPEND);
			}
			String helpedData = seedStratCount + ", " + breedStratCount + ", " + deepBreedStratCount + ", " + crossBreedStratCount +
					", " + seedStratHelpedCount + ", " + breedStratHelpedCount + ", " + deepBreedStratHelpedCount +
					", " + crossBreedStratHelpedCount + "\n";
			String data = highwaterData + ", " + helpedData;
			Files.write(Paths.get(METADATA_FILE), data.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException ex) {
			System.err.println("!!Error writing metadata to file.");
			ex.printStackTrace();
		}
	}
	
	public void breedingMethod1 () {
		long startTime;
		long endTime;
		createAndRunNewStrats(500);
		
		startTime = System.currentTimeMillis();
		breedStrategies(hallOfFame.getPotentials(), 3);
		endTime = System.currentTimeMillis();
		System.out.println("Breeding took: " + (endTime - startTime));
		
		startTime = System.currentTimeMillis();
		breedStrategies(hallOfFame.getFamers(), 50);
		endTime = System.currentTimeMillis();
		System.out.println("Breeding took: " + (endTime - startTime));
		
		startTime = System.currentTimeMillis();
		deepBreedStrategies(hallOfFame.getPotentials(), 2);
		endTime = System.currentTimeMillis();
		System.out.println("Breeding took: " + (endTime - startTime));
		
		deepBreedStrategies(hallOfFame.getFamers(), 20);
		crossBreedStrategies(hallOfFame.getFamers(), 2);
	}
	
	public void breedingMethodNewBlood () {
		System.out.println("Getting somewhat desparate... going to 2nd breeding method.");
		List<AdaptiveStrategy> bestNewBlood = takeTopPercent(createAndRunNewStrats(1000), 0.2); //200
		List<AdaptiveStrategy> bestBred = takeTopPercent(breedStrategies(bestNewBlood, 5), 0.2); //200
		List<AdaptiveStrategy> bestGcs = takeTopPercent(deepBreedStrategies(bestBred, 5), 0.1); //100
		List<AdaptiveStrategy> bestGggcs = takeTopPercent(deepBreedStrategies(bestGcs, 5), 0.1); //50
		bestGggcs = takeTopQuantity(bestGggcs, 20);
		List<AdaptiveStrategy> bestSomething = takeTopPercent(crossBreedStrategies(bestGggcs, 1), 0.1); //?
		breedStrategies(bestSomething, 10);
	}
	
	public void breedingMethodTrueDesparation () {
		System.out.println("Getting truly desparate... going to 3rd breeding method (this might take a while).");
		List<AdaptiveStrategy> bestNewBlood = takeTopPercent(createAndRunNewStrats(5000), 0.1); //500
		List<AdaptiveStrategy> bestGgcs = takeTopPercent(deepBreedStrategies(bestNewBlood, 10), 0.1); //500
		List<AdaptiveStrategy> bestBred = takeTopPercent(breedStrategies(bestNewBlood, 10), 0.1); //500
		
		List<AdaptiveStrategy> best = new ArrayList<>();
		best.addAll(takeTopPercent(bestNewBlood, 0.2)); //100
		best.addAll(takeTopPercent(bestGgcs, 0.2)); //200
		best.addAll(takeTopPercent(bestBred, 0.2)); //300
		
		best = takeTopPercent(best, 0.5); //150
		breedStrategies(best, 4);
		crossBreedStrategies(takeTopPercent(best, 0.1), 2);
	}

	/* This method takes the Hall of Fame strategies, runs them 5000 times each,
	 * sums up their level attained counts (i.e. "Died on level 15 3 times, died on level 16 2 times"),
	 * and writes this info to data/gritty/<currentTimeMillis>/<strategyName>.csv
	 * for use in excel spreadsheet and the like.
	 */
	public void getGrittyHallOfFameData () {
		System.out.println("Getting nitty gritty details on Hall of Fame top 20.");
		String directory = "data/gritty/" + System.currentTimeMillis() + "/";
		new File(directory).mkdirs();
		for (AdaptiveStrategy strategy : hallOfFame.getFamers()) {
			strategy.averageLevelAttained = 0.0;
			for (int i = 0; i < 5000; i++) {
				int result = new ClimbingGame(strategy).playGame();
				strategy.addAttainment(result);
			}
			List<Integer> levelsAttained = strategy.getAttainment();
			Map<Integer, Integer> levelsAttainedCountMap = new HashMap<>();
			for (Integer level : levelsAttained) {
				if (levelsAttainedCountMap.containsKey(level)) {
					levelsAttainedCountMap.put(level, levelsAttainedCountMap.get(level) + 1);
				} else {
					levelsAttainedCountMap.put(level, 1);
				}
			}
			String fileName = directory + strategy.getName() + "_gritty.csv";
			try (FileWriter fileWriter = new FileWriter(new File(fileName))){
				List<Integer> levelsAttainedKeys = levelsAttainedCountMap.keySet().stream().collect(Collectors.toList());
				Collections.sort(levelsAttainedKeys);
				for (Integer level : levelsAttainedKeys) {
					fileWriter.write(level + ", " + levelsAttainedCountMap.get(level));
					fileWriter.write("\n");
				}
				fileWriter.close();
			} catch (IOException ex) {
				System.err.println("!!Problem writing to file: " + fileName);
			}
		}
		
		System.out.println("Finished getting gritty.");
	}
	
	/* Test the variability to find out how many runs it takes to get a solid read
	 * on how successful a given strategy is
	 * Result: variability ~= 1.3367*runCount^-0.504  . Basically 1.33/sqrt(runCount)
	 * Variability with runCount 500 = 0.0583 (normal amount)
	 * Variability with runCount 10000 = 0.01288
	 * 
	 * Variability is calculated as a function of the runCount as follows:
	 * run a strategy runCount times, then again. 
	 * The difference in average attainment is the variability.
	 */
	public void testVariability2 () {
		Map<Integer, Double> runCountToDifferenceMap = new HashMap<>();
		for (int runCount = 5; runCount < 150; runCount += 5) { //<- this is dead code because of the AssertionError
			List<AdaptiveStrategy> strategies = new ArrayList<>();
			for (int i = 0; i < 1000; i++) {
				strategies.add(new AdaptiveStrategy(new Hero()));
			}
			runStrategies(strategies, runCount);
			Map<AdaptiveStrategy, Double> initialAverageAttainment = new HashMap<>();
			for (AdaptiveStrategy strategy : strategies) {
				initialAverageAttainment.put(strategy, strategy.averageLevelAttained);
				for (int j = 0; j < runCount; j++) {
					int result = new ClimbingGame(strategy).playGame();
					strategy.addAttainment(result);
				}
			}
			calcAverageAttainment(strategies);
			double totalDifference = 0.0;
			for (AdaptiveStrategy strategy : strategies) {
				double difference = Math.abs(strategy.averageLevelAttained - initialAverageAttainment.get(strategy));
				totalDifference += difference;
			}
			System.out.println("Run count = " + runCount);
			System.out.println("Average difference = " + totalDifference / 1000.0);
			runCountToDifferenceMap.put(runCount, totalDifference / 1000.0);
			//strategyToLevelsAttainedMap = new HashMap<>();
			if (true) {
				throw new AssertionError("fix commented out code");
			}
		}
		System.out.println("===");
		for (Integer runCount : runCountToDifferenceMap.keySet()) {
			System.out.println(runCount + ", " + runCountToDifferenceMap.get(runCount));
		}
	}
	
	public void testHallOfFameVariability () {
		System.out.println("Running variability tests for Hall of Fame...");
		hallOfFame.getFamers().forEach(this::testStrategysVariability);
		System.out.println("Finished running tests.");
	}
	
	//Method to test the variability of a specific strategy
	public void testStrategysVariability (AdaptiveStrategy strategy) {
		System.out.println("Running variability tests for strategy " + strategy.getName());
		String VARIABILITY_DIRECTORY = "data/variability2/"; 
		new File(VARIABILITY_DIRECTORY).mkdirs();
		Map<Integer, Double> runCountToDifferenceMap = new HashMap<>();
		for (int runCount = 5; runCount < 150; runCount+= 5) {
			double differenceSum = 0.0;
			int rerunCount = 30;
			for (int j = 0; j < rerunCount; j++) {
				int sum = 0;
				for (int i = 0; i < runCount; i++) {
					sum += new ClimbingGame(strategy).playGame();
				}
				double firstAverage = (double) sum / runCount;
				sum = 0;
				for (int i = 0; i < runCount; i++) {
					sum += new ClimbingGame(strategy).playGame();
				}
				double secondAverage = (double) sum / runCount;
				differenceSum += Math.abs(firstAverage - secondAverage);
			}
			runCountToDifferenceMap.put(runCount, differenceSum / (double) rerunCount);
		}
		try {
			File file = new File(VARIABILITY_DIRECTORY + strategy.getName() + "_variability.csv");
			file.createNewFile(); //We're going to overwrite any pre-existing file
			FileWriter fileWriter = new FileWriter(file);
			List<Integer> runCounts = runCountToDifferenceMap.keySet().stream().collect(Collectors.toList());
			Collections.sort(runCounts);
			for (Integer runCount : runCounts) {
				fileWriter.write(runCount + ", " + runCountToDifferenceMap.get(runCount) + "\n");
			}
			fileWriter.close();
		} catch (IOException ex) {
			System.err.println("Problem writing variability data to file.");
			ex.printStackTrace();
		}
	}
	
	public void calcAverageAttainment (List<AdaptiveStrategy> strategies) {
		for (AdaptiveStrategy strategy : strategies) {
			List<Integer> attainments = strategy.getAttainment();
			int sum = 0;
			synchronized (attainments) {
				Iterator<Integer> it = attainments.iterator();
				while (it.hasNext()) {
					Integer i = it.next();
					sum += i;
				}
			}
			double average = (double) sum / (double) attainments.size();
			strategy.averageLevelAttained = average;
		}
	}
	
	public void recertifyHallOfFame () {
		List<AdaptiveStrategy> famers = hallOfFame.getFamers();
		System.out.println("\"Recertifying\" hall of fame by running each strategy 5,000 times.");
		runStrategies(famers, 5000);
		//TODO: implement
	}
	
	//Set to 0.0 to turn optimization off
	public static double PERCENT_NEEDED_FOR_OPTIMIZATION = 0.7;
	public void runStrategies (List<AdaptiveStrategy> strategies, int numTimes) {
		threadService = Executors.newCachedThreadPool();
		for (AdaptiveStrategy strategy : strategies) {
			threadService.submit(() -> {
				if (numTimes >= 300) {
					int sum = 0;
					for (int i = 0; i < 100; i++) {
						int result = new ClimbingGame(strategy).playGame();
						sum += result;
						strategy.addAttainment(result);
					}
					double needed = hallOfFame.getPotentialsMinAttainmentNeeded();
					double average = (double) sum / 100.0;
					//Check if we are close to qualifying for Potentials
					//If so, go ahead and do the remaining runs
					if (average > needed * PERCENT_NEEDED_FOR_OPTIMIZATION) {
						for (int i = 100; i < numTimes; i++) {
							int result = new ClimbingGame(strategy).playGame();
							strategy.addAttainment(result);
						}
					} else {
						optimizedCount.incrementAndGet();
					}
				} else {
					for (int i = 0; i < numTimes; i++) {
						int result = new ClimbingGame(strategy).playGame();
						strategy.addAttainment(result);
					}
				}
			});
		}
		
		threadService.shutdown();
		try {
			threadService.awaitTermination(240, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		
		calcAverageAttainment(strategies);
	}
	
	public void runStrategiesSingleThread (List<AdaptiveStrategy> strategies, int numTimes) {
		for (AdaptiveStrategy strategy: strategies) {
			for (int i = 0; i < numTimes; i++) {
				int result = new ClimbingGame(strategy).playGame();
				strategy.addAttainment(result);
			}
		}
		
		calcAverageAttainment(strategies);
	}
	
	public List<AdaptiveStrategy> createAndRunSmallBatch () {
		System.out.println("Creating and running 40 strategies 10 times each.");
		List<AdaptiveStrategy> newStrats = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			newStrats.add(new AdaptiveStrategy(new Hero()));
		}
		runStrategies(newStrats, 10);
		return newStrats;
	}
	
	//Create 500 new strategies and run them 500 times each	
	public List<AdaptiveStrategy> createAndRunNewStrats (int numStrategies) {
		System.out.println("Seeding Hall of Fame by creating and running " + numStrategies + " strategies 500 times.");
		System.out.println("(Process should take < 20 seconds.)");
		List<AdaptiveStrategy> newStrats = new ArrayList<>();
		for (int i = 0; i < numStrategies; i++) {
			newStrats.add(new AdaptiveStrategy(new Hero()));
		}
		runStrategies(newStrats, 500);
		seedStratCount += numStrategies;
		seedStratHelpedCount += hallOfFame.addPotentialMembers(newStrats);
		return newStrats;
	}
	
	public List<AdaptiveStrategy> breedStrategies (List<AdaptiveStrategy> strategies, int numChildren) {
		System.out.println("Received " + strategies.size() + " strategies.");
		System.out.println("Breeding by giving each " + numChildren + " children.");
		List<AdaptiveStrategy> children = new ArrayList<>();
		for (AdaptiveStrategy strategy : strategies) {
			for (int i = 0; i < numChildren; i++) {
				children.add(strategy.tweak());
			}
		}
		runStrategies(children, 500);
		Collections.sort(children);
		System.out.println("highest attainment = " + children.get(0).averageLevelAttained);
		breedStratCount += children.size();
		breedStratHelpedCount += hallOfFame.addPotentialMembers(children);
		return children;
	}
	
	public List<AdaptiveStrategy> deepBreedStrategies (List<AdaptiveStrategy> strategies, int numGreatGrandchildren) {
		System.out.println("Received " + strategies.size() + " strategies.");
		System.out.println("Deep breeding each by giving it " + numGreatGrandchildren + " great-great-grandchildren.");
		List<AdaptiveStrategy> children = new ArrayList<>();
		for (AdaptiveStrategy strategy : strategies) {
			for (int i = 0; i < numGreatGrandchildren; i++) {
				AdaptiveStrategy grGrGrandChild = strategy.tweak().tweak().tweak().tweak();
				children.add(grGrGrandChild);
			}
		}
		runStrategies(children, 500);
		Collections.sort(children);
		System.out.println("highest attainment = " + children.get(0).averageLevelAttained);
		deepBreedStratCount += children.size();
		deepBreedStratHelpedCount += hallOfFame.addPotentialMembers(children);
		return children;
	}
	
	public List<AdaptiveStrategy> crossBreedStrategies (List<AdaptiveStrategy> strategies, int numChildren) {
		System.out.println("Received " + strategies.size() + " strategies.");
		System.out.println("Cross-breeding these strategies by giving each pair " + 2 * numChildren + " children.");
		int numStrategies = strategies.size() * (strategies.size() - 1) * 2 * numChildren;
		System.out.println("This will create " + numStrategies + " strategies.");
		System.out.println("(Cross-breeding is an O(n^2) operation.)");
		
		if (numStrategies > 2000) {
			System.out.println("!!Warning - this will create over 2000 strategies and take a substantial amount of time.");
			System.out.println("Confirm by typing 'y' to proceed.");
			Scanner scanner = new Scanner(System.in);
			String response = scanner.nextLine();
			if (!response.toLowerCase().contains("y")) {
				System.out.println("Operation not confirmed. Aborting.");
				scanner.close();
				return null;
			} else {
				System.out.println("Confirmed, proceeding...");
			}
			scanner.close();
		}
		
		List<AdaptiveStrategy> children = new ArrayList<>();
		for (AdaptiveStrategy dad : strategies) {
			for (AdaptiveStrategy mom : strategies) {
				if (dad.equals(mom)) {
					continue;
				}
				for (int i = 0; i < numChildren; i++) {
					children.add(new AdaptiveStrategy(dad, mom));
				}
			}
		}
		runStrategies(children, 500);
		Collections.sort(children);
		System.out.println("highest attainment = " + children.get(0).averageLevelAttained);
		crossBreedStratCount += children.size();
		crossBreedStratHelpedCount += hallOfFame.addPotentialMembers(children);
		return children;
	}
	
	public static List<AdaptiveStrategy> takeTopPercent (List<AdaptiveStrategy> strategies, double percentToKeep) {
		Collections.sort(strategies);
		List<AdaptiveStrategy> best = new ArrayList<>();
		int index = 0;
		while (best.size() < strategies.size() * percentToKeep) {
			best.add(strategies.get(index));
			index++;
		}
		
		return best;
	}
	
	public static List<AdaptiveStrategy> takeTopQuantity (List<AdaptiveStrategy> strategies, int quantity) {
		Collections.sort(strategies);
		List<AdaptiveStrategy> best = new ArrayList<>();
		int index = 0;
		while (best.size() < quantity) {
			best.add(strategies.get(index));
			index++;
		}
		return best;
	}
}
