/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import static shipyard.MaterialType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shipyard {
	
	private List<Starship> ships;
	
	private Storage storage;
	
	Random random;
	
	public static final String[] NAMES = {"Enterprise", "Voyager", "Dauntless", "Perilous", "Valiant"};
	
	public Shipyard () {
		ships = new ArrayList<>();
		storage = new Storage(10000);
		random = new Random();
		
		storage.addMaterials(new Material(HULL, 0), 200);
		storage.addMaterials(new Material(HULL, 1), 100);
		storage.addMaterials(new Material(HULL, 2), 50);
		
		
		storage.addMaterials(new Material(SHIELD, 0), 2);
		storage.addMaterials(new Material(SHIELD, 1), 1);
		
		storage.addMaterials(new Material(WEAPON, 0), 5);
		storage.addMaterials(new Material(WEAPON, 1), 2);
		PowerCrystal first = new PowerCrystal();
		storage.addCrystal(first);
		storage.addCrystal(new PowerCrystal());
	}
	
	public Storage getStorage () {
		return storage;
	}
	
	public boolean createCrystal () {
		storage.addCrystal(new PowerCrystal());
		return true;
	}
	
	public boolean createPart (ShipPart part) {
		System.out.println("Attempting to create: " + part);
		int amountRequired;
		if (part.getMaterial().getType() == MaterialType.HULL) {
			amountRequired = part.getSize().getSize();
		} else {
			amountRequired = 1;
		}
		Material mat = part.getMaterial();
		if (storage.getCount(mat) >= amountRequired) {
			storage.addPart(part);
			storage.removeMaterials(mat, amountRequired);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean createShip (ShipPart hull, PowerCrystal powerCrystal) {
		if (hull.getMaterial().getType() != HULL) {
			throw new AssertionError("Cannot create a ship without a hull");
		}
		String name = NAMES[random.nextInt(NAMES.length - 1)];
		storage.removeCrystal(powerCrystal);
		storage.removePart(hull);
		Starship ship = new Starship(name, powerCrystal, hull);
		ships.add(ship);
		return true;
	}
	
	public void printInventory () {
		System.out.println("Here is your available inventory:");
		System.out.println(storage);
		System.out.println("Ships:");
		ships.forEach(ship -> System.out.println("\t" + ship));
	}
}
