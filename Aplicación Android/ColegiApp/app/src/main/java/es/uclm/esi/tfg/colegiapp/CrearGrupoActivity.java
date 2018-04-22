package es.uclm.esi.tfg.colegiapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static es.uclm.esi.tfg.colegiapp.MainActivity.CORREO;
import static es.uclm.esi.tfg.colegiapp.MainActivity.TELEFONO;

public class CrearGrupoActivity extends AppCompatActivity {

    private EditText txtNombreGrupo;
    private ListView lstFamilias;
    private Button btnCrearGrupo;

    private FirebaseFirestore db;

    private ArrayList<Familia> usuarios;
    private AdaptadorListaFamilias adaptador;

    private ProgressDialog progressDialog;

    private int identificadorDocente;
    private Docente docente;

    private ArrayList<Familia> candidatos;

    private int contador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_grupo);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("identificadorDocente")) {
                identificadorDocente = getIntent().getExtras().getInt("identificadorDocente");
            }

            if (getIntent().getExtras().containsKey("usuarioJavaDocente")) {
                docente = getIntent().getExtras().getParcelable("usuarioJavaDocente");
            }
        }

        db = FirebaseFirestore.getInstance();

        setTitle("Nuevo Chat Grupal");

        txtNombreGrupo = (EditText) findViewById(R.id.txtNombreGrupo);
        lstFamilias = (ListView) findViewById(R.id.lstFamilias);
        btnCrearGrupo = (Button) findViewById(R.id.btnCrearGrupo);

        progressDialog = ProgressDialog.show(CrearGrupoActivity.this, "",
                getString(R.string.msgCargandoUsuarios), true);

        usuarios = new ArrayList<Familia>();
        candidatos = new ArrayList<Familia>();
        adaptador = new AdaptadorListaFamilias(this, usuarios);
        lstFamilias.setAdapter(adaptador);

        obtenerUsuarios();

        btnCrearGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtNombreGrupo.getText().toString().equalsIgnoreCase("") ||
                        txtNombreGrupo.getText().toString().length() < 3) {
                    Toast.makeText(CrearGrupoActivity.this, getString(R.string.msgNombreGrupoError), Toast.LENGTH_LONG).show();
                } else {
                    preguntarUsuario();
                }
            }
        });
    }

    static class ViewHolder {
        CheckBox checkBox;
        TextView familia;
        TextView tutor1;
        TextView tutor2;
    }

    private void obtenerUsuarios() {
        db.collection("Usuarios")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Familia usuario = new Familia(document.getId(),
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
                            }

                            adaptador.notifyDataSetChanged();

                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(CrearGrupoActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void preguntarUsuario() {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle(getString(R.string.lblConfirmar));
        dialogo1.setMessage(getString(R.string.msgConfirmarGrupo));
        dialogo1.setCancelable(false);

        dialogo1.setPositiveButton(getString(R.string.lblConfirmar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                aceptar();
            }
        });

        dialogo1.setNegativeButton(getString(R.string.lblCancelar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                cancelar();
            }
        });

        dialogo1.show();
    }

    private void aceptar() {
        progressDialog = ProgressDialog.show(CrearGrupoActivity.this, "",
                getString(R.string.msgCreandoGrupo), true);
        obtenerContador();
    }

    private void obtenerContador() {
        db.collection("ChatsGrupales").document("Control").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        contador = Integer.parseInt(document.get("Contador").toString());
                        crearGrupo(contador);
                    } else {
                        Toast.makeText(CrearGrupoActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CrearGrupoActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void cancelar() {

    }

    private void crearGrupo(int contador) {
        Map<String, Object> mapContador = new HashMap<>();
        Map<String, Object> mapGrupo = new HashMap<>();
        Map<String, Object> mapDocente = new HashMap<>();

        DocumentReference doc;
        CollectionReference coleccion;

        Mensaje mensaje = new Mensaje();

        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).isChecked()) {
                candidatos.add(usuarios.get(i));
            }
        }


        ChatGrupal chatGrupal = new ChatGrupal("chatGrupal" + contador,
                txtNombreGrupo.getText().toString(),
                docente, candidatos);

        doc = db.collection("ChatsGrupales").document(chatGrupal.getId());

        mapGrupo.put("Id", chatGrupal.getId());
        mapGrupo.put("Nombre", chatGrupal.getNombre());

        mapDocente.put("Id", docente.getId());
        mapDocente.put("Nombre", docente.getNombre());
        mapDocente.put("Apellido1", docente.getApellido1());
        mapDocente.put("Apellido2", docente.getApellido2());
        mapDocente.put("Correo", docente.getCorreo());
        mapDocente.put("Telefono", docente.getTelefono());

        mapGrupo.put("Administrador", mapDocente);

        doc.set(mapGrupo);

        coleccion = db.collection("ChatsGrupales").document(chatGrupal.getId()).collection("Familias");

        for (int i = 0; i < candidatos.size(); i++) {
            coleccion.document(candidatos.get(i).getNombreFamilia()).set(candidatos.get(i));
        }

        coleccion = db.collection("ChatsGrupales").document(chatGrupal.getId()).collection("Mensajes");

        if (identificadorDocente == CORREO) {
            mensaje = new Mensaje(docente.getCorreo(),
                    docente.getNombre(),
                    docente.getApellido1(),
                    docente.getApellido2(),
                    getString(R.string.msgGrupoNuevo),
                    new Date());
        } else if (identificadorDocente == TELEFONO) {
            mensaje = new Mensaje(docente.getTelefono(),
                    docente.getNombre(),
                    docente.getApellido1(),
                    docente.getApellido2(),
                    getString(R.string.msgGrupoNuevo),
                    new Date());
        }

        coleccion.add(mensaje);

        mapContador.put("Contador", contador + 1);

        db.collection("ChatsGrupales").document("Control").set(mapContador);

        progressDialog.dismiss();

        finish();

    }

}
