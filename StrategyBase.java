/**
 * @author Paul Dennis (pd236m)
 * May 10, 2018
 */
package sts_heuristics;

import java.util.Arrays;
import java.util.List;

public class StrategyBase implements Comparable<StrategyBase> {
	
	private List<String> cardPrefs;
	private List<String> highLevelPrefs;
	
	protected double averageLevelAttained;
	
	public StrategyBase () {
		cardPrefs = Arrays.asList("heal", "strikeDefend", "strikeExhaust", "healBlock");
		highLevelPrefs = Arrays.asList("addCard", "upgradeCard", "removeCard", "maxHp");
	}
	
	public List<String> getCardPrefs () {
		return cardPrefs;
	}
	
	public List<String> getHighLevelPrefs () {
		return highLevelPrefs;
	}
	
	@Override
	public int compareTo(StrategyBase other) {
		if (this.averageLevelAttained > other.averageLevelAttained) {
			return -1;
		} else if (this.averageLevelAttained < other.averageLevelAttained) {
			return 1;
		}
		return 0;
	}

	public double getAverageLevelAttained() {
		return averageLevelAttained;
	}

	public void setAverageLevelAttained(double averageLevelAttained) {
		this.averageLevelAttained = averageLevelAttained;
	}

	public void setCardPrefs(List<String> cardPrefs) {
		this.cardPrefs = cardPrefs;
	}

	public void setHighLevelPrefs(List<String> highLevelPrefs) {
		this.highLevelPrefs = highLevelPrefs;
	}
	
}
