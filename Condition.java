/**
 * @author Paul Dennis (pd236m)
 * May 9, 2018
 */
package sts_heuristics;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@SuppressWarnings("serial")
public class Condition implements Serializable {

	boolean greaterThan;
	//Should either be byRatio or compareToConstant not both
	boolean byRatio;
	double ratio;
	
	int priorityLevel;
	boolean altersHighLevelPrefs;
	
	boolean compareToConstant;
	double constant;
	
	String firstFieldName;
	String secondFieldName;
	
	transient Boolean evaluation;
	transient double averageAttainment;
	
	final List<String> FIELD_NAMES = Arrays.asList("nonUpgradedCards", "numCards", "averageDamagePerCard",
			"averageBlockPerCard", "averageHealPerCard", "level", "maxHp", "currentHp",
			"starterCardsUnupgraded", "unupgradedNonStarterCards");
	
	//Format: "$firstFieldName < $secondFieldName * ratio
	public Condition (String text) {
		String[] words = text.split(" ");
		firstFieldName = words[0];
		priorityLevel = 0;
		try {
			constant = Double.parseDouble(words[2]);
			compareToConstant = true;
		} catch (NumberFormatException ex) {
			secondFieldName = words[2];
			compareToConstant = false;
		}
		if (words[1].equals(">")) {
			greaterThan = true;
		} else if (words[1].equals("<")) {
			greaterThan = false;
		} else {
			throw new AssertionError("Bad input string for condition constructor. Input text was: " + text);
		}
		if (words.length >= 5) {
			try {
				ratio = Double.parseDouble(words[4]);
				byRatio = true;
			} catch (NumberFormatException ex) {
				byRatio = false;
			}
		} else {
			byRatio = false;
		}
		if (words[words.length - 1].contains("HLP")) {
			altersHighLevelPrefs = true;
		} else if (words[words.length - 1].contains("Cards")) {
			altersHighLevelPrefs = false;
		} else {
			throw new AssertionError("Bad input string. Missing info on High level prefs or cards. (What does this condition do?)");
		}
			
		validate();
	}
	
	public static void main(String[] args) {
		Scanner inputScanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input condition string:");
			String text = inputScanner.nextLine();
			if (text.equals("")) {
				break;
			}
			try {
				Condition condition = new Condition(text);
				System.out.println("Created condition: " + condition);
			} catch (Throwable err) {
				System.out.println("An exception was thrown: " + err.getMessage());
			}
		}
		inputScanner.close();
	}
	
	public Condition (Random r, boolean altersHighLevelPrefs) {
		firstFieldName = FIELD_NAMES.get(r.nextInt(FIELD_NAMES.size()));
		compareToConstant = r.nextBoolean();
		DecimalFormat decFormat = new DecimalFormat("#.#");
		if (compareToConstant) {
			//Constant from 0.1 to 10.1
			double c = r.nextDouble() * 10 + 0.1;
			//Rounded to nearest 0.1
			constant = Double.parseDouble(decFormat.format(c));
		} else {
			secondFieldName = FIELD_NAMES.get(r.nextInt(FIELD_NAMES.size()));
			while (secondFieldName.equals(firstFieldName)) {
				secondFieldName = FIELD_NAMES.get(r.nextInt(FIELD_NAMES.size()));
			}
			byRatio = r.nextBoolean();
			if (byRatio) {
				//Ratio from 0.3 to 3.0
				double d = r.nextDouble() * 2.7 + 0.3;
				//Rounded to nearest 0.1
				ratio = Double.parseDouble(decFormat.format(d));
			}
		}
		greaterThan = r.nextBoolean();
		priorityLevel = 0;
		this.altersHighLevelPrefs = altersHighLevelPrefs;
		
		validate();
	}
	
	//Take an "old" condition that was fairly successful and tweak it a little
	public Condition (Random r, Condition old) {
		this.firstFieldName = old.firstFieldName;
		this.compareToConstant = old.compareToConstant;
		this.greaterThan = old.greaterThan;
		this.byRatio = old.byRatio;
		this.ratio = old.ratio;
		this.constant = old.constant;
		this.secondFieldName = old.secondFieldName;
		this.altersHighLevelPrefs = old.altersHighLevelPrefs;
		
		DecimalFormat decFormat = new DecimalFormat("#.#");
		
		if (compareToConstant) {
			//Change constant
			double adjustment = r.nextDouble() + 0.5;
			constant = Double.parseDouble(decFormat.format(adjustment * constant));
		} else if (byRatio) {
			//change ratio
			//Number from 0.5 to 1.5
			double adjustment = r.nextDouble() + 0.5;
			ratio = Double.parseDouble(decFormat.format(adjustment * ratio));
		} else {
			//create ratio
			byRatio = true;
			//But in a more constrained range - 0.7 to 1.4
			double d = r.nextDouble() * 0.7 + 0.7;
			ratio = Double.parseDouble(new DecimalFormat("#.#").format(d));
		}
		//Priority level should be higher since it's been tweaked
		priorityLevel = old.priorityLevel + 1;
		
		validate();
	}
	
	private void validate () {
		if (byRatio && compareToConstant) {
			throw new AssertionError("Cannot be byRatio and compareToConstant (constant should just be bigger/smaller)");
		}
		if (byRatio && ratio == 0.0) {
			throw new AssertionError("Missing ratio information.");
		}
		if (!compareToConstant && secondFieldName == null) {
			throw new AssertionError("Missing second field name.");
		}
		if (!FIELD_NAMES.contains(firstFieldName)) {
			throw new AssertionError("Invalid field name: " + firstFieldName);
		}
		if (secondFieldName != null && !FIELD_NAMES.contains(secondFieldName)) {
			throw new AssertionError("Invalid second field name: " + secondFieldName);
		}
		if (secondFieldName != null && firstFieldName.equals(secondFieldName)) {
			throw new AssertionError("Field names are the same. Field cannot be compared to itself.");
		}
	}
	
	public boolean evaluate (DeckReport report) {
		Class<? extends DeckReport> c = report.getClass();
		double firstVal;
		double secondVal;
		try {
			firstVal = c.getField(firstFieldName).getDouble(report);
			if (compareToConstant) {
				secondVal = constant;
			} else {
				secondVal = c.getField(secondFieldName).getDouble(report);
			}
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
		//1 if by ref, 2 if by val
		if (byRatio) {
			//We're changing secondVal because we want firstVal
			//to be greater than/less than that value by ratio
			secondVal *= ratio;
		}
		if (greaterThan) {
			evaluation = firstVal > secondVal;
			return firstVal > secondVal;
		} else {
			evaluation = firstVal < secondVal;
			return firstVal < secondVal;
		}
	}
	
	@Override
	public String toString () {
		String response = firstFieldName;
		if (greaterThan) {
			response += " > ";
		} else {
			response += " < ";
		}
		if (compareToConstant) {
			response += constant;
		} else {
			response += secondFieldName;
		}
		if (byRatio) {
			response += " * " + ratio;
		}
		if (altersHighLevelPrefs) {
			response += " (HLP)-";
		} else {
			response += " (Cards)-";
		}
		if (evaluation != null) {
			//response += ", Evaluated to: " + evaluation;
		}
		return response;
	}

	@Override
	public boolean equals (Object otherObj) {
		if (otherObj instanceof Condition) {
			Condition other = (Condition) otherObj;
			return this.toString().equals(other.toString());
		}
		return false;
	}
	
	public boolean isGreaterThan() {
		return greaterThan;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (altersHighLevelPrefs ? 1231 : 1237);
		result = prime * result + (byRatio ? 1231 : 1237);
		result = prime * result + (compareToConstant ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(constant);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((firstFieldName == null) ? 0 : firstFieldName.hashCode());
		result = prime * result + (greaterThan ? 1231 : 1237);
		temp = Double.doubleToLongBits(ratio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((secondFieldName == null) ? 0 : secondFieldName.hashCode());
		return result;
	}

	public void setGreaterThan(boolean greaterThan) {
		this.greaterThan = greaterThan;
	}

	public boolean isByRatio() {
		return byRatio;
	}


	public void setByRatio(boolean byRatio) {
		this.byRatio = byRatio;
	}


	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public int getPriorityLevel() {
		return priorityLevel;
	}

	public void setPriorityLevel(int priorityLevel) {
		this.priorityLevel = priorityLevel;
	}

	public boolean isAltersHighLevelPrefs() {
		return altersHighLevelPrefs;
	}

	public void setAltersHighLevelPrefs(boolean altersHighLevelPrefs) {
		this.altersHighLevelPrefs = altersHighLevelPrefs;
	}

	public boolean isCompareToConstant() {
		return compareToConstant;
	}

	public void setCompareToConstant(boolean compareToConstant) {
		this.compareToConstant = compareToConstant;
	}

	public double getConstant() {
		return constant;
	}

	public void setConstant(double constant) {
		this.constant = constant;
	}

	public String getFirstFieldName() {
		return firstFieldName;
	}

	public void setFirstFieldName(String firstFieldName) {
		this.firstFieldName = firstFieldName;
	}

	public String getSecondFieldName() {
		return secondFieldName;
	}

	public void setSecondFieldName(String secondFieldName) {
		this.secondFieldName = secondFieldName;
	}
}
