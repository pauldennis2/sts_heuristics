/**
 * @author Paul Dennis 
 * Jul 9, 2018
 */
package shipyard;

public enum Amenity {
	
	HOLODECK (10),
	TRAINING_SIMULATOR (8),
	CARGO_HOLD (5),
	MEDICAL_BAY (10),
	CLOAKING_GENERATOR (20),
	SECURITY (6),
	DIPLOMATIC_QUARTERS(5), //Provides space to host diplomatic missions in comfort
	RESEARCH_LAB(5),
	TACTICS_ROOM(5), //Battle bridge
	ASTROMETRICS_LAB(5),
	;
	
	private int cost;
	
	Amenity (int cost) {
		this.cost = cost;
	}
	
	public int getCost () {
		return cost;
	}
	
	@Override
	public String toString () {
		return MyStringUtils.capitalizeWords(super.toString());
	}
}