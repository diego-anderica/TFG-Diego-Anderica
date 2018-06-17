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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private final int INVISIBLE = 0;
    private final int VISIBLE = 1;

    private TextView lblEntradaTfno;
    private TextView lblInfoTfno;
    private EditText txtTelefono;
    private Button btnEnviarCodigo;

    private TextView lblIntroducirCodigo;
    private EditText txtCodigo;
    private Button btnValidarCodigo;

    private String numTelefono;

    private String mVerificationId;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseFirestore db;

    private Boolean docente;
    private String coleccion;

    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("docente")) {
            docente = getIntent().getExtras().getBoolean("docente");

            if (docente) {
                coleccion = "Docentes";
            } else {
                coleccion = "Usuarios";
            }
        }

        lblEntradaTfno = (TextView) findViewById(R.id.lblEntradaTfno);
        lblInfoTfno = (TextView) findViewById(R.id.lblInfoTfno);
        txtTelefono = (EditText) findViewById(R.id.txtTelefono);
        btnEnviarCodigo = (Button) findViewById(R.id.btnEntrarCTfno);

        lblIntroducirCodigo = (TextView) findViewById(R.id.lblIntroducirCodigo);
        txtCodigo = (EditText) findViewById(R.id.txtCodigo);
        btnValidarCodigo = (Button) findViewById(R.id.btnValidarCodigo);

        db = FirebaseFirestore.getInstance();

        modificarVisibilidadSegundaParte(INVISIBLE);

        txtTelefono.requestFocus();
        mostrarTeclado();

        btnEnviarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preguntarUsuario();
            }
        });

        btnValidarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCodigo();
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    private void mostrarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void ocultarTeclado(EditText txt) {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
    }

    private void modificarVisibilidadPrimeraParte(int modo) {
        if (modo == VISIBLE) {
            lblEntradaTfno.setVisibility(View.VISIBLE);
            lblInfoTfno.setVisibility(View.VISIBLE);
            txtTelefono.setVisibility(View.VISIBLE);
            btnEnviarCodigo.setVisibility(View.VISIBLE);
        } else if (modo == INVISIBLE) {
            lblEntradaTfno.setVisibility(View.GONE);
            lblInfoTfno.setVisibility(View.GONE);
            txtTelefono.setVisibility(View.GONE);
            btnEnviarCodigo.setVisibility(View.GONE);
        }
    }

    private void modificarVisibilidadSegundaParte(int modo) {
        if (modo == VISIBLE) {
            lblIntroducirCodigo.setVisibility(View.VISIBLE);
            txtCodigo.setVisibility(View.VISIBLE);
            btnValidarCodigo.setVisibility(View.VISIBLE);
        } else if (modo == INVISIBLE) {
            lblIntroducirCodigo.setVisibility(View.GONE);
            txtCodigo.setVisibility(View.GONE);
            btnValidarCodigo.setVisibility(View.GONE);
        }
    }

    private void enviarCodigo() {
        //TODO: Controlar prefijos telefónicos
        String numTelefono = txtTelefono.getText().toString();

        numTelefono = "+34" + numTelefono;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                numTelefono,
                60,
                TimeUnit.SECONDS,
                PhoneLoginActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signInWithCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgFalloAutenticacion), Toast.LENGTH_LONG).show();
                        modificarVisibilidadPrimeraParte(VISIBLE);
                        modificarVisibilidadSegundaParte(INVISIBLE);
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        modificarVisibilidadPrimeraParte(INVISIBLE);
                        modificarVisibilidadSegundaParte(VISIBLE);

                        txtCodigo.requestFocus();

                        mVerificationId = verificationId;
                        mResendToken = token;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String verificationId) {
                        super.onCodeAutoRetrievalTimeOut(verificationId);
                        Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgTiempoEsperaAgotado), Toast.LENGTH_LONG).show();
                        modificarVisibilidadPrimeraParte(VISIBLE);
                        modificarVisibilidadSegundaParte(INVISIBLE);
                    }
                }
        );
    }

    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgIdentificacionOK), Toast.LENGTH_SHORT).show();
                    FirebaseUser user = task.getResult().getUser();
                    lanzarMainActivity(user);
                } else {
                    Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                    modificarVisibilidadPrimeraParte(VISIBLE);
                    modificarVisibilidadSegundaParte(INVISIBLE);
                }
            }
        });
    }

    public void lanzarMainActivity(FirebaseUser user) {
        Intent intent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        intent.putExtra("docente", docente);

        if (docente) {
            intent.putExtra("usuarioJava", usuarioJavaDocente);
        } else {
            intent.putExtra("usuarioJava", usuarioJavaFamilia);
        }

        startActivity(intent);
        ocultarTeclado(txtCodigo);
        finish();
    }

    private void validarCodigo() {
        String code = txtCodigo.getText().toString();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgIntroduceCodigo), Toast.LENGTH_SHORT).show();
        } else {
            signInWithCredential(PhoneAuthProvider.getCredential(mVerificationId, code));
        }
    }

    private void preguntarUsuario() {
        final String telefono = txtTelefono.getText().toString();

        if (TextUtils.isEmpty(telefono)) {
            Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgIntroducirNumeroTfno), Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle(getString(R.string.lblConfirmar));
            dialogo1.setMessage(getString(R.string.msgConfirmarTelefono) + " " + telefono + "?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton(getString(R.string.lblConfirmar), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    aceptar(telefono);
                }
            });
            dialogo1.setNegativeButton(getString(R.string.lblCancelar), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {

                }
            });
            dialogo1.show();
        }
    }

    private void aceptar(final String telefono) {

        if (coleccion.equals("Docentes")) {
            db.collection(coleccion)
                    .whereEqualTo("Telefono", "+34" + telefono)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(Task<QuerySnapshot> task) {
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgUsuarioNoEncontrado), Toast.LENGTH_SHORT).show();
                            } else {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PhoneLoginActivity.this, "Hola, " + task.getResult().getDocuments().get(0).get("Nombre"), Toast.LENGTH_SHORT).show();
                                    //obtenerUsuarioJava(task.getResult().getDocuments().get(0));
                                    enviarCodigo();
                                } else {
                                    Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        } else if (coleccion.equals("Usuarios")) {
            buscarFamiliaEnBBDD(telefono, 1);
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

    private void buscarFamiliaEnBBDD(final String telefono, int pasada) {
        if (pasada == 1) {
            db.collection(coleccion)
                    .whereEqualTo("TelefonoTutor1", "+34" + telefono)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(Task<QuerySnapshot> task) {
                            if (task.getResult().isEmpty()) {
                                buscarFamiliaEnBBDD(telefono, 2);
                            } else {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PhoneLoginActivity.this, "Hola, " + task.getResult().getDocuments().get(0).get("NombreTutor1"), Toast.LENGTH_SHORT).show();
                                    //obtenerUsuarioJava(task.getResult().getDocuments().get(0));
                                    enviarCodigo();
                                } else {
                                    Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        } else if (pasada == 2) {
            db.collection(coleccion)
                    .whereEqualTo("TelefonoTutor2", "+34" + telefono)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(Task<QuerySnapshot> task) {
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgUsuarioNoEncontrado), Toast.LENGTH_SHORT).show();
                            } else {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PhoneLoginActivity.this, "Hola, " + task.getResult().getDocuments().get(0).get("NombreTutor2"), Toast.LENGTH_SHORT).show();
                                    //obtenerUsuarioJava(task.getResult().getDocuments().get(0));
                                    enviarCodigo();
                                } else {
                                    Toast.makeText(PhoneLoginActivity.this, getString(R.string.msgErrorIdentificacion), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

}
