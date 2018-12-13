/**
 * @author Paul Dennis
 * Jul 20, 2018
 */
package shipyard;

public class SpaceTunnel {
	
	private Starsystem firstSystem;
	private Starsystem secondSystem;
	private TunnelType type;
	
	private int length;
	
	public SpaceTunnel (Starsystem first, Starsystem second) {
		this.firstSystem = first;
		this.secondSystem = second;
		
		int length = (int) (StarNetwork.getDistance(firstSystem, secondSystem) * 100);
	}
	
	public SpaceTunnel (Starsystem first, Starsystem second, TunnelType type) {
		this (first, second);
		this.type = type;
	}
	
	public Starsystem getFirstSystem () {
		return firstSystem;
	}
	
	public Starsystem getSecondSystem () {
		return secondSystem;
	}
	
	public TunnelType getType () {
		return type;
	}
}
enum TunnelType {
	GREEN,
	BLUE,
	RED;
}
