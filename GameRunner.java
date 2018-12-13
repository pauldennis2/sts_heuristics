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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


public class GameRunner {

	private HallOfFame hallOfFame;
	private ExecutorService threadService;
	private AtomicLong optimizedCount;
	
	@SuppressWarnings("unused")
	private int optimizedCount2;
	
	private int timesSinceImprovement;
	
	List<AdaptiveStratWithRecording> measuredStrats;
	
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
	public static int OUTPUT_LEVEL = 0;
	
	private double bestHof;
	
	public GameRunner () {
		optimizedCount = new AtomicLong(0);
		optimizedCount2 = 0;
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
		if (OUTPUT_LEVEL >= 0) {
			System.out.println("Times since improvement = " + timesSinceImprovement);
		}
	}
	
	public static void main(String[] args) {
		GameRunner runner = new GameRunner();
		
		runner.getGrittyHallOfFameData();
	}
	
	public void evaluateFamersConditions () {
		purgeUnusedConditions(hallOfFame.getFamers());
		evaluateConditions(hallOfFame.getFamers());
	}
	
	public void analyzeHof () {
		StrategyGroupReport report = new StrategyGroupReport(hallOfFame.getFamers());
		System.out.println(report);
	}
	
	//Uses the runStrategies() method to run the strat.
	public double runStrategyWithRunStrategies (String name, int numDummies, boolean multiThread, int numTimes) {
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		List<AdaptiveStrategy> famers = hallOfFame.getFamers();
		AdaptiveStrategy theOne = null;
		for (AdaptiveStrategy strategy : famers) {
			if (strategy.getName().equals(name)) {
				theOne = strategy;
			}
		}
		if (theOne != null) {
			System.out.println("Creating " + numDummies + " dummy strategies for testing.");
			for (int i = 0; i < numDummies; i++) {
				strategies.add(new AdaptiveStrategy(new Hero()));
			}
			strategies.add(theOne);
			if (multiThread) {
				runStrategiesMultiThread(strategies, numTimes);
			} else {
				runStrategies(strategies, numTimes);
			}
			if (OUTPUT_LEVEL >= 1) {
				for (AdaptiveStrategy strat : strategies) {
					System.out.println("\t" + strat.getName() + " : " + strat.getAttainment());
				}
			}
			System.out.println(name + " achieved average attainment of " + theOne.getAverageLevelAttained() + ".");
			return theOne.getAverageLevelAttained();
		} else {
			System.err.println("Couldn't find " + name + ".");
			return 0.0;
		}
	}
	
	public void getGrittyDetailsOnConditionsOfHOF () {
		measuredStrats = 
				AdaptiveStratWithRecording.buildRecordingStrategiesFromFile(ROOT_DIR + "hall_of_fame.txt");
		
		for (AdaptiveStratWithRecording strategy : measuredStrats) {
			for (int i = 0; i < 3000; i++) {
				new ClimbingGame(strategy).playGame();
			}
		}
	}
	
	public List<AdaptiveStrategy> evaluateConditions (List<AdaptiveStrategy> strategies) {
		
		//Run each strategy to determine a baseline
		for (AdaptiveStrategy famer : strategies) {
			for (int i = 0; i < 1000; i++) {
				int result = new ClimbingGame(famer).playGame();
				famer.addAttainment(result);
			}
		}
		
		calcAverageAttainment(strategies);
		Set<ConditionAndValues> goodConditions = new HashSet<>();
		//Run each strategy with a partial lobotomy.
		for (AdaptiveStrategy strategy : strategies) {
			System.out.println("Analyzing " + strategy.getName() + " by removing one condition at a time.");
			Map<Conditional, Double> attainmentWithConditionsRemoved = new HashMap<>();
			for (Conditional condition : strategy.getConditionsAndValuesMap().keySet()) {
				AdaptiveStrategy copy = strategy.getCopy();
				Map<Conditional, Map<String, Double>> conditionsAndValuesMap = copy.getConditionsAndValuesMap();
				conditionsAndValuesMap.remove(condition);
				copy.setConditionsAndValuesMap(conditionsAndValuesMap);
				int sum = 0;
				for (int i = 0; i < 1000; i++) {
					sum += new ClimbingGame(copy).playGame();
				}
				double average = (double) sum / 1000.0;
				
				attainmentWithConditionsRemoved.put(condition, average);
			}
			double baseAverage = strategy.getAverageLevelAttained();
			System.out.println("Report:");
			System.out.println(strategy.getName() + " attained " + baseAverage + " with all conditions.");
			List<Conditional> conditionsToRemove = new ArrayList<>();
			for (Conditional condition : attainmentWithConditionsRemoved.keySet()) {
				double withConditionRemoved = attainmentWithConditionsRemoved.get(condition);
				System.out.println("\t" + condition + " : " + withConditionRemoved);
				//If it performs WORSE with the condition removed, the condition is good.
				if (baseAverage > withConditionRemoved) {
					//Keep track of any conditions that REALLY help
					if (baseAverage - withConditionRemoved > 3.0) {
						goodConditions.add(new ConditionAndValues(condition, strategy.getConditionsAndValuesMap().get(condition)));
					}
				} else {
					conditionsToRemove.add(condition);
				}
			}
			//Remove any conditions that aren't helping
			Map<Conditional, Map<String, Double>> conditionsAndValuesMap = strategy.getConditionsAndValuesMap();
			for (Conditional condition : conditionsToRemove) {
				conditionsAndValuesMap.remove(condition);
			}
			strategy.setConditionsAndValuesMap(conditionsAndValuesMap);
		}
		AdaptiveStrategy.addSuccessfulConditionsValues(goodConditions);
		return strategies;
	}
	
	public List<AdaptiveStrategy> purgeUnusedConditions (List<AdaptiveStrategy> strategies) {
		if (measuredStrats == null) {
			getGrittyDetailsOnConditionsOfHOF();
		}
		
		Map<String, AdaptiveStrategy> strategyMap = new HashMap<>();
		
		
		strategies.stream().forEach(strat -> strategyMap.put(strat.getName(), strat));
		Set<Conditional> unusedConditions = new HashSet<>();
		for (AdaptiveStratWithRecording record : measuredStrats) {
			AdaptiveStrategy corresponding = strategyMap.get(record.getName());
			int numConditionsBefore = corresponding.getConditionsAndValuesMap().keySet().size();
			Map<Conditional, Map<String, Double>> conditionsAndValuesMap = corresponding.getConditionsAndValuesMap();
			CountMap<Conditional> countMap = record.getCountMap();
			Map<Conditional, Map<String, Double>> keptConditionsAndValues = new HashMap<>();
			for (Conditional condition : conditionsAndValuesMap.keySet()) {
				if (countMap.get(condition) != 0) {
					keptConditionsAndValues.put(condition, conditionsAndValuesMap.get(condition));
				} else {
					unusedConditions.add(condition);
				}
			}
			corresponding.setConditionsAndValuesMap(keptConditionsAndValues);
			int numConditionsAfter = corresponding.getConditionsAndValuesMap().keySet().size();
			System.out.println("Removed " + (numConditionsBefore - numConditionsAfter) 
					+ " conditions from " + corresponding.getName());
		}
		SingleCondition.addUnusedConditions(unusedConditions);
		return strategies;
	}
	
	public AdaptiveStrategy findStrategyFromHOF (String name) {
		System.out.println("Looking for " + name + "...");
		AdaptiveStrategy theStrat = null;
		for (AdaptiveStrategy strategy : hallOfFame.getFamers()) {
			if (strategy.getName().equals(name)) {
				theStrat = strategy;
			}
		}
		if (theStrat == null) {
			System.err.println("Could not find. Returning null.");
		}
		return theStrat;
	}
	
	public void runStrategy (String name, int numTimes) {
		System.out.println("Looking for " + name + "...");
		AdaptiveStrategy theOne = findStrategyFromHOF(name);
		System.out.println(theOne);
		if (theOne != null) {
			System.out.println("Running " + name + " " + numTimes + " times...");
			int sum = 0;
			for (int i = 0; i < numTimes; i++) {
				int result = new ClimbingGame(theOne).playGame();
				sum += result;
				if (OUTPUT_LEVEL >= 1) {
					System.out.println("\tLevel attained:" + result);
				}
			}
			double average = (double) sum / (double) numTimes;
			System.out.println("Average = " + average);
		} else {
			System.out.println("Couldn't find strategy " + name + " in famers.");
		}
	}
	
	public void run () {
		long startTime = System.currentTimeMillis();
		breedingMethod1();
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
		superLongBreedingMethod();
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		writeMetadata(duration);
		hallOfFame.close();
		System.out.println("Whole process took: " + duration + " milliseconds.");
		System.out.println("First cut helped: " + firstCutHelped);
		System.out.println("Second cut helped: " + secondCutHelped);
		System.out.println("Third cut helped: " + thirdCutHelped);
	}
	
	private void superLongBreedingMethod () {
		System.out.println("Running a super long duration breeding method.");
		breedingMethod1();
		breedingMethodNewBlood();
		breedingMethod1();
		breedingMethodTrueDesparation();
		breedingMethod1();
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
			String helpedData = seedStratCount + ", " + breedStratCount + 
					", " + deepBreedStratCount + ", " + crossBreedStratCount +
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
		crossBreedGroups(hallOfFame.getFamers(), hallOfFame.getPotentials(), 20);
	}
	
	public void breedingMethodNewBlood () {
		System.out.println("===Getting somewhat desparate... going to 2nd breeding method.===");
		List<AdaptiveStrategy> bestNewBlood = takeTopPercent(createAndRunNewStrats(100), 0.2); //20
		List<AdaptiveStrategy> bestBred = takeTopPercent(breedStrategies(bestNewBlood, 5), 0.2); //20
		List<AdaptiveStrategy> bestGcs = takeTopPercent(deepBreedStrategies(bestBred, 5), 0.2); //20
		List<AdaptiveStrategy> bestGggcs = takeTopPercent(deepBreedStrategies(bestGcs, 5), 0.2); //20
		List<AdaptiveStrategy> bestSomething = takeTopPercent(crossBreedStrategies(bestGggcs, 1), 0.1); //?
		List<AdaptiveStrategy> best = breedStrategies(bestSomething, 10);
		crossBreedGroups(best, hallOfFame.getFamers(), 10);
	}
	
	public void breedingMethodTrueDesparation () {
		System.out.println("===Getting truly desparate... going to 3rd breeding method (this might take a while).===");
		List<AdaptiveStrategy> bestNewBlood = takeTopPercent(createAndRunNewStrats(500), 0.1); //50
		List<AdaptiveStrategy> bestGgcs = takeTopPercent(deepBreedStrategies(bestNewBlood, 10), 0.2); //100
		List<AdaptiveStrategy> bestBred = takeTopPercent(breedStrategies(bestNewBlood, 10), 0.1); //50
		
		List<AdaptiveStrategy> best = new ArrayList<>();
		best.addAll(takeTopPercent(bestNewBlood, 0.5)); //25
		best.addAll(takeTopPercent(bestGgcs, 0.5)); //50
		best.addAll(takeTopPercent(bestBred, 0.5)); //25
		
		best = takeTopPercent(best, 0.5); //50
		crossBreedGroups(best, hallOfFame.getFamers(), 30);
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
		for (int runCount = 5; runCount < 150; runCount += 5) {
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
	
	public void testHallOfFame500Variability () {
		System.out.println("Running 500 variability...");
		testStrategysVariabilityOverNRuns(hallOfFame.getFamers().get(0), 1000);
		System.out.println("Finished.");
	}
	
	public void testStrategysVariabilityOverNRuns (AdaptiveStrategy strategy, int numRuns) {
		long start = System.currentTimeMillis();
		System.out.println("Running variability tests with " + numRuns + " runcount for " + strategy.getName());
		List<Double> attainmentAverageList = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			int sum = 0;
			for (int j = 0; j < numRuns; j++) {
				sum += new ClimbingGame(strategy).playGame();
			}
			double average = (double) sum / (double) numRuns;
			attainmentAverageList.add(average);
		}
		
		double lowestAverage = 10000000.0;
		double highestAverage = 0.0;
		double sum = 0.0;
		
		for (Double d : attainmentAverageList) {
			sum += d;
			if (d > highestAverage) {
				highestAverage = d;
			}
			if (d < lowestAverage) {
				lowestAverage = d;
			}
		}
		double averageAverage = sum / 100.0;
		long end = System.currentTimeMillis();
		System.out.println("Process completed in " + (end - start) + " milliseconds.");
		System.out.println("Ran 100 separate trials of " + strategy.getName() + " " + numRuns + " times each.");
		System.out.println("Highest: " + highestAverage);
		System.out.println("Lowest:" + lowestAverage);
		System.out.println("Range: " + (highestAverage - lowestAverage));
		System.out.println("Average: " + averageAverage);
	}
	
	//Method to test the variability of a specific strategy
	public void testStrategysVariability (AdaptiveStrategy strategy) {
		System.out.println("Running variability tests for strategy " + strategy.getName());
		String VARIABILITY_DIRECTORY = "data/variability1/"; 
		new File(VARIABILITY_DIRECTORY).mkdirs();
		Map<Integer, Double> runCountToDifferenceMap = new HashMap<>();
		for (int runCount = 5; runCount < 150; runCount+= 5) {
			double differenceSum = 0.0;
			int rerunCount = 1;
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
	//This method doesn't work reliably
	public void runStrategiesMultiThread (List<AdaptiveStrategy> strategies, int numTimes) {
		threadService = Executors.newCachedThreadPool();
		for (AdaptiveStrategy strategy : strategies) {
			threadService.submit(() -> {
				if (numTimes >= 300) {
					int sum = 0;
					for (int i = 0; i < 100; i++) {
						int result = new ClimbingGame(strategy).playGame();
						strategy.addAttainment(result);
						sum += result;
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
	
	public static final double FIRST_CUT_PERCENT = 0.65;
	public static final int FIRST_CUT_NUM_RUNS = 20;
	public static final double SECOND_CUT_PERCENT = 0.75;
	public static final int SECOND_CUT_NUM_RUNS = 50;
	public static final double THIRD_CUT_PERCENT = 0.85;
	public static final int THIRD_CUT_NUM_RUNS = 100;
	
	private int firstCutHelped = 0;
	private int secondCutHelped = 0;
	private int thirdCutHelped = 0;
	
	public void runStrategies (List<AdaptiveStrategy> strategies, int numTimes) {
		//TODO This should probably be re-written to use recursion
		for (AdaptiveStrategy strategy : strategies) {
			if (numTimes >= THIRD_CUT_NUM_RUNS) {
				int sum = 0;
				int i = 0;
				for (; i < FIRST_CUT_NUM_RUNS; i++) {
					int result = new ClimbingGame(strategy).playGame();
					strategy.addAttainment(result);
					sum += result;
				}
				double average = (double) sum / (double) FIRST_CUT_NUM_RUNS;
				double needed = hallOfFame.getPotentialsMinAttainmentNeeded();
				if (average > needed * FIRST_CUT_PERCENT) {
					for (; i < SECOND_CUT_NUM_RUNS; i++) {
						int result = new ClimbingGame(strategy).playGame();
						strategy.addAttainment(result);
						sum += result;
					}
					average = (double) sum / (double) SECOND_CUT_NUM_RUNS;
					if (average > needed * SECOND_CUT_PERCENT) {
						for (; i < THIRD_CUT_NUM_RUNS; i++) {
							int result = new ClimbingGame(strategy).playGame();
							strategy.addAttainment(result);
							sum += result;
						}
						average = (double) sum / (double) THIRD_CUT_NUM_RUNS;
						if (average > needed * THIRD_CUT_NUM_RUNS) {
							for (; i < numTimes; i++) {
								int result = new ClimbingGame(strategy).playGame();
								strategy.addAttainment(result);
							}
						} else {
							thirdCutHelped++;
						}
					} else {
						secondCutHelped++;
					}
				} else {
					firstCutHelped++;
				}
			} else {
				for (int i = 0; i < numTimes; i++) {
					int result = new ClimbingGame(strategy).playGame();
					strategy.addAttainment(result);
				}
			}
		}
		
		calcAverageAttainment(strategies);
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
	
	public List<AdaptiveStrategy> crossBreedGroups 
		(List<AdaptiveStrategy> first, List<AdaptiveStrategy> second, int numChildren) {
		System.out.println("Received two groups of strategies, with " + first.size() 
			+ " and " + second.size() + " strategies apiece.");
		System.out.println("Cross-breeding these groups.");
		List<AdaptiveStrategy> children = new ArrayList<>();
		List<AdaptiveStrategy> smaller;
		List<AdaptiveStrategy> larger;
		Random r = new Random();
		if (first.size() == 0 || second.size() == 0) {
			System.out.println("One of the lists is empty though. Aborting.");
			return null;
		}
		if (first.size() > second.size()) {
			larger = first;
			smaller = second;
		} else {
			larger = second;
			smaller = first;
		}
		System.out.println("Each strategy in the smaller group will have " + numChildren 
				+ " children randomly with the larger group.");
		System.out.println("This will produce " + smaller.size() * numChildren + " strategies.");
		for (int i = 0; i < numChildren; i++) {
			for (AdaptiveStrategy dad : smaller) {
				AdaptiveStrategy mom = larger.get(r.nextInt(larger.size() - 1));
				children.add(new AdaptiveStrategy(dad, mom));
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
		if (strategies.size() < quantity) {
			return strategies;
		}
		Collections.sort(strategies);
		List<AdaptiveStrategy> best = new ArrayList<>();
		int index = 0;
		while (best.size() < quantity) {
			best.add(strategies.get(index));
			index++;
		}
		return best;
	}
	
	public static List<AdaptiveStrategy> takeTop (List<AdaptiveStrategy> strategies, Number number) {
		if (number instanceof Double) {
			return takeTopPercent(strategies, (Double) number);
		} else if (number instanceof Integer) {
			return takeTopQuantity(strategies, (Integer) number);
		} else {
			System.err.println("takeTop() called without a Double or Integer");
			return null;
		}
	}
}
