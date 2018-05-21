/**
 * @author Paul Dennis (pd236m)
 * May 21, 2018
 */
package sts_heuristics;

public interface Conditional extends Tweakable {
	boolean evaluate (DeckReport report);
	boolean altersHighLevelPrefs();
	int getPriorityLevel();
	
	@Override
	Conditional tweak ();
}
