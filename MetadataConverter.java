/**
 * @author Paul Dennis (pd236m)
 * Jun 6, 2018
 */
package sts_heuristics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MetadataConverter {
	
	public static final String FILE_LOC = "data/hall_of_fame/metadata.txt";
	public static final String DESTINATION = "data/hall_of_fame/metadata.csv";
	
	public static void main(String[] args) {
		File file = new File(FILE_LOC);
		File dest = new File(DESTINATION);
		try (Scanner fileScanner = new Scanner(file); FileWriter fileWriter = new FileWriter(dest)) {
			while (fileScanner.hasNextLine()) {
				String line = fileScanner.nextLine();
				if (line.startsWith("#")) {
					continue;
				}
				fileWriter.write(line + "\n");
			}
		} catch (IOException ex) {
			System.err.println("Problem writing to file:");
			ex.printStackTrace();
		}
		System.out.println("Successfully removed comments and converted to csv.");
	}
}
