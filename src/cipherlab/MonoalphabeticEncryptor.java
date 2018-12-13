/**
 * @author Paul Dennis
 * Aug 7, 2018
 */
package cipherlab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonoalphabeticEncryptor {
	
	Map<Character, Character> key;
	Map<Character, Character> decryptKey;
	String plainText;
	String cipherText;
	
	public MonoalphabeticEncryptor (String plainText) {
		this.plainText = plainText.toUpperCase();
		key = new HashMap<>();
		decryptKey = new HashMap<>();
		encrypt();
	}
	
	private void encrypt () {
		//Create a random monoalphabetic key
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		List<Character> charList = new ArrayList<>();
		for (char c : alphabet.toCharArray()) {
			charList.add(c);
		}
		Collections.shuffle(charList);
		int i = 0;
		for (char c : alphabet.toCharArray()) {
			key.put(c, charList.get(i));
			decryptKey.put(charList.get(i), c);
			i++;
		}
		cipherText = "";
		for (char c : plainText.toCharArray()) {
			cipherText += key.get(c);
		}
	}
	
	private String decrypt () {
		String response = "";
		for (char c : cipherText.toCharArray()) {
			response += decryptKey.get(c);
		}
		return response;
	}
	
	public static void main(String[] args) {
		MonoalphabeticEncryptor encryptor = new MonoalphabeticEncryptor("ilovebananas");
		System.out.println(encryptor.cipherText);
		System.out.println(encryptor.decrypt());
	}
}
