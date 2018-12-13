/**
 * @author Paul Dennis
 * Jul 11, 2018
 */
package shipyard;

import java.io.Closeable;
import java.util.Scanner;

public class SafeScanner implements Closeable {
	
	Scanner scanner;
	
	public SafeScanner () {
		scanner = new Scanner(System.in);
	}
	
	//Get any integer
	public int getSafeInt () {
		try {
			int response = Integer.parseInt(scanner.nextLine());
			return response;
		} catch (NumberFormatException ex) {
			System.out.println("That wasn't a number. Please try again.");
			return getSafeInt();
		}
	}
	
	//Get an int between 0 and bound, inclusive
	public int getSafeInt (int bound) {
		try {
			int response = Integer.parseInt(scanner.nextLine());
			if (response < 0 || response > bound) {
				System.out.println("Number was outside the expected range of 0 to " + bound + " inclusive.");
				return getSafeInt(bound);
			}
			return response;
		} catch (NumberFormatException ex) {
			System.out.println("That wasn't a number. Please try again.");
			return getSafeInt(bound);
		}
	}
	
	public void close () {
		scanner.close();
	}
}
