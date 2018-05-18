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
			currentHealth -= amount;
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
		blockHp += amount;
	}
}
