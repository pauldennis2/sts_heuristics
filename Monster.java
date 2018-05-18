/**
 * @author Paul Dennis (pd236m)
 * May 8, 2018
 */
package sts_heuristics;

import java.util.Random;

public class Monster {

	int health;
	int damage;
	
	public Monster (int level) {
		Random random = new Random();
		damage = level;
		health = 10 + random.nextInt(level) + random.nextInt(level);
	}
}
