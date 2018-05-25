/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import static sts_heuristics.EffectType.ATTACK;
import static sts_heuristics.EffectType.BLOCK;
import static sts_heuristics.EffectType.BLOCK_AND_ATTACK;
import static sts_heuristics.EffectType.HEAL;
import static sts_heuristics.EffectType.HEAL_AND_BLOCK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Card implements Comparable<Card>{
	
	private int magnitude;
	private boolean isAttack;
	private boolean isBlock;
	private boolean isHeal;
	private boolean isExhaust;
	private boolean isPower;
	private boolean isStatusImmunity;
	
	private boolean isStatIncrease;
	private boolean isStrengthIncrease;
	private boolean isPerTurnStatIncreasePower;
	
	private boolean upgraded;
	
	private EffectType effectType;
	
	//Represents the level of a card
	//0 - Basic starter card
	//1 - Nonstarter card or upgraded starter
	//2 - Upgraded non-starter card
	private int level;
	
	//Almost all other magnitudes are based on this one
	public static final int DEFAULT_STRIKE_BLOCK_MAGNITUDE = 6;
	
	public static final List<String> REGULAR_CARD_NAMES = Arrays.asList("heal", "strikeDefend", "strikeExhaust", "healBlock");
	public static final List<String> POWER_CARD_NAMES = Arrays.asList("strPerTurn", "dexPerTurn", "staticStr", "staticDex",
			"healPerTurn", "blockPerTurn", "ribbon");
	
	public static final List<String> ALL_CARD_NAMES = Arrays.asList("heal", "strikeDefend", "strikeExhaust", "healBlock",
			"strPerTurn", "dexPerTurn", "staticStr", "staticDex", "healPerTurn", "blockPerTurn", "ribbon");
	
	public Card (Card copy) {
		this.magnitude = copy.magnitude;
		this.isAttack = copy.isAttack;
		this.isBlock = copy.isBlock;
		this.isHeal = copy.isHeal;
		this.isExhaust = copy.isExhaust;
		this.level = copy.level;
		this.upgraded = copy.upgraded;
		
		this.isPower = copy.isPower;
		this.isStatusImmunity = copy.isStatusImmunity;
		this.isStatIncrease = copy.isStatIncrease;
		this.isStrengthIncrease = copy.isStrengthIncrease;
		
		this.effectType = copy.effectType;
	}
	
	public static List<Card> getStartingDeck () {
		List<Card> deck = new ArrayList<>();
		deck.add(new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE, ATTACK));
		deck.add(new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE, ATTACK));
		deck.add(new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE, ATTACK));
		
		deck.add(new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE, BLOCK));
		deck.add(new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE, BLOCK));
		deck.add(new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE, BLOCK));
		
		return deck;
	}
	
	private static final Map<String, Card> CARD_MAP = new HashMap<>();
	private static boolean mapInit = false;
	//Just a design sandbox for the moment
	public static Map<String, Card> getCardMap () {
		//Map<String, Card> powers = new HashMap<>();
		if (!mapInit) {
			CARD_MAP.put("strPerTurn", new Card(1, EffectType.STR_PER_TURN, 1, true));
			CARD_MAP.put("dexPerTurn", new Card(1, EffectType.DEX_PER_TURN, 1, true));
			CARD_MAP.put("staticStr", new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE / 2, EffectType.STRENGTH, 1, true));
			CARD_MAP.put("staticDex", new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE / 2, EffectType.DEXTERITY, 1, true));
			CARD_MAP.put("ribbon", new Card(0, EffectType.STATUS_IMMUNE, 1, true)); 
			CARD_MAP.put("healPerTurn", new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE / 2, EffectType.HEAL_PER_TURN, 1, true));
			CARD_MAP.put("blockPerTurn", new Card((DEFAULT_STRIKE_BLOCK_MAGNITUDE / 2) + 1, EffectType.BLOCK_PER_TURN, 1, true));
			
			CARD_MAP.put("strikeDefend", new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE - 2, BLOCK_AND_ATTACK, 1));
			CARD_MAP.put("heal", new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE, HEAL, 1));
			CARD_MAP.put("healBlock", new Card(DEFAULT_STRIKE_BLOCK_MAGNITUDE - 2, HEAL_AND_BLOCK, 1));
			Card exhaust = new Card (DEFAULT_STRIKE_BLOCK_MAGNITUDE * 2, ATTACK, 1);
			exhaust.setExhaust(true);
			CARD_MAP.put("strikeExhaust", exhaust);
			
			mapInit = true;
		}
		
		return CARD_MAP;
	}
	
	public Card (int magnitude, EffectType effectType) {
		this.magnitude = magnitude;
		this.effectType = effectType;
		isAttack = false;
		isBlock = false;
		isHeal = false;
		isExhaust = false;
		switch (effectType) {
			case ATTACK:
				isAttack = true;
				break;
			case BLOCK:
				isBlock = true;
				break;
			case HEAL:
				isHeal = true;
				break;
			case BLOCK_AND_ATTACK:
				isAttack = true;
				isBlock = true;
				break;
			case HEAL_AND_BLOCK:
				isHeal = true;
				isBlock = true;
				break;
			default:
		}
		
		level = 0;
		upgraded = false;
	}
	
	public Card (int magnitude, EffectType effectType, int level) {
		this(magnitude, effectType);
		this.level = level;
		if (level > 1) {
			throw new AssertionError("????");
		}
	}
	
	public Card (int magnitude, EffectType effectType, int level, boolean isPower) {
		this(magnitude, effectType, level);
		if (!isPower) {
			throw new AssertionError("This constructor is for powers only.");
		}
		switch (effectType) {
		case STRENGTH:
			isStatIncrease = true;
			isStrengthIncrease = true;
			isPerTurnStatIncreasePower = false;
			break;
		case DEXTERITY:
			isStatIncrease = true;
			isStrengthIncrease = false;
			isPerTurnStatIncreasePower = false;
			break;
		case STR_PER_TURN:
			isStatIncrease = true;
			isStrengthIncrease = true;
			isPerTurnStatIncreasePower = true;
			break;
		case DEX_PER_TURN:
			isStatIncrease = true;
			isStrengthIncrease = false;
			isPerTurnStatIncreasePower = true;
			break;
		case STATUS_IMMUNE:
			isStatusImmunity = true;
			break;
		case HEAL_PER_TURN:
			break;
		case BLOCK_PER_TURN:
			break;
		default:
			throw new AssertionError("Bad effect type for power constructor.");
		}
		this.isPower = true;
		isExhaust = true;
	}
	
	public int compareTo (Card other) {
		//Generates a descending by level order when Collections.sort() is called
		return other.level - this.level;
	}
	
	public static void main(String[] args) {
		System.out.println("Testing upgrade.");
		Card dexPerTurn = getCardMap().get("dexPerTurn");
		System.out.println(dexPerTurn);
		dexPerTurn.upgrade();
		System.out.println("Upgraded:");
		System.out.println(dexPerTurn);
		try {
			dexPerTurn.upgrade();
		} catch (Throwable t) {
			System.out.println("Couldn't upgrade again, as expected.");
		}
	}
	
	public void upgrade () {
		if (!upgraded) {
			if (isPower) {
				magnitude++;
				if (!isPerTurnStatIncreasePower) {
					magnitude++;
				}
			} else {
				magnitude += 2;
				if (isExhaust) {
					magnitude += 2;
				}
			}
			level++;
			upgraded = true;
		} else {
			throw new AssertionError("Card cannot be upgraded twice.");
		}
	}
	
	@Override
	public String toString () {
		String response = "Level " + level + " - Effects: ";
		if (isAttack) {
			response += " Attack ";
		}
		if (isBlock) {
			response += " Block ";
		}
		if (isHeal) {
			response += " Heal ";
		}
		if (isExhaust) {
			response += " EXHAUST ";
		}
		response += ", Magnitude: " + magnitude;
		response += ", EffectType: " + effectType;
		return response;
	}
	
	public boolean isAttack () {
		return isAttack;
	}

	public boolean isBlock () {
		return isBlock;
	}
	
	public boolean isHeal () {
		return isHeal;
	}
	
	public boolean isExhaust () {
		return isExhaust;
	}
	
	public void setExhaust (boolean isExhaust) {
		this.isExhaust = isExhaust;
	}
	
	public boolean isUpgraded () {
		return upgraded;
	}
	
	public int getMagnitude () {
		return magnitude;
	}
	
	public int getLevel () {
		return level;
	}
	
	public boolean isPower () {
		return isPower;
	}
	
	public EffectType getEffectType () {
		return effectType;
	}
	
	public boolean isStatIncrease () {
		return isStatIncrease;
	}
	
	public boolean isStrengthIncrease () {
		return isStrengthIncrease;
	}
	
	public boolean isPerTurnStatIncreasePower () {
		return isPerTurnStatIncreasePower;
	}
	
	public boolean isStatusImmunity () {
		return isStatusImmunity;
	}
}
