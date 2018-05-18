/**
 * @author Paul Dennis (pd236m)
 * May 11, 2018
 */
package sts_heuristics;

//Class to store a string and an index paired together
public class StringIndex {
	
	String content;
	int index;

	public StringIndex(String content, int index) {
		super();
		this.content = content;
		this.index = index;
	}
	
	@Override
	public String toString () {
		return content + " at " + index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + index;
		return result;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof StringIndex) {
			StringIndex si = (StringIndex) other;
			return this.content.equals(si.content) && this.index == si.index;
		} else {
			return false;
		}
	}
}
