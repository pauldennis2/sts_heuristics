/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class PowerCrystal {
	
	/**
	 * Natural alignment of Matrix:
	 * Light |  Air  | Chaos
	 * -------------------
	 * Fire  |       | Water
	 * -------------------
	 * Order | Earth | Dark
	 */
	
	private int shipLevel;
	private int matrixLevel; //Number of times the matrix has leveled up
	private Set<PowerCrystalTrait> traits;
	private int[][] matrix;
	private int[][] overMatrix;
	private int powerOutput; //The sum of the squares of the matrix values
	
	private int availablePerkPoints = 0;
	
	private boolean stackingEnabled = false;
	private String name;
	public static final String[] NAMES = {"Jane", "Elizabeth", "William", "Cathy", "Elijah", "Joseph"};
	
	private int traumatizedRemaining = 0;
	
	private ElementalDevotion elementalDevotion;
	private FundamentalDevotion fundamentalDevotion;
	
	Random random;
	
	public PowerCrystal () {
		traits = new HashSet<>();
		matrixLevel = 1;
		shipLevel = 0;
		matrix = new int[9][9];
		overMatrix = new int[3][3];
		overMatrix[1][1] = 1;
		matrix[4][4] = 1;
		powerOutput = 1;
		random = new Random();
		name = NAMES[random.nextInt(NAMES.length - 1)];
	}
	
	public PowerCrystal (String name) {
		this();
		this.name = name;
	}
	
	public void levelUpMatrix () {
		if (traumatizedRemaining > 0) {
			traumatizedRemaining--;
		} else {
			if (matrixLevel >= 81 && !stackingEnabled) {
				System.out.println("Already at max level for non-stacking matrix");
				return;
			}
			matrixLevel++;
			while (true) {
				int i = random.nextInt(9);
				int j = random.nextInt(9);
				if (matrix[i][j] == 1 && stackingEnabled) {
					powerOutput -= matrix[i][j] * matrix[i][j];
					matrix[i][j]++;
					powerOutput += matrix[i][j] * matrix[i][j];
					break;
				} else if (matrix[i][j] == 1) {
					continue;
				}
				if (checkForAdjacent(i, j)) {
					matrix[i][j]++;
					int x = i / 3;
					int y = j / 3;
					overMatrix[x][y]++;
					if (overMatrix[x][y] == 2 || overMatrix[x][y] == 9) {
						levelUpShip();
					}
					powerOutput++;
					break;
				}
			}
		}
	}
	
	public void levelUpMatrix (int numTimes) {
		for (int i = 0; i < numTimes; i++) {
			levelUpMatrix();
		}
	}
	
	private void levelUpShip () {
		shipLevel++;
		if (shipLevel % 4 == 1) {
			addRandomTrait();
		}
		if (shipLevel % 4 == 2) {
			availablePerkPoints++;
		}
		if (shipLevel % 4 == 3) {
			//Nothing happens
		}
		if (shipLevel % 4 == 0) {
			availablePerkPoints++;
		}
	}
	
	private void addRandomTrait() {
		List<PowerCrystalTrait> possibleTraits = PowerCrystalTrait.getPossibleRandoms().stream()
				.filter(trait -> !traits.contains(trait)) //Remove traits we already have
				.filter(trait -> { //Remove traits where we don't meet the prereqs
					List<PowerCrystalTrait> prereqs = trait.getPrereqs();
					for (PowerCrystalTrait prereq : prereqs) {
						if (traits.contains(prereq)) {
							return false;
						}
					}
					return true;
				})
				.filter(trait -> { //Remove traits that conflict with ones we already have
					for (PowerCrystalTrait existingTrait : traits) {
						if (existingTrait.getExclusiveWith() == trait) {
							return false;
						}
					}
					return true;
				})
				.collect(Collectors.toList());
		
		PowerCrystalTrait newTrait = possibleTraits.get(random.nextInt(possibleTraits.size()));
		
		traits.add(newTrait);
		System.out.println("Power Crystal " + name + " just gained a new trait: " + newTrait);
	}
	
	public void purchaseTrait (PowerCrystalTrait trait) {
		if (trait.getCost() <= availablePerkPoints && !traits.contains(trait)) {
			traits.add(trait);
			availablePerkPoints -= trait.getCost();
			System.out.println("Added " + trait);
		} else if (trait.getCost() > availablePerkPoints) {
			System.out.println("Cannot afford " + trait);
		} else if (traits.contains(trait)) {
			System.out.println("Already have " + trait);
		}
		
		List<PowerCrystalTrait> prereqs = trait.getPrereqs();
		for (PowerCrystalTrait prereq : prereqs) {
			traits.remove(prereq);
		}
	}
	
	public boolean checkForAdjacent (int x, int y) {
		boolean hasAdjacent = false;
		if (x > 0) {
			//Check left
			if (matrix[x - 1][y] > 0) {
				hasAdjacent = true;
			}
		}
		if (y > 0) {
			//Check above
			if (matrix[x][y - 1] > 0) {
				hasAdjacent = true;
			}
		}
		if (x < 8) {
			//Check right
			if (matrix[x + 1][y] > 0) {
				hasAdjacent = true;
			}
		}
		if (y < 8) {
			//Check below
			if (matrix[x][y + 1] > 0) {
				hasAdjacent = true;
			}
		}
		return hasAdjacent;
	}
	
	public void printAllInfo () {
		System.out.println(this);
		System.out.println(stackingEnabled ? "Matrix - CHARGED:" : "Matrix:");
		System.out.println(getMatrixString());
		System.out.println("Overmatrix:");
		
		System.out.println(getOverMatrixString());
		System.out.println("Traits: " + traits);
		System.out.println("Available points: " + availablePerkPoints);
		if (elementalDevotion != null) {
			System.out.println("Element: " + elementalDevotion);
		}
		if (fundamentalDevotion != null) {
			System.out.println("Fundament: " + fundamentalDevotion);
		}
	}
	
	@Override
	public String toString () {
		return name + ", Level " + shipLevel + " (" + matrixLevel + ")";
	}
	
	public String getMatrixString () {
		StringBuilder response = new StringBuilder();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				response.append(matrix[i][j] + " ");
				if (j % 3 == 2 && j < 7) {
					response.append("| ");
				}
			}
			
			response.append("\n");
			if (i % 3 == 2 && i < 7) {
				response.append("---------------------\n");
			}
		}
		return response.toString();
	}
	
	public String getOverMatrixString () {
		StringBuilder response = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				response.append(overMatrix[i][j] + " | ");
			}
			
			response.append("\n----\n");
		}
		return response.toString();
	}
	
	public int getPowerOutput () {
		return powerOutput;
	}
	
	public int getAvailablePerkPoints () {
		return availablePerkPoints;
	}
	
	public ElementalDevotion getElementalDevotion () {
		return elementalDevotion;
	}
	
	public void setElementalDevotion (ElementalDevotion elementalDevotion) {
		this.elementalDevotion = elementalDevotion;
	}
	
	public FundamentalDevotion getFundamentalDevotion () {
		return fundamentalDevotion;
	}
	
	public void setFundamentalDevotion (FundamentalDevotion fundamentalDevotion) {
		this.fundamentalDevotion = fundamentalDevotion;
	}
	
	public boolean hasTrait (PowerCrystalTrait trait) {
		return traits.contains(trait);
	}
	
	public int getShipLevel () {
		return shipLevel;
	}
	
	public void enableStacking () {
		stackingEnabled = true;
	}
	
	public int[][] getMatrix () {
		return matrix;
	}
	
	public int[][] getOverMatrix () {
		return overMatrix;
	}
}
