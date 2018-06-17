package es.uclm.esi.tfg.educhat;

import android.content.Intent;
import android.os.StrictMode;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

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

    private double pondAnger;
    private double pondFear;
    private double pondJoy;
    private double pondSadness;

    private double pondAnalytical;
    private double pondConfident;
    private double pondTentative;

    private int pondPeso;

    private boolean datosObtenidos = false;

    private ArrayList<Familia> lstIntegrantes;

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
    private String nombreChat;
    private boolean isDocente;
    private int identificadorUsuario;
    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;
    private String idUsuario;

    private RecyclerView recyclerViewMensajes;
    private AdaptadorListaMensajes adaptadorListaMensajes;
    private ArrayList<Mensaje> lstMensajes;
    private double tonoUltimoMensaje;

    private ImageView imgAddMensaje;
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

        imgAddMensaje = (ImageView) findViewById(R.id.imgAddMensaje);
        txtMensaje = (EditText) findViewById(R.id.txtMensajeAEnviar);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        obtenerExtras();

        db = FirebaseFirestore.getInstance();

        dbChat = db.collection("ChatsGrupales").document(chatID);
        dbMensajes = dbChat.collection("Mensajes");

        lstIntegrantes = new ArrayList<Familia>();
        lstMensajes = new ArrayList<Mensaje>();
        recyclerViewMensajes = (RecyclerView) findViewById(R.id.recyclerViewMensajes);
        adaptadorListaMensajes = new AdaptadorListaMensajes(ChatActivity.this, lstMensajes);
        recyclerViewMensajes.setAdapter(adaptadorListaMensajes);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewMensajes.setLayoutManager(linearLayoutManager);

        try {
            credentials_tone = new JSONObject(obtenerJSON(R.raw.credentials_tone_ibm));
            credentials_tradu = new JSONObject(obtenerJSON(R.raw.credentials_tradu_ibm));

            // Extract the two values
            username_tone = credentials_tone.getString("username");
            password_tone = credentials_tone.getString("password");

            // Extract the two values
            username_tradu = credentials_tradu.getString("username");
            password_tradu = credentials_tradu.getString("password");
        } catch (JSONException e) {
            Toast.makeText(this, R.string.msgErrorJSON, Toast.LENGTH_SHORT).show();
        }

        toneAnalyzer.setUsernameAndPassword(username_tone, password_tone);
        translator.setUsernameAndPassword(username_tradu, password_tradu);

        txtMensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (txtMensaje.getText().length() == 0) {
                    btnEnviar.setEnabled(false);
                } else {
                    if (datosObtenidos) {
                        btnEnviar.setEnabled(true);
                    }
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

        if (!isDocente) {
            imgAddMensaje.setVisibility(View.GONE);
            obtenerScoreActual();
        } else {
            datosObtenidos = true;
        }

        imgAddMensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCalendario();
            }
        });

        iniciarOyentes();
        obtenerIntegrantes();
    }

    private void addCalendario() {
        String correos = "";
        Intent calendario = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);

        for (int i = 0; i < lstIntegrantes.size(); i++) {
            correos = correos + lstIntegrantes.get(i).getCorreoTutor1();

            if (!lstIntegrantes.get(i).getCorreoTutor2().equals("")) {
                correos = correos + ", " + lstIntegrantes.get(i).getCorreoTutor2();
            }

            if (i != lstIntegrantes.size() - 1 && !lstIntegrantes.get(i + 1).getCorreoTutor1().equals("")) {
                correos = correos + ", ";
            }

        }

        calendario.putExtra(Intent.EXTRA_EMAIL,correos);

        startActivity(calendario);
    }

    private String obtenerJSON(int archivo) {
        InputStream iStream = this.getResources().openRawResource(archivo);
        ByteArrayOutputStream byteStream = null;
        try {
            byte[] buffer = new byte[iStream.available()];
            iStream.read(buffer);
            byteStream = new ByteArrayOutputStream();
            byteStream.write(buffer);
            byteStream.close();
            iStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteStream.toString();
    }

    private void enviarMensaje() {
        Mensaje mensaje = crearMensaje();

        if (!isDocente) {
            tonoUltimoMensaje = 0;
            analizarMensaje(mensaje);
        } else {
            tonoUltimoMensaje = 100.0;
        }

        mensaje.setTono(tonoUltimoMensaje);

        dbMensajes.add(mensaje).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                txtMensaje.setText("");
                txtMensaje.setEnabled(true);
            }
        });

    }

    private void analizarMensaje(Mensaje mensaje) {
        List<SentenceAnalysis> listaOraciones;

        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(mensaje.getMensaje())
                .modelId("es-en")
                .build();

        TranslationResult result = translator.translate(translateOptions)
                .execute();

        ToneOptions toneOptions = new ToneOptions.Builder().html(result.getTranslations().get(0).getTranslation()).build();
        ToneAnalysis tone = toneAnalyzer.tone(toneOptions).execute();

        listaOraciones = tone.getSentencesTone();

        if (listaOraciones == null) {
            if (!tone.getDocumentTone().getTones().isEmpty()) {
                analizarTonos(tone.getDocumentTone().getTones(), true);
            } else {
                tonoUltimoMensaje = 75.0;
            }
        } else {
            if (!tone.getSentencesTone().isEmpty()) {
                analizarTonosOraciones(listaOraciones);
            }
        }

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

        if (listaTonos.size() == 0) {
            tonoUltimoMensaje = 75.0;
        }

        for (int i = 0; i < listaTonos.size(); i++) {
            if (listaTonos.get(i).getToneId().equalsIgnoreCase(ANGER)) {
                scoreMensaje -= listaTonos.get(i).getScore() * pondAnger;
                tonoNegativo = true;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(FEAR)) {
                scoreMensaje -= listaTonos.get(i).getScore() * pondFear;
                tonoNegativo = true;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(SADNESS)) {
                scoreMensaje -= listaTonos.get(i).getScore() * pondSadness;
                tonoNegativo = true;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(JOY)) {
                scoreMensaje += listaTonos.get(i).getScore() * pondJoy;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(ANALYTICAL)) {
                scoreCorrector += listaTonos.get(i).getScore() * pondAnalytical * pondPeso;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(CONFIDENT)) {
                scoreCorrector += listaTonos.get(i).getScore() * pondConfident * pondPeso;
            } else if (listaTonos.get(i).getToneId().equalsIgnoreCase(TENTATIVE)) {
                scoreCorrector += listaTonos.get(i).getScore() * pondTentative * pondPeso;
            }
        }

        Log.d("ScoreMensaje", Double.toString(scoreMensaje));
        Log.d("ScoreCorrector", Double.toString(scoreCorrector));

        if (tonoNegativo) {
            scoreUsuario = scoreUsuario + (scoreMensaje - scoreCorrector);
            tonoUltimoMensaje = (tonoUltimoMensaje + (scoreMensaje - scoreCorrector)) * 10;
        } else {
            scoreUsuario = scoreUsuario + (scoreMensaje + scoreCorrector);
            tonoUltimoMensaje = (tonoUltimoMensaje + (scoreMensaje + scoreCorrector)) * 10;
        }

        Log.d("ScoreUltimo", Double.toString(tonoUltimoMensaje));

        if (scoreUsuario < 0.0) {
            scoreUsuario = 0.0;
        } else if (scoreUsuario > 100.0) {
            scoreUsuario = 100.0;
        }

        if (tonoUltimoMensaje < 0.0) {
            tonoUltimoMensaje = 0.0;
        } else if (tonoUltimoMensaje > 100.0) {
            tonoUltimoMensaje = 100.0;
        }

        if (actualizarBBDD) {
            actualizarScoreUsuarioBBDD();
        }

    }

    private void actualizarScoreUsuarioBBDD() {
        dbChat.collection("Tonos").document(usuarioJavaFamilia.getNombreFamilia()).update(campo, scoreUsuario);
    }

    private void obtenerScoreActual() {

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
                    scoreUsuario = task.getResult().getDouble(campo);

                    obtenerPonderacionTonos();
                }
            }
        });

    }

    private void obtenerPonderacionTonos() {
        dbChat.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                HashMap<String, Object> ponderacion = ((HashMap<String, Object>) documentSnapshot.get("PonderacionTonos"));

                pondAnger = Double.parseDouble(ponderacion.get("PondAnger").toString());
                pondFear= Double.parseDouble(ponderacion.get("PondFear").toString());
                pondJoy= Double.parseDouble(ponderacion.get("PondJoy").toString());
                pondSadness= Double.parseDouble(ponderacion.get("PondSadness").toString());

                pondAnalytical= Double.parseDouble(ponderacion.get("PondAnalytical").toString());
                pondConfident= Double.parseDouble(ponderacion.get("PondConfident").toString());
                pondTentative= Double.parseDouble(ponderacion.get("PondTentative").toString());

                pondPeso = Integer.parseInt(ponderacion.get("PondPeso").toString());

                datosObtenidos = true;
            }
        });
    }

    private Mensaje crearMensaje() {
        Mensaje mensaje = null;

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
                document.getDate("fecha"),
                document.getDouble("tono"),
                isDocente);

        Log.d("MensajeChat", msj.toString());

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
                nombreChat = getIntent().getExtras().getString("nombreChat");
                setTitle(nombreChat);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chatactivity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuBtnInfoGrupo:
                lanzarActivityInfoGrupo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void lanzarActivityInfoGrupo() {
        Intent i = new Intent(this, InfoGrupoActivity.class);

        i.putExtra("chatID", chatID);
        i.putExtra("isDocente", isDocente);
        i.putExtra("nombreChat", nombreChat);
        i.putExtra("identificadorUsuario", identificadorUsuario);
        i.putExtra("usuarioJavaDocente", usuarioJavaDocente);

        startActivity(i);
    }

    private void iniciarOyentes() {
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
                        case MODIFIED:
                            break;
                        case REMOVED:
                            break;
                    }
                }
            }
        });

        dbChat.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(ChatActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_SHORT).show();
                    finish();
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    setTitle(documentSnapshot.getString("Nombre"));
                }
            }
        });
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

                                lstIntegrantes.add(usuario);
                            }
                        } else {
                            Toast.makeText(ChatActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

}
