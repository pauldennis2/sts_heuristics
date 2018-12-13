/**
 * @author Paul Dennis
 * Aug 7, 2018
 */
package cipherlab;

import java.util.HashMap;
import java.util.Map;

public class MonoalphabeticSolver {

	Map<Character, Double> frequencyAnalysis;
	Map<Character, Character> substitutions;
	
	static Map<Character, Double> standardPlaintextFrequency;
	
	String cipherText;
	String plainText;
	
	
	public MonoalphabeticSolver (String cipherText) {
		
		frequencyAnalysis = new HashMap<>();
		
		this.cipherText = cipherText;
	}
	
	public static void initStandardFrequency () {
		standardPlaintextFrequency = new HashMap<>();
		standardPlaintextFrequency.put('E', 0.1202);
		standardPlaintextFrequency.put('T', 0.0910);
		standardPlaintextFrequency.put('A', 0.0812);
		standardPlaintextFrequency.put('O', 0.0768);
		standardPlaintextFrequency.put('I', 0.0731);
		standardPlaintextFrequency.put('N', 0.0695);
		standardPlaintextFrequency.put('S', 0.0628);
		standardPlaintextFrequency.put('R', 0.0602);
		standardPlaintextFrequency.put('H', 0.0592);
		standardPlaintextFrequency.put('D', 0.0432);
		standardPlaintextFrequency.put('L', 0.0398);
		standardPlaintextFrequency.put('U', 0.0288);
		standardPlaintextFrequency.put('C', 0.0271);
		standardPlaintextFrequency.put('M', 0.0261);
		standardPlaintextFrequency.put('F', 0.0230);
		standardPlaintextFrequency.put('Y', 0.0211);
		standardPlaintextFrequency.put('W', 0.0209);
		standardPlaintextFrequency.put('G', 0.0203);
		standardPlaintextFrequency.put('P', 0.0182);
		standardPlaintextFrequency.put('B', 0.0149);
		standardPlaintextFrequency.put('V', 0.0111);
		standardPlaintextFrequency.put('K', 0.0069);
		standardPlaintextFrequency.put('X', 0.0017);
		standardPlaintextFrequency.put('Q', 0.0011);
		standardPlaintextFrequency.put('J', 0.0010);
		standardPlaintextFrequency.put('Z', 0.0007);
		
	}
	
	public static Map<Character, Double> doFrequencyAnalysis (String text) {
		String letters = getLetters(text);
		Map<Character, Double> frequencyAnalysis = new HashMap<>();
		
		int size = letters.length();
		CountMap<Character> characterCountMap = new CountMap<>();
		for (Character c : letters.toCharArray()) {
			characterCountMap.increment(c);
		}
		
		for (Character c : characterCountMap.getMap().keySet()) {
			int count = characterCountMap.get(c);
			double percent = (double) count / (double) size;
			frequencyAnalysis.put(c, percent);
		}
		
		return frequencyAnalysis;
	}
	
	public static String getLetters (String input) {
		return input.toUpperCase().replaceAll("\\W", "").replaceAll("\\d", "");
	}
	
	public static void main(String[] args) {
		String input1 = "ilovebananas";
		initStandardFrequency();
		Map<Character, Double> frequencyAnalysis = doFrequencyAnalysis(input1);
		frequencyAnalysis.keySet().forEach(letter -> System.out.println(letter + " : " + frequencyAnalysis.get(letter)));
	}
}
