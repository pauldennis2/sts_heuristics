/**
 * @author Paul Dennis
 * Jul 11, 2018
 */
package shipyard;

public enum FundamentalDevotion {
	LIGHT(0, 0),
	DARK(2, 2),
	CHAOS(2, 0),
	ORDER(0, 2);
	
	private final int requiredX;
	private final int requiredY;
	
	private FundamentalDevotion(int requiredX, int requiredY) {
		this.requiredX = requiredX;
		this.requiredY = requiredY;
	}
	
	public int getRequiredX () {
		return requiredX;
	}
	
	public int getRequiredY () {
		return requiredY;
	}
	
	@Override
	public String toString () {
		return MyStringUtils.capitalizeWords(super.toString());
	}
}
