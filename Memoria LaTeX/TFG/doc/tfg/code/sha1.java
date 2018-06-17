private void btnCifrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCifrarActionPerformed
    if (txtPass.getPassword().length == 0) {
        JOptionPane.showMessageDialog(this, "¡El campo de contraseña no puede estar vacío!");
    } else {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA1");
            StringBuffer sb = new StringBuffer();
            byte[] result = md.digest(txtPass.getText().getBytes());

            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
            }

            txtResultado.setText(sb.toString());
            
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        //http://www.sha1-online.com/sha1-java/
    }
}//GEN-LAST:event_btnCifrarActionPerformed