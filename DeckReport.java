/**
 * @author Paul Dennis (pd236m)
 * May 10, 2018
 */
package sts_heuristics;

import java.util.List;
import java.util.stream.Collectors;

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
	
	//Power fields
	public double numPowers;
	public double numRibbons;
	public double powerStaticStr;
	public double powerStaticDex;
	public double powerStrPerTurn;
	public double powerDexPerTurn;
	public double powerBlockPerTurn;
	public double powerHealPerTurn;
	
	public DeckReport (Hero hero) {
		this.hero = hero;
		maxHp = hero.getMaxHealth();
		currentHp = hero.getCurrentHealth();
		analyzeDeck();
	}

	public void analyzeDeck () {
		List<Card> deck = hero.getDeck();
		numCards = deck.size();
		int healAmount = deck.stream()
				.filter(Card::isHeal)
				.mapToInt(Card::getMagnitude)
				.sum();
		int blockAmount = deck.stream()
				.filter(Card::isBlock)
				.mapToInt(Card::getMagnitude)
				.sum();
		int damageAmount = deck.stream()
				.filter(Card::isAttack)
				.mapToInt(Card::getMagnitude)
				.sum();
		nonUpgradedCards = deck.stream()
				.filter(card -> !card.isUpgraded())
				.count();
		starterCardsUnupgraded = deck.stream()
				.filter(card -> card.getLevel() == 0)
				.count();
		unupgradedNonStarterCards = nonUpgradedCards - starterCardsUnupgraded;
		
		averageBlockPerCard = (double) blockAmount / (double) numCards;
		averageHealPerCard = (double) healAmount / (double) numCards;
		averageDamagePerCard = (double) damageAmount / (double) numCards;
		
		List<Card> powers = deck.stream().filter(Card::isPower).collect(Collectors.toList());
		numPowers = powers.size();
		List<Card> statIncreasePowers = powers.stream().filter(Card::isStatIncrease).collect(Collectors.toList());
		for (Card card : statIncreasePowers) {
			if (card.isStrengthIncrease()) {
				if (card.isPerTurnStatIncreasePower()) {
					powerStrPerTurn += card.getMagnitude();
				} else {
					powerStaticStr += card.getMagnitude();
				}
			} else {
				if (card.isPerTurnStatIncreasePower()) {
					powerDexPerTurn += card.getMagnitude();
				} else {
					powerStaticDex += card.getMagnitude();
				}
			}
		}
		for (Card card : powers) {
			if (card.isStatusImmunity()) {
				numRibbons++;
			}
			if (card.getEffectType() == EffectType.HEAL_PER_TURN) {
				powerHealPerTurn += card.getMagnitude();
			}
			if (card.getEffectType() == EffectType.BLOCK_PER_TURN) {
				powerBlockPerTurn += card.getMagnitude();
			}
		}
		
		if (debug) {
			System.out.println("Analyzed this deck with " + numCards + " cards:");
			System.out.println("healAmount = " + healAmount + ", average = " + averageHealPerCard);
			System.out.println("blockAmount = " + blockAmount + ", average = " + averageBlockPerCard);
			System.out.println("damageAmount = " + damageAmount + ", average = " + averageDamagePerCard);
			System.out.println("nonUpgradedCards = " + nonUpgradedCards);
		}
	}
}
