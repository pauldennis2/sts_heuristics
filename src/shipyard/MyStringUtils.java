/**
 * @author Paul Dennis
 * Jul 18, 2018
 */
package shipyard;

public class MyStringUtils {
	
	//Remove capitalization but re-capitalize words. Replace underscores with spaces
	public static String capitalizeWords (String input) {
		StringBuilder response = new StringBuilder();
		String[] words = input.toLowerCase().replaceAll("_", " ").split(" ");
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			response.append(word.substring(0, 1).toUpperCase() + word.substring(1));
			if (i + 1 < words.length) {
				response.append(" ");
			}
		}
		return response.toString();
	}
}
