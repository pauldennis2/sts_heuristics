/**
 * @author Paul Dennis (pd236m)
 * May 7, 2018
 */
package reflection_messin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionMessin2 {

	public static void main(String[] args) throws Exception {
		Person person = new Person();
		
		Field f = person.getClass().getField("favoriteColor");
		Color c = (Color) f.get(person);
		System.out.println("Color = " + c);
		
		List words = new ArrayList();
		words.add("Hello");
		List<?> nums = new ArrayList<>();
		//nums.add(new Object());
	}
}
