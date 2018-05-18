/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.Collections;
import java.util.List;

@Deprecated
public class Strategy extends StrategyBase {
	
	/*
	 * There are (4 x 3 x 2 x 1) x (4 x 3) distinct strategies
	 * = 288 total
	 */
	
	public Strategy () {
		super();
		Collections.shuffle(highLevelPrefs);
		Collections.shuffle(cardPrefs);
	}
	
	@Override
	public String toString () {
		//return preferences.toString();
		//Consolidation for now, since only first two choices matter:
		return highLevelPrefs.toString() + ", " +
		"[" + cardPrefs.get(0) + ", " + cardPrefs.get(1) + "] - Average: " + averageLevelAttained;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cardPrefs == null) ? 0 : cardPrefs.hashCode());
		result = prime * result + ((highLevelPrefs == null) ? 0 : highLevelPrefs.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Strategy other = (Strategy) obj;
		if (cardPrefs == null) {
			if (other.cardPrefs != null)
				return false;
		} else if (!cardPrefs.equals(other.cardPrefs))
			return false;
		if (highLevelPrefs == null) {
			if (other.highLevelPrefs != null)
				return false;
		} else if (!highLevelPrefs.equals(other.highLevelPrefs))
			return false;
		return true;
	}
	
	@Override
	public List<String> getHighLevelPrefs () {
		return highLevelPrefs;
	}
	
	@Override
	public List<String> getCardPrefs () {
		return cardPrefs;
	}
	
	
}
