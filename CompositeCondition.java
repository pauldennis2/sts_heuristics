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
	
	public CompositeCondition ()  {
		Random r = new Random();
		if (r.nextBoolean()) {
			first = new SingleCondition(r);
		} else {
			first = new CompositeCondition();
		}
		second = new SingleCondition(r);
		isAnd = r.nextBoolean();
	}
	
	public CompositeCondition(Conditional first, Conditional second, boolean isAnd) {
		super();
		this.first = first;
		this.second = second;
		this.isAnd = isAnd;
		
		priorityLevel = 1;
	}
	/*
	public CompositeCondition (String text) {
		char[] charArray = text.toCharArray();
		String textNotInsideParens = "";
		int openParenCount = 0;
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] == '(') {
				openParenCount++;
			} else if (charArray[i] == ')') {
				openParenCount--;
			} else if (openParenCount == 0){
				textNotInsideParens += charArray[i];
			}
		}
		String splitter;
		if (textNotInsideParens.contains("&&")) {
			splitter = "&&";
			isAnd = true;
		} else if (textNotInsideParens.contains("||")) {
			splitter = "\\|\\|";
			isAnd = false;
		} else {
			throw new AssertionError("Cannot parse this text to a compositeCondition: " + text);
		}
		String[] parts;
		if (text.contains("-")) {
			String notTheLastBit = text.split("-")[0].trim();
			parts = notTheLastBit.split(splitter);
		} else {
			parts = text.split(splitter);
		}
		int index = 0;
		for (String part : parts) {
			part = part.trim();
			if (!part.startsWith("(") || !part.endsWith(")")) {
				System.err.println("Failed with overall input: " + text);
				System.err.println("Parts were:");
				//System.err.println(parts);
				for (String p2 : parts) {
					System.err.println(p2);
				}
				throw new AssertionError("Bad format. Expected parens at beginning and end. String was *" + part + "*");
			}
			String conditionText = part.substring(1, part.length() - 1);
			if (conditionText.contains("&&") || conditionText.contains("||")) {
				if (index == 0) {
					first = new CompositeCondition(conditionText);
				} else {
					second = new CompositeCondition(conditionText);
				}
			} else {
				conditionText = conditionText.replaceAll("[()]", "");
				if (index == 0) {
					first = new SingleCondition(conditionText);
				} else {
					second = new SingleCondition(conditionText);
				}
			}
			index++;
		}
		if (text.contains("*HLP*")) {
			altersHighLevelPrefs = true;
		} else if (text.contains("*Cards*")) {
			altersHighLevelPrefs = false;
		} else {
			altersHighLevelPrefs = null;
		}
	}*/
	
	public CompositeCondition (String text) {
		char[] charArray = text.toCharArray();
		StringBuilder textNotInsideParens = new StringBuilder();
		StringBuilder firstSb = new StringBuilder();
		StringBuilder secondSb = new StringBuilder();
		int openParenCount = 0;
		boolean onFirstString = true;
		for (int i = 0; i < charArray.length; i++) {
			if (onFirstString) {
				firstSb.append(charArray[i]);
			} else if (openParenCount == 0 && charArray[i] != '(') {
				textNotInsideParens.append(charArray[i]);
			} else {
				secondSb.append(charArray[i]);
			}
			if (charArray[i] == '(') {
				openParenCount++;
			} else if (charArray[i] == ')') {
				openParenCount--;
				if (openParenCount == 0) {
					onFirstString = false;
				}
			} 
		}
		String firstString = firstSb.toString();
		String secondString = secondSb.toString();
		//Remove the parentheses
		String firstText = firstString.substring(1, firstString.length() - 1);
		if (firstText.contains("&&") || firstText.contains("||")) {
			first = new CompositeCondition(firstText);
		} else {
			first = new SingleCondition(firstText);
		}
		String secondText = secondString.substring(1, secondString.length() - 1);
		if (secondText.contains("&&") || secondText.contains("||")) {
			second = new CompositeCondition(secondText);
		} else {
			second = new SingleCondition(secondText);
		}
		if (textNotInsideParens.toString().contains("&&")) {
			isAnd = true;
		} else if (textNotInsideParens.toString().contains("||")) {
			isAnd = false;
		} else {
			throw new AssertionError("Missing boolean logic (&& or ||)");
		}
	}
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 1000; i++) {
			CompositeCondition cc = new CompositeCondition();
			CompositeCondition rebuilt = new CompositeCondition(cc.toString());
			if (!cc.equals(rebuilt)) {
				System.out.println("Not equal!");
				System.out.println(cc);
				System.out.println(rebuilt);
				System.out.println("-------");
			}
			
		}
		/*String text = "(((level < 3.4) || (powerHealPerTurn < numRibbons * 1.3)) && (averageHealPerCard > maxHp)) || (powerBlockPerTurn < nonUpgradedCards * 2.4)";
		CompositeCondition cc = new CompositeCondition(text);
		System.out.println(cc);
		System.out.println(text);*/
		System.out.println("Done");
		
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
		String response = "(" + first.toString() + ") " + boolPart + " (" + second.toString() + ")";
		return response;
	}
	
	@Override
	public boolean equals (Object obj) {
		if (!(obj instanceof CompositeCondition)) {
			return false;
		}
		CompositeCondition other = (CompositeCondition) obj;
		return this.toString().equals(other.toString());
	}
	
	//A list will be sorted by default in Ascending order of priority
	@Override
	public int compareTo (Conditional other) {
		return this.priorityLevel - other.getPriorityLevel();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + (isAnd ? 1231 : 1237);
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}


	@Override
	public int getPriorityLevel () {
		return priorityLevel;
	}

	public void setPriorityLevel (int priorityLevel) {
		this.priorityLevel = priorityLevel;
	}
	
}
