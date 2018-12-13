/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

public class Material {

	private MaterialType type;
	private int level;
	
	public Material (MaterialType type, int level) {
		this.type = type;
		this.level = level;
	}
	
	public MaterialType getType () {
		return type;
	}
	
	public int getLevel () {
		return level;
	}
	
	@Override
	public String toString () {
		switch (type) {
		case HULL:
			switch (level) {
			case 0:
				return "Iron Alloy";
			case 1:
				return "Titanium Alloy";
			case 2:
				return "Tritanium Alloy";
			case 3:
				return "Arcanite Alloy";
			default:
				return "Unknown Metal";
			}
		case SHIELD:
			switch (level) {
			case 0:
				return "Quartz Crystal";
			case 1:
				return "Wavellite Crystal";
			case 2:
				return "Celestine Crystal";
			case 3:
				return "Ultima Crystal";
			default:
				return "Unknown Shield Crystal";
			}
		case WEAPON:
			switch (level) {
			case 0:
				return "Pyrite weapon";
			case 1:
				return "Quicksilver weapon";
			case 2:
				return "Level 2 weapon";
			case 3:
				return "Level 3 weapon";
			default:
				return "Unknown Weapon Material";
			}
		case BUILDING_PARTS:
			switch (level) {
			case 0:
				return "Basic Building Parts";
			case 1:
				return "Automated Building Parts";
			}
		case GREENSTONE:
			switch (level) {
			case 0:
				return "Greenstone";
			case 1:
				return "Enchanted Greenstone";
			}
		case DEVOTION_STONE:
			return "Devotion Stone";
		default:
			return "Unknown Type??";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Material other = (Material) obj;
		if (level != other.level)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	
}
