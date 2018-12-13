/**
 * @author Paul Dennis (pd236m)
 * May 4, 2018
 */
package reflection_messin;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ReflectionMesser {

	public static void main(String[] args) {
		SomeClass myObj1 = new SomeClass();
		SomeClass myObj2 = new SomeClass(5, true, -3.6, "Charlie");
		System.out.println("Here are the fields:");
		Field[] fields = myObj1.getClass().getFields();
		System.out.println("Number of fields = " + fields.length);
		Arrays.stream(fields).forEach(System.out::println);
		
		fields = myObj2.getClass().getFields();

		System.out.println("Number of fields = " + fields.length);
		Arrays.stream(fields).forEach(System.out::println);
		for (Field f : fields) {
			System.out.println("Type = " + f.getType());
			try {
				System.out.println("getBoolean() returns: " + f.getBoolean(myObj2));
				//f.setBoolean(obj, z);
			} catch (IllegalAccessException ex) {
				System.out.println("Caught an IllegalAccessException from getBoolean()");
			} catch (IllegalArgumentException ex) {
				System.out.println("Caught an IllegalArgumentException from getBoolean()");
			}
		}
	}
}
