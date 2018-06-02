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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.SentenceAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final int CORREO = 1;
    private static final int TELEFONO = 2;

    private final ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2018-05-19");
    private final LanguageTranslator translator = new LanguageTranslator();

    private final String ANGER = "anger";
    private final String FEAR = "fear";
    private final String JOY = "joy";
    private final String SADNESS = "sadness";
    private final String CONFIDENT = "confident";
    private final String TENTATIVE = "tentative";
    private final String ANALYTICAL = "analytical";

    private static final double POND_ANGER = 33.333333;
    private static final double POND_FEAR = 29.166666666;
    private static final double POND_JOY = 8.33333333;
    private static final double POND_SADNESS = 29.166666666;

    private static final double POND_ANALYTICAL = 0.666666;
    private static final double POND_CONFIDENT = 0.1666666;
    private static final double POND_TENTATIVE = 0.1666666;

    private static final int POND_PESO = 10;

    private JSONObject credentials_tone;
    private JSONObject credentials_tradu;
    private String username_tone;
    private String password_tone;
    private String username_tradu;
    private String password_tradu;

    private FirebaseFirestore db;
    private DocumentReference dbChat;
    private CollectionReference dbMensajes;

    private String chatID;
    private boolean isDocente;
    private int identificadorUsuario;
    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;
    private String idUsuario;

    private RecyclerView recyclerViewMensajes;
    private AdaptadorListaMensajes adaptadorListaMensajes;
    private ArrayList<Mensaje> lstMensajes;

    private EditText txtMensaje;
    private Button btnEnviar;
    private LinearLayoutManager linearLayoutManager;

    private String campo;
    private double scoreUsuario = 0.0;

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

        if (!isDocente) {
            obtenerScoreActual();
        }
    }

    private void enviarMensaje() {
        Mensaje mensaje = crearMensaje();

        dbMensajes.add(mensaje).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                txtMensaje.setText("");
                txtMensaje.setEnabled(true);
            }
        });

        if (!isDocente) {
            analizarMensaje(mensaje);
        }
    }

    private void analizarMensaje(Mensaje mensaje) {
        List<SentenceAnalysis> listaOraciones;
        List<ToneScore> listaTonos;
        boolean correcto = true;
        double score;

        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(mensaje.getMensaje())
                .modelId("es-en")
                .build();

        TranslationResult result = translator.translate(translateOptions)
                .execute();

        ToneOptions toneOptions = new ToneOptions.Builder().html(result.getTranslations().get(0).getTranslation()).build();
        ToneAnalysis tone = toneAnalyzer.tone(toneOptions).execute();

        //analizarMensaje (tone, mensaje.getRemitenteId());

        listaOraciones = tone.getSentencesTone();

        if (listaOraciones == null) {
            if (!tone.getDocumentTone().getTones().isEmpty()) {
                analizarTonos(tone.getDocumentTone().getTones(), true);
            }
        } else {
            if (!tone.getSentencesTone().isEmpty()) {
                analizarTonosOraciones(listaOraciones);
            }
        }

        //Log.d("ListaOraciones", listaOraciones.toString());

        /*if (listaOraciones != null && !listaOraciones.isEmpty()) {
            for (int i = 0; i < listaOraciones.size(); i++) {
                listaTonos = listaOraciones.get(i).getTones();

                for (int j = 0; j < listaTonos.size(); j++) {
                    if (listaTonos.get(j).getToneId().equalsIgnoreCase(ANGER)) {

                    }
                }
            }
        }*/

        /*if (!listaTonos.isEmpty()) {
            for (int i = 0; i < listaTonos.size(); i++) {
                if (tonosEmociones.contains(listaTonos.get(i).getToneId()) ||
                        tonosLenguaje.contains(listaTonos.get(i).getToneId())) {
                    correcto = false;
                    break;
                }
            }
        } else {
            correcto = true;
        }*/
    }

    private void analizarTonosOraciones(List<SentenceAnalysis> listaOraciones) {
        for (int i = 0; i < listaOraciones.size(); i++) {

            if (i == (listaOraciones.size() - 1)) {
                analizarTonos(listaOraciones.get(i).getTones(), true);
            } else {
                analizarTonos(listaOraciones.get(i).getTones(), false);
            }

        }
    }

    private void analizarTonos(List<ToneScore> listaTonos, boolean actualizarBBDD) {
        double scoreMensaje = 0.0;
        double scoreCorrector = 0.0;
        boolean tonoNegativo = false;

        for (int i = 0; i < listaTonos.size(); i++) {
            if (listaTonos.get(i).getToneId().equalsIgnoreCase(ANGER)) {
                scoreMensaje -= listaTonos.get(i).getScore() * POND_ANGER;
                tonoNegativo = true;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(FEAR)) {
                scoreMensaje -= listaTonos.get(i).getScore() * POND_FEAR;
                tonoNegativo = true;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(SADNESS)) {
                scoreMensaje -= listaTonos.get(i).getScore() * POND_SADNESS;
                tonoNegativo = true;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(JOY)) {
                scoreMensaje += listaTonos.get(i).getScore() * POND_JOY;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(ANALYTICAL)) {
                scoreCorrector += listaTonos.get(i).getScore() * POND_ANALYTICAL * POND_PESO;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(CONFIDENT)) {
                scoreCorrector += listaTonos.get(i).getScore() * POND_CONFIDENT * POND_PESO;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(TENTATIVE)) {
                scoreCorrector += listaTonos.get(i).getScore() * POND_TENTATIVE * POND_PESO;
            }
        }

        Log.d("Score", "ScoreUsuario: " + scoreUsuario);
        Log.d("Score", "ScoreMensaje: " + scoreMensaje);
        Log.d("Score", "ScoreCorrector: " + scoreCorrector);

        if (tonoNegativo) {
            scoreUsuario = scoreUsuario + (scoreMensaje - scoreCorrector);
        } else {
            scoreUsuario = scoreUsuario + (scoreMensaje + scoreCorrector);
        }

        if (scoreUsuario < 0.0) {
            scoreUsuario = 0.0;
        } else if (scoreUsuario > 100.0) {
            scoreUsuario = 100.0;
        }

        if (actualizarBBDD) {
            actualizarScoreUsuarioBBDD();
        }

    }

    private void actualizarScoreUsuarioBBDD() {
        dbChat.collection("Tonos").document(usuarioJavaFamilia.getNombreFamilia()).update(campo, scoreUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d ("ActualizarScore", "Se ha actualizado correctamente: " + scoreUsuario);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d ("ActualizarScore", "Ha ocurrido un error al actualizar Score");
            }
        });
    }

    private void obtenerScoreActual () {

        if (identificadorUsuario == CORREO) {
            idUsuario = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (idUsuario.equals(usuarioJavaFamilia.getCorreoTutor1())) {
                campo = "PuntuacionTutor1";
            } else if (idUsuario.equals(usuarioJavaFamilia.getCorreoTutor2())) {
                campo = "PuntuacionTutor2";
            }

        } else if (identificadorUsuario == TELEFONO) {
            idUsuario = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

            if (idUsuario.equals(usuarioJavaFamilia.getTelefonoTutor1())) {
                campo = "PuntuacionTutor1";
            } else if (idUsuario.equals(usuarioJavaFamilia.getTelefonoTutor2())) {
                campo = "PuntuacionTutor2";
            }

        }

        dbChat.collection("Tonos").document(usuarioJavaFamilia.getNombreFamilia()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    scoreUsuario = document.getDouble(campo);
                }
            }
        });

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
