package es.uclm.esi.tfg.educhat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class EmailLoginActivity extends AppCompatActivity {

    private final int LOGIN = 0;
    private final int RECUPERACION = 1;

    private EditText txtCorreo;
    private EditText txtContrasena;

    private TextView lblIntroducirCorreoYContrasena;
    private TextView lblOlvidoContrasena;

    private Button btnEntrar;
    private Button btnVolver;

    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private Boolean docente;
    private String coleccion;
    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("docente")) {
            docente = getIntent().getExtras().getBoolean("docente");

            if (docente) {
                coleccion = "Docentes";
            } else {
                coleccion = "Usuarios";
            }
        }

        txtCorreo = (EditText) findViewById(R.id.txtCorreo);
        txtContrasena = (EditText) findViewById(R.id.txtContrasena);

        lblIntroducirCorreoYContrasena = (TextView) findViewById(R.id.lblIntroduirCorreoYContrasena);
        lblOlvidoContrasena = (TextView) findViewById(R.id.lblOlvidoContrasena);

        btnEntrar = (Button) findViewById(R.id.btnEntrar);
        btnVolver = (Button) findViewById(R.id.btnVolver);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        lblOlvidoContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificarVisibilidad(RECUPERACION);
            }
        });

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnEntrar.getText().equals(getString(R.string.btnEntrar))) {
                    registrarUsuario(txtCorreo.getText().toString(), txtContrasena.getText().toString(), false);
                } else if (btnEntrar.getText().equals(getString(R.string.btnEnviarCorreo))) {
                    enviarCorreoRecuperacion();
                }
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(EmailLoginActivity.this, LoginActivity.class);
                ocultarTeclado(txtCorreo);
                startActivity(login);
                finish();
            }
        });

        mostrarTeclado();
    }

    private void registrarUsuario(final String correo, final String contrasena, boolean registroFinal) {

        if (!registroFinal && credencialesValidas(correo, contrasena) && coleccion.equals("Docentes")) {
            db.collection(coleccion)
                    .whereEqualTo("Correo", correo)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(Task<QuerySnapshot> task) {
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(EmailLoginActivity.this, getString(R.string.msgUsuarioNoEncontrado), Toast.LENGTH_SHORT).show();
                            } else {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EmailLoginActivity.this, "Hola, " + task.getResult().getDocuments().get(0).get("Nombre"), Toast.LENGTH_SHORT).show();
                                    obtenerUsuarioJava(task.getResult().getDocuments().get(0));
                                    registrarUsuario(correo, contrasena, true);
                                } else {
                                    Toast.makeText(EmailLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

        } else if (!registroFinal && credencialesValidas(correo, contrasena) && coleccion.equals("Usuarios")) {
            buscarFamiliaBBDD(correo, contrasena, 1);
        } else if (registroFinal) {
            mAuth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            try {
                                if (task.isSuccessful()) {
                                    lanzarMainActivity();
                                } else {
                                    throw task.getException();
                                }
                            } catch (FirebaseAuthUserCollisionException e) {
                                loguearUsuario(correo, contrasena);
                            } catch (FirebaseAuthWeakPasswordException e) {
                                Toast.makeText(EmailLoginActivity.this, getString(R.string.msgContrasenaDebil), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void obtenerUsuarioJava(DocumentSnapshot documento) {
        if (docente) {
            usuarioJavaDocente = new Docente(documento.getId(),
                    documento.getString("Nombre"),
                    documento.getString("Apellido1"),
                    documento.getString("Apellido2"),
                    documento.getString("Correo"),
                    documento.getString("Telefono"));
        } else {
            usuarioJavaFamilia = new Familia(documento.getId(),
                    documento.getString("NombreTutor1"),
                    documento.getString("Apellido1Tutor1"),
                    documento.getString("Apellido2Tutor1"),
                    documento.getString("CorreoTutor1"),
                    documento.getString("TelefonoTutor1"),
                    documento.getString("NombreTutor2"),
                    documento.getString("Apellido1Tutor2"),
                    documento.getString("Apellido2Tutor2"),
                    documento.getString("CorreoTutor2"),
                    documento.getString("TelefonoTutor2"));
        }
    }

    public void lanzarMainActivity() {
        user = mAuth.getCurrentUser();
        Toast.makeText(EmailLoginActivity.this, getString(R.string.msgIdentificacionOK), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
        intent.putExtra("docente", docente);
        //intent.putExtra("procedencia", "correo");
        //intent.putExtra("correo", user.getEmail());

        if (docente) {
            intent.putExtra("usuarioJava", usuarioJavaDocente);
        } else {
            intent.putExtra("usuarioJava", usuarioJavaFamilia);
        }

        startActivity(intent);
        ocultarTeclado(txtContrasena);
        finish();
    }

    private void buscarFamiliaBBDD(final String correo, final String contrasena, int pasada) {

        if (pasada == 1) {
            db.collection(coleccion)
                    .whereEqualTo("CorreoTutor1", correo)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(Task<QuerySnapshot> task) {
                            if (task.getResult().isEmpty()) {
                                buscarFamiliaBBDD(correo, contrasena, 2);
                            } else {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EmailLoginActivity.this, "Hola, " + task.getResult().getDocuments().get(0).get("NombreTutor1"), Toast.LENGTH_SHORT).show();
                                    obtenerUsuarioJava(task.getResult().getDocuments().get(0));
                                    registrarUsuario(correo, contrasena, true);
                                } else {
                                    Toast.makeText(EmailLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        } else if (pasada == 2) {
            db.collection(coleccion)
                    .whereEqualTo("CorreoTutor2", correo)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(Task<QuerySnapshot> task) {
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(EmailLoginActivity.this, getString(R.string.msgUsuarioNoEncontrado), Toast.LENGTH_SHORT).show();
                            } else {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EmailLoginActivity.this, "Hola, " + task.getResult().getDocuments().get(0).get("NombreTutor2"), Toast.LENGTH_SHORT).show();
                                    obtenerUsuarioJava(task.getResult().getDocuments().get(0));
                                    registrarUsuario(correo, contrasena, true);
                                } else {
                                    Toast.makeText(EmailLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    private boolean credencialesValidas(String correo, String contrasena) {
        boolean seguir = false;

        if (!correoValido(correo)) {
            return seguir;
        } else if (!contrasenaValida(contrasena)) {
            return seguir;
        } else {
            seguir = true;
        }

        return seguir;
    }


    private boolean correoValido(String correo) {

        boolean seguir = false;

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(EmailLoginActivity.this, getString(R.string.msgIntroduceCorreo), Toast.LENGTH_SHORT).show();
        } else if (!correo.contains("@")) {
            Toast.makeText(EmailLoginActivity.this, getString(R.string.msgIntroduceCorreo), Toast.LENGTH_SHORT).show();
        } else {
            seguir = true;
        }

        return seguir;
    }

    private boolean contrasenaValida(String contrasena) {
        boolean seguir = false;

        if (TextUtils.isEmpty(contrasena)) {
            Toast.makeText(EmailLoginActivity.this, getString(R.string.msgIntroduceContrasena), Toast.LENGTH_SHORT).show();
        } else {
            seguir = true;
        }

        return seguir;
    }

    private void loguearUsuario(String correo, String contrasena) {
        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            lanzarMainActivity();
                        } else {
                            Toast.makeText(EmailLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void mostrarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void ocultarTeclado(EditText txt) {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
    }

    private void modificarVisibilidad(int modo) {
        if (modo == LOGIN) {
            lblIntroducirCorreoYContrasena.setText(getString(R.string.lblEmailLogin));
            txtContrasena.setVisibility(View.VISIBLE);
            lblOlvidoContrasena.setVisibility(View.VISIBLE);
            btnEntrar.setText(getString(R.string.btnEntrar));
            btnVolver.setVisibility(View.GONE);
        } else if (modo == RECUPERACION) {
            lblIntroducirCorreoYContrasena.setText(getString(R.string.lblIntroducirCorreo));
            txtContrasena.setVisibility(View.GONE);
            lblOlvidoContrasena.setVisibility(View.GONE);
            btnEntrar.setText(getString(R.string.btnEnviarCorreo));
            btnVolver.setVisibility(View.VISIBLE);
        }
    }

    private void enviarCorreoRecuperacion() {
        preguntarUsuario();
    }

    private void preguntarUsuario() {
        final String correo = txtCorreo.getText().toString();

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(EmailLoginActivity.this, getString(R.string.msgIntroduceCorreo), Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(getString(R.string.lblConfirmar));
            dialogo1.setMessage(getString(R.string.msgConfirmarCorreo) + " " + correo + "?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton(getString(R.string.lblConfirmar), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    aceptar(correo);
                    modificarVisibilidad(LOGIN);
                }
            });
            dialogo1.setNegativeButton(getString(R.string.lblCancelar), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {

                }
            });
            dialogo1.show();
        }
    }

    private void aceptar(final String correo) {
        mAuth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailLoginActivity.this, getString(R.string.msgCorreoEnviado) + " " + correo, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(EmailLoginActivity.this, getString(R.string.msgErrorEnvioCorreo), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}