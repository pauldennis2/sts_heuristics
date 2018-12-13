/**
 * @author Paul Dennis
 * Jul 11, 2018
 */
package shipyard;

public enum ElementalDevotion {
	AIR(1, 0),
	FIRE(0, 1),
	WATER(2, 1),
	EARTH(1, 2);
	
	private final int requiredX;
	private final int requiredY;
	
	private ElementalDevotion(int requiredX, int requiredY) {
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
