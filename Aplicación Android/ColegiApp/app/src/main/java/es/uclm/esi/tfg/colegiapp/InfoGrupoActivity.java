package es.uclm.esi.tfg.colegiapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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
    private Button btnCambiarImagenGrupo;
    private Button btnCambiarNombreGrupo;

    private ArrayList<Familia> integrantesGrupo;
    private AdaptadorListaFamilias adaptador;

    private FirebaseFirestore db;
    private DocumentReference dbChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_grupo);

        imgGrupo = (ImageView) findViewById(R.id.imgGrupo);
        txtNombreGrupo = (EditText) findViewById(R.id.txtNombreGrupo);
        txtNuevoNombre = (EditText) findViewById(R.id.txtNuevoNombre);
        btnCambiarImagenGrupo = (Button) findViewById(R.id.btnCambiarImagenGrupo);
        btnCambiarNombreGrupo = (Button) findViewById(R.id.btnCambiarNombreGrupo);

        obtenerExtras();

        db = FirebaseFirestore.getInstance();
        dbChat = db.collection("ChatsGrupales").document(chatID);

        rellenarInfoGrupo();
        iniciarOyente();

        if (!isDocente) {
            btnCambiarImagenGrupo.setVisibility(View.GONE);
            btnCambiarNombreGrupo.setVisibility(View.GONE);
        }

        btnCambiarImagenGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarImagenGrupo();
            }
        });

        btnCambiarNombreGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarNombreGrupo();
            }
        });
    }

    private void rellenarInfoGrupo() {
        setTitle(R.string.lblInfoDeGrupo);
        txtNombreGrupo.setText(nombreChat);
    }

    private void cambiarImagenGrupo() {

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
                    new Date());
        } else if (isDocente && identificadorUsuario == TELEFONO) {
            mensaje = new Mensaje(usuarioJavaDocente.getTelefono(),
                    usuarioJavaDocente.getNombre(),
                    usuarioJavaDocente.getApellido1(),
                    usuarioJavaDocente.getApellido2(),
                    texto,
                    new Date());
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

}
