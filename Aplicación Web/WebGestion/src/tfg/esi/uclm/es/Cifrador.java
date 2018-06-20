package tfg.esi.uclm.es;

import java.security.MessageDigest;

public class Cifrador {
	public static String encriptar(String pwd) throws Exception {
		StringBuffer sb = new StringBuffer();
		MessageDigest md;
		byte[] result;
		
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.reset(); //Resetear digest
			md.update(pwd.getBytes()); //Actualizar digest con contraseña a encriptar
	        
	        result = md.digest(); //Obtener digest de la contraseña
	
	        //Convertir resultado a cadena hexadecimal
	        for (int i = 0; i < result.length; i++) {
	            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
	// Fuente: http://www.sha1-online.com/sha256-java/
}
