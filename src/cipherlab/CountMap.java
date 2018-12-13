
package cipherlab;

/**
 * @author Paul Dennis 
 * May 31, 2018
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CountMap<K> {

	private Map<K, Integer> countMap;
	private boolean negativeAllowed = false;
	
	public CountMap () {
		countMap = new HashMap<>();
	}
	
	public void increment (K k) {
		if (countMap.containsKey(k)) {
			countMap.put(k, countMap.get(k) + 1);
		} else {
			countMap.put(k, 1);
		}
	}
	
	public void add (K k, int amount) {
		if (amount < 1) {
			throw new AssertionError("Cannot add a negative/zero amount. Use subtract().");
		}
		if (countMap.containsKey(k)) {
			countMap.put(k, countMap.get(k) + amount);
		} else {
			countMap.put(k, amount);
		}
	}
	
	public void subtract (K k, int amount) {
		if (amount < 1) {
			throw new AssertionError("Cannot subtract a negative/zero amount. Use add().");
		}
		if (countMap.containsKey(k)) {
			countMap.put(k, countMap.get(k) - amount);
			if (!negativeAllowed && countMap.get(k) < 0) {
				throw new AssertionError("Negative values not allowed in this CountMap.");
			}
		} else {
			throw new AssertionError("Cannot subtract - don't even have a mapping.");
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
	
	public int getSum () {
		int sum = 0;
		for (K k : countMap.keySet()) {
			sum += countMap.get(k);
		}
		return sum;
	}
	
	@Override
	public String toString () {
		//Class<? extends Object> class1 = countMap.keySet().stream().collect(Collectors.toList()).get(0).getClass();

		//String response = "CountMap for " + class1.getSimpleName() + "s";
		String response = "";
		for (K k : countMap.keySet()) {
			response += "\n\t" + k + ", " + countMap.get(k);
		}
		
		return response;
	}
}
