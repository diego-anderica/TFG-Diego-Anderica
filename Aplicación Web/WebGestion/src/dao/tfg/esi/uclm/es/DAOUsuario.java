package dao.tfg.esi.uclm.es;

import java.math.BigInteger;
import java.security.MessageDigest;

public class DAOUsuario {
	public static String encriptar(String pwd) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] messageDigest = md.digest(pwd.getBytes());
		BigInteger number = new BigInteger(1, messageDigest);
		String hashtext = number.toString(16);

		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		
		return hashtext;

	}
}
