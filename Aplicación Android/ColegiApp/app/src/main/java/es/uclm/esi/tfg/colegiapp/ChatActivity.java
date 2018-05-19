package es.uclm.esi.tfg.colegiapp;

import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
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
import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    public static final int CORREO = 1;
    public static final int TELEFONO = 2;

    final ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2018-05-19");
    final LanguageTranslator translator = new LanguageTranslator();

    private JSONObject credentials_tone;
    private JSONObject credentials_tradu;
    private String username_tone;
    private String password_tone;
    private String username_tradu;
    private String password_tradu;

    final List<String> tonosEmociones = Arrays.asList("anger", "sadness");
    final List<String> tonosLenguaje = Arrays.asList("tentative");

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
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        txtMensaje = (EditText) findViewById(R.id.txtMensaje);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        obtenerExtras();

        db = FirebaseFirestore.getInstance();

        dbChat = db.collection("ChatsGrupales").document(chatID);
        dbMensajes = dbChat.collection("Mensajes");

        lstMensajes = new ArrayList<Mensaje>();
        recyclerViewMensajes = (RecyclerView) findViewById(R.id.recyclerViewMensajes);
        adaptadorListaMensajes = new AdaptadorListaMensajes(ChatActivity.this, lstMensajes);
        recyclerViewMensajes.setAdapter(adaptadorListaMensajes);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewMensajes.setLayoutManager(linearLayoutManager);

        try {
            credentials_tone = new JSONObject(IOUtils.toString(getResources().openRawResource(R.raw.credentials_tone_ibm), "UTF-8")); // Convert the file into a JSON object
            credentials_tradu = new JSONObject(IOUtils.toString(getResources().openRawResource(R.raw.credentials_tradu_ibm), "UTF-8")); // Convert the file into a JSON object

            // Extract the two values
            username_tone = credentials_tone.getString("username");
            password_tone = credentials_tone.getString("password");

            // Extract the two values
            username_tradu = credentials_tradu.getString("username");
            password_tradu = credentials_tradu.getString("password");
        }catch (IOException e) {
            Log.d("Error", "Error en credentials");
        } catch (JSONException e) {
            Log.d("Error", "Error al poner strings");
        }

        toneAnalyzer.setUsernameAndPassword(username_tone, password_tone);
        translator.setUsernameAndPassword(username_tradu, password_tradu);

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
        Mensaje mensaje = crearMensaje();

        if (comprobarTonos(mensaje)) {

            dbMensajes.add(mensaje).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    txtMensaje.setText("");
                    txtMensaje.setEnabled(true);
                }
            });
        } else {
            Toast.makeText(this, "Texto no permitido", Toast.LENGTH_SHORT).show();
            txtMensaje.setText("");
            txtMensaje.setEnabled(true);
        }
    }

    private boolean comprobarTonos(Mensaje mensaje) {
        List<ToneScore> listaTonos;
        boolean correcto = true;

        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(mensaje.getMensaje())
                .modelId("es-en")
                .build();

        TranslationResult result = translator.translate(translateOptions)
                .execute();

        ToneOptions toneOptions = new ToneOptions.Builder().html(result.getTranslations().get(0).getTranslation()).build();
        ToneAnalysis tone = toneAnalyzer.tone(toneOptions).execute();

        listaTonos = tone.getDocumentTone().getTones();

        if (!listaTonos.isEmpty()) {
            for (int i = 0; i < listaTonos.size(); i++) {
                if (tonosEmociones.contains(listaTonos.get(i).getToneId()) ||
                        tonosLenguaje.contains(listaTonos.get(i).getToneId())) {
                    correcto = false;
                    break;
                }
            }
        } else {
            correcto = true;
        }

        return correcto;
    }

    private Mensaje crearMensaje() {
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

        return mensaje;
    }

    public void addMensajeLista(DocumentSnapshot document) {
        Mensaje msj = new Mensaje(document.getString("remitenteID"),
                document.getString("nombre"),
                document.getString("apellido1"),
                document.getString("apellido2"),
                document.getString("mensaje"),
                document.getDate("fecha"));

        lstMensajes.add(msj);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewMensajes.getContext(),
                linearLayoutManager.getOrientation());
        recyclerViewMensajes.addItemDecoration(dividerItemDecoration);
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
