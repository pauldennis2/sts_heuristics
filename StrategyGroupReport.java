/**
 * @author Paul Dennis (pd236m)
 * May 10, 2018
 */
package sts_heuristics;

import java.util.Map;

public class StrategyGroupReport {

	Map<StringIndex, Integer> cardPrefOccurances;
	Map<StringIndex, Integer> hlpOccurances;

	public StrategyGroupReport(Map<StringIndex, Integer> cardPrefOccurances, Map<StringIndex, Integer> hlpOccurances) {
		super();
		this.cardPrefOccurances = cardPrefOccurances;
		this.hlpOccurances = hlpOccurances;
	}
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("Report:");
		builder.append("\n\tCard Preferences:");
		for (StringIndex si : cardPrefOccurances.keySet()) {
			builder.append("\n\t\t" + si + " : " + cardPrefOccurances.get(si));
		}
		builder.append("\n\tHigh Level Preferences");
		for (StringIndex si : hlpOccurances.keySet()) {
			builder.append("\n\t\t" + si + " : " + hlpOccurances.get(si));
		}
		return builder.toString();
	}
}
