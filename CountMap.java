/**
 * @author Paul Dennis (pd236m)
 * May 31, 2018
 */
package sts_heuristics;

import java.util.HashMap;
import java.util.Map;

public class CountMap<K> {

	private Map<K, Integer> countMap;
	
	public CountMap () {
		countMap = new HashMap<>();
	}
	
	public void add (K k) {
		if (countMap.containsKey(k)) {
			countMap.put(k, countMap.get(k) + 1);
		} else {
			countMap.put(k, 1);
		}
	}
}
