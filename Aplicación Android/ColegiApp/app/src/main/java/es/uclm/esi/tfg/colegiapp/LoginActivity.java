package es.uclm.esi.tfg.colegiapp;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private Button btnEntrarCTfno;
    private Button btnEntrarCCorreo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnEntrarCTfno = (Button) findViewById(R.id.btnEnviarCodigo);
        btnEntrarCCorreo = (Button) findViewById(R.id.btnEntrarCCorreo);

        btnEntrarCTfno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActivityTfno();
            }
        });

        btnEntrarCCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActivityCorreo();
            }
        });

    }

    public void lanzarActivityTfno(){
        Intent activityTfno = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        Toast.makeText(this, "Entrando con número de teléfono...", Toast.LENGTH_SHORT).show();
        startActivity(activityTfno);
        finish();
    }

    private void lanzarActivityCorreo() {
        Intent activityCorreo = new Intent(LoginActivity.this, EmailLoginActivity.class);
        Toast.makeText(this, "Entrando con correo y contraseña...", Toast.LENGTH_SHORT).show();
        startActivity(activityCorreo);
        finish();
    }

}
