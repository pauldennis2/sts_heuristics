/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.List;


public class Hero {
	
	private List<Card> deck;
	private int currentHealth;
	private int maxHealth;
	private int blockHp;
	private AdaptiveStrategy strategy;
	
	private int numActionsPerRound;
	
	private int strength = 0;
	private int dexterity = 0;
	private int powerHealPerTurn = 0;
	private int powerBlockPerTurn = 0;
	private int powerStrPerTurn = 0;
	private int powerDexPerTurn = 0;
	
	private double vulnerableDamageFactor = 1.0;
	private double weakDamageFactor = 1.0;
	private int poisonCount = 0;
	
	private boolean statusImmune = false;
	
	private double strongerBlockFactor = 1.0;
	private int increaseMaxHpPerRound = 0;
	
	private int level = 1;
	
	public Hero () {
		maxHealth = 40;
		currentHealth = 40;
		blockHp = 0;
		numActionsPerRound = 2;
		deck = Card.getStartingDeck();
	}
	
	public Hero (AdaptiveStrategy strategy) {
		this();
		this.strategy = strategy;
	}
	
	public void takeDamage (int amount) {
		amount -= blockHp;
		if (amount > 0) {
			currentHealth -= amount * vulnerableDamageFactor;
			if (ClimbingGame.OUTPUT_LEVEL >= 2) {
				System.out.println("\tTook " + amount + " damage.");
			}
		}
	}
	
	public void heal (int amount) {
		currentHealth += amount;
		if (currentHealth > maxHealth) {
			currentHealth = maxHealth;
		}
		if (ClimbingGame.OUTPUT_LEVEL >= 2) {
			System.out.println("\tHealed " + amount);
		}
	}
	
	public void addPower (Card powerCard) {
		if (!powerCard.isPower()) {
			throw new AssertionError("You made an ass out of you and ertion.");
		}
		if (powerCard.getEffectType() == EffectType.HEAL_PER_TURN) {
			powerHealPerTurn += powerCard.getMagnitude();
		} else if (powerCard.getEffectType() == EffectType.BLOCK_PER_TURN) {
			powerBlockPerTurn += powerCard.getMagnitude();
		} else if (powerCard.getEffectType() == EffectType.STATUS_IMMUNE) {
			statusImmune = true;
			if (powerCard.isUpgraded()) {
				vulnerableDamageFactor = 1.0;
				weakDamageFactor = 1.0;
				poisonCount = 0;
			}
		}
		if (ClimbingGame.OUTPUT_LEVEL >= 4) {
			System.out.println("\tAdded new power: " + powerCard);
		}
	}
	
	//Called at the start of each round, responsible for handling powers
	public void startRound () {
		heal(powerHealPerTurn);
		blockHp += powerBlockPerTurn;
		strength += powerStrPerTurn;
		dexterity += powerDexPerTurn;
		takeDamage(poisonCount);
	}
	
	//Called after combat is over to reset stats
	public void endCombat (int level) {
		if (ClimbingGame.OUTPUT_LEVEL >= 4) {
			System.out.println("\tStats reset.");
		}
		strength = 0;
		dexterity = 0;
		powerHealPerTurn = 0;
		powerBlockPerTurn = 0;
		powerStrPerTurn = 0;
		powerDexPerTurn = 0;
		
		statusImmune = false;
		
		if (level < 50) {
			vulnerableDamageFactor = 1.0;
			weakDamageFactor = 1.0;
			poisonCount = 0;
		}
		this.level = level;
		increaseMaxHp(increaseMaxHpPerRound);
	}
	
	public void increaseVulnerability (double amount) {
		String message;
		if (!statusImmune) {
			vulnerableDamageFactor += amount;
			message = "\tHero becomes more vulnerable to attacks. Now taking " + vulnerableDamageFactor + " of base.";
		} else {
			message = "\tHero dodges vulnerability increase with Status Immune Ribbon";
		}
		if (ClimbingGame.OUTPUT_LEVEL >= 4) {
			System.out.println(message);
		}
	}
	
	public void increaseWeakness (double amount) {
		String message;
		if (!statusImmune) {
			if (weakDamageFactor > 0.1) {
				weakDamageFactor -= amount;
				if (weakDamageFactor < 0.1) {
					weakDamageFactor = 0.1;
				}
				message = "\tHero's attacks become weaker. Now dealing " + weakDamageFactor + " of base.";
			} else {
				message = "\tHero cannot become any more weak.";
			}
		} else {
			message = "\tHero dodges weakness increase with Status Immune Ribbon";
		}
		if (ClimbingGame.OUTPUT_LEVEL >= 4) {
			System.out.println(message);
		}
	}
	
	public void increasePoison () {
		String message;
		if (!statusImmune) {
			poisonCount++;
			message = "\tHero was poisoned. Now at " + poisonCount + " poison.";
		} else {
			message = "\tHero dodges poison effect with Status Immune Ribbon";
		}
		if (ClimbingGame.OUTPUT_LEVEL >= 4) {
			System.out.println(message);
		}
	}
	
	public void increaseMaxHp (int amount) {
		currentHealth += amount;
		maxHealth += amount;
		if (ClimbingGame.OUTPUT_LEVEL >= 2 && amount != 0) {
			System.out.println("\tHero's max hp increased by " + amount + ".");
		}
	}
	
	public int getCurrentHealth () {
		return currentHealth;
	}
	
	public int getMaxHealth () {
		return maxHealth;
	}
	
	public int getBlockHp () {
		return blockHp;
	}
	
	public List<Card> getDeck () {
		return deck;
	}
	
	public void addCard (Card card) {
		deck.add(card);
	}
	
	public boolean removeCard (Card card) {
		return deck.remove(card);
	}
	
	public AdaptiveStrategy getStrategy () {
		return strategy;
	}
	
	public void setBlockHp (int amount) {
		blockHp = amount;
	}
	
	public void addBlockHp (int amount) {
		blockHp += (amount + dexterity) * strongerBlockFactor;
	}
	
	public int getStrength () {
		return strength;
	}
	
	public int getDexterity () {
		return dexterity;
	}
	
	public double getWeakDamageFactor () {
		return weakDamageFactor;
	}
	
	public void setStrategy (AdaptiveStrategy strategy) {
		this.strategy = strategy;
	}
	
	public int getNumActionsPerRound () {
		return numActionsPerRound;
	}
	
	public void incrementActionsPerRound () {
		if (ClimbingGame.OUTPUT_LEVEL >= 1) {
			System.out.println("\tHero gets an extra action per round.");
		}
		numActionsPerRound++;
	}
	
	public void enableStrongerBlocks () {
		if (ClimbingGame.OUTPUT_LEVEL >= 1) {
			System.out.println("\tHero's blocks become 30% stronger.");
		}
		strongerBlockFactor = 1.3;
	}
	
	public void enableIncreaseHpPerRound () {
		if (ClimbingGame.OUTPUT_LEVEL >= 1) {
			System.out.println("\tHero now gaining 2 max hp per round.");
		}
		increaseMaxHpPerRound = 2;
	}
	
	public int getLevel () {
		return level;
	}
}
