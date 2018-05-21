/**
 * @author Paul Dennis (pd236m)
 * May 21, 2018
 */
package sts_heuristics;

import java.util.Random;

public class CompositeCondition implements Conditional {
	
	private SingleCondition first;
	private SingleCondition second;
	private boolean isAnd;
	
	public CompositeCondition(SingleCondition first, SingleCondition second, boolean isAnd) {
		super();
		this.first = first;
		this.second = second;
		this.isAnd = isAnd;
	}
	
	@Override
	public boolean evaluate (DeckReport report) {
		if (isAnd) {
			return first.evaluate(report) && second.evaluate(report);
		} else {
			return first.evaluate(report) || second.evaluate(report);
		}
	}
	
	@Override
	public CompositeCondition tweak () {
		Random r = new Random();
		CompositeCondition newCompCond = new CompositeCondition(first, second, isAnd);
		if (r.nextBoolean()) {
			newCompCond.isAnd = !isAnd;
		} else {
			if (r.nextBoolean() || r.nextBoolean()) { //Tweak one of the conditions
				if (r.nextBoolean()) {//Tweak first condition
					newCompCond.first = first.tweak();
				} else {
					newCompCond.second = second.tweak();
				}
			}
		}
		//TODO; finish implementation
		return newCompCond;
	}
	
	@Override
	public String toString () {
		String boolPart;
		if (isAnd) {
			boolPart = "&&";
		} else {
			boolPart = "||";
		}
		return first.toString() + boolPart + second.toString();
	}
	
}
