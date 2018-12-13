/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

public class BoundedInt {

	private int value;
	private int max;
	private int min;
	
	public BoundedInt (int max) {
		this.value = max;
		this.max = max;
		this.min = 0;
	}
	
	public BoundedInt (int value, int max) {
		this.value = value;
		this.max = max;
		this.min = 0;
	}
	
	public BoundedInt (int value, int max, int min) {
		this.value = value;
		this.max = max;
		this.min = min;
	}
	
	public int getValue () {
		return value;
	}
	
	public int getMax () {
		return max;
	}
	
	public void setValue (int value) {
		if (value > max) {
			this.value = max;
		} else if (value < min) {
			this.value = min;
		} else {
			this.value = value;
		}
	}
	
	public void setMax (int max) {
		if (max < min) {
			throw new AssertionError("Max cannot be less than min");
		}
		this.max = max;
	}
	
	@Override
	public String toString () {
		return value + "/" + max;
	}
}
