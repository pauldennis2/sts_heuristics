/**
 * @author Paul Dennis (pd236m)
 * May 4, 2018
 */
package reflection_messin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SomeClass {
	
	public int i;
	public boolean b;
	double d;
	String name;
	
	public SomeClass () {
		
	}

	public SomeClass(int i, boolean b, double d, String name) {
		super();
		this.i = i;
		this.b = b;
		this.d = d;
		this.name = name;
	}
	
	public static int getListSize (List<?> list) {
		return list.size();
	}
	
	public static void sumList (List<?> list) {
		int sum = 0;
		String concat = "";
		if (list.get(0) instanceof Integer) {
			for (Object o : list) {
				sum += (Integer) o;
			}
			System.out.println("Sum = " + sum);
		} else if (list.get(0) instanceof String) {
			for (Object o : list) {
				concat += (String) o;
			}
			System.out.println("Concat = " + concat);
		} else {
			System.out.println("Don't know the type of this one.");
		}
		System.out.println("Type = ");
		//System.out.print(list.getClass().getTypeParameters());
		System.out.print(list.getClass().getTypeName());
	}
	
	public static Object sumListAndReturn (List<?> list) {
		int sum = 0;
		String concat = "";
		Object response;
		if (list.get(0) instanceof Integer) {
			for (Object o : list) {
				sum += (Integer) o;
			}
			response = sum;
		} else if (list.get(0) instanceof String) {
			for (Object o : list) {
				concat += (String) o;
			}
			response = concat;
		} else {
			response = null;
		}
		return response;
	}
	
	public static void sumList(List list, Class<?> type) {
		Class<?> thing = (Class<?>) list.get(0);
	}
	
	public static void main(String[] args) {
		List<Integer> nums = Arrays.asList(3, 1, 9, 14); //27
		List<String> groceries = Arrays.asList("apples", "oranges", "banana hammock");
		
		sumList(nums);
		sumList(groceries);
		
		System.out.println("sum list and return:");
		System.out.println(sumListAndReturn(nums));
		System.out.println(sumListAndReturn(groceries));
	}
	
}
