/**
 * @author Paul Dennis (pd236m)
 * May 9, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class SingleCondition implements Conditional {

	private boolean greaterThan;
	//Should either be byRatio or compareToConstant not both
	private boolean byRatio;
	private double ratio;
	
	private int priorityLevel;
	
	private boolean compareToConstant;
	private double constant;
	
	private String firstFieldName;
	private String secondFieldName;
	
	transient Boolean evaluation;
	transient double averageAttainment;
	
	static List<String> FIELD_NAMES = Arrays.stream(DeckReport.class.getFields()).map(field -> field.getName()).collect(Collectors.toList());
	public static final String UNUSED_CONDITIONS_FILE = "data/conditions/unused_conditions.txt";
	
	private static Set<Conditional> unusedConditions;
	
	//Format: "$firstFieldName < $secondFieldName * $ratio
	public SingleCondition (String text) {
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
			
		validate();
	}
	
	public static void addUnusedConditions (Set<Conditional> unused) {
		if (unusedConditions == null) {
			unusedConditions = readUnusedConditionsFromFile(UNUSED_CONDITIONS_FILE);
		}
		unusedConditions.addAll(unused);
		
		writeUnusedConditionsToFile();
	}
	
	private static Set<Conditional> readUnusedConditionsFromFile (String fileName) {
		Set<Conditional> unusedConditions = new HashSet<>();
		try (Scanner scanner = new Scanner(new File(fileName))) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("&&") || line.contains("||")) {
					unusedConditions.add(new CompositeCondition(line));
				} else {
					unusedConditions.add(new SingleCondition(line));
				}
			}
		} catch (FileNotFoundException ex) {
			System.err.println("Error reading from " + fileName + ".");
			ex.printStackTrace();
		}
		return unusedConditions;
	}
	
	private static void writeUnusedConditionsToFile () {
		File directory = new File("data/conditions/");
		directory.mkdirs();
		try (FileWriter fileWriter = new FileWriter(new File(UNUSED_CONDITIONS_FILE))) {
			for (Conditional condition : unusedConditions) {
				fileWriter.write(condition.toString());
				fileWriter.write("\n");
			}
		} catch (IOException ex) {
			System.err.println("Error writing to " + UNUSED_CONDITIONS_FILE + ".");
			ex.printStackTrace();
		}
	}
	
	public static void testTextBasedConditionCreation () {
		Scanner inputScanner = new Scanner(System.in);
		while (true) {
			System.out.println("Please input condition string:");
			String text = inputScanner.nextLine();
			if (text.equals("")) {
				break;
			}
			try {
				SingleCondition condition = new SingleCondition(text);
				System.out.println("Created condition: " + condition);
			} catch (Throwable err) {
				System.out.println("An exception was thrown: " + err.getMessage());
			}
		}
		inputScanner.close();
	}
	
	private SingleCondition () {
		
	}
	
	public static SingleCondition getRandomCondition () {
		Random r = new Random();
		SingleCondition condition = new SingleCondition();
		condition.firstFieldName = FIELD_NAMES.get(r.nextInt(FIELD_NAMES.size()));
		condition.compareToConstant = r.nextBoolean();
		DecimalFormat decFormat = new DecimalFormat("#.#");
		if (condition.compareToConstant) {
			//Constant from 0.1 to 10.1
			double c = r.nextDouble() * 10 + 0.1;
			//Rounded to nearest 0.1
			condition.constant = Double.parseDouble(decFormat.format(c));
		} else {
			condition.secondFieldName = FIELD_NAMES.get(r.nextInt(FIELD_NAMES.size()));
			while (condition.secondFieldName.equals(condition.firstFieldName)) {
				condition.secondFieldName = FIELD_NAMES.get(r.nextInt(FIELD_NAMES.size()));
			}
			condition.byRatio = r.nextBoolean();
			if (condition.byRatio) {
				//Ratio from 0.3 to 3.0
				double d = r.nextDouble() * 2.7 + 0.3;
				//Rounded to nearest 0.1
				condition.ratio = Double.parseDouble(decFormat.format(d));
			}
		}
		condition.greaterThan = r.nextBoolean();
		condition.priorityLevel = 0;
		if (unusedConditions == null) {
			unusedConditions = readUnusedConditionsFromFile(UNUSED_CONDITIONS_FILE);
		}
		if (unusedConditions.contains(condition)) {
			return getRandomCondition();
		}
		condition.validate();
		return condition;
	}
	
	//Take an "old" condition that was fairly successful and tweak it a little
	//public Condition (Random r, Condition old) {
	public SingleCondition tweak () {
		Random r = new Random();
		SingleCondition newCond = new SingleCondition();
		newCond.firstFieldName = this.firstFieldName;
		newCond.compareToConstant = this.compareToConstant;;
		newCond.greaterThan = this.greaterThan;
		newCond.byRatio = this.byRatio;
		newCond.ratio = this.ratio;
		newCond.constant = this.constant;
		newCond.secondFieldName = this.secondFieldName;
		
		DecimalFormat decFormat = new DecimalFormat("#.#");
		
		if (compareToConstant) {
			//Change constant
			double adjustment = r.nextDouble() + 0.5;
			newCond.constant = Double.parseDouble(decFormat.format(adjustment * constant));
		} else if (byRatio) {
			//change ratio
			//Number from 0.5 to 1.5
			double adjustment = r.nextDouble() + 0.5;
			newCond.ratio = Double.parseDouble(decFormat.format(adjustment * ratio));
		} else {
			//create ratio
			newCond.byRatio = true;
			//But in a more constrained range - 0.7 to 1.4
			double d = r.nextDouble() * 0.7 + 0.7;
			newCond.ratio = Double.parseDouble(decFormat.format(d));
		}
		//Priority level should be higher since it's been tweaked
		newCond.priorityLevel = this.priorityLevel + 1;
		
		newCond.validate();
		return newCond;
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
	
	@Override
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
		if (evaluation != null) {
			//response += ", Evaluated to: " + evaluation;
		}
		return response;
	}
	
	//A list will be sorted by default in Ascending order of priority
	@Override
	public int compareTo (Conditional other) {
		return this.priorityLevel - other.getPriorityLevel();
	}
	
	public boolean isGreaterThan() {
		return greaterThan;
	}
	
	public void setGreaterThan(boolean greaterThan) {
		this.greaterThan = greaterThan;
	}
	
	@Override
	public boolean equals (Object otherObj) {
		if (otherObj instanceof SingleCondition) {
			SingleCondition other = (SingleCondition) otherObj;
			return this.toString().equals(other.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (byRatio ? 1231 : 1237);
		result = prime * result + (compareToConstant ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(constant);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((firstFieldName == null) ? 0 : firstFieldName.hashCode());
		result = prime * result + (greaterThan ? 1231 : 1237);
		result = prime * result + priorityLevel;
		temp = Double.doubleToLongBits(ratio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((secondFieldName == null) ? 0 : secondFieldName.hashCode());
		return result;
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
