package es.uclm.esi.tfg.colegiapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    public static final int CORREO = 1;
    public static final int TELEFONO = 2;

    private String mUsername;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore db;
    private CollectionReference coleccionChatsGrupales;

    private Boolean isDocente;
    private int identificadorUsuario;

    private ListView lstChats;

    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;

    private ArrayList<ChatGrupal> chatsGrupales;
    private HashMap<String, String> idNombreChatsGrupales;
    private AdaptadorListaChats adaptador;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onResume();

        setContentView(R.layout.activity_main);

        setTitle("Chats");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;

        db = FirebaseFirestore.getInstance();

        coleccionChatsGrupales = db.collection("ChatsGrupales");

        // Inicializar Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // El usuario no se ha identificado, se lanza la actividad de logueo
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            /*mUsername = mFirebaseUser.getDisplayName();

            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }*/

        }

        obtenerExtras();

        lstChats = (ListView) findViewById(R.id.lstChats);

        lstChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lanzarChatActivity(i);
            }
        });

        chatsGrupales = new ArrayList<ChatGrupal>();
        idNombreChatsGrupales = new HashMap<>();
        adaptador = new AdaptadorListaChats(this, chatsGrupales);
        lstChats.setAdapter(adaptador);

        progressDialog = ProgressDialog.show(MainActivity.this, "",
                getString(R.string.msgCargandoChats), true);
        poblarChats();

        invalidateOptionsMenu();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    private void obtenerExtras() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("docente")) {
                isDocente = getIntent().getExtras().getBoolean("docente");
            }

            if (mFirebaseUser.getEmail() != null) {
                identificadorUsuario = CORREO;
            } else if (mFirebaseUser.getPhoneNumber() != null) {
                identificadorUsuario = TELEFONO;
            }

            if (isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaDocente = getIntent().getParcelableExtra("usuarioJava");
            } else if (!isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaFamilia = getIntent().getParcelableExtra("usuarioJava");
            }

            guardarEstado();
        }
    }

    private void lanzarChatActivity(int i) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("docente", isDocente);

        if (isDocente) {
            intent.putExtra("usuarioJava", usuarioJavaDocente);
        } else {
            intent.putExtra("usuarioJava", usuarioJavaFamilia);
        }

        intent.putExtra("chatID", chatsGrupales.get(i).getId());

        startActivity(intent);

    }

    private void poblarChats() {
        coleccionChatsGrupales
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (!document.getId().equals("Control")) {

                                    comprobarPertenencia(document);

                                }
                            }

                            adaptador.notifyDataSetChanged();
                            progressDialog.dismiss();

                        } else {
                            Toast.makeText(MainActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void comprobarPertenencia(DocumentSnapshot document) {
        if (isDocente) {
            HashMap<String, Object> administrador = ((HashMap<String, Object>) document.get("Administrador"));

            if (usuarioJavaDocente.getCorreo().equals(administrador.get("Correo").toString()) ||
                    usuarioJavaDocente.getTelefono().equals(administrador.get("Telefono").toString())) {
                chatsGrupales.add(new ChatGrupal(document.getId(), document.getString("Nombre")));
            }

        } else {
            poblarChatsFamilia(document);
        }
    }

    private void poblarChatsFamilia(final DocumentSnapshot documentChat) {
        coleccionChatsGrupales
                .document(documentChat.getId())
                .collection("Familias")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {

                                if (usuarioJavaFamilia.getNombreFamilia().equals(document.getId())) {
                                    chatsGrupales.add(new ChatGrupal(documentChat.getId(), documentChat.getString("Nombre")));
                                }

                                adaptador.notifyDataSetChanged();
                            }
                        }

                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        guardarEstado();
    }

    private void guardarEstado() {
        SharedPreferences sharedPref = (MainActivity.this).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json;

        if (isDocente) {
            json = gson.toJson(usuarioJavaDocente);
        } else {
            json = gson.toJson(usuarioJavaFamilia);
        }

        editor.putBoolean("docente", isDocente);
        editor.putInt("identificadorUsuario", identificadorUsuario);
        editor.putString("usuarioJava", json);

        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = (MainActivity.this).getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json;

        isDocente = sharedPref.getBoolean("docente", false);
        identificadorUsuario = sharedPref.getInt("identificadorUsuario", 2);

        json = sharedPref.getString("usuarioJava", "");

        if (isDocente) {
            usuarioJavaDocente = gson.fromJson(json, Docente.class);
        } else {
            usuarioJavaFamilia = gson.fromJson(json, Familia.class);
        }

        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.mnuBtnCrearGrupo).setVisible(isDocente);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuBtnCrearGrupo:
                crearGrupo();
                return true;
            case R.id.mnuBtnSalir:
                salir();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void salir() {
        mFirebaseAuth.signOut();
        mFirebaseUser = null;
        mUsername = ANONYMOUS;
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void crearGrupo() {
        Intent intent = new Intent(MainActivity.this, CrearGrupoActivity.class);

        intent.putExtra("identificadorDocente", identificadorUsuario);
        intent.putExtra("usuarioJavaDocente", usuarioJavaDocente);

        startActivity(intent);
    }
}
