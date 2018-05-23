/**
 * @author Paul Dennis (pd236m)
 * May 22, 2018
 */
package sts_heuristics;

//Represents the pair of an option (String) and value (Double)
public class OptionValue implements Comparable<OptionValue> {

	private String option;
	private double value;
	
	public OptionValue(String option, double value) {
		super();
		this.option = option;
		this.value = value;
	}
	
	public OptionValue (OptionValue toCopy) {
		this.option = toCopy.option;
		this.value = toCopy.value;
	}

	@Override
	public int compareTo (OptionValue other) {
		if (this.value > other.value) {
			return 1;
		} else if (this.value < other.value) {
			return -1;
		}
		return 0;
	}
	
	public double getValue () {
		return value;
	}

	public String getOption () {
		return option;
	}
	
	public void setValue (double value) {
		if (value >= 0) {
			this.value = value;
		} else {
			System.out.println("CardOption::Bad arg passed to setValue() - cannot be negative.");
		}
	}
	
	public void addValue (double value) {
		if (this.value + value >= 0) {
			this.value += value;
		} else {
			this.value = 0;
			System.out.println("CardOption::value cannot be < 0. Value set to 0.");
		}
	}
}
