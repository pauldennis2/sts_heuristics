/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import java.util.ArrayList;
import java.util.List;

public class Storage {
	
	private BoundedInt capacity;
	
	private CountMap<Material> materials;
	private CountMap<ShipPart> parts;
	private List<PowerCrystal> powerCrystals;
	
	public static final int PART_SIZE = 5;
	public static final int CRYSTAL_SIZE = 10;
	
	public Storage (int capacity) {
		this.capacity = new BoundedInt(0, capacity);
		materials = new CountMap<>();
		parts = new CountMap<>();
		powerCrystals = new ArrayList<>();
	}
	
	public int getCount (Material material) {
		if (materials.get(material) != null) {
			return materials.get(material);
		} else {
			return 0;
		}
	}
	
	public int getCount (ShipPart part) {
		if (parts.get(part) != null) {
			return parts.get(part);
		} else {
			return 0;
		}
	}
	
	public boolean addMaterials (Material material, int amount) {
		int sum = amount;
		if (sum + capacity.getValue() <= capacity.getMax()) {
			materials.add(material, amount);
			capacity.setValue(capacity.getValue() + sum);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean addMaterials (CountMap<Material> materials) {
		int sum = materials.getSum();
		if (sum + capacity.getValue() <= capacity.getMax()) {
			materials.getMap().keySet().forEach(material -> this.materials.add(material, materials.get(material)));
			capacity.setValue(capacity.getValue() + sum);
			return true;
		} else {
			return false;
		}	
	}
	
	public boolean removeMaterials (Material material, int count) {
		if (materials.get(material) >= count) {
			materials.subtract(material, count);
			capacity.setValue(capacity.getValue() - count);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean addParts (ShipPart part, int amount) {
		int sum = amount * PART_SIZE;
		if (sum + capacity.getValue() <= capacity.getMax()) {
			parts.add(part, amount);
			capacity.setValue(capacity.getValue() + sum);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean addPart (ShipPart part) {
		return addParts(part, 1);
	}
	
	public boolean addParts (CountMap<ShipPart> parts) {
		int sum = parts.getSum() * PART_SIZE;
		if (sum + capacity.getValue() <= capacity.getMax()) {
			parts.getMap().keySet().forEach(part -> this.parts.add(part, parts.get(part)));
			capacity.setValue(capacity.getValue() + sum);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean removeParts (ShipPart part, int count) {
		if (parts.get(part) >= count) {
			parts.subtract(part, count);
			capacity.setValue(capacity.getValue() - count * PART_SIZE);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean removePart (ShipPart part) {
		return removeParts(part, 1);
	}
	
	public boolean addCrystal (PowerCrystal crystal) {
		int sum = CRYSTAL_SIZE;
		if (sum + capacity.getValue() <= capacity.getMax()) {
			powerCrystals.add(crystal);
			capacity.setValue(capacity.getValue() + sum);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean removeCrystal (PowerCrystal crystal) {
		if (powerCrystals.contains(crystal)) {
			capacity.setValue(capacity.getValue() - CRYSTAL_SIZE);
			return powerCrystals.remove(crystal);
		} else {
			return false;
		}
	}
	
	public Storage removeAll () {
		Storage removed = new Storage(capacity.getMax());
		removed.materials = this.materials;
		removed.parts = this.parts;
		this.materials = new CountMap<>();
		this.parts = new CountMap<>();
		capacity.setValue(0);
		return removed;
	}
	
	@Override
	public String toString () {
		StringBuilder response = new StringBuilder("Capacity: " + capacity);
		response.append("Materials:");
		response.append(materials);
		response.append("Parts:");
		response.append(parts);
		response.append("Power Crystals:");
		powerCrystals.forEach(pc -> response.append("\t" + pc));
		return response.toString();
	}
	
	public List<PowerCrystal> getPowerCrystals () {
		return powerCrystals;
	}
}
