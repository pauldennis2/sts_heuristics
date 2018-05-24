/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.Random;
import java.util.Scanner;

public class Monster {
	
	public static final double VULN_FACTOR = 0.1;
	public static final double WEAK_FACTOR = 0.05;

	private int health;
	private int damage;
	
	private int strength;
	
	//Power information
	private int damageReduction = 0;
	private int healPerTurn = 0;
	private int strIncreasePerTurn = 0;
	private double hpGrowthFactor = 1.0;
	private boolean hasWeakeningAttacks = false;
	private boolean hasVulnerableAttacks = false;
	private boolean hasPoisonAttacks = false;
	
	private int level;
	
	public Monster (int level) {
		this.level = level;
		Random random = new Random();
		if (level <= 8) {
			damage = level;
		} else {
			damage = 8 + (level - 8) / 2;
		}
		if (level >= 10) {
			addAbility();
		}
		if (level >= 25) {
			addAbility();
		}
		if (level >= 40) {
			addAbility();
		}
		if (level >= 60) {
			addAbility();
		}
		if (level >= 10) {
			//Should smooth out randomness a bit
			health = 10 + random.nextInt(level / 2) + random.nextInt(level / 2) + random.nextInt(level / 2) + random.nextInt(level / 2);
		} else {
			health = 10 + random.nextInt(level) + random.nextInt(level);
		}
	}
	
	public static void testMonsterCreation () {
		Scanner inputScanner = new Scanner(System.in);
		while (true) {
			System.out.println("Enter level (any non-number to exit).");
			String input = inputScanner.nextLine();
			try {
				int level = Integer.parseInt(input);
				Monster monster = new Monster(level);
				System.out.println(monster);
			} catch (NumberFormatException ex) {
				System.out.println("Exiting.");
				break;
			}
		}
		inputScanner.close();
	}
	
	private void addAbility () {
		Random r = new Random();
		int highLevelAdjustment = 0;
		if (level >= 50) {
			highLevelAdjustment = 1;
		}
		switch (r.nextInt(7)) {
			case 0:
				damageReduction += 1 + highLevelAdjustment;
				break;
			case 1:
				healPerTurn += 2 + highLevelAdjustment;
				break;
			case 2:
				strIncreasePerTurn = 1;
				break;
			case 3:
				hpGrowthFactor += 0.1;
				break;
			case 4:
				if (hasVulnerableAttacks) {
					addAbility();
				} else {
					hasVulnerableAttacks = true;
				}
				break;
			case 5:
				if (hasWeakeningAttacks) {
					addAbility();
				} else {
					hasWeakeningAttacks = true;
				}
				break;
			case 6:
				if (hasPoisonAttacks) {
					addAbility();
				} else {
					hasPoisonAttacks = true;
				}
				break;
		}
	}
	
	public void takeDamage (int amount) {
		amount -= damageReduction;
		if (amount > 0) {
			health -= amount;
		}
	}
	
	public int getHealth () {
		return health;
	}
	
	public int getDamage () {
		return damage + strength;
	}
	
	public void endRound () {
		health += healPerTurn; //Monsters don't have max health (their hp can grow without limit)
		health *= hpGrowthFactor;
		strength += strIncreasePerTurn;
	}
	
	@Override
	public String toString () {
		String response = "Damage: " + damage + ", Health: " + health;
		if (strength > 0) {
			response += ", Strength: " + strength;
		}
		if (strIncreasePerTurn > 0) {
			response += ", Str per turn: " + strIncreasePerTurn;
		}
		if (hpGrowthFactor > 1.0) {
			response += ", HP growth: " + hpGrowthFactor;
		}
		if (hasVulnerableAttacks) {
			response += ", Vulnerable Strikes";
		}
		if (hasWeakeningAttacks) {
			response += ", Weakening Strikes";
		}
		if (damageReduction > 0) {
			response += ", Damage Reduction: " + damageReduction;
		}
		if (healPerTurn > 0) {
			response += ", Regen: " + healPerTurn;
		}
		return response;
	}
	
	public boolean hasVulnerableAttacks () {
		return hasVulnerableAttacks;
	}
	
	public boolean hasWeakeningAttacks () {
		return hasWeakeningAttacks;
	}
	
	public boolean hasPoisonAttacks () {
		return hasPoisonAttacks;
	}
}
