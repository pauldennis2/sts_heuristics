/**
 * @author Paul Dennis (pd236m)
 * May 22, 2018
 */
package sts_heuristics;

public class CardOption implements Comparable<CardOption> {

	private String cardName;
	private double value;
	
	public CardOption(String cardName, double value) {
		super();
		this.cardName = cardName;
		this.value = value;
	}

	@Override
	public int compareTo (CardOption other) {
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
	
	public String getCardName () {
		return cardName;
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
