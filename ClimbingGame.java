/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClimbingGame {
	
	private Hero hero;
	private Monster monster;
	private int level;
	
	private List<Card> drawPile;
	private List<Card> discardPile;
	
	static Map<String, Card> newCardOptions;
	static Map<String, HeroAction> advUpgradeOptions;
	
	//0 - used for highest level output
	//1 - used for things that occur 1-2 times per game
	//2 - used for things that occur timers per round
	public static int OUTPUT_LEVEL = 0;
	private static boolean init = false;
	
	public ClimbingGame () {
		if (!init) {
			staticInit();
		}
		hero = new Hero();
		level = 1;
	}
	
	public ClimbingGame (AdaptiveStrategy strategy) {
		this();
		strategy.setHero(hero);
		hero.setStrategy(strategy);
		if (OUTPUT_LEVEL >= 2) {
			System.out.println("Starting new game with this strategy: " + strategy);
		}
	}
	
	public static void staticInit () {
		advUpgradeOptions = new HashMap<>();
		//Remove the least powerful card from the deck
		advUpgradeOptions.put("removeCard", hero -> {
			List<Card> deck = hero.getDeck();
			Collections.sort(deck);
			Card removed = deck.get(deck.size() - 1);
			//Last card should be the least powerful
			
			int nonExhaustCards = (int) deck.stream()
					.filter(card -> !card.isExhaust())
					.count();
			if ((removed.isExhaust() || nonExhaustCards >= 2) && (removed.getLevel() < 2)) {
				hero.removeCard(removed);
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\t+++Removed " + removed);
				}
			} else {
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\tCannot allow removal of last non-exhaust card" 
							+ " or level 2 cards. Did not remove: " + removed);
				}
			}
		});
		//Upgrade the most powerful un-upgraded card
		advUpgradeOptions.put("upgradeCard", hero -> {
			//This code is uglier than it should be because I'm not comfortable enough with ref/value and copying
			List<Card> deck = new ArrayList<>(hero.getDeck());
			deck.removeIf(Card::isUpgraded);
			if (deck.size() == 0) {
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("All cards already upgraded.");
				}
			} else {
				Collections.sort(deck);
				//First card is the highest level non-upgraded card
				Card upg = deck.get(0);
				boolean success = hero.removeCard(upg);
				if (!success) {
					throw new AssertionError("Failure temporarily removing card");
				}
				upg.upgrade();
				hero.addCard(upg);
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\t+++Upgraded " + upg);
				}
			}
		});
		//Add a new card to the deck
		//List<String> options = Arrays.asList("heal", "strikeDefend", "strikeExhaust", "healBlock");
		advUpgradeOptions.put("addCard", hero -> {
			//Ugly ugly code to take all available upgrade options and remove a random one
			List<String> regCardNames = Card.REGULAR_CARD_NAMES;
			List<String> powerCardNames = Card.POWER_CARD_NAMES;
			Collections.shuffle(regCardNames);
			Collections.shuffle(powerCardNames);
			List<String> options = new ArrayList<>();
			//In any given round we get to choose between two regular cards and a power.
			options.add(regCardNames.get(0));
			options.add(regCardNames.get(1));
			options.add(powerCardNames.get(0));
			if (hero.getLevel() >= 50) {
				options.add(regCardNames.get(2));
				options.add(powerCardNames.get(1));
			}
		
			if (OUTPUT_LEVEL >= 3) {
				System.out.println("\tChoices: " + options);
			}
			
			List<String> preferences = hero.getStrategy().getCardPrefs();
			Card chosen = null;
			for (String pref : preferences) {
				if (options.contains(pref)) {
					Card temp = Card.getCardMap().get(pref);
					if (temp == null) {
						throw new AssertionError("Bad card name chosen: " + pref);
					}
					chosen = new Card(temp);
					break;
				}
			}
			hero.addCard(chosen);
			if (OUTPUT_LEVEL >= 2) {
				System.out.println("\t+++Added " + chosen + " to deck.");
			}
			if (chosen.getLevel() > 1) {
				throw new AssertionError("New card cannot be already upgraded: " + chosen);
			}
		});
		//Increase the hero's max hitpoints
		advUpgradeOptions.put("maxHp", hero -> {
			hero.increaseMaxHp(3);
			if (OUTPUT_LEVEL >= 2) {
				System.out.println("\t+++Increased maxHp by 3");
			}
		});
		init = true;
	}

	public int playGame () {
		boolean keepGoing = true;
		while (keepGoing) {
			if (OUTPUT_LEVEL >= 2) {
				System.out.println("Level " + level + "\tHealth " + hero.getCurrentHealth() + "/" + hero.getMaxHealth());
			}
			monster = new Monster(level);
			drawPile = new ArrayList<>(hero.getDeck());
			discardPile = new ArrayList<>();
			Collections.shuffle(drawPile);
			int roundCount = 0;
			if (level == 10 || level == 25) {
				hero.incrementActionsPerRound();
			}
			//Combat
			while (true) {
				hero.setBlockHp(0);
				//Do two hero attacks
				for (int i = 0; i < hero.getNumActionsPerRound(); i++) {
					doCardEffects(drawCard());
				}
				if (OUTPUT_LEVEL >= 2) {
					System.out.println("\t-----");
				}
				if (monster.getHealth() <= 0) {
					if (OUTPUT_LEVEL >= 2) {
						System.out.println("\tMonster defeated.");
					}
					break;
				}
				
				//Do monster attack
				hero.takeDamage(monster.getDamage());
				if (monster.hasVulnerableAttacks()) {
					hero.increaseVulnerability(Monster.VULN_FACTOR);
				}
				if (monster.hasWeakeningAttacks()) {
					hero.increaseWeakness(Monster.WEAK_FACTOR);
				}
				if (monster.hasPoisonAttacks()) {
					hero.increasePoison();
				}
				monster.endRound();
				if (hero.getCurrentHealth() <= 0) {
					if (OUTPUT_LEVEL >= 1) {
						System.out.println("Hero died on level " + level + ".");
					}
					keepGoing = false;
					break; //Out of the individual round loop
				}
				if (roundCount >= 30) {
					if (OUTPUT_LEVEL >= 1) {
						System.out.println("Timed out on combat after 30+ rounds. Hero loses on level " + level + ".");
					}
					keepGoing = false;
					break;
				}
				roundCount++;
			}
			if (keepGoing) {
				hero.endCombat(level);
				//Restore HP
				if (level % 4 == 0) {
					hero.heal(4 + (level / 6));
				}
				
				//Add a new card
				level++;
				
				makeUpgradeDecision();
			} else {
				return level;
			}
		}//End while(keepGoing)
		throw new AssertionError("Should not reach this point.");
	}
	
	public void makeUpgradeDecision () {
		List<String> highLevelPrefs = hero.getStrategy().getHighLevelPrefs();
		List<String> advUpgradeOptionNames = advUpgradeOptions.keySet().stream().collect(Collectors.toList());
		Collections.shuffle(advUpgradeOptionNames);
		//Take out 2 options if level is under 50, otherwise just one
		advUpgradeOptionNames.remove(0);
		if (level < 50) {
			advUpgradeOptionNames.remove(0);
		}
		
		if (level == 45) {
			String bonusChoice = hero.getStrategy().getBonusChoice();
			//("extraCard", "strongerBlock", "bulkHpIncrease", "perTurnHpIncrease");
			switch (bonusChoice) {
				case "extraCard":
					hero.incrementActionsPerRound();
					break;
				case "strongerBlock":
					hero.enableStrongerBlocks();
					break;
				case "bulkHpIncrease":
					hero.increaseMaxHp(30);
					break;
				case "perTurnHpIncrease":
					hero.enableIncreaseHpPerRound();
					break;
				default:
					throw new AssertionError("Invalid bonus choice.");	
			}
			if (OUTPUT_LEVEL >= 1) {
				System.out.println("Added bonus for hero: " + bonusChoice);
			}
		}
		
		String choice = null;
		//Find the first available choice
		for (String s : highLevelPrefs) {
			if (advUpgradeOptionNames.contains(s)) {
				choice = s;
				break;
			}
		}
		if (OUTPUT_LEVEL >= 2) {
			System.out.println("\tOptions were: " + advUpgradeOptionNames);
			System.out.println("\tChoice was " + choice);
		}
		advUpgradeOptions.get(choice).doAction(hero);
	}
	
	public void doCardEffects (Card card) {
		if (OUTPUT_LEVEL >= 2) {
			System.out.println("\tDrew and played: " + card);
		}
		//Execute card properties. These are NOT mutually exclusive
		if (card.isPower()) {
			hero.addPower(card);
		} else {
			if (card.isAttack()) {
				int damageAmount = (int) ((card.getMagnitude() + hero.getStrength()) * hero.getWeakDamageFactor());
				monster.takeDamage(damageAmount);
			}
			if (card.isBlock()) {
				hero.addBlockHp(card.getMagnitude() + hero.getDexterity());
			}
			if (card.isHeal()) {
				hero.heal(card.getMagnitude());
			}
			if (!card.isExhaust()) { //If NOT exhaust
				discardPile.add(card);
			}
		}
	}
	
	public Card drawCard () {
		if (drawPile.size() == 0) {
			drawPile = new ArrayList<>(discardPile);
			discardPile = new ArrayList<>();
		}
		return drawPile.remove(0);
	}
}
