/**
 * @author Paul Dennis (pd236m)
 * Aug 7, 2018
 */
package cipherlab;

public class PolyalphabeticEncryptor {

	String plainText;
	String cipherText;
	String key;
	
	public PolyalphabeticEncryptor (String plainText, String key) {
		this.plainText = plainText.toUpperCase();
		this.key = key.toUpperCase();
		
		encrypt();
	}
	
	//A = 65, Z = 90
	private void encrypt () {
		cipherText = "";
		for (int i = 0; i < plainText.length();) {
			for (int j = 0; j < key.length() && i < plainText.length(); j++) {
				char plain = plainText.charAt(i);
				char k = key.charAt(j);
				int offset = k - 65;
				int encrypt = (int) plain + offset;
				if (encrypt > 90) {
					encrypt -= 26;
				}
				cipherText += (char) encrypt;
				i++;
			}
		}
	}
	
	private String decrypt () {
		String returnedPlaintext = "";
		for (int i = 0; i < cipherText.length();) {
			for (int j = 0; j < key.length() && i < cipherText.length(); j++) {
				char cipher = cipherText.charAt(i);
				char k = key.charAt(j);
				int offset = k - 65;
				int plain = (int) cipher - offset;
				if (plain < 65) {
					plain += 26;
				}
				returnedPlaintext += (char) plain;
				i++;
			}
		}
		return returnedPlaintext;
	}
	
	public static void main(String[] args) {
		PolyalphabeticEncryptor encryptor = new PolyalphabeticEncryptor("Ilikebananas", "cat");
		System.out.println(encryptor.cipherText);
		System.out.println(encryptor.decrypt());
	}
}
