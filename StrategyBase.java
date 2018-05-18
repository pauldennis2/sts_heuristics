/**
 * @author Paul Dennis (pd236m)
 * May 10, 2018
 */
package sts_heuristics;

import java.util.Arrays;
import java.util.List;

public class StrategyBase implements Comparable<StrategyBase> {
	
	protected List<String> cardPrefs;
	protected List<String> highLevelPrefs;
	
	double averageLevelAttained;
	
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

	/**
	 * @return the averageLevelAttained
	 */
	public double getAverageLevelAttained() {
		return averageLevelAttained;
	}

	/**
	 * @param averageLevelAttained the averageLevelAttained to set
	 */
	public void setAverageLevelAttained(double averageLevelAttained) {
		this.averageLevelAttained = averageLevelAttained;
	}

	/**
	 * @param cardPrefs the cardPrefs to set
	 */
	public void setCardPrefs(List<String> cardPrefs) {
		this.cardPrefs = cardPrefs;
	}

	/**
	 * @param highLevelPrefs the highLevelPrefs to set
	 */
	public void setHighLevelPrefs(List<String> highLevelPrefs) {
		this.highLevelPrefs = highLevelPrefs;
	}
	
	

}
