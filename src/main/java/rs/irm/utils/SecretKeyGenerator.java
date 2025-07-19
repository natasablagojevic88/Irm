package rs.irm.utils;

import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

public class SecretKeyGenerator {

	public static void main(String[] args) {
		
		// 16 characters
		String password="jw9kwel8v0btzw0f";
		
		try {
			SecretKeySpec keySpec = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
			String base64Key = Base64.getEncoder().encodeToString(keySpec.getEncoded());
	        System.out.println("Private key secret: " + base64Key);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		

	}

}
