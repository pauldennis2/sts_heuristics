/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.List;

public class GalacticEmpire {
	
	Shipyard shipyard;
	List<Planet> planets;
	
	static GalacticEmpire empire;
	static SafeScanner scanner;
	static int turn;
	
	public GalacticEmpire () {
		shipyard = new Shipyard();
		planets = new ArrayList<>();
	}
	
	public void addPlanet () {
		planets.add(new Planet());
	}
	
	public static void main(String[] args) {
		scanner = new SafeScanner();
		turn = 1;
		
		empire = new GalacticEmpire(); 
	}
	
	public static void freeActionMenu () {
		System.out.println("What would you like to do?");
		System.out.println("0. Return to main menu");
		System.out.println("1. View power crystals.");
		System.out.println("2. Go to shipyard");
		int response = scanner.getSafeInt(2);
		switch (response) {
		case 0:
			break;
		case 1:
			powerCrystalMenu();
			break;
		case 2:
			shipyardMenu();
			break;
		}
	}
	
	public static void shipyardMenu () {
		System.out.println("You're at the shipyard. Here is your inventory:");
		empire.shipyard.printInventory();
	}
	
	public static void powerCrystalMenu () {
		System.out.println("Here are your Power Crystals:");
		List<PowerCrystal> crystals = empire.shipyard.getStorage().getPowerCrystals();
		for (int i = 0; i < crystals.size(); i++) {
			System.out.println((i + 1) + ". " + crystals.get(i));
		}
		int response = scanner.getSafeInt(crystals.size()) - 1;
		PowerCrystal crystal = crystals.get(response);
		crystal.printAllInfo();
		if (crystal.getAvailablePerkPoints() > 0) {
			System.out.println("What would you like to do?");
			System.out.println("0. Return");
			System.out.println("1. Add a trait");
			int response2 = scanner.getSafeInt(1);
			switch (response2) {
			case 0:
				break;
			case 1:
				System.out.println("Possible traits:");
				
				break;
			}
		} else {
			System.out.println("Returning (no options available)");
		}
	}
	
	public static void turnOptionMenu () {
		System.out.println("Turn " + turn);
		System.out.println("What would you like to do this turn?");
		System.out.println("1. Add a new planet");
		System.out.println("2. Create a power crystal");
		System.out.println("3. Build a building");
		System.out.println("4. Transfer resources");
		System.out.println("0. Exit");
		int response = scanner.getSafeInt(4);
		switch (response) {
		case 0:
			System.out.println("Exiting");
			break;
		case 1:
			empire.addPlanet();
			break;
		case 2:
			empire.shipyard.createCrystal();
			break;
		case 3:
			//Building
			break;
		case 4:
			//Transfer resource
			break;
		}
	}
}
