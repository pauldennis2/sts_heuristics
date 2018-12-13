/**
 * @author Paul Dennis
 * Jul 18, 2018
 */
package shipyard;

import static shipyard.ElementalDevotion.AIR;
import static shipyard.ElementalDevotion.EARTH;
import static shipyard.ElementalDevotion.FIRE;
import static shipyard.ElementalDevotion.WATER;
import static shipyard.FundamentalDevotion.CHAOS;
import static shipyard.FundamentalDevotion.DARK;
import static shipyard.FundamentalDevotion.LIGHT;
import static shipyard.FundamentalDevotion.ORDER;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/*
 * Class to hold all the static methods/menus created to work with the
 * text-based starship interface.
 */
public class StarshipManager {
	
	private static SafeScanner safeScanner;
	private static Starship ship;
	private static Scanner scanner;
	
	public static void main(String[] args) {
		PowerCrystalTrait.initialize();
		System.out.println("Welcome to Ship and Crystal management.");
		scanner = new Scanner(System.in);
		safeScanner = new SafeScanner();
		System.out.println("Let's create a new ship. What would you like to name it?");
		String shipName = scanner.nextLine();
		System.out.println("OK. I'll need to create a Power Crystal for that ship. Provide a name for the Crystal?");
		String crystalName = scanner.nextLine();
		ShipPart titaniumHull = new ShipPart(new Material(MaterialType.HULL, 1), ShipClass.CRUISER);
		PowerCrystal powerCrystal = new PowerCrystal(crystalName);
		ship = new Starship(shipName, powerCrystal, titaniumHull);
		System.out.println(ship);
		System.out.println("Adding crew...");
		ship.addCrew(new Crew(0, 1.0, 20, ShipClass.CRUISER));
		
		shipActionsMenu();
		
		scanner.close();
		safeScanner.close();
	}
	
	public static void shipActionsMenu () {
		ship.printDetails();
		System.out.println("Ship Actions - What would you like to do?");
		System.out.println("0. Exit");
		System.out.println("1. Level up the power crystal");
		System.out.println("2. Make changes to the power crystal");
		System.out.println("3. Set a mission");
		System.out.println("4. Level up crew");
		
		int response = safeScanner.getSafeInt(4);
		
		switch (response) {
		case 0:
			System.out.println("Exiting...");
			return;
		case 1:
			System.out.println("How many times?");
			int numTimes = safeScanner.getSafeInt();
			ship.getPowerCrystal().levelUpMatrix(numTimes);
			break;
		case 2:
			powerCrystalMenu();
			break;
		case 3:
			setMissionMenu();
			break;
		case 4:
			ship.getCrew().levelUp();
			break;
		}
		shipActionsMenu();
	}
	
	public static void setMissionMenu () {
		System.out.println("What mission type?");
		MissionType[] values = MissionType.values();
		for (int i = 0; i < values.length; i++) {
			System.out.println((i + 1) + ". " + values[i]);
		}
		safeScanner = new SafeScanner();
		int response = safeScanner.getSafeInt(values.length);
		System.out.println("Ok, setting to : " + values[response - 1]);
		ship.setMission(values[response - 1]);
	}
	
	public static void powerCrystalMenu () {
		System.out.println("Power Crystal Maintenance Chamber - What would you like to do?");
		System.out.println("0. Return to previous menu");
		System.out.println("1. Add a new trait");
		System.out.println(ship.getPowerCrystal().getElementalDevotion() == null 
				? "2. Add elemental devotion" : "2. Remove elemental devotion");
		System.out.println(ship.getPowerCrystal().getFundamentalDevotion() == null 
				? "3. Add fundamental devotion" : "3. Remove fundamental devotion");
		System.out.println("4. Charge the Crystal (enable stacking)");
		
		int response = safeScanner.getSafeInt(4);
		PowerCrystal powerCrystal = ship.getPowerCrystal();
		
		switch (response) {
		case 0:
			return;
		case 1:
			addTraitMenu();
			break;
		case 2:
			if (powerCrystal.getElementalDevotion() == null) {
				addElementalDevotionMenu();
			} else {
				System.out.println("Removing elemental devotion (" + powerCrystal.getElementalDevotion() + ")");
				powerCrystal.setElementalDevotion(null);
			}
			break;
		case 3:
			if (ship.getPowerCrystal().getFundamentalDevotion() == null) {
				addFundamentalDevotionMenu();
			} else {
				System.out.println("Removing fundamental devotio (" + powerCrystal.getFundamentalDevotion() + ")");
				powerCrystal.setFundamentalDevotion(null);
			}
			break;
		case 4:
			powerCrystal.enableStacking();
			break;
		}
		powerCrystalMenu();
	}
	
	public static void addTraitMenu () {
		
		List<PowerCrystalTrait> traits = Arrays.asList(PowerCrystalTrait.values());
		
		PowerCrystal powerCrystal = ship.getPowerCrystal();
		
		List<PowerCrystalTrait> possibleTraits = traits.stream()
			.filter(trait -> trait.evaluatePrereqs(powerCrystal))
			.collect(Collectors.toList());
		
		List<PowerCrystalTrait> missingPrereqs = traits.stream()
			.filter(trait -> !trait.evaluatePrereqs(powerCrystal))
			.collect(Collectors.toList());
		
		possibleTraits.sort((t1, t2) -> {
			return t1.getCost() - t2.getCost();
		});
		
		System.out.println("---\n(Prereqs not met)");
		missingPrereqs.forEach(trait -> System.out.println(trait + " - " + trait.evaluatePrereqsAndReturnReason(powerCrystal)));
		int i = 1;
		System.out.println("What trait to add?");
		System.out.println("0. Cancel");
		for (PowerCrystalTrait trait : possibleTraits) {
			System.out.println(i + ". " + trait + " - Cost: " + trait.getCost());
			i++;
		}
		int response = safeScanner.getSafeInt(possibleTraits.size());
		if (response != 0) {
			PowerCrystalTrait trait = possibleTraits.get(response - 1);
			
			if (powerCrystal.getAvailablePerkPoints() >= trait.getCost()) {
				System.out.println("adding " + trait);
				powerCrystal.purchaseTrait(trait);
			} else {
				System.out.println("Insufficient points to acquire that trait");
			}
		}
	}
	
	public static void addElementalDevotionMenu () {
		System.out.println("Which elemental devotion to add?");
		System.out.println("0. Cancel");
		System.out.println("1. Air");
		System.out.println("2. Water");
		System.out.println("3. Earth");
		System.out.println("4. Fire");
		
		int response = safeScanner.getSafeInt(4);
		PowerCrystal powerCrystal = ship.getPowerCrystal();
		switch (response) {
		case 0:
			return;
		case 1:
			powerCrystal.setElementalDevotion(AIR);
			break;
		case 2:
			powerCrystal.setElementalDevotion(WATER);
			break;
		case 3:
			powerCrystal.setElementalDevotion(EARTH);
			break;
		case 4:
			powerCrystal.setElementalDevotion(FIRE);
			break;
		}
	}
	
	public static void addFundamentalDevotionMenu () {
		System.out.println("Which fundamental devotion to add?");
		System.out.println("0. Cancel");
		System.out.println("1. Chaos");
		System.out.println("2. Dark");
		System.out.println("3. Order");
		System.out.println("4. Light");
		
		int response = safeScanner.getSafeInt(4);
		PowerCrystal powerCrystal = ship.getPowerCrystal();
		switch (response) {
		case 0:
			return;
		case 1:
			powerCrystal.setFundamentalDevotion(CHAOS);
			break;
		case 2:
			powerCrystal.setFundamentalDevotion(DARK);
			break;
		case 3:
			powerCrystal.setFundamentalDevotion(ORDER);
			break;
		case 4:
			powerCrystal.setFundamentalDevotion(LIGHT);
			break;
		}
	}
}
