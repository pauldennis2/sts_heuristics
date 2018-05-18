/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.ArrayList;
import java.util.List;

import static sts_heuristics.EffectType.*;

public class Hero {
	
	List<Card> deck;
	int currentHealth;
	int maxHealth;
	int blockHp;
	
	AdaptiveStrategy strategy;
	
	int level;
	
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
		
		strategy = new AdaptiveStrategy(this);
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
}
