/**
 * @author Paul Dennis (pd236m)
 * May 31, 2018
 */
package sts_heuristics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	public void addInitialZeroCount (Collection<K> group) {
		for (K k : group) {
			countMap.put(k, 0);
		}
	}
	
	public Map<K, Integer> getMap () {
		return countMap;
	}
	
	public Integer get (K k) {
		return countMap.get(k);
	}
	
	@Override
	public String toString () {
		Class<? extends Object> class1 = countMap.keySet().stream().collect(Collectors.toList()).get(0).getClass();

		String response = "CountMap for " + class1.getSimpleName() + "s";
		
		for (K k : countMap.keySet()) {
			response += "\n\t" + k + ", " + countMap.get(k);
		}
		
		return response;
	}
}
