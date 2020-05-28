package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

	//Function to hash byte array to string of hash
	public static String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] hash = digest.digest(data);
		BigInteger hashInt = new BigInteger(1, hash);
		String temp = hashInt.toString(16);
		if(temp.length() < 40) {
			int paddingZeros = 40 - temp.length();
			String zeros = "";
			for(int i = 0; i < paddingZeros; i++)
				zeros += "0";
			temp += zeros;
		}
		return temp;
	}
}
