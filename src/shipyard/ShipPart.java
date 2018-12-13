/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

//This class isn't very well defined. could use some work
public class ShipPart {

	private Material material;
	private ShipClass hullSize;
	
	public ShipPart (Material material) {
		this.material = material;
	}
	
	public ShipPart(Material material, ShipClass size) {
		this.material = material;
		this.hullSize = size;
	}

	public String getName () {
		String response = material + " ";
		if (hullSize != null) {
			response += hullSize.toString() + " ";
		}
		response += material.getType().toString();
		return response;
	}
	
	public String toString () {
		return getName();
	}
	
	public Material getMaterial () {
		return material;
	}
	
	public ShipClass getSize () {
		return hullSize;
	}
}
