/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sts_heuristics.EffectType.*;

public class ClimbingGame {
	
	private Hero hero;
	private Monster monster;
	private int level;
	
	private List<Card> drawPile;
	private List<Card> discardPile;
	
	static Map<String, Card> newCardOptions;
	static Map<String, HeroAction> advUpgradeOptions;
	static Map<AdaptiveStrategy, Double> strategyAverageAttainment;
	
	static Map<AdaptiveStrategy, List<Integer>> strategyToLevelsAttainedMap;
	
	static HallOfFame hallOfFame;
	
	//0 - used for highest level output
	//1 - used for things that occur 1-2 times per game
	//2 - used for things that occur timers per round
	public static int OUTPUT_LEVEL = 4;
	private static boolean init = false;
	
	public ClimbingGame () {
		if (!init) {
			staticInit();
		}
		hero = new Hero();
		level = 1;
	}
	
	public ClimbingGame (AdaptiveStrategy strategy) {
		this();
		strategy.setHero(hero);
		hero.setStrategy(strategy);
		if (OUTPUT_LEVEL >= 1) {
			System.out.println("Starting new game with this strategy: " + strategy);
		}
	}
	
	public static void staticInit () {
		hallOfFame = new HallOfFame();
		strategyAverageAttainment = new HashMap<>();
		strategyToLevelsAttainedMap = new HashMap<>();
		
		newCardOptions = Card.getCardMap();
		
		advUpgradeOptions = new HashMap<>();
		//Remove the least powerful card from the deck
		advUpgradeOptions.put("removeCard", hero -> {
			List<Card> deck = hero.getDeck();
			Collections.sort(deck);
			Card removed = deck.get(deck.size() - 1);
			//Last card should be the least powerful
			
			int nonExhaustCards = (int) deck.stream()
					.filter(card -> !card.isExhaust())
					.count();
			if (removed.isExhaust() || nonExhaustCards >= 2) {
				hero.removeCard(removed);
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\t+++Removed " + removed);
				}
			} else {
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\tCannot allow removal of last non-exhaust card. Did not remove: " + removed);
				}
			}
		});
		//Upgrade the most powerful un-upgraded card
		advUpgradeOptions.put("upgradeCard", hero -> {
			//This code is uglier than it should be because I'm not comfortable enough with ref/value and copying
			List<Card> deck = new ArrayList<>(hero.getDeck());
			deck.removeIf(Card::isUpgraded);
			if (deck.size() == 0) {
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("All cards already upgraded.");
				}
			} else {
				Collections.sort(deck);
				//First card is the highest level non-upgraded card
				Card upg = deck.get(0);
				boolean success = hero.removeCard(upg);
				if (!success) {
					throw new AssertionError("Failure temporarily removing card");
				}
				upg.upgrade();
				hero.addCard(upg);
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\t+++Upgraded " + upg);
				}
			}
		});
		//Add a new card to the deck
		//List<String> options = Arrays.asList("heal", "strikeDefend", "strikeExhaust", "healBlock");
		advUpgradeOptions.put("addCard", hero -> {
			//Ugly ugly code to take all available upgrade options and remove a random one
			List<String> regCards = Card.REGULAR_CARD_NAMES;
			List<String> powerCards = Card.POWER_CARD_NAMES;
			Collections.shuffle(regCards);
			Collections.shuffle(powerCards);
			List<String> options = new ArrayList<>();
			options.add(regCards.get(0));
			options.add(regCards.get(1));
			options.add(powerCards.get(0));
			
			Map<String, Card> availableUpgradeOptions = new HashMap<>();
			for (String cardName : newCardOptions.keySet()) {
				availableUpgradeOptions.put(cardName, new Card(newCardOptions.get(cardName)));
			}

			availableUpgradeOptions.remove(options.get(0));
			if (OUTPUT_LEVEL >= 3) {
				System.out.println("\tChoices: " + availableUpgradeOptions.keySet());
			}
			
			List<String> preferences = hero.getStrategy().getCardPrefs();
			String topPref = preferences.get(0);
			Card chosen;
			if (availableUpgradeOptions.containsKey(topPref)) {
				chosen = availableUpgradeOptions.get(topPref);
			} else {
				//Since only one option was removed, we know that if the first choice wasn't available
				//The second choice must be
				chosen = availableUpgradeOptions.get(preferences.get(1));
			}
			hero.addCard(chosen);
			if (OUTPUT_LEVEL >= 2) {
				System.out.println("\t+++Added " + chosen + " to deck.");
			}
			if (chosen.getLevel() > 1) {
				throw new AssertionError("New card cannot be already upgraded.");
			}
		});
		//Increase the hero's max hitpoints
		advUpgradeOptions.put("maxHp", hero -> {
			hero.increaseMaxHp(3);
			if (OUTPUT_LEVEL >= 2) {
				System.out.println("\t+++Increased maxHp by 3");
			}
		});
		init = true;
	}
	
	public static void main(String[] args) {
		/*
		staticInit();
		breedingMethod1();
		hallOfFame.close();
		*/
		new ClimbingGame(new AdaptiveStrategy(new Hero())).playGame();
	}
	
	public static void breedingMethod1 () {
		seedHallOfFame();
		breedStrategies(hallOfFame.getPotentials(), 3);
		breedStrategies(hallOfFame.getFamers(), 50);
		
		deepBreedStrategies(hallOfFame.getPotentials(), 2);
		deepBreedStrategies(hallOfFame.getFamers(), 10);
		crossBreedStrategies(hallOfFame.getFamers(), 3);
	}

	//This method takes the Hall of Fame strategies, runs them 500 times each,
	//sums up their level attained counts (i.e. "Died on level 15 3 times, died on level 16 2 times"),
	//and writes this info to data/gritty/<currentTimeMillis>/<strategyName>.csv
	//for use in excel spreadsheet and the like
	public static void getGrittyHallOfFameData () {
		//TODO: test fun new functionality!
		strategyToLevelsAttainedMap = new HashMap<>();
		//Map<AdaptiveStrategy, Map<Integer, Integer>> strategiesToCountMaps = new HashMap<>();
		String directory = "data/gritty/" + System.currentTimeMillis();
		new File(directory).mkdirs();
		for (AdaptiveStrategy strategy : hallOfFame.getFamers()) {
			for (int i = 0; i < 500; i++) {
				new ClimbingGame(strategy).playGame();
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
			String fileName = directory + "/" + strategy.getName() + ".csv";
			try (FileWriter fileWriter = new FileWriter(new File(fileName))){
				List<Integer> levelsAttainedKeys = levelsAttainedCountMap.keySet().stream().collect(Collectors.toList());
				Collections.sort(levelsAttainedKeys);
				for (Integer level : levelsAttainedKeys) {
					fileWriter.write(level + ", " + levelsAttainedCountMap.get(level));
				}
				fileWriter.close();
			} catch (IOException ex) {
				System.err.println("!!Problem writing to file: " + fileName);
			}
		}
	}
	
	//Create 500 new strategies and run them 500 times each	
	public static void seedHallOfFame () {
		System.out.println("Seeding Hall of Fame by running 500 strategies 500 times.");
		System.out.println("(Process should take < 20 seconds.)");
		List<AdaptiveStrategy> newStrats = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			newStrats.add(new AdaptiveStrategy(new Hero()));
		}
		runStrategies(newStrats, 500);
		hallOfFame.addPotentialMembers(newStrats);
	}
	
	public static void breedStrategies (List<AdaptiveStrategy> strategies, int numChildren) {
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
	
	public static void deepBreedStrategies (List<AdaptiveStrategy> strategies, int numGreatGrandchildren) {
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
	
	public static void crossBreedStrategies (List<AdaptiveStrategy> strategies, int numChildren) {
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
	
	public static void runStrategies (List<AdaptiveStrategy> strategies, int numTimes) {
		for (AdaptiveStrategy strategy : strategies) {
			for (int i = 0; i < numTimes; i++) {
				new ClimbingGame(strategy).playGame();
			}
		}
		calcAverageAttainment();
	}
	
	//Test the variability to find out how many runs it takes to get a solid read
	//on how successful a given strategy is
	//Result: variability ~= 1.3367*runCount^-0.504  . Basically 1.33/sqrt(runCount)
	//Variability with runCount 500 = 0.0583 (normal amount)
	//Variability with runCount 10000 = 0.01288
	public static void testVariability2 () {
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
					new ClimbingGame(strategy).playGame();
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
	
	public static void joyfulDragonTest () {
		AdaptiveStrategy joyfulDragon = AdaptiveStrategy.buildJoyfulDragon();
		for (int i = 0; i < 5000; i++) {
			ClimbingGame game = new ClimbingGame(joyfulDragon);
			game.playGame();
		}
		double av = strategyToLevelsAttainedMap.get(joyfulDragon).stream()
			.mapToInt(num -> num)
			.average()
			.getAsDouble();
		
		System.out.println("Average attainment = " + av);
	}
	
	@Deprecated
	public static void geneticsTest () {
		//Count: 5000
		for (int i = 0; i < 5000; i++) {
			new ClimbingGame().playGame();
		}
		calcAverageAttainment();
		
		List<AdaptiveStrategy> strategies = strategyToLevelsAttainedMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(strategies);
		
		//Count: 500
		List<AdaptiveStrategy> bestStrategies = takeTopPercent(strategies, 0.1);
		
		strategyToLevelsAttainedMap = new HashMap<>();
		Map<AdaptiveStrategy, List<AdaptiveStrategy>> baseStratToTweaks = new HashMap<>();
		for (AdaptiveStrategy strategy : bestStrategies) {
			List<AdaptiveStrategy> tweakedStrats = new ArrayList<>();
			//Make three children
			for (int i = 0; i < 3; i++) {
				tweakedStrats.add(strategy.tweak());
			}
			baseStratToTweaks.put(strategy, tweakedStrats);
		}
		//Count: 2000
		List<AdaptiveStrategy> parentsAndKids = new ArrayList<>();
		for (AdaptiveStrategy baseStrategy : baseStratToTweaks.keySet()) {
			parentsAndKids.add(baseStrategy);
			//Play each game 10 times
			for (int i = 0; i < 10; i++) {
				new ClimbingGame(baseStrategy).playGame();
			}
			//Play each child 10 times
			for (AdaptiveStrategy child : baseStratToTweaks.get(baseStrategy)) {
				parentsAndKids.add(child);
				for (int i = 0; i < 10; i++) {
					new ClimbingGame(child).playGame();
				}
			}
		}
		calcAverageAttainment();
		List<AdaptiveStrategy> top1Percent = takeTopPercent(parentsAndKids, 0.01); //Count: 20
		List<AdaptiveStrategy> top10Percent = takeTopPercent(parentsAndKids, 0.1); //Count: 200
		hallOfFame.addPotentialMembers(top1Percent);
		long time = System.currentTimeMillis();
		AdaptiveStrategy.writeStrategiesToFile(top1Percent, "data/top1percent_" + time +".txt");
		AdaptiveStrategy.writeStrategiesToFile(top10Percent, "data/top10percent_" + time + ".txt");
		
		if (OUTPUT_LEVEL >= 1) {
			for (AdaptiveStrategy baseStrategy : baseStratToTweaks.keySet()) {
				System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
				System.out.println("Base Strat: " + baseStrategy);
				System.out.println("Children:");
				for (AdaptiveStrategy child : baseStratToTweaks.get(baseStrategy)) {
					System.out.println("{\n" + child + "\n}");
				}
				System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			}
		}
		//!!!Unlike other maps the keys here are the children and the values are the parents
		Map<AdaptiveStrategy, AdaptiveStrategy> mostImprovedChildrenToParents = new HashMap<>();
		//If we don't find a way to work these back in we risk losing the best strats
		List<AdaptiveStrategy> parentsWithNoImprovedChildren = new ArrayList<>();
		for (AdaptiveStrategy baseStrategy : baseStratToTweaks.keySet()) {
			double bestAverage = baseStrategy.averageLevelAttained;
			AdaptiveStrategy mostImprovedChild = null;
			for (AdaptiveStrategy child : baseStratToTweaks.get(baseStrategy)) {
				if (child.averageLevelAttained > bestAverage) {
					bestAverage = child.averageLevelAttained;
					mostImprovedChild = child;
				}
			}
			if (mostImprovedChild != null) {
				mostImprovedChildrenToParents.put(mostImprovedChild, baseStrategy);
			} else {
				parentsWithNoImprovedChildren.add(baseStrategy);
			}
		}
		
		List<AdaptiveStrategy> mostImprovedChildren = mostImprovedChildrenToParents.keySet().stream().collect(Collectors.toList());
		//This should be the 50 most improved children of the original top 500 strategies.
		List<AdaptiveStrategy> bestMostImprovedChildren = takeTopPercent(mostImprovedChildren, 0.1);
		bestMostImprovedChildren.forEach(child -> {
			if (OUTPUT_LEVEL >= 0) {
				AdaptiveStrategy parent = mostImprovedChildrenToParents.get(child);
				System.out.println("<<<------------------->>>");
				System.out.println("Parent: " + parent);
				System.out.println("Child: " + child);
				System.out.println("Diff: " + AdaptiveStrategy.getDifferences(child, parent));
				System.out.println("<<<------------------->>>");
			}
		});
	}
	
	//fractionToKeep is a percentage double, like 0.6
	//We don't care about the rounding here (+/- one makes no difference)
	public static List<AdaptiveStrategy> takeTopPercent (List<AdaptiveStrategy> strategies, double fractionToKeep) {
		if (fractionToKeep >= 1.0 || fractionToKeep < 0.0) {
			throw new AssertionError("Bad input: not usable as a fraction.");
		}
		Collections.sort(strategies);
		List<AdaptiveStrategy> bestStrategies = new ArrayList<>();
		
		int index = 0;
		while (bestStrategies.size() < (strategies.size() * fractionToKeep)) {
			bestStrategies.add(strategies.get(index));
			index++;
		}
		
		return bestStrategies;
	}
	
	public static void calcAverageAttainment () {
		strategyToLevelsAttainedMap.keySet().forEach(strat -> {
			List<Integer> levels = strategyToLevelsAttainedMap.get(strat);
			double average = levels.stream().mapToInt(e -> e).average().getAsDouble();
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
	
	@Deprecated
	public static void bigMappingTest () {
		for (int i = 0; i < 3000; i++) {
			new ClimbingGame().playGame();
		}
		System.out.println("====================");
		System.out.println("Final Report:");
		
		
		calcAverageAttainment();
		
		List<AdaptiveStrategy> strategies = strategyToLevelsAttainedMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(strategies);
		
		System.out.println("******");
		System.out.println("Strategies Ranked:");
		strategies.forEach(System.out::println);
		
		List<AdaptiveStrategy> bestStrategies = takeTopPercent(strategies, 0.1);
		
		System.out.println("***====***===****====");
		System.out.println("Best strats:");
		bestStrategies.forEach(System.out::println);
		
		strategyToLevelsAttainedMap = new HashMap<>();
		
		for (AdaptiveStrategy strategy : bestStrategies) {
			for (int i = 0; i < 100; i++) {
				new ClimbingGame(strategy).playGame();
			}
		}
		
		calcAverageAttainment();
		
		List<StrategyBase> bestOfBest = strategyToLevelsAttainedMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(bestOfBest);
		
		System.out.println("<><><><><><><><>");
		System.out.println("Best of the best of the best, sir!");
		System.out.println("...With honors!");
		bestOfBest.forEach(System.out::println);
	}
	
	public void playGame () {
		boolean keepGoing = true;
		while (keepGoing) {
			if (OUTPUT_LEVEL >= 2) {
				System.out.println("Level " + level + "\tHealth " + hero.getCurrentHealth() + "/" + hero.getMaxHealth());
			}
			monster = new Monster(level);
			drawPile = new ArrayList<>(hero.getDeck());
			discardPile = new ArrayList<>();
			Collections.shuffle(drawPile);
			int roundCount = 0;
			//Combat
			while (true) {
				hero.setBlockHp(0);
				//Do two hero attacks
				doCardEffects(drawCard());
				doCardEffects(drawCard());
				if (level >= 10) {
					doCardEffects(drawCard());
				}
				if (level >= 25) {
					doCardEffects(drawCard());
				}
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\t-----");
				}
				if (monster.getHealth() <= 0) {
					if (OUTPUT_LEVEL >= 2) {
						System.out.println("\tMonster defeated.");
					}
					break;
				}
				
				//Do monster attack
				hero.takeDamage(monster.getDamage());
				if (monster.hasVulnerableAttacks()) {
					hero.increaseVulnerability(Monster.VULN_FACTOR);
				}
				if (monster.hasWeakeningAttacks()) {
					hero.increaseWeakness(Monster.WEAK_FACTOR);
				}
				monster.endRound();
				if (hero.getCurrentHealth() <= 0) {
					if (OUTPUT_LEVEL >= 1) {
						System.out.println("Hero died on level " + level + ".");
					}
					keepGoing = false;
					break; //Out of the individual round loop
				}
				if (roundCount >= 30) {
					if (OUTPUT_LEVEL >= 1) {
						System.out.println("Timed out on combat after 30+ rounds. Hero loses.");
					}
					keepGoing = false;
					break;
				}
				roundCount++;
			}
			if (keepGoing) {
				hero.endCombat();
				//Restore HP
				if (level % 4 == 0) {
					hero.heal(4 + (level / 6));
				}
				
				//Add a new card
				level++;
				
				makeUpgradeDecision();
			} else {
				List<Integer> levelsAttained;
				if (strategyToLevelsAttainedMap.containsKey(hero.getStrategy())) {
					levelsAttained = strategyToLevelsAttainedMap.get(hero.getStrategy());
				} else {
					levelsAttained = new ArrayList<>();
				}
				levelsAttained.add(level);
				strategyToLevelsAttainedMap.put(hero.getStrategy(), levelsAttained);
			}
		}//End while(keepGoing)
	}
	
	public void makeUpgradeDecision () {
		List<String> highLevelPrefs = hero.getStrategy().getHighLevelPrefs();
		List<String> advUpgradeOptionNames = advUpgradeOptions.keySet().stream().collect(Collectors.toList());
		Collections.shuffle(advUpgradeOptionNames);
		//Take out two options, leaving 2
		advUpgradeOptionNames.remove(0);
		advUpgradeOptionNames.remove(0);
		String choice = null;
		//Find the first available choice
		for (String s : highLevelPrefs) {
			if (advUpgradeOptionNames.contains(s)) {
				choice = s;
				break;
			}
		}
		if (OUTPUT_LEVEL >= 2) {
			System.out.println("\tOptions were: " + advUpgradeOptionNames);
			System.out.println("\tChoice was " + choice);
		}
		advUpgradeOptions.get(choice).doAction(hero);
	}
	
	public void doCardEffects (Card card) {
		if (OUTPUT_LEVEL >= 2) {
			System.out.println("\tDrew and played: " + card);
		}
		//Execute card properties. These are NOT mutually exclusive
		if (card.isPower()) {
			hero.addPower(card);
		} else {
			if (card.isAttack()) {
				int damageAmount = (int) ((card.getMagnitude() + hero.getStrength()) * hero.getWeakDamageFactor());
				monster.takeDamage(damageAmount);
			}
			if (card.isBlock()) {
				hero.addBlockHp(card.getMagnitude() + hero.getDexterity());
			}
			if (card.isHeal()) {
				hero.heal(card.getMagnitude());
			}
			if (!card.isExhaust()) { //If NOT exhaust
				discardPile.add(card);
			}
		}
	}
	
	public Card drawCard () {
		if (drawPile.size() == 0) {
			drawPile = new ArrayList<>(discardPile);
			discardPile = new ArrayList<>();
		}
		return drawPile.remove(0);
	}
}
