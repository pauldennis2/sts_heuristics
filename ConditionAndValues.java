/**
 * @author Paul Dennis (pd236m)
 * Jun 7, 2018
 */
package sts_heuristics;

import java.util.Map;

public class ConditionAndValues {
	
	private Conditional condition;
	private Map<String, Double> values;

	public ConditionAndValues(Conditional condition, Map<String, Double> values) {
		super();
		this.condition = condition;
		this.values = values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		ConditionAndValues other = (ConditionAndValues) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}
	
	@Override
	public String toString () {
		return condition.toString() + "-" + values.toString();
	}

	public Conditional getCondition() {
		return condition;
	}

	public Map<String, Double> getValues() {
		return values;
	}
	
}
