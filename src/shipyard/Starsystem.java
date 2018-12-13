/**
 * @author Paul Dennis
 * Jul 10, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Starsystem {
	
	private List<Planet> planets;
	private String name;
	
	private List<SpaceTunnel> tunnels;
	
	public static final String[] NAMES = {"Terra", "Beetlejuice", "Betelgeuse", "Pern", "Spira", "Wogas", "Amber",
			"Fomulhaut", "Freblinsee", "Connecticut", "Rhode Island", "Apocalypse", "Mara", "Chichu", "Morning", "Afternoon", "Evening", "Night"};
	
	public static final String[] NUMERALS = {"I", "II", "III", "IV", "V"};
	
	//Used only for purposes of drawing the map - no effect on gameplay
	private int xCoord;
	private int yCoord;
	
	public Starsystem (int xCoord, int yCoord) {
		Random random = new Random();
		tunnels = new ArrayList<>();
		name = NAMES[random.nextInt(NAMES.length - 1)];
		planets = new ArrayList<>();
		int numPlanets = random.nextInt(4);
		
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		
		for (int i = 0; i < numPlanets; i++) {
			planets.add(new Planet(name + " " + NUMERALS[i]));
		}
	}
	
	public Starsystem (int xCoord, int yCoord, String name) {
		this(xCoord, yCoord);
		this.name = name;
	}
	
	public List<Starsystem> getConnectedSystems () {
		List<Starsystem> connectedSystems = new ArrayList<>();
		for (SpaceTunnel tunnel: tunnels) {
			if (tunnel.getFirstSystem() == this) {
				connectedSystems.add(tunnel.getFirstSystem());
			}
			if (tunnel.getSecondSystem() != this) {
				connectedSystems.add(tunnel.getSecondSystem());
			}
		}
		return connectedSystems;
	}
	
	public boolean isConnectedTo (Starsystem otherSystem) {
		for (SpaceTunnel tunnel : tunnels) {
			if (tunnel.getFirstSystem() == otherSystem || tunnel.getSecondSystem() == otherSystem) {
				return true;
			}
		}
		return false;
	}
	
	public void addTunnel (SpaceTunnel tunnel) {
		tunnels.add(tunnel);
	}
	
	public String toString () {
		return name + "\n" + planets;
	}
	
	public static void main(String[] args) {
		System.out.println(new Starsystem(3, 2));
	}
	
	public int getXCoord () {
		return xCoord;
	}
	
	public int getYCoord () {
		return yCoord;
	}
	
	public String getName () {
		return name;
	}
}
