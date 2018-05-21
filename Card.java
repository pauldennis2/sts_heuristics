/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.ArrayList;
import java.util.List;

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
	}
	
	//Just a design sandbox for the moment
	public static void makePowerCards () {
		List<Card> powers = new ArrayList<>();
		powers.add(new Card(1, EffectType.STR_PER_TURN, 1, true));
		powers.add(new Card(1, EffectType.DEX_PER_TURN, 1, true));
		powers.add(new Card(3, EffectType.STRENGTH, 1, true));
		powers.add(new Card(3, EffectType.DEXTERITY, 1, true));
		powers.add(new Card(0, EffectType.STATUS_IMMUNE, 1, true));
		powers.add(new Card(3, EffectType.HEAL_PER_TURN, 1, true));
		powers.add(new Card(4, EffectType.BLOCK_PER_TURN, 1, true));
	}
	
	public Card (int magnitude, EffectType effectType) {
		this.magnitude = magnitude;
		
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
				throw new AssertionError("Not one of the standard effects. Bad constructor usage.");
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
		this.effectType = effectType;
		this.isPower = true;
		isExhaust = true;
	}
	
	public int compareTo (Card other) {
		//Generates a descending by level order when Collections.sort() is called
		return other.level - this.level;
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
