/**
 * @author Paul Dennis (pd236m)
 * May 10, 2018
 */
package sts_heuristics;

import java.util.List;

public class DeckReport {
	
	Hero hero;
	
	static boolean debug = false;
	
	//We're going to deal exclusively in doubles to make things easier
	public double level;
	public double numCards;
	public double maxHp;
	public double currentHp;
	public double averageHealPerCard;
	public double averageBlockPerCard;
	public double averageDamagePerCard;
	public double nonUpgradedCards;
	public double starterCardsUnupgraded;
	public double unupgradedNonStarterCards;
	
	public DeckReport (Hero hero) {
		this.level = hero.level;
		this.hero = hero;
		maxHp = hero.maxHealth;
		currentHp = hero.currentHealth;
		analyzeDeck();
	}

	public void analyzeDeck () {
		List<Card> deck = hero.deck;
		numCards = deck.size();
		int healAmount = deck.stream()
				.filter(card -> card.isHeal)
				.mapToInt(card -> card.magnitude)
				.sum();
		int blockAmount = deck.stream()
				.filter(card -> card.isBlock)
				.mapToInt(card -> card.magnitude)
				.sum();
		int damageAmount = deck.stream()
				.filter(card -> card.isAttack)
				.mapToInt(card -> card.magnitude)
				.sum();
		nonUpgradedCards = deck.stream()
				.filter(card -> !card.upgraded)
				.count();
		starterCardsUnupgraded = deck.stream()
				.filter(card -> card.level == 0)
				.count();
		unupgradedNonStarterCards = nonUpgradedCards - starterCardsUnupgraded;
		
		averageBlockPerCard = (double) blockAmount / (double) numCards;
		averageHealPerCard = (double) healAmount / (double) numCards;
		averageDamagePerCard = (double) damageAmount / (double) numCards;
		
		if (debug) {
			System.out.println("Analyzed this deck with " + numCards + " cards:");
			System.out.println("healAmount = " + healAmount + ", average = " + averageHealPerCard);
			System.out.println("blockAmount = " + blockAmount + ", average = " + averageBlockPerCard);
			System.out.println("damageAmount = " + damageAmount + ", average = " + averageDamagePerCard);
			System.out.println("nonUpgradedCards = " + nonUpgradedCards);
		}
	}
}
