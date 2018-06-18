package dao.tfg.esi.uclm.es;

import java.security.MessageDigest;

public class DAOUsuario {
	public static String encriptar(String pwd) throws Exception {
		MessageDigest md;
		StringBuffer sb = new StringBuffer();
		byte[] result;
		
		try {
			md = MessageDigest.getInstance("SHA1");
	        
	        result = md.digest(pwd.getBytes());
	
	        for (int i = 0; i < result.length; i++) {
	            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
}
