/**
 * @author Paul Dennis (pd236m)
 * May 4, 2018
 */
package reflection_messin;

import java.lang.reflect.Field;

public class NumsReflectionMessin {

	public static void main(String[] args) throws Exception {
		ThreeNums nums = new ThreeNums();
		nums.a = 2;
		nums.b = 3;
		nums.c = 4;
		
		Field f = nums.getClass().getField("a");
		System.out.println(f);
	}
}
