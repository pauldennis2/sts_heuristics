/**
 * @author Paul Dennis (pd236m)
 * May 9, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AdaptiveStrategy extends StrategyBase {

	//A Map between Conditions (which make dynamic decisions based on game state)
	//and List<String> representing how we will change our preferences if the condition is true
	Map<Condition, List<String>> conditionsAndResultsMap;	
	
	transient Hero hero;
	transient AdaptiveStrategy old;
	
	double improvement;
	boolean isDiff = false;
	String name;
	
	String dadsName;
	String momsName;
	
	//List of 100 3-6 letter words that can be combined to make strategy names.
	//Like "peruse-dragon98". 1,000,000 possibilities
	final static List<String> NAME_FRAGMENTS = Arrays.asList("fierce", "strong", "silly", "react", "strike", "deny",
			"speak", "adapt", "seek", "frothy", "shake", "stir", "heal", "stompy", "newly", "prime", "singe", "sear",
			"strife", "happy", "kooky", "strange", "finish", "demand", "oblong", "peruse", "rogue",
			"copy", "joyful", "white", "green", "guard", "dragon", "delete", "verify", "curse", "grace",
			"arch", "plated", "smash", "crush", "death", "honor", "scar", "ranger", "sharp", "evade",
			"river", "brook", "lava", "shield", "sword", "cube", "galaxy", "moon", "star", "burst", "snow",
			"sphere", "spoon", "fork", "dirk", "salad", "magic", "cloak", "face", "pasta", "tiger", "kitten",
			"ninja", "pirate", "demon", "card", "hands", "goop", "war", "peace", "mage", "knight", "druid",
			"egg", "kernel", "panic", "feast", "turtle", "tale", "unwind", "relax", "spear", "relic", "shadow", "genie",
			"titan", "naga", "imp", "angel", "toad", "sage", "short", "flash");
	
	final static List<String> HLPS = Arrays.asList("addCard", "upgradeCard", "removeCard", "maxHp");
	final static List<String> CARDS = Arrays.asList("heal", "strikeDefend", "strikeExhaust", "healBlock");
	
	//For the difference function
	private AdaptiveStrategy () {
		isDiff = true;
		name = "diff";
	}
	
	//Create a new random strategy
	public AdaptiveStrategy (Hero hero) {
		this.hero = hero;
		Collections.shuffle(highLevelPrefs);
		Collections.shuffle(cardPrefs);
		name = getName();
		
		Random r = new Random();
		conditionsAndResultsMap = new HashMap<>();

		//Create 0-2 high level pref conditions and 0-2 card pref conditions
		int conditionsCount = 0;
		while (r.nextBoolean() || r.nextBoolean()) {
			//Alter High level prefs
			List<String> alteredHLPrefs = new ArrayList<>(highLevelPrefs);
			Collections.shuffle(alteredHLPrefs);
			//If the condition doesn't actually make any changes don't add it
			if (alteredHLPrefs.equals(highLevelPrefs)) {
				continue;
			}
			
			conditionsAndResultsMap.put(new Condition(r, true), alteredHLPrefs);
			
			conditionsCount++;
			if (conditionsCount >= 2) {
				break;
			}
		}
		conditionsCount = 2;//A little weird but we don't want 4 card conditions
		while (r.nextBoolean()) {
			//Alter card prefs
			List<String> alteredCardPrefs = new ArrayList<>(cardPrefs);
			Collections.shuffle(alteredCardPrefs);
			if (alteredCardPrefs.equals(cardPrefs)) {
				continue;
			}
			
			conditionsAndResultsMap.put(new Condition(r, false), alteredCardPrefs);
			
			conditionsCount++;
			if (conditionsCount >= 4) {
				break;
			}
		}
		validate();
	}
	
	public AdaptiveStrategy (AdaptiveStrategy old) {
		this.old = old;
		this.conditionsAndResultsMap = new HashMap<>(old.conditionsAndResultsMap);
		this.highLevelPrefs = new ArrayList<>(old.highLevelPrefs);
		this.cardPrefs = new ArrayList<>(old.cardPrefs);
		name = getChildName(old.name);
	
		Random r = new Random();
		
		//Half of the time we'll change a condition
		//Other half we'll change basic preferences
		int numConditions = conditionsAndResultsMap.size();
		if (r.nextBoolean() && numConditions > 0) {//Change conditions
			//Half of the time we will tweak an existing condition
			//Other half we will add or remove a condition
			if (r.nextBoolean()) { //Tweak a condition
				List<Condition> conditions = conditionsAndResultsMap.keySet().stream().collect(Collectors.toList());
				Condition toTweak = conditions.get(r.nextInt(conditions.size()));
				List<String> preferences = conditionsAndResultsMap.get(toTweak);
				if (r.nextBoolean()) {//By keeping preferences the same but re-rolling the condition itself
					Condition tweaked = new Condition(r, toTweak);
					conditionsAndResultsMap.remove(toTweak);
					conditionsAndResultsMap.put(tweaked, preferences);
				} else {//By changing preferences but leaving the condition itself unchanged
					Collections.shuffle(preferences);
					conditionsAndResultsMap.put(toTweak, preferences);
				}
			} else { //Add/remove a condition
				if (r.nextBoolean() || numConditions < 2) { //Add a condition
					boolean altersHighLevelPrefs = r.nextBoolean();
					Condition condition = new Condition (r, altersHighLevelPrefs);
					List<String> preferences;
					if (altersHighLevelPrefs) {
						preferences = new ArrayList<>(highLevelPrefs);
					} else {
						preferences = new ArrayList<>(cardPrefs);
					}
					Collections.shuffle(preferences);
					conditionsAndResultsMap.put(condition, preferences);
				} else if (numConditions >= 2) { //Remove a condition
					Condition cond = conditionsAndResultsMap.keySet().stream().collect(Collectors.toList()).get(0);
					conditionsAndResultsMap.remove(cond);
				}
			}
		} else {//Change base preferences
			if (r.nextBoolean()) {//By changing high level prefs
				Collections.shuffle(highLevelPrefs);
			} else { //By changing card prefs
				Collections.shuffle(cardPrefs);
			}
		}
		validate();
	}
	
	//Sometimes when a mommy strategy and a daddy strategy love each other VERY much...
	public AdaptiveStrategy (AdaptiveStrategy dad, AdaptiveStrategy mom) {
		this.dadsName = dad.name;
		this.momsName = mom.name;
		this.name = get2ParentChildName(dadsName, momsName);
		Random r = new Random();
		conditionsAndResultsMap = new HashMap<>();
		
		if (r.nextBoolean()) {
			this.highLevelPrefs = new ArrayList<>(dad.highLevelPrefs);
		} else {
			this.highLevelPrefs = new ArrayList<>(mom.highLevelPrefs);
		}
		if (r.nextBoolean()) {
			this.cardPrefs = new ArrayList<>(dad.cardPrefs);
		} else {
			this.cardPrefs = new ArrayList<>(mom.cardPrefs);
		}
		
		//Keep 75% (on average) of each parent's conditions, then discard 25% of total
		//0.75 + 0.75 = 1.5,    1.5 * 0.75 = 1.125
		//This means a 12% average increase in conditions, which is about right
		Map<Condition, List<String>> newConditions = new HashMap<>();
		for (Condition condition : dad.conditionsAndResultsMap.keySet()) {
			if (r.nextDouble() > 0.25) {
				newConditions.put(condition, dad.conditionsAndResultsMap.get(condition));
			}
		}
		for (Condition condition : mom.conditionsAndResultsMap.keySet()) {
			if (r.nextDouble() > 0.25) {
				newConditions.put(condition, mom.conditionsAndResultsMap.get(condition));
			}
		}
		for (Condition condition : newConditions.keySet()) {
			if (r.nextDouble() > 0.25) {
				this.conditionsAndResultsMap.put(condition, newConditions.get(condition));
			}
		}
		
		validate();
	}
	
	public AdaptiveStrategy (String fileString) {
		String[] tokens = fileString.split(":");
		name = tokens[0];
		averageLevelAttained = Double.parseDouble(tokens[1]);
		//Remove any whitespace or brackets []
		String hlpString = tokens[2].replaceAll("[\\[\\] ]", "");
		String cardPrefString = tokens[3].replaceAll("[\\[\\] ]", "");
		highLevelPrefs = Arrays.asList(hlpString.split(","));
		cardPrefs = Arrays.asList(cardPrefString.split(","));
		conditionsAndResultsMap = new HashMap<>();
		
		if (tokens.length >= 5) {
			String conditionsMap = tokens[4];
			String[] conditionsStrings = conditionsMap.split(";");
			
			for (String conditionString : conditionsStrings) {
				String[] mapTokens = conditionString.split("-");
				Condition condition = new Condition(mapTokens[0].trim());
				List<String> results = new ArrayList<>();
				String[] resultsTokens = mapTokens[1].split(",");
				for (String resultsToken : resultsTokens) {
					resultsToken = resultsToken.replaceAll("[\\[\\] ]", "");
					results.add(resultsToken);
				}
				conditionsAndResultsMap.put(condition, results);
			}
		}
		validate();
	}
	
	public AdaptiveStrategy getCopy () {
		AdaptiveStrategy copy = new AdaptiveStrategy();
		copy.cardPrefs = this.cardPrefs;
		copy.highLevelPrefs = this.highLevelPrefs;
		copy.isDiff = false;
		copy.conditionsAndResultsMap = this.conditionsAndResultsMap;
		copy.name = this.name;
		copy.validate();
		return copy;
	}
	
	//Defines the file format
	public String getFileString () {
		StringBuilder response = new StringBuilder();
		response.append(name + ":" + averageLevelAttained + ":");
		response.append(highLevelPrefs.toString() + ":" + cardPrefs + ":");
		boolean first = true;
		for (Condition condition : conditionsAndResultsMap.keySet()) {
			if (first) {
				first = false;
			} else {
				response.append("; ");
			}
			response.append(condition.toString() + conditionsAndResultsMap.get(condition).toString());
		}
		return response.toString();
	}
	
	private void validate () {
		
		if ((!highLevelPrefs.containsAll(HLPS)) || !cardPrefs.containsAll(CARDS)) {
			throw new AssertionError("Missing card or prefs (bad list)");
		}
		if (highLevelPrefs.size() != HLPS.size() || cardPrefs.size() != CARDS.size()) {
			throw new AssertionError("Length mismatch.");
		}
		if (name == null || name.length() == 0) {
			throw new AssertionError("Missing name.");
		}
		for (Condition condition : conditionsAndResultsMap.keySet()) {
			if (condition.altersHighLevelPrefs) {
				if (!conditionsAndResultsMap.get(condition).containsAll(HLPS)) {
					throw new AssertionError("Missing high level pref in conditionsAndResults");
				}
			} else {
				if (!conditionsAndResultsMap.get(condition).containsAll(CARDS)) {
					throw new AssertionError("Missing card pref in conditionsAndResults");
				}
			}
		}
	}

 	public static AdaptiveStrategy buildJoyfulDragon () {
		AdaptiveStrategy joyfulDragon = new AdaptiveStrategy();
		joyfulDragon.isDiff = false;
		joyfulDragon.name = "joyful-dragon90";
		joyfulDragon.cardPrefs = Arrays.asList("healBlock", "strikeExhaust", "strikeDefend", "heal");
		joyfulDragon.highLevelPrefs = Arrays.asList("addCard", "maxHp", "removeCard", "upgradeCard");
		joyfulDragon.conditionsAndResultsMap = new HashMap<>();
		
		Condition upgradeNonStarters = new Condition("unupgradedNonStarterCards > 0 (HLP)");
		upgradeNonStarters.priorityLevel = 2;
		List<String> unsPrefs = Arrays.asList("upgradeCard", "addCard", "maxHp", "removeCard");
		joyfulDragon.conditionsAndResultsMap.put(upgradeNonStarters, unsPrefs);
		
		Condition maxHp = new Condition("maxHp < level * 6 (HLP)");
		maxHp.priorityLevel = 1;
		List<String> mhpPrefs = Arrays.asList("maxHp", "addCard", "removeCard", "upgradeCard");
		joyfulDragon.conditionsAndResultsMap.put(maxHp, mhpPrefs);
		
		Condition valueDamage = new Condition("averageDamagePerCard < level * 0.4 (Cards)");
		valueDamage.priorityLevel = 0;
		List<String> vdPrefs = Arrays.asList("strikeExhaust", "strikeDefend", "healBlock", "heal");
		joyfulDragon.conditionsAndResultsMap.put(valueDamage, vdPrefs);
	
		return joyfulDragon;
	}
	
	public static void testWriteToFile () {
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			strategies.add(new AdaptiveStrategy(new Hero()));
		}
		writeStrategiesToFile(strategies, "data/strategies.txt");
		
		List<AdaptiveStrategy> rebuilt = readStrategiesFromFile("data/strategies.txt");
		
		System.out.println("Equal = " + rebuilt.equals(strategies));
	}
	
	public static List<AdaptiveStrategy> readStrategiesFromFile (String fileName) {
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		try (Scanner fileScanner = new Scanner(new File(fileName))) {
			while (fileScanner.hasNextLine()) {
				strategies.add(new AdaptiveStrategy(fileScanner.nextLine()));
			}
		} catch (FileNotFoundException ex) {
			System.err.println("!!Could not read from file with name: " + fileName);
		}
		return strategies;
	}
	
	public static void writeStrategiesToFile (List<AdaptiveStrategy> strategies, String fileName) {
		try (FileWriter writer = new FileWriter(new File(fileName))) {
			for (AdaptiveStrategy strategy : strategies) {
				writer.write(strategy.getFileString());
				writer.write("\n");
			}
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AssertionError("Couldn't write to file: " + fileName);
		}
	}
	
	public static String getParentName (String childName) {
		String[] words = childName.split("_");
		if (words.length < 2) {
			return "NO PARENT";
		}
		String response = "";
		for (int i = 0; i < words.length - 1; i++) {
			response += words[i];
		}
		return response;
	}
	
	private static String getName () {
		String name = "";
		Random r = new Random();
		int index = r.nextInt(NAME_FRAGMENTS.size());
		int otherIndex = r.nextInt(NAME_FRAGMENTS.size());
		if (otherIndex == index) { //If it lands on the same index three times oh well
			otherIndex = r.nextInt(NAME_FRAGMENTS.size());
		}
		name += NAME_FRAGMENTS.get(index);
		name += "-";
		name += NAME_FRAGMENTS.get(otherIndex);
		name += r.nextInt(10);
		name += r.nextInt(10);
		
		if (name.startsWith("joyful-dragon")) { //<-Reserved for The Creator
			return getName();
		}
		
		return name;
	}
	
	private static String get2ParentChildName (String dadsName, String momsName) {
		String dadsDigits = dadsName.replaceAll("[^\\d]", "");
		String momsDigits = momsName.replaceAll("[^\\d]", "");
		
		String dadsFirst = dadsName.split("-")[0];
		String momsFirst = momsName.split("-")[0];
		
		String response = dadsFirst + dadsDigits + "-" + momsFirst + momsDigits + "_";
		Random r = new Random();
		response += NAME_FRAGMENTS.get(r.nextInt(NAME_FRAGMENTS.size()));
		response += r.nextInt(10);
		return response;
	}
	
	private static String getChildName (String parentName) {
		Random r = new Random();
		String childName = parentName;
		childName += "_" + NAME_FRAGMENTS.get(r.nextInt(NAME_FRAGMENTS.size()));
		childName += r.nextInt(10);
		return childName;
	}
	
	public static void main(String[] args) {
		test2ParentNameGeneration();
	}
	
	public static void test2ParentNameGeneration () {
		String dadsName = "pasta-snow50_guard9_frothy4_shield3_pirate5_relic0";
		String momsName = "unwind-mage54_pirate8_frothy2_turtle0_grace9_evade6_panic5";
		String childsName = get2ParentChildName(dadsName, momsName);
		System.out.println(childsName);
		if (!childsName.startsWith("pasta5094350-unwind54820965_")) {
			throw new AssertionError("Name generation error.");
		}
	}
	
	public static void testNameGeneration() {
		for (int i = 0; i < 20; i++) {
			String name = getName();
			System.out.print(name);
			String childName = getChildName(name);
			System.out.println("\t\t" + childName);
		}
		System.out.println("Possible names = " + NAME_FRAGMENTS.size() * NAME_FRAGMENTS.size() * 100);
		String child = "spoon-death72_stir0";
		System.out.println(child + " is a child of " + getParentName(child));
		System.out.println(getChildName(child) + " is a child of " + child);
		
		System.out.println("get parent name of peruse-dragon98: " + getParentName("peruse-dragon98"));
		System.out.println("\n\n");
		
		List<AdaptiveStrategy> strategies = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			strategies.add(new AdaptiveStrategy(new Hero()));
		}
		StrategyGroupReport report = analyzeStrategies(strategies);
		System.out.println(report);
	}

	@Override 
	public List<String> getHighLevelPrefs () {
		DeckReport report = new DeckReport(hero);
		List<Condition> liveConditions = conditionsAndResultsMap.keySet().stream()
			.filter(condition -> condition.altersHighLevelPrefs)
			.filter(condition -> condition.evaluate(report))
			.collect(Collectors.toList());
		//Sort in descending order of priority
		if (liveConditions.size() >= 1) {
			Collections.sort(liveConditions, (cond1, cond2) -> {
				return cond2.priorityLevel - cond1.priorityLevel;
			});
			if (ClimbingGame.OUTPUT_LEVEL >= 3) {
				System.out.println("\tAll live conditions:");
				liveConditions.forEach(condition -> System.out.println("\t\t" + condition));
			}
			Condition highestPrio = liveConditions.get(0);
			return conditionsAndResultsMap.get(highestPrio);
		}
		return highLevelPrefs;
	}
	
	@Override 
	public List<String> getCardPrefs () {
		DeckReport report = new DeckReport(hero);
		List<Condition> liveConditions = conditionsAndResultsMap.keySet().stream()
			.filter(condition -> !condition.altersHighLevelPrefs)
			.filter(condition -> condition.evaluate(report))
			.collect(Collectors.toList());
		//Sort in descending order of priority
		if (liveConditions.size() >= 1) {
			Collections.sort(liveConditions, (cond1, cond2) -> {
				return cond2.priorityLevel - cond1.priorityLevel;
			});
			if (ClimbingGame.OUTPUT_LEVEL >= 3) {
				System.out.println("\tAll live conditions:");
				liveConditions.forEach(condition -> System.out.println("\t\t" + condition));
			}
			Condition highestPrio = liveConditions.get(0);
			return conditionsAndResultsMap.get(highestPrio);
		}
		return cardPrefs;
	}
	
	@Override
	public String toString () {
		if (!isDiff) {
			String response = "";
			response += "High Level Prefs: " + highLevelPrefs;
			response += "\nCard Prefs: " + cardPrefs;
			response += "\nConditions:";
			for (Condition condition : conditionsAndResultsMap.keySet()) {
				response += "\n\t" + condition + " - Result: " + conditionsAndResultsMap.get(condition);
			}
			if (averageLevelAttained != 0.0) {
				response += "\nAverage Attainment: " + averageLevelAttained;
			}
			return response;
		} else {
			return diffToString();
		}
	}
	
	private String diffToString () {
		if (!isDiff) {
			throw new AssertionError("Bad method call");
		}
		String response = "Difference:";
		if (highLevelPrefs != null) {
			response += "\nHigh Level Prefs: " + highLevelPrefs;
		}
		if (cardPrefs != null) {
			response += "\nCard Prefs: " + cardPrefs;
		}
		if (conditionsAndResultsMap != null) {
			for (Condition condition : conditionsAndResultsMap.keySet()) {
				response += "\n\t" + condition + " - Result: " + conditionsAndResultsMap.get(condition);
			}
		}
		return response;
	}
	
	public AdaptiveStrategy getDifferences () {
		return getDifferences(this, old);
	}
	
	//Does not compare the average level attained since this isn't part
	//of our definition of equality
	@Override
	public boolean equals (Object other) {
		if (other instanceof AdaptiveStrategy) {
			AdaptiveStrategy otherStrat = (AdaptiveStrategy) other;
			boolean mapSame = this.conditionsAndResultsMap.equals(otherStrat.conditionsAndResultsMap);
			boolean cpsSame = this.cardPrefs.equals(otherStrat.cardPrefs);
			boolean hlpSame = this.highLevelPrefs.equals(otherStrat.highLevelPrefs);
			return mapSame && cpsSame && hlpSame;
		}
		return false;
	}
	
	/**
	 * Returns a partially empty AdaptiveStrategy representing any fields of strat1
	 * that are different from those of strat2. Only strategy differences are considered
	 * (not results like averageLevelAttained).
	 * @param strat1
	 * @param strat2
	 * @return
	 * 
	 * !Alert!:
	 * Order matters here a LOT. getDifferences(myStrat, otherStrat) and getDifferences(otherStrat, myStrat)
	 * will have different outputs.
	 */
	public static AdaptiveStrategy getDifferences (AdaptiveStrategy strat1, AdaptiveStrategy strat2) {
		AdaptiveStrategy diff = new AdaptiveStrategy();
		if (!strat1.cardPrefs.equals(strat2.cardPrefs)) {
			diff.cardPrefs = new ArrayList<>(strat1.cardPrefs);
		}
		if (!strat1.highLevelPrefs.equals(strat2.highLevelPrefs)) {
			diff.highLevelPrefs = new ArrayList<>(strat1.highLevelPrefs);
		}
		if (!strat1.conditionsAndResultsMap.equals(strat2.conditionsAndResultsMap)) {
			Map<Condition, List<String>> diffMap = new HashMap<>();
			for (Condition condition : strat1.conditionsAndResultsMap.keySet()) {
				//If strat2 doesn't have it or if it is different (same check works for both)
				if (!strat1.conditionsAndResultsMap.get(condition).equals(strat2.conditionsAndResultsMap.get(condition))) {
					diffMap.put(condition, strat1.conditionsAndResultsMap.get(condition));
				}
			}
			diff.conditionsAndResultsMap = diffMap;
		}
		return diff;
	}
	
	//All Strategies must be either diffs or NOT diffs (not a mix)
	public static StrategyGroupReport analyzeStrategies (List<AdaptiveStrategy> strategies) {
		Map<StringIndex, Integer> cardPrefOccurances = new HashMap<>();
		Map<StringIndex, Integer> hlpOccurances = new HashMap<>();
		boolean diffs = strategies.get(0).isDiff;
		for (AdaptiveStrategy strategy : strategies) {
			if (strategy.isDiff != diffs) {
				throw new AssertionError("Cannot accept mix of diffs and non-diffs.");
			}
			int index = 0;
			if (strategy.cardPrefs != null) {
				for (String card : strategy.cardPrefs) {
					StringIndex si = new StringIndex(card, index);
					if (cardPrefOccurances.containsKey(si)) {
						cardPrefOccurances.put(si, cardPrefOccurances.get(si) + 1);
					} else {
						cardPrefOccurances.put(si, 1);
					}
					index++;
				}
			}
			index = 0;
			if (strategy.highLevelPrefs != null) {
				for (String hlp : strategy.highLevelPrefs) {
					StringIndex si = new StringIndex(hlp, index);
					if (hlpOccurances.containsKey(si)) {
						hlpOccurances.put(si, hlpOccurances.get(si) + 1);
					} else {
						hlpOccurances.put(si, 1);
					}
					index++;
				}
			}
			if (strategy.conditionsAndResultsMap != null) {
				//TODO : some analysis of the conditions/results
			}
		}
		return new StrategyGroupReport(cardPrefOccurances, hlpOccurances);
	}
	
	

	/**
	 * @return the conditionsAndResultsMap
	 */
	public Map<Condition, List<String>> getConditionsAndResultsMap() {
		return conditionsAndResultsMap;
	}

	/**
	 * @param conditionsAndResultsMap the conditionsAndResultsMap to set
	 */
	public void setConditionsAndResultsMap(Map<Condition, List<String>> conditionsAndResultsMap) {
		this.conditionsAndResultsMap = conditionsAndResultsMap;
	}

	/**
	 * @return the improvement
	 */
	public double getImprovement() {
		return improvement;
	}

	/**
	 * @param improvement the improvement to set
	 */
	public void setImprovement(double improvement) {
		this.improvement = improvement;
	}

	/**
	 * @return the isDiff
	 */
	public boolean isDiff() {
		return isDiff;
	}

	/**
	 * @param isDiff the isDiff to set
	 */
	public void setDiff(boolean isDiff) {
		this.isDiff = isDiff;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
}
