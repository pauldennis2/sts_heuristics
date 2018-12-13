/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

public enum MaterialType {
	
	HULL, //Material used to construct ship hulls
	SHIELD, //Material used to build shields
	WEAPON, //Material used to build weapons
	BUILDING_PARTS, //Material that can be used to aid in quick construction of buildings
	GREENSTONE, //Material used to work with Power Crystals
	DEVOTION_STONE, //Special stone used to declare devotion to Elements/Fundaments
	SHADOWCLOAK, //Material used to build cloaking devices
	MEDICINE, //Material used for med bays
	; 
	
	@Override
	public String toString () {
		String start = super.toString().toLowerCase().replaceAll("_", " ");
		return start.substring(0, 1).toUpperCase() + start.substring(1);
	}
	
	
}
