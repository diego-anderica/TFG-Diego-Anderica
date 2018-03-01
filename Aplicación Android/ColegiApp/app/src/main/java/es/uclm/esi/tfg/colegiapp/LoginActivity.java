package es.uclm.esi.tfg.colegiapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    Button btnEntrarCTfno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnEntrarCTfno = (Button) findViewById(R.id.btnEnviarCodigo);

        btnEntrarCTfno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActivityTfno();
            }
        });

    }

    public void lanzarActivityTfno(){
        Intent activityTfno = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        Toast.makeText(this, "Entrando con número de teléfono...", Toast.LENGTH_SHORT).show();
        startActivity(activityTfno);
        finish();
    }
}
