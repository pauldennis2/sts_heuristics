/**
 * @author Paul Dennis
 * Jul 9, 2018
 */
package shipyard;

public abstract class Building implements TickTock {
	
	private int level;
	private int requiredPersonnel; //People required to run the building effectively
	private int productionRate;
	
	public Building (int requiredPersonnel) {
		this.level = 0;
		this.requiredPersonnel = requiredPersonnel;
		productionRate = 2;
	}
	
	public void upgrade () {
		level++;
	}
	
	public int getLevel () {
		return level;
	}
	
	public int getProductionRate () {
		return productionRate;
	}
	
	public int getRequiredPersonnel () {
		return requiredPersonnel;
	}
	
	protected void setProductionRate (int productionRate) {
		this.productionRate = productionRate;
	}
	
	protected void setRequiredPersonnel (int requiredPersonnel) {
		this.requiredPersonnel = requiredPersonnel;
	}
	
	public abstract String toString();
}