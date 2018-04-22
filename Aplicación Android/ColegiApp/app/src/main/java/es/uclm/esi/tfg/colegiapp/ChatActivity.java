package es.uclm.esi.tfg.colegiapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    public static final int CORREO = 1;
    public static final int TELEFONO = 2;

    private FirebaseFirestore db;
    private DocumentReference dbChat;
    private CollectionReference dbMensajes;

    private String chatID;
    private boolean isDocente;
    private int identificadorUsuario;
    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;

    private RecyclerView recyclerViewMensajes;
    private AdaptadorListaMensajes adaptadorListaMensajes;
    private ArrayList<Mensaje> lstMensajes;

    private EditText txtMensaje;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        txtMensaje = (EditText) findViewById(R.id.lblMensaje);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        obtenerExtras();

        db = FirebaseFirestore.getInstance();

        dbChat = db.collection("ChatsGrupales").document(chatID);
        dbMensajes = dbChat.collection("Mensajes");

        lstMensajes = new ArrayList<Mensaje>();
        recyclerViewMensajes = (RecyclerView) findViewById(R.id.recyclerViewMensajes);
        adaptadorListaMensajes = new AdaptadorListaMensajes(ChatActivity.this, lstMensajes);
        recyclerViewMensajes.setAdapter(adaptadorListaMensajes);
        recyclerViewMensajes.setLayoutManager(new LinearLayoutManager(this));

        //obtenerMensajes();

        txtMensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2 == 0) {
                    btnEnviar.setEnabled(false);
                } else {
                    btnEnviar.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEnviar.setEnabled(false);
                txtMensaje.setEnabled(false);
                enviarMensaje();
            }
        });

        dbMensajes.orderBy("fecha").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(ChatActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_SHORT).show();
                    finish();
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            addMensajeLista(dc.getDocument());
                            break;
                        /*case MODIFIED:
                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                            break;*/
                    }
                }
            }
        });
    }

    private void enviarMensaje() {
        Mensaje mensaje = new Mensaje();

        if (isDocente && identificadorUsuario == CORREO) {
            mensaje = new Mensaje(usuarioJavaDocente.getCorreo(),
                    usuarioJavaDocente.getNombre(),
                    usuarioJavaDocente.getApellido1(),
                    usuarioJavaDocente.getApellido2(),
                    txtMensaje.getText().toString(),
                    new Date());
        } else if (isDocente && identificadorUsuario == TELEFONO) {
            mensaje = new Mensaje(usuarioJavaDocente.getTelefono(),
                    usuarioJavaDocente.getNombre(),
                    usuarioJavaDocente.getApellido1(),
                    usuarioJavaDocente.getApellido2(),
                    txtMensaje.getText().toString(),
                    new Date());
        } else if (!isDocente && identificadorUsuario == CORREO) {
            String id = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (id.equals(usuarioJavaFamilia.getCorreoTutor1())) {
                mensaje = new Mensaje(id,
                        usuarioJavaFamilia.getNombreTutor1(),
                        usuarioJavaFamilia.getApellido1Tutor1(),
                        usuarioJavaFamilia.getApellido2Tutor1(),
                        txtMensaje.getText().toString(),
                        new Date());
            } else if (id.equals(usuarioJavaFamilia.getCorreoTutor2())) {
                mensaje = new Mensaje(id,
                        usuarioJavaFamilia.getNombreTutor2(),
                        usuarioJavaFamilia.getApellido1Tutor2(),
                        usuarioJavaFamilia.getApellido2Tutor2(),
                        txtMensaje.getText().toString(),
                        new Date());
            }

        } else if (!isDocente && identificadorUsuario == TELEFONO) {
            String id = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

            if (id.equals(usuarioJavaFamilia.getTelefonoTutor1())) {
                mensaje = new Mensaje(id,
                        usuarioJavaFamilia.getNombreTutor1(),
                        usuarioJavaFamilia.getApellido1Tutor1(),
                        usuarioJavaFamilia.getApellido2Tutor1(),
                        txtMensaje.getText().toString(),
                        new Date());
            } else if (id.equals(usuarioJavaFamilia.getTelefonoTutor2())) {
                mensaje = new Mensaje(id,
                        usuarioJavaFamilia.getNombreTutor2(),
                        usuarioJavaFamilia.getApellido1Tutor2(),
                        usuarioJavaFamilia.getApellido2Tutor2(),
                        txtMensaje.getText().toString(),
                        new Date());
            }
        }

        dbMensajes.add(mensaje).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                txtMensaje.setText("");
                txtMensaje.setEnabled(true);
            }
        });
    }

    public void addMensajeLista(DocumentSnapshot document) {
        Mensaje msj = new Mensaje(document.getString("remitenteID"),
                document.getString("nombre"),
                document.getString("apellido1"),
                document.getString("apellido2"),
                document.getString("mensaje"),
                document.getDate("fecha"));

        lstMensajes.add(msj);
        adaptadorListaMensajes.notifyItemInserted(lstMensajes.size() - 1);
        recyclerViewMensajes.scrollToPosition(lstMensajes.size() - 1);
    }

    private void obtenerExtras() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("nombreChat")) {
                setTitle(getIntent().getExtras().getString("nombreChat"));
            }

            if (getIntent().getExtras().containsKey("isDocente")) {
                isDocente = getIntent().getBooleanExtra("isDocente", false);
            }

            if (getIntent().getExtras().containsKey("chatID")) {
                chatID = getIntent().getExtras().getString("chatID");
            }

            if (getIntent().getExtras().containsKey("identificadorUsuario")) {
                identificadorUsuario = getIntent().getExtras().getInt("identificadorUsuario");
            }

            if (isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaDocente = getIntent().getParcelableExtra("usuarioJava");
            } else if (!isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaFamilia = getIntent().getParcelableExtra("usuarioJava");
            }
        }
    }
}
