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

public class AdaptiveStrategy extends StrategyBase implements Tweakable {

	//A Map between Conditions (which make dynamic decisions based on game state)
	//and List<String> representing how we will change our preferences if the condition is true
	//private Map<Conditional, List<String>> conditionsAndResultsMap;	
	
	private transient Hero hero;
	private transient AdaptiveStrategy old;
	
	private double improvement;
	private boolean isDiff = false;
	private String name;
	
	private String dadsName;
	private String momsName;
	
	//List of 100 3-6 letter words that can be combined to make strategy names.
	//Like "peruse-dragon98". 1,000,000 possibilities
	public final static List<String> NAME_FRAGMENTS = Arrays.asList("fierce", "strong", "silly", "react", "strike", "deny",
			"speak", "adapt", "seek", "frothy", "shake", "stir", "heal", "stompy", "newly", "prime", "singe", "sear",
			"strife", "happy", "kooky", "strange", "finish", "demand", "oblong", "peruse", "rogue",
			"copy", "joyful", "white", "green", "guard", "dragon", "delete", "verify", "curse", "grace",
			"arch", "plated", "smash", "crush", "death", "honor", "scar", "ranger", "sharp", "evade",
			"river", "brook", "lava", "shield", "sword", "cube", "galaxy", "moon", "star", "burst", "snow",
			"sphere", "spoon", "fork", "dirk", "salad", "magic", "cloak", "face", "pasta", "tiger", "kitten",
			"ninja", "pirate", "demon", "card", "hands", "goop", "war", "peace", "mage", "knight", "druid",
			"egg", "kernel", "panic", "feast", "turtle", "tale", "unwind", "relax", "spear", "relic", "shadow", "genie",
			"titan", "naga", "imp", "angel", "toad", "sage", "short", "flash");
	
	public final static List<String> HLPS = Arrays.asList("addCard", "upgradeCard", "removeCard", "maxHp");
	//public final static List<String> CARDS = Arrays.asList("heal", "strikeDefend", "strikeExhaust", "healBlock");
	
	Map<String, Double> cardValues;
	Map<String, Double> hlpValues;
	Map<Conditional, Map<String, Double>> conditionsAndValuesMap;
	
	//For the difference function
	private AdaptiveStrategy () {
		isDiff = true;
		name = "diff";
	}
	
	//Create a new random strategy
	public AdaptiveStrategy (Hero hero) {
		this.hero = hero;
		name = getNewName();
		Random r = new Random();
		
		cardValues = new HashMap<>();
		hlpValues = new HashMap<>();
		cardPrefs.forEach(cardName -> cardValues.put(cardName, r.nextDouble()));
		highLevelPrefs.forEach(hlpref -> hlpValues.put(hlpref, r.nextDouble()));
		
		conditionsAndValuesMap = new HashMap<>();

		//Create 0-2 high level pref conditions and 0-2 card pref conditions
		int conditionsCount = 0;
		while (r.nextBoolean() || r.nextBoolean()) {
			Map<String, Double> values = getRandomValues(highLevelPrefs);
			
			if (r.nextBoolean() || r.nextBoolean()) {
				conditionsAndValuesMap.put(new SingleCondition(r, true), values);
			} else {
				conditionsAndValuesMap.put(new CompositeCondition(true), values);
			}
			
			conditionsCount++;
			if (conditionsCount >= 2) {
				break;
			}
		}
		conditionsCount = 2;//A little weird but we don't want 4 card conditions
		while (r.nextBoolean()) {
			//Alter card prefs
			Map<String, Double> values = getRandomValues(cardPrefs);
			
			if (r.nextBoolean() || r.nextBoolean()) {
				conditionsAndValuesMap.put(new SingleCondition(r, false), values);
			} else {
				conditionsAndValuesMap.put(new CompositeCondition(false), values);
			}
			
			conditionsCount++;
			if (conditionsCount >= 4) {
				break;
			}
		}
		validate();
	}
	
	/*Helper method. Given a list, will return a map with at least one
	 * of those values mapped to a random Double
	 */
	private static Map<String, Double> getRandomValues (List<String> inputList) {
		Random r = new Random();
		Map<String, Double> values = new HashMap<>();
		do {
			String prefName = inputList.get(r.nextInt(inputList.size() - 1));
			values.put(prefName, r.nextDouble());
		} while (r.nextBoolean());
		return values;
	}
	
	@Override
	public AdaptiveStrategy tweak () {
		AdaptiveStrategy newStrat = new AdaptiveStrategy();
		newStrat.isDiff = false;
		newStrat.conditionsAndValuesMap = new HashMap<>(this.conditionsAndValuesMap);
		newStrat.highLevelPrefs = new ArrayList<>(this.highLevelPrefs);
		newStrat.cardPrefs = new ArrayList<>(cardPrefs);
		newStrat.name = getChildName(this.name);
	
		Random r = new Random();
		
		//Half of the time we'll change a condition
		//Other half we'll change basic preferences
		int numConditions = conditionsAndValuesMap.size();
		if (r.nextBoolean() && numConditions > 0) {//Change conditions
			//Half of the time we will tweak an existing condition
			//Other half we will add or remove a condition
			if (r.nextBoolean()) { //Tweak a condition
				List<Conditional> conditions = conditionsAndValuesMap.keySet().stream().collect(Collectors.toList());
				Conditional toTweak = conditions.get(r.nextInt(conditions.size()));
				Map<String, Double> valuesToTweak = conditionsAndValuesMap.get(toTweak);
				if (r.nextBoolean()) {//By keeping preferences the same but re-rolling the condition itself
					Conditional tweaked = toTweak.tweak();
					newStrat.conditionsAndValuesMap.remove(toTweak);
					newStrat.conditionsAndValuesMap.put(tweaked, valuesToTweak);
				} else {//By changing preferences but leaving the condition itself unchanged
					//Building it into an OptionValue list sidesteps the ConcurrencyModification issue
					//we'd see otherwise.
					List<OptionValue> optionValues = new ArrayList<>();
					valuesToTweak.keySet().stream()
						.filter(prefName -> r.nextBoolean()) //Randomly toss out ~half
						.forEach(prefName -> optionValues.add(new OptionValue(prefName, valuesToTweak.get(prefName))));
					for (OptionValue optionValue : optionValues) {
						valuesToTweak.put(optionValue.getOption(), r.nextDouble());
					}
					newStrat.conditionsAndValuesMap.put(toTweak, valuesToTweak);
				}
			} else { //Add/remove a condition
				if (r.nextBoolean() || numConditions < 2) { //Add a condition
					boolean altersHighLevelPrefs = r.nextBoolean();
					Conditional condition = new SingleCondition (r, altersHighLevelPrefs);
					Map<String, Double> values;
					if (altersHighLevelPrefs) {
						values = getRandomValues(highLevelPrefs);
					} else {
						values = getRandomValues(cardPrefs);
					}
					newStrat.conditionsAndValuesMap.put(condition, values);
				} else if (numConditions >= 2) { //Remove a condition
					Conditional cond = conditionsAndValuesMap.keySet().stream().collect(Collectors.toList()).get(0);
					newStrat.conditionsAndValuesMap.remove(cond);
				}
			}
		} else {//Change base preferences
			if (r.nextBoolean()) {//By changing high level prefs
				Collections.shuffle(newStrat.highLevelPrefs);
			} else { //By changing card prefs
				Collections.shuffle(newStrat.cardPrefs);
			}
		}
		newStrat.validate();
		return newStrat;
	}
	
	//Sometimes when a mommy strategy and a daddy strategy love each other VERY much...
	public AdaptiveStrategy (AdaptiveStrategy dad, AdaptiveStrategy mom) {
		this.dadsName = dad.name;
		this.momsName = mom.name;
		this.name = get2ParentChildName(dadsName, momsName);
		Random r = new Random();
		conditionsAndValuesMap = new HashMap<>();
		
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
		
		/*Keep 75% (on average) of each parent's conditions, then discard 25% of total
		 * 0.75 + 0.75 = 1.5,    1.5 * 0.75 = 1.125
		 * This means a 12% average increase in conditions, which is about right
		 * (We want child strategies to generally move toward being more complex)
		 */
		Map<Conditional, Map<String, Double>> newConditions = new HashMap<>();
		for (Conditional condition : dad.conditionsAndValuesMap.keySet()) {
			if (r.nextDouble() > 0.25) {
				newConditions.put(condition, dad.conditionsAndValuesMap.get(condition));
			}
		}
		for (Conditional condition : mom.conditionsAndValuesMap.keySet()) {
			if (r.nextDouble() > 0.25) {
				newConditions.put(condition, mom.conditionsAndValuesMap.get(condition));
			}
		}
		for (Conditional condition : newConditions.keySet()) {
			if (r.nextDouble() > 0.25) {
				this.conditionsAndValuesMap.put(condition, newConditions.get(condition));
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
				SingleCondition condition = new SingleCondition(mapTokens[0].trim());
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
		for (Conditional condition : conditionsAndResultsMap.keySet()) {
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
		if ((!highLevelPrefs.containsAll(HLPS)) || !cardPrefs.containsAll(Card.ALL_CARD_NAMES)) {
			throw new AssertionError("Missing card or prefs (bad list)");
		}
		if (highLevelPrefs.size() != HLPS.size() || cardPrefs.size() != Card.ALL_CARD_NAMES.size()) {
			throw new AssertionError("Length mismatch.");
		}
		if (name == null || name.length() == 0) {
			throw new AssertionError("Missing name.");
		}
		for (Conditional condition : conditionsAndResultsMap.keySet()) {
			if (condition.altersHighLevelPrefs()) {
				if (!conditionsAndResultsMap.get(condition).containsAll(HLPS)) {
					throw new AssertionError("Missing high level pref in conditionsAndResults");
				}
			} else {
				if (!conditionsAndResultsMap.get(condition).containsAll(Card.ALL_CARD_NAMES)) {
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
		
		SingleCondition upgradeNonStarters = new SingleCondition("unupgradedNonStarterCards > 0 (HLP)");
		upgradeNonStarters.setPriorityLevel(2);
		List<String> unsPrefs = Arrays.asList("upgradeCard", "addCard", "maxHp", "removeCard");
		joyfulDragon.conditionsAndResultsMap.put(upgradeNonStarters, unsPrefs);
		
		SingleCondition maxHp = new SingleCondition("maxHp < level * 6 (HLP)");
		maxHp.setPriorityLevel(1);
		List<String> mhpPrefs = Arrays.asList("maxHp", "addCard", "removeCard", "upgradeCard");
		joyfulDragon.conditionsAndResultsMap.put(maxHp, mhpPrefs);
		
		SingleCondition valueDamage = new SingleCondition("averageDamagePerCard < level * 0.4 (Cards)");
		valueDamage.setPriorityLevel(0);
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
	
	private static String getNewName () {
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
			return getNewName();
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
			String name = getNewName();
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
		return getPreferences(true);
	}
	
	@Override 
	public List<String> getCardPrefs () {
		return getPreferences(false);
	}
	
	private List<String> getPreferences (boolean highLevel) {
		DeckReport report = new DeckReport(hero);
		List<Conditional> liveConditions = conditionsAndValuesMap.keySet().stream()
			.filter(condition -> condition.altersHighLevelPrefs() == highLevel)
			.filter(condition -> condition.evaluate(report))
			.collect(Collectors.toList());
		Map<String, Double> tempValues;
		if (liveConditions.size() >= 1) {
			//Sort in ascending order of priority
			Collections.sort(liveConditions);
			if (ClimbingGame.OUTPUT_LEVEL >= 3) {
				System.out.println("\tAll live conditions:");
				liveConditions.forEach(condition -> System.out.println("\t\t" + condition));
			}
			tempValues = new HashMap<>();
			Map<String, Double> optionValues;
			//Copy over the values
			if (highLevel) {
				optionValues = hlpValues;
			} else {
				optionValues = cardValues;
			}
			optionValues.keySet().forEach(optionName -> tempValues.put(optionName, optionValues.get(optionName)));
			//Edit them based on conditions
			for (Conditional condition : liveConditions) {
				Map<String, Double> overwriteValues = conditionsAndValuesMap.get(condition);
				for (String optionName : overwriteValues.keySet()) {
					tempValues.put(optionName, overwriteValues.get(optionName));
				}
			}
			
		} else {
			//If there were no active rules, use simpler logic
			if (highLevel) {
				tempValues = hlpValues;
			} else {
				tempValues = cardValues;
			}
		}
		
		List<OptionValue> listToSort = new ArrayList<>();
		for (String optionName : tempValues.keySet()) {
			listToSort.add(new OptionValue(optionName, tempValues.get(optionName)));
		}
		Collections.sort(listToSort);
		return listToSort.stream().map(OptionValue::getOption).collect(Collectors.toList());
	}
	
	@Override
	public String toString () {
		if (!isDiff) {
			String response = "";
			response += "High Level Prefs: " + highLevelPrefs;
			response += "\nCard Prefs: " + cardPrefs;
			response += "\nConditions:";
			for (Conditional condition : conditionsAndResultsMap.keySet()) {
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
			for (Conditional condition : conditionsAndResultsMap.keySet()) {
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
			Map<Conditional, List<String>> diffMap = new HashMap<>();
			for (Conditional condition : strat1.conditionsAndResultsMap.keySet()) {
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
	
	public void setHero (Hero hero) {
		this.hero = hero;
	}
	
	public Map<Conditional, List<String>> getConditionsAndResultsMap() {
		return conditionsAndResultsMap;
	}

	public void setConditionsAndResultsMap(Map<Conditional, List<String>> conditionsAndResultsMap) {
		this.conditionsAndResultsMap = conditionsAndResultsMap;
	}

	public double getImprovement() {
		return improvement;
	}

	public void setImprovement(double improvement) {
		this.improvement = improvement;
	}

	public boolean isDiff() {
		return isDiff;
	}

	public void setDiff(boolean isDiff) {
		this.isDiff = isDiff;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName () {
		return name;
	}	
}
