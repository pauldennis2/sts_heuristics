/**
 * @author Paul Dennis (pd236m)
 * May 25, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GameRunner {
	
	private Map<AdaptiveStrategy, Double> strategyAverageAttainment;
	private Map<AdaptiveStrategy, List<Integer>> strategyToLevelsAttainedMap;

	private HallOfFame hallOfFame;
	
	private ExecutorService threadService;
	
	private static int OUTPUT_LEVEL = 0;
	
	public GameRunner () {
		strategyToLevelsAttainedMap = new ConcurrentHashMap<>();
		strategyAverageAttainment = new ConcurrentHashMap<>();
		hallOfFame = new HallOfFame();
	}
	
	public static void main(String[] args) {
		new GameRunner().run();
	}
	
	public void run () {
		long startTime = System.currentTimeMillis();
		breedingMethod1();
		//getGrittyHallOfFameData();
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		hallOfFame.close(duration);
		System.out.println("Whole process took: " + duration + " milliseconds.");
	}
	
	public void breedingMethod1 () {
		long startTime;
		long endTime;
		seedHallOfFame();
		
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
		crossBreedStrategies(hallOfFame.getFamers(), 3);
	}

	/* This method takes the Hall of Fame strategies, runs them 500 times each,
	 * sums up their level attained counts (i.e. "Died on level 15 3 times, died on level 16 2 times"),
	 * and writes this info to data/gritty/<currentTimeMillis>/<strategyName>.csv
	 * for use in excel spreadsheet and the like.
	 */
	public void getGrittyHallOfFameData () {
		System.out.println("Getting nitty gritty details on Hall of Fame top 20.");
		strategyToLevelsAttainedMap = new HashMap<>();
		//Map<AdaptiveStrategy, Map<Integer, Integer>> strategiesToCountMaps = new HashMap<>();
		String directory = "data/gritty/" + System.currentTimeMillis() + "/";
		new File(directory).mkdirs();
		for (AdaptiveStrategy strategy : hallOfFame.getFamers()) {
			for (int i = 0; i < 5000; i++) {
				int result = new ClimbingGame(strategy).playGame();
				recordResults(strategy, result);
			}
			List<Integer> levelsAttained = strategyToLevelsAttainedMap.get(strategy);
			Map<Integer, Integer> levelsAttainedCountMap = new HashMap<>();
			for (Integer level : levelsAttained) {
				if (levelsAttainedCountMap.containsKey(level)) {
					levelsAttainedCountMap.put(level, levelsAttainedCountMap.get(level) + 1);
				} else {
					levelsAttainedCountMap.put(level, 1);
				}
			}
			String fileName = directory + strategy.getName() + ".csv";
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
	 */
	public void testVariability2 () {
		Map<Integer, Double> runCountToDifferenceMap = new HashMap<>();
		for (int runCount = 5; runCount < 150; runCount+= 5) {
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
					recordResults(strategy, result);
				}
			}
			calcAverageAttainment();
			double totalDifference = 0.0;
			for (AdaptiveStrategy strategy : strategies) {
				double difference = Math.abs(strategy.averageLevelAttained - initialAverageAttainment.get(strategy));
				totalDifference += difference;
			}
			System.out.println("Run count = " + runCount);
			System.out.println("Average difference = " + totalDifference / 1000.0);
			runCountToDifferenceMap.put(runCount, totalDifference / 1000.0);
			strategyToLevelsAttainedMap = new HashMap<>();
		}
		System.out.println("===");
		for (Integer runCount : runCountToDifferenceMap.keySet()) {
			System.out.println(runCount + ", " + runCountToDifferenceMap.get(runCount));
		}
	}
	
	public void calcAverageAttainment () {
		strategyToLevelsAttainedMap.keySet().forEach(strat -> {
			List<Integer> levels = strategyToLevelsAttainedMap.get(strat);
			//double average = levels.stream().mapToInt(e -> e).average().getAsDouble();
			int sum = 0;
			for (Integer i : levels) {
				sum += i;
			}
			double average = (double) sum / (double) levels.size();
			int count = levels.size();
			
			if (OUTPUT_LEVEL >= 2) {
				System.out.println("Strategy: " + strat);
				System.out.println("Average Level Reached: " + average);
				System.out.println("Occurances: " + count);
			}
			strategyAverageAttainment.put(strat, average);
			strat.averageLevelAttained = average;
		});
	}
	
	public void recordResults (AdaptiveStrategy strategy, int result) {
		List<Integer> levelsAttained;
		if (strategyToLevelsAttainedMap.containsKey(strategy)) {
			levelsAttained = strategyToLevelsAttainedMap.get(strategy);
		} else {
			levelsAttained = Collections.synchronizedList(new ArrayList<>());
		}
		levelsAttained.add(result);
		strategyToLevelsAttainedMap.put(strategy, levelsAttained);
	}
	
	public void runStrategies (List<AdaptiveStrategy> strategies, int numTimes) {
		threadService = Executors.newCachedThreadPool();
		for (AdaptiveStrategy strategy : strategies) {
			threadService.submit(() -> {
				for (int i = 0; i < numTimes; i++) {
					int result = new ClimbingGame(strategy).playGame();
					recordResults(strategy, result);
				}
			});
		}
		
		threadService.shutdown();
		try {
			threadService.awaitTermination(240, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		
		calcAverageAttainment();
	}
	
	//Create 500 new strategies and run them 500 times each	
	public void seedHallOfFame () {
		System.out.println("Seeding Hall of Fame by running 500 strategies 500 times.");
		System.out.println("(Process should take < 20 seconds.)");
		List<AdaptiveStrategy> newStrats = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			newStrats.add(new AdaptiveStrategy(new Hero()));
		}
		runStrategies(newStrats, 500);
		hallOfFame.addPotentialMembers(newStrats);
	}
	
	public void breedStrategies (List<AdaptiveStrategy> strategies, int numChildren) {
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
		
		hallOfFame.addPotentialMembers(children);
	}
	
	public void deepBreedStrategies (List<AdaptiveStrategy> strategies, int numGreatGrandchildren) {
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
		
		hallOfFame.addPotentialMembers(children);
	}
	
	public void crossBreedStrategies (List<AdaptiveStrategy> strategies, int numChildren) {
		System.out.println("Received " + strategies.size() + " strategies.");
		System.out.println("Cross-breeding these strategies by giving each pair " + 2 * numChildren + " children.");
		int numPairs = strategies.size() * (strategies.size() - 1);
		System.out.println("This will create " + numPairs * 2 + " strategies.");
		System.out.println("(Cross-breeding is an O(n^2) operation.)");
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
		
		hallOfFame.addPotentialMembers(children);
	}
}
