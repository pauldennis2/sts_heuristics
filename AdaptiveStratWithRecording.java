/**
 * @author Paul Dennis (pd236m)
 * Jun 6, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AdaptiveStratWithRecording extends AdaptiveStrategy {

	private CountMap<Conditional> conditionsToNumTimesUsed;
	
	public AdaptiveStratWithRecording(String fileString) {
		super(fileString);
		
		conditionsToNumTimesUsed = new CountMap<>();
		conditionsToNumTimesUsed.addInitialZeroCount(this.getConditionsAndValuesMap().keySet());
	}
	
	@Override 
	public List<String> getHighLevelPrefs () {
		return getPreferences(true);
	}
	
	@Override 
	public List<String> getCardPrefs () {
		return getPreferences(false);
	}
	
	@Override
	protected List<String> getPreferences (boolean highLevel) {
		DeckReport report = new DeckReport(getHero());
		List<Conditional> liveConditions = this.getConditionsAndValuesMap().keySet().stream()
			.filter(condition -> condition.evaluate(report))
			.collect(Collectors.toList());
		for (Conditional condition : liveConditions) {
			conditionsToNumTimesUsed.add(condition);
		}
		Map<String, Double> tempValues;
		if (liveConditions.size() >= 1) {
			//Sort in ascending order of priority
			Collections.sort(liveConditions);
			if (ClimbingGame.OUTPUT_LEVEL >= 3) {
				System.out.println("\tAll live conditions:");
				liveConditions.forEach(condition -> System.out.println("\t\t" + condition));
			}
			tempValues = new RoundedDoubleMap();
			Map<String, Double> optionValues;
			if (highLevel) {
				optionValues = this.getHlpValues();
			} else {
				optionValues = this.getCardValues();
			}
			//Copy over the values
			optionValues.keySet().forEach(optionName -> tempValues.put(optionName, optionValues.get(optionName)));
			//Edit them based on conditions
			for (Conditional condition : liveConditions) {
				Map<String, Double> overwriteValues = this.getConditionsAndValuesMap().get(condition);
				for (String optionName : overwriteValues.keySet()) {
					//Replace IFF it's a HLP and we care about HLPS or it's a Card and we care about cards
					if ((highLevel && HLPS.contains(optionName)) || (!highLevel && CARDS.contains(optionName))){
						tempValues.put(optionName, overwriteValues.get(optionName));
					}
				}
			}
			
		} else {
			//If there were no active rules, use simpler logic
			if (highLevel) {
				tempValues = this.getHlpValues();
			} else {
				tempValues = this.getCardValues();
			}
		}
		
		List<OptionValue> listToSort = new ArrayList<>();
		for (String optionName : tempValues.keySet()) {
			listToSort.add(new OptionValue(optionName, tempValues.get(optionName)));
		}
		Collections.sort(listToSort);
		return listToSort.stream().map(OptionValue::getOption).collect(Collectors.toList());
	}
	
	public static List<AdaptiveStratWithRecording> buildRecordingStrategiesFromFile (String fileName) {
		List<AdaptiveStratWithRecording> strategies = new ArrayList<>();
		try (Scanner fileScanner = new Scanner(new File(fileName))) {
			while (fileScanner.hasNextLine()) {
				strategies.add(new AdaptiveStratWithRecording(fileScanner.nextLine()));
			}
		} catch (FileNotFoundException ex) {
			System.err.println("!!Could not read from file with name: " + fileName);
		}
		return strategies;
	}
	
	@Override
	public String toString () {
		String response = this.getName();
		response += "\nHigh Level Values: " + this.getHlpValues();
		response += "\nCard Values: " + this.getCardValues();
		response += "\nBonus Choice: " + this.getBonusChoice();
		response += "\nConditions:";
		for (Conditional condition : this.getConditionsAndValuesMap().keySet()) {
			response += "\n\t" + conditionsToNumTimesUsed.get(condition) + " :: " + condition 
					+ " - Values: " + this.getConditionsAndValuesMap().get(condition);
		}
		if (averageLevelAttained != 0.0) {
			response += "\nAverage Attainment: " + averageLevelAttained;
		}
		return response;
	}
	
	public CountMap<Conditional> getCountMap () {
		return conditionsToNumTimesUsed;
	}

}
