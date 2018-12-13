/**
 * @author Paul Dennis
 * Jul 11, 2018
 */
package shipyard;

import java.util.Random;

public class GreatLeader {
	
	private String name;
	private MissionType focus;
	
	public static final String[] NAMES = {"Caesar", "Galileo", "Newton", "Captain Picard", "Janeway"};
	
	public GreatLeader () {
		Random random = new Random();
		name = NAMES[random.nextInt(NAMES.length)];
		focus = MissionType.values()[random.nextInt(MissionType.values().length)];
	}
	
	public GreatLeader (String name, MissionType focus) {
		this.name = name;
		this.focus = focus;
	}
	
	@Override
	public String toString () {
		return name + " (" + focus + ")";
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 15; i++) {
			System.out.println(new GreatLeader());
		}
	}
}