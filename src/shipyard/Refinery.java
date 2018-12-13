/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

//A building that allows the mining and processing of a given raw material
public class Refinery extends Building {
	
	private Material material;
	private double efficiency; //Represents free gains like productivity in Factorio
	
	public Refinery () {
		super(2);
	}
	
	@Override
	public void upgrade () {
		
	}
	
	@Override
	public void doTurn () {
		
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Material getMaterial () {
		return material;
	}
	
	public double getEfficiency () {
		return efficiency;
	}
}
