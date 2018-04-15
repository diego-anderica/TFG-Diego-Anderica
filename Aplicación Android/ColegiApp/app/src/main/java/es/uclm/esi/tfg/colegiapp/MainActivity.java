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
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";

    private String mUsername;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore db;

    private Boolean isDocente;
    private String identificadorUsuario;

    private ListView lstChats;

    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;

    private ArrayList<String> chatsGrupales;
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

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("docente")) {
                isDocente = getIntent().getExtras().getBoolean("docente");
            }

            if (mFirebaseUser.getEmail() != null) {
                identificadorUsuario = mFirebaseUser.getEmail();
            } else if (mFirebaseUser.getPhoneNumber() != null) {
                identificadorUsuario = mFirebaseUser.getPhoneNumber();
            }

            if (isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaDocente = getIntent().getParcelableExtra("usuarioJava");
            } else if (!isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaFamilia = getIntent().getParcelableExtra("usuarioJava");
            }

            guardarEstado();
        }

        lstChats = (ListView) findViewById(R.id.lstChats);
        chatsGrupales = new ArrayList<String>();
        adaptador = new AdaptadorListaChats(this, chatsGrupales);
        lstChats.setAdapter(adaptador);

        progressDialog = ProgressDialog.show(MainActivity.this, "",
                getString(R.string.msgCargandoChats), true);
        obtenerChats();

        invalidateOptionsMenu();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    private void obtenerChats() {
        if (isDocente) {
            poblarChatsDocente();
        } else {
            //poblarChatsFamilia();
        }
    }

    private void poblarChatsDocente() {
        db.collection("ChatsGrupales")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (!document.getId().equals("Control")) {
                                    String nombre = document.getString("Nombre");
                                    HashMap<String, Object> administrador = ((HashMap<String, Object>) document.get("Administrador"));

                                    if (identificadorUsuario.equals(administrador.get("Correo").toString()) ||
                                            identificadorUsuario.equals(administrador.get("Telefono").toString())) {
                                        chatsGrupales.add(nombre);
                                    }

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
        editor.putString("identificadorUsuario", identificadorUsuario);
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
        identificadorUsuario = sharedPref.getString("identificadorUsuario", "");

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
