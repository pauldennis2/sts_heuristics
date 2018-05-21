/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.ArrayList;
import java.util.List;

import static sts_heuristics.EffectType.*;

public class Hero {
	
	private List<Card> deck;
	private int currentHealth;
	private int maxHealth;
	private int blockHp;
	private AdaptiveStrategy strategy;
	
	private int strength = 0;
	private int dexterity = 0;
	private int powerHealPerTurn = 0;
	private int powerBlockPerTurn = 0;
	private int powerStrPerTurn = 0;
	private int powerDexPerTurn = 0;
	
	private double vulnerableDamageFactor = 1.0;
	private double weakDamageFactor = 1.0;
	
	private boolean statusImmune = false;
	
	public Hero () {
		maxHealth = 40;
		currentHealth = 40;
		blockHp = 0;
		
		deck = new ArrayList<>();
		deck.add(new Card(5, ATTACK));
		deck.add(new Card(5, ATTACK));
		deck.add(new Card(5, ATTACK));
		
		deck.add(new Card(5, BLOCK));
		deck.add(new Card(5, BLOCK));
		deck.add(new Card(5, BLOCK));
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
		}
		if (ClimbingGame.OUTPUT_LEVEL >= 4) {
			System.out.println("Added new power: " + powerCard);
		}
	}
	
	//Called at the start of each round, responsible for handling powers
	public void startRound () {
		heal(powerHealPerTurn);
		blockHp += powerBlockPerTurn;
		strength += powerStrPerTurn;
		dexterity += powerDexPerTurn;
	}
	
	//Called after combat is over to reset stats
	public void endCombat () {
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
		
		vulnerableDamageFactor = 1.0;
		weakDamageFactor = 1.0;
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
	
	public void increaseMaxHp (int amount) {
		currentHealth += amount;
		maxHealth += amount;
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
		blockHp += amount + dexterity;
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
}
