/**
 * @author Paul Dennis (pd236m)
 * May 14, 2018
 */
package sts_heuristics;

//Could be used to generify the AdaptiveStrategy class if we want to.
//Requires some small tweaks and serves no immediate purpose
//so I'm leaving as is for now.
public abstract class GeneticStrategy implements Comparable<GeneticStrategy>{
	
	private double attainment;
	
	//Returns a (randomly) modified version of this Strategy
	//returned.equals(this) => false
	public abstract GeneticStrategy getChild ();
	
	public double getAttainment () {
		return attainment;
	}
	
	public void setAttainment (double attainment) {
		this.attainment = attainment;
	}
	
	@Override
	public int compareTo(GeneticStrategy other) {
		if (this.attainment > other.attainment) {
			return -1;
		} else if (this.attainment < other.attainment) {
			return 1;
		}
		return 0;
	}
}
