/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

public class Card implements Comparable<Card>{
	
	private int magnitude;
	private boolean isAttack;
	private boolean isBlock;
	private boolean isHeal;
	private boolean isExhaust;
	
	private boolean upgraded;
	
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
	
	public int compareTo (Card other) {
		//Generates a descending by level order when Collections.sort() is called
		return other.level - this.level;
	}
	
	public void upgrade () {
		if (!upgraded) {
			magnitude += 2;
			if (isExhaust) {
				magnitude += 2;
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
}
