/**
 * @author Paul Dennis
 * Jul 20, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

//Class representing all the star systems and tunnels
public class StarNetwork {
	
	private List<Starsystem> systems;
	private List<SpaceTunnel> tunnels;
	
	public StarNetwork (String foo) {
		systems = new ArrayList<>();
		tunnels = new ArrayList<>();
		systems.add(new Starsystem(10, 50, "Hyrule"));
		systems.add(new Starsystem(20, 50, "Royal Castle"));
		systems.add(new Starsystem(60, 60, "Lylat"));
		systems.add(new Starsystem(40, 40, "Corneria"));
		
		//tunnels.add(new SpaceTunnel(systems.get(0), systems.get(1)));
		//tunnels.add(new SpaceTunnel(systems.get(2), systems.get(3)));
		connect(systems.get(0), systems.get(1));
		connect(systems.get(2), systems.get(3));
	}
	
	public StarNetwork () {
		systems = new ArrayList<>();
		tunnels = new ArrayList<>();
		
		Random random = new Random();
		
		int numSystems = random.nextInt(5) + 5;
		
		List<CoordinatePair> usedPairs = new ArrayList<>();
		
		for (int i = 0; i < numSystems; i++) {
			int x = random.nextInt(100);
			int y = random.nextInt(100);
			
			//TODO: make sure this equality check works
			CoordinatePair pair = new CoordinatePair(x, y);
			if (usedPairs.contains(pair)) {
				i--;
				continue;
			}
			systems.add(new Starsystem(x, y));
		}
	}
	
	public SpaceTunnel makeConnection () {
		Random random = new Random();
		for (Starsystem system : systems) {
			for (Starsystem otherSystem : systems) {
				//Closer the systems are the more *likely* they are to be connected
				double distance = getDistance(system, otherSystem);
				if (distance == 0.0) {
					continue;
				}
				double chance = 10 / (distance * (system.getConnectedSystems().size() + 1));
				if (random.nextDouble() < chance) {
					return connect(system, otherSystem);
				}
			}
		}
		return null;
	}
	
	//TODO fix so that systems cant have multiple tunnels connecting them
	private SpaceTunnel connect (Starsystem first, Starsystem second) {
		if(first.isConnectedTo(second)) {
			return null;
		}
		SpaceTunnel tunnel = new SpaceTunnel(first, second);
		first.addTunnel(tunnel);
		second.addTunnel(tunnel);
		tunnels.add(tunnel);
		return tunnel;
	}
	
	public boolean isConnected () {
		Set<Starsystem> connectedSystems = new HashSet<>();
		addSystemToConnectedSet(connectedSystems, systems.get(0));
		return connectedSystems.size() == systems.size();
	}
	
	private void addSystemToConnectedSet (Set<Starsystem> connectedSystems, Starsystem newSystem) {
		boolean success = connectedSystems.add(newSystem);
		if (success) {
			List<Starsystem> connectedToNew = newSystem.getConnectedSystems();
			connectedToNew.forEach(system -> addSystemToConnectedSet(connectedSystems, system));
		}
	}
	
	public static double getDistance (Starsystem first, Starsystem second) {
		
		int xDist = Math.abs(second.getXCoord() - first.getXCoord());
		int yDist = Math.abs(second.getYCoord() - first.getYCoord());
		return Math.sqrt(xDist * xDist + yDist * yDist);
	}
	
	public List<Starsystem> getSystems () {
		return systems;
	}
	
	public List<SpaceTunnel> getTunnels () {
		return tunnels;
	}
}

class CoordinatePair {
	
	final int x;
	final int y;
	
	public CoordinatePair (int x, int y) {
		this.x = x;
		this.y = y;
	}
}