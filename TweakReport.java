/**
 * @author Paul Dennis (pd236m)
 * May 10, 2018
 */
package sts_heuristics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TweakReport {

	AdaptiveStrategy base;
	
	Double baseAverage;
	Map<AdaptiveStrategy, Double> childrenAverages;
	/**
	 * @param base
	 * @param baseAverage
	 * @param childrenAverages
	 */
	public TweakReport(AdaptiveStrategy base, Double baseAverage, Map<AdaptiveStrategy, Double> childrenAverages) {
		super();
		this.base = base;
		this.baseAverage = baseAverage;
		this.childrenAverages = childrenAverages;
	}
	
	//Very possibly an empty list
	public List<AdaptiveStrategy> getImprovedChildren () {
		return childrenAverages.keySet().stream()
				.filter(strat -> childrenAverages.get(strat) > baseAverage)
				.collect(Collectors.toList());
	}
	
	public AdaptiveStrategy getBestStrategy () {
		return null;//List<AdaptiveStrategy> 
	}
	
	
}
