/**
 * @author Paul Dennis
 * Jul 10, 2018
 */
package shipyard;

public enum MissionType {
	
	WAR,
	SCIENCE,
	EXPLORATION,
	TRAINING,
	DIPLOMACY, //Includes Trade with foreign powers
	NONE;
	
	@Override
	public String toString () {
		String start = super.toString().toLowerCase();
		return start.substring(0, 1).toUpperCase() + start.substring(1);
	}
}
