/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static shipyard.MaterialType.*;

public class Planet {
	
	private String name;
	private PlanetaryDefense planetaryDefenses;
	private List<Building> buildings;
	private CountMap<Material> resources;
	
	private ElementalDevotion elementalShrine;
	private FundamentalDevotion fundamentalShrine;
	
	Storage storage;
	
	public static final String[] NAMES = {"Terra", "Beetlejuice", "Betelgeuse", "Pern", "Spira", "Wogas", "Amber"};
	
	//TODO fix duplicated constructors
	public Planet () {
		buildings = new ArrayList<>();
		resources = new CountMap<>();
		storage = new Storage(200);
		
		Random random = new Random();
		
		name = NAMES[random.nextInt(NAMES.length - 1)];
		
		Material iron = new Material(HULL, 0);
		resources.add(iron, random.nextInt(1000) + 500);
		
		Material titanium = new Material(HULL, 1);
		resources.add(titanium, random.nextInt(150) + 50);
		
		Material l1Weap = new Material(WEAPON, 0);
		resources.add(l1Weap, random.nextInt(50) + 25);
		
		Material l2Weap = new Material(WEAPON, 1);
		resources.add(l2Weap, random.nextInt(3) + 3);
		
		Material l1Shield = new Material(SHIELD, 0);
		resources.add(l1Shield, random.nextInt(10) + 20);
		
		Material l2Shield = new Material(SHIELD, 1);
		resources.add(l2Shield, random.nextInt(2) + 2);
		
		Material l3;
		if (random.nextBoolean()) {
			l3 = new Material(WEAPON, 2);
		} else {
			l3 = new Material(SHIELD, 2);
		}
		resources.add(l3, random.nextInt(3) + 2);
		
		Material l3Metal = new Material(HULL, 2);
		resources.add(l3Metal, random.nextInt(80) + 20);
		
		if (random.nextBoolean()) {
			storage.addMaterials(new Material(DEVOTION_STONE, 0), 1);
		}
		if (random.nextDouble() > 0.75) {
			elementalShrine = ElementalDevotion.values()[random.nextInt(4)];
		} else if (random.nextDouble() > 0.8) {
			fundamentalShrine = FundamentalDevotion.values()[random.nextInt(4)];
		}
	}
	
	public Planet (String name) {
		this();
		
		this.name = name;
	}
	
	@Override
	public String toString () {
		return name + resources;
	}
	
	public ElementalDevotion getElementalShrine () {
		return elementalShrine;
	}
	
	public FundamentalDevotion getFundamentalShrine () {
		return fundamentalShrine;
	}
	
	public PlanetaryDefense getPlanetaryDefenses () {
		return planetaryDefenses;
	}
	
	public List<Building> getBuildings () {
		return buildings;
	}
}

class PlanetaryDefense {
	
	ShipPart shield;
	List<ShipPart> weapons;
}
