package es.uclm.esi.tfg.colegiapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class InfoGrupoActivity extends AppCompatActivity {
    private static final int CORREO = 1;
    private static final int TELEFONO = 2;

    private String chatID;
    private boolean isDocente;
    private String nombreChat;
    private int identificadorUsuario;
    private Docente usuarioJavaDocente;

    private ImageView imgGrupo;
    private EditText txtNombreGrupo;
    private EditText txtNuevoNombre;
    private Button btnCambiarNombreGrupo;
    private ListView lstIntegrantes;

    private ArrayList<Familia> integrantesGrupo;
    private AdaptadorListaIntegrantes adaptador;

    private FirebaseFirestore db;
    private DocumentReference dbChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_grupo);

        imgGrupo = (ImageView) findViewById(R.id.imgGrupo);
        txtNombreGrupo = (EditText) findViewById(R.id.txtNombreGrupo);
        txtNuevoNombre = (EditText) findViewById(R.id.txtNuevoNombre);
        btnCambiarNombreGrupo = (Button) findViewById(R.id.btnCambiarNombreGrupo);
        lstIntegrantes = (ListView) findViewById(R.id.lstIntegrantesGrupo);

        integrantesGrupo = new ArrayList<Familia>();
        adaptador = new AdaptadorListaIntegrantes(this, integrantesGrupo);
        lstIntegrantes.setAdapter(adaptador);

        obtenerExtras();

        db = FirebaseFirestore.getInstance();
        dbChat = db.collection("ChatsGrupales").document(chatID);

        rellenarInfoGrupo();
        iniciarOyente();

        if (!isDocente) {
            btnCambiarNombreGrupo.setVisibility(View.GONE);
        }

        btnCambiarNombreGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarNombreGrupo();
            }
        });

        lstIntegrantes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (isDocente) {
                    lanzarActivityPerfil(position);
                }
            }
        });

    }

    private void lanzarActivityPerfil(int position) {
        Intent perfil = new Intent(this, PerfilUsuarioActivity.class);

        perfil.putExtra("idFamilia", integrantesGrupo.get(position).getNombreFamilia());
        perfil.putExtra("perfilPropio", false);
        perfil.putExtra("isDocente", isDocente);
        perfil.putExtra("procedencia", "infoGrupo");

        if (isDocente) {
            perfil.putExtra("usuarioJava", usuarioJavaDocente);
        }

        startActivity(perfil);
    }

    private void rellenarInfoGrupo() {
        setTitle(R.string.lblInfoDeGrupo);
        txtNombreGrupo.setText(nombreChat);
        obtenerIntegrantes();
    }

    private void cambiarNombreGrupo() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText input = new EditText(this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.btnCambiarNombreGrupo);
        lp.setMargins(50, 50, 50, 50);
        input.setLayoutParams(lp);

        alertDialog.setPositiveButton(R.string.lblConfirmar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dbChat.update("Nombre", input.getText().toString());
                        crearMensajeCambioNombre();
                    }
                });

        alertDialog.setNegativeButton(R.string.lblCancelar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.setView(input);
        alertDialog.show();
    }

    private void crearMensajeCambioNombre() {
        dbChat.collection("Mensajes").add(crearMensaje("Se ha cambiado el nombre del grupo"));
    }

    private Mensaje crearMensaje(String texto) {
        Mensaje mensaje = new Mensaje();

        if (isDocente && identificadorUsuario == CORREO) {
            mensaje = new Mensaje(usuarioJavaDocente.getCorreo(),
                    usuarioJavaDocente.getNombre(),
                    usuarioJavaDocente.getApellido1(),
                    usuarioJavaDocente.getApellido2(),
                    texto,
                    new Date(),
                    100.0);
        } else if (isDocente && identificadorUsuario == TELEFONO) {
            mensaje = new Mensaje(usuarioJavaDocente.getTelefono(),
                    usuarioJavaDocente.getNombre(),
                    usuarioJavaDocente.getApellido1(),
                    usuarioJavaDocente.getApellido2(),
                    texto,
                    new Date(),
                    100.0);
        }

        return mensaje;
    }

    private void iniciarOyente() {
        dbChat.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(InfoGrupoActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_SHORT).show();
                    finish();
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    txtNombreGrupo.setText(documentSnapshot.getString("Nombre"));
                }
            }
        });
    }

    private void obtenerExtras() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("chatID")) {
                chatID = getIntent().getExtras().getString("chatID");
            }

            if (getIntent().getExtras().containsKey("isDocente")) {
                isDocente = getIntent().getExtras().getBoolean("isDocente");
            }

            if (getIntent().getExtras().containsKey("nombreChat")) {
                nombreChat = getIntent().getExtras().getString("nombreChat");
            }

            if (getIntent().getExtras().containsKey("identificadorUsuario")) {
                identificadorUsuario = getIntent().getExtras().getInt("identificadorUsuario");
            }

            if (getIntent().getExtras().containsKey("usuarioJavaDocente")) {
                usuarioJavaDocente = getIntent().getParcelableExtra("usuarioJavaDocente");
            }
        }
    }

    private void obtenerIntegrantes() {
        dbChat.collection("Familias")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Familia usuario = new Familia(document.getId(),
                                        document.getString("nombreTutor1"),
                                        document.getString("apellido1Tutor1"),
                                        document.getString("apellido2Tutor1"),
                                        document.getString("correoTutor1"),
                                        document.getString("telefonoTutor1"),
                                        document.getString("nombreTutor2"),
                                        document.getString("apellido1Tutor2"),
                                        document.getString("apellido2Tutor2"),
                                        document.getString("correoTutor2"),
                                        document.getString("telefonoTutor2"));

                                integrantesGrupo.add(usuario);
                            }

                            if (isDocente) {
                                obtenerTonos();
                            }

                            adaptador.notifyDataSetChanged();

                        } else {
                            Toast.makeText(InfoGrupoActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void obtenerTonos() {
        final DecimalFormat df = new DecimalFormat("#.00");

        dbChat.collection("Tonos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                for (int i = 0; i < integrantesGrupo.size(); i++) {
                                    if (document.getId().equals(integrantesGrupo.get(i).getNombreFamilia())) {
                                        integrantesGrupo.get(i).setApellido2Tutor1(integrantesGrupo.get(i).getApellido2Tutor1() + " " + df.format(document.getDouble("PuntuacionTutor1")) + "%");

                                        if (!integrantesGrupo.get(i).getNombreTutor2().equals("")) {
                                            integrantesGrupo.get(i).setApellido2Tutor2(integrantesGrupo.get(i).getApellido2Tutor2() + " " + df.format(document.getDouble("PuntuacionTutor2")) + "%");
                                        }
                                    }
                                }
                            }

                            adaptador.notifyDataSetChanged();
                        } else {
                            Toast.makeText(InfoGrupoActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

}
