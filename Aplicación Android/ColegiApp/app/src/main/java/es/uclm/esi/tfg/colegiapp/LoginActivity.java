package es.uclm.esi.tfg.colegiapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private Button btnEntrarCTfno;
    private Button btnEntrarCCorreo;
    private Button btnEntrarDocente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnEntrarCTfno = (Button) findViewById(R.id.btnEntrarCTfno);
        btnEntrarCCorreo = (Button) findViewById(R.id.btnEntrarCCorreo);
        btnEntrarDocente = (Button) findViewById(R.id.btnEntrarDocente);

        btnEntrarCTfno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActivityTfno(false);
            }
        });

        btnEntrarCCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActivityCorreo(false);
            }
        });

        btnEntrarDocente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerForContextMenu(view);
                openContextMenu(view);
            }
        });

    }

    private void lanzarActivityTfno(boolean docente) {
        Intent activityTfno = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        Toast.makeText(this, "Entrando con número de teléfono...", Toast.LENGTH_SHORT).show();

        if (docente) {
            activityTfno.putExtra("docente", true);
        } else {
            activityTfno.putExtra("docente", false);
        }

        startActivity(activityTfno);
        finish();
    }

    private void lanzarActivityCorreo(boolean docente) {
        Intent activityCorreo = new Intent(LoginActivity.this, EmailLoginActivity.class);
        Toast.makeText(this, "Entrando con correo y contraseña...", Toast.LENGTH_SHORT).show();

        if (docente) {
            activityCorreo.putExtra("docente", true);
        } else {
            activityCorreo.putExtra("docente", false);
        }

        startActivity(activityCorreo);
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = new MenuInflater(this);

        menu.setHeaderTitle("Seleccione el método de entrada para docentes");
        inflater.inflate(R.menu.ctxmenu_login_docente, menu);
    }

    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuItemDocenteCorreo:
                lanzarActivityCorreo(true);
                return true;
            case R.id.menuItemDocenteTfno:
                lanzarActivityTfno(true);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}
