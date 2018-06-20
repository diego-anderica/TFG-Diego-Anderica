public static String encriptar(String pwd) throws Exception {
	MessageDigest md;
	StringBuffer sb = new StringBuffer();
	byte[] result;
	
	try {
		md = MessageDigest.getInstance("SHA-256");
		result = md.digest(pwd.getBytes());

		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}
        
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	return sb.toString();
}

// Fuente: http://www.sha1-online.com/sha256-java/