package dao.tfg.esi.uclm.es;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

public class DAOUsuario {	
	public static String encriptar(String pwd) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA1");
        StringBuffer sb = new StringBuffer();
        
        byte[] result = md.digest(pwd.getBytes());

        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
	}
}
