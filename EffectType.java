/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

public enum EffectType {
	ATTACK, BLOCK, HEAL, BLOCK_AND_ATTACK, HEAL_AND_BLOCK, //Regular cards
	STR_PER_TURN, DEX_PER_TURN, STRENGTH, DEXTERITY, HEAL_PER_TURN, BLOCK_PER_TURN, STATUS_IMMUNE; //Powers
}
