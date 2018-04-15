package es.uclm.esi.tfg.colegiapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CrearGrupo extends AppCompatActivity {

    private EditText txtNombreGrupo;
    private ListView lstUsuarios;
    private Button btnCrearGrupo;

    private FirebaseFirestore db;

    private ArrayList<Usuario> usuarios;
    AdaptadorLista adaptador;

    ProgressDialog progressDialog;
    
    private String identificadorDocente;
    private String procedencia;
    
    private Docente docente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_grupo);
        
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("correo")) {
                identificadorDocente = getIntent().getExtras().getString("correo");
            } else if (getIntent().getExtras().containsKey("telefono")) {
                identificadorDocente = getIntent().getExtras().getString("telefono");
            }

            if (getIntent().getExtras().containsKey("procedencia")) {
                procedencia = getIntent().getExtras().getString("procedencia");
            }
        }

        db = FirebaseFirestore.getInstance();

        buscarDocente();

        setTitle("Nuevo Chat Grupal");

        txtNombreGrupo = (EditText) findViewById(R.id.txtNombreGrupo);
        lstUsuarios = (ListView) findViewById(R.id.lstUsuarios);
        btnCrearGrupo = (Button) findViewById(R.id.btnCrearGrupo);
        lstUsuarios = (ListView) findViewById(R.id.lstUsuarios);

        progressDialog = ProgressDialog.show(CrearGrupo.this, "",
                "Cargando usuarios...", true);

        usuarios = new ArrayList<Usuario>();
        adaptador = new AdaptadorLista(this, usuarios);
        lstUsuarios.setAdapter(adaptador);

        obtenerUsuarios();

        btnCrearGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearGrupo();
            }
        });
    }

    private void obtenerUsuarios() {
        db.collection("Usuarios")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Usuario usuario = new Usuario(document.getId(),
                                        document.getString("NombreTutor1"),
                                        document.getString("Apellido1Tutor1"),
                                        document.getString("Apellido2Tutor1"),
                                        document.getString("CorreoTutor1"),
                                        document.getString("TelefonoTutor1"),
                                        document.getString("NombreTutor2"),
                                        document.getString("Apellido1Tutor2"),
                                        document.getString("Apellido2Tutor2"),
                                        document.getString("CorreoTutor2"),
                                        document.getString("TelefonoTutor2"));

                                usuarios.add(usuario);

                                progressDialog.dismiss();
                            }
                        } else {
                            Toast.makeText(CrearGrupo.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void buscarDocente() {
        String campo = "";

        if (procedencia.equalsIgnoreCase("correo")) {
            campo = "Correo";
        } else if (procedencia.equalsIgnoreCase("telefono")) {
            campo = "Telefono";
        }

        db.collection("Docentes")
                .whereEqualTo(campo, identificadorDocente)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                docente = new Docente(document.getId(),
                                        document.getString("Nombre"),
                                        document.getString("Apellido1"),
                                        document.getString("Apellido2"),
                                        document.getString("Correo"),
                                        document.getString("Telefono"));
                                Log.d("Docente", docente.toString());
                            }
                        } else {
                            Toast.makeText(CrearGrupo.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void crearGrupo() {

    }
}
