/**
 * @author Paul Dennis (pd236m)
 * May 23, 2018
 */
package sts_heuristics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This class overrides HashMap in two useful ways:
 * 1. It rounds all Double values coming in to two places
 * 2. Its toString() method sorts based on value
 */
@SuppressWarnings("serial")
public class RoundedDoubleMap extends HashMap<String, Double>{
	
	DecimalFormat decFormat;
	
	public RoundedDoubleMap() {
		super();
		decFormat = new DecimalFormat("#.##");
	}
	
	public RoundedDoubleMap (Map<String, Double> map) {
		super(map);
		decFormat = new DecimalFormat("#.##");
	}

	@Override
	public Double put (String k, Double v) {
		Double d1 = Double.parseDouble(decFormat.format(v));
		super.put(k, d1);
		return null;
	}
	
	@Override
	public String toString () {
		StringBuilder response = new StringBuilder();
		response.append("{");
		List<OptionValue> optionValues = new ArrayList<>();
		for (String optionName : this.keySet()) {
			optionValues.add(new OptionValue(optionName, this.get(optionName)));
		}
		Collections.sort(optionValues);
		boolean first = true;
		for(OptionValue optionValue : optionValues) {
			if (!first) {
				response.append(", ");
			} else {
				first = false;
			}
			response.append(optionValue);
		}
		response.append("}");
		return response.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((decFormat == null) ? 0 : decFormat.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoundedDoubleMap other = (RoundedDoubleMap) obj;
		if (decFormat == null) {
			if (other.decFormat != null)
				return false;
		} else if (!decFormat.equals(other.decFormat))
			return false;
		return true;
	}
	
	
}
