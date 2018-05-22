/**
 * @author Paul Dennis (pd236m)
 * May 21, 2018
 */
package sts_heuristics;

import java.util.Random;

public class CompositeCondition implements Conditional {
	
	private Conditional first;
	private Conditional second;
	private boolean isAnd;
	
	private int priorityLevel;
	private Boolean altersHighLevelPrefs;
	
	public CompositeCondition(Conditional first, Conditional second, boolean isAnd, Boolean altersHighLevelPrefs) {
		super();
		this.first = first;
		this.second = second;
		this.isAnd = isAnd;
		this.altersHighLevelPrefs = altersHighLevelPrefs;
		
		priorityLevel = 1;
	}
	
	public static void main(String[] args) {

		//Condition to control taking a status immunity or not
		
		SingleCondition condition1 = new SingleCondition("numRibbons < 1.1");
		SingleCondition condition2 = new SingleCondition("level > 10");
		CompositeCondition cc = new CompositeCondition(condition1, condition2, true, false);
		System.out.println(cc);
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
		CompositeCondition newCompCond = new CompositeCondition(first, second, isAnd, altersHighLevelPrefs);
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
		String response = "(" + first.toString() + ") " + boolPart + " (" + second.toString() + ")";
		if (altersHighLevelPrefs != null) {
			if (altersHighLevelPrefs) {
				response += " - (HLP)";
			} else {
				response += " - (Cards)";
			}
		}
		return response;
	}
	
	@Override
	public int getPriorityLevel () {
		return priorityLevel;
	}
	
	@Override
	public Boolean altersHighLevelPrefs () {
		return altersHighLevelPrefs;
	}
	
	public void setPriorityLevel (int priorityLevel) {
		this.priorityLevel = priorityLevel;
	}
	
}
