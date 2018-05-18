/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.List;
import java.util.Random;

public class Monster {

	private int health;
	private int damage;
	
	List<MonsterAbility> abilities;
	
	public Monster (int level) {
		Random random = new Random();
		damage = level;
		health = 10 + random.nextInt(level) + random.nextInt(level);
	}
	
	public void takeDamage (int amount) {
		health -= amount;
	}
	
	public int getHealth () {
		return health;
	}
	
	public int getDamage () {
		return damage;
	}
}
