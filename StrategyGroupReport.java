/**
 * @author Paul Dennis (pd236m)
 * May 10, 2018
 */
package sts_heuristics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyGroupReport {
	
	int numStrategies;
	List<AdaptiveStrategy> strategies;
	
	int numConditionsSum;
	int lowestNumConditions = 1000000;
	int highestNumConditions = -1;
	double numConditionsAv;
	
	Map<String, Double> cardValuesSums;
	Map<String, Double> hlpValuesSums;
	
	Map<String, Double> cardValuesAv;
	Map<String, Double> hlpValuesAv;
	
	CountMap<String> bonusChoiceCount;
	CountMap<Conditional> conditionsCount;
	
	CountMap<String> conditionalValuesCount;
	
	double attainmentSum;
	double attainmentAv;
	
	double highestAttainment = 0.0;
	double lowestAttainment = 1000000000.0;
	
	public StrategyGroupReport(List<AdaptiveStrategy> strategies) {
		this.strategies = strategies;
		numStrategies = strategies.size();
		
		cardValuesSums = new HashMap<>();
		hlpValuesSums = new HashMap<>();
		bonusChoiceCount = new CountMap<>();
		conditionsCount = new CountMap<>();
		conditionalValuesCount = new CountMap<>();
		
		for (AdaptiveStrategy strategy : strategies) {
			int numConditions = strategy.getConditionsAndValuesMap().keySet().size();
			numConditionsSum += numConditions;
			if (numConditions > highestNumConditions) {
				highestNumConditions = numConditions;
			}
			if (numConditions < lowestNumConditions) {
				lowestNumConditions = numConditions;
			}
			Map<String, Double> cardValues = strategy.getCardValues();
			for (String card : cardValues.keySet()) {
				if (cardValuesSums.containsKey(card)) {
					cardValuesSums.put(card, cardValuesSums.get(card) + cardValues.get(card));
				} else {
					cardValuesSums.put(card, cardValues.get(card));
				}
			}
			Map<String, Double> hlpValues = strategy.getHlpValues();
			for (String hlp : hlpValues.keySet()) {
				if (hlpValuesSums.containsKey(hlp)) {
					hlpValuesSums.put(hlp, hlpValuesSums.get(hlp) + hlpValues.get(hlp));
				} else {
					hlpValuesSums.put(hlp, hlpValues.get(hlp));
				}
			}
			bonusChoiceCount.add(strategy.getBonusChoice());
			
			for (Conditional condition : strategy.getConditionsAndValuesMap().keySet()) {
				conditionsCount.add(condition);
				Map<String, Double> values = strategy.getConditionsAndValuesMap().get(condition);
				for (String key : values.keySet()) {
					conditionalValuesCount.add(key);
				}
			}
			
			attainmentSum += strategy.getAverageLevelAttained();
			double attainment = strategy.getAverageLevelAttained();
			if (attainment > highestAttainment) {
				highestAttainment = attainment;
			}
			if (attainment < lowestAttainment) {
				lowestAttainment = attainment;
			}
		}
		
		cardValuesAv = new HashMap<>();
		hlpValuesAv = new HashMap<>();
		
		for (String card : cardValuesSums.keySet()) {
			cardValuesAv.put(card, cardValuesSums.get(card) / numStrategies);
		}
		for (String hlp : hlpValuesSums.keySet()) {
			hlpValuesAv.put(hlp, hlpValuesSums.get(hlp) / numStrategies);
		}
		
		attainmentAv = attainmentSum / numStrategies;
		numConditionsAv = numConditionsSum / numStrategies;
	}
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("Analysis of Strategies:");
		builder.append("\n===================");
		builder.append("\nNumber of strategies analyzed: " + numStrategies);
		builder.append("\nAttainment ranged from " + lowestAttainment 
				+ " to " + highestAttainment + " with an average of " + attainmentAv + ".");
		builder.append("\nNumber of Conditions ranged from " + lowestNumConditions
				+ " to " + highestNumConditions + " with an average of " + numConditionsAv + ".");
		builder.append("\nAverage Card Values:");
		for (String card : cardValuesAv.keySet()) {
			builder.append("\n\t" + card + ": " + cardValuesAv.get(card));
		}
		builder.append("\nAverage HLP Values:");
		for (String hlp : hlpValuesAv.keySet()) {
			builder.append("\n\t" + hlp + ": " + hlpValuesAv.get(hlp));
		}
		builder.append("\nBonus Choice Count: ");
		builder.append(bonusChoiceCount.toString());
		
		builder.append("\nConditions Count Map:");
		builder.append(conditionsCount.toString());
		builder.append("\nConditional Values Counts:");
		builder.append(conditionalValuesCount.toString());
		return builder.toString();
	}
}
