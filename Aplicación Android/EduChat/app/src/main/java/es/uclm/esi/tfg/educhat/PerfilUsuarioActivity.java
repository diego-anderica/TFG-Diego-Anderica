package es.uclm.esi.tfg.educhat;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.lang3.StringUtils;

public class PerfilUsuarioActivity extends AppCompatActivity {
    //Constante de rastreo de intent
    private static final int PICK_IMAGE_REQUEST = 220;

    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;

    private Familia familia;
    private String idFamilia;
    private boolean isDocente;
    private String procedencia;
    private String nombreImagen;

    private ImageView imgPerfil;
    private Button btnCambiarImagenPerfil;
    private Button btnEliminarImagenPerfil;

    private TextView lblNombreTutor1;
    private TextView lblCorreoTutor1;
    private TextView lblTelefonoTutor1;
    private TextView lblNombreTutor2;
    private TextView lblCorreoTutor2;
    private TextView lblTelefonoTutor2;

    private TextView txtNombreTutor1;
    private TextView txtCorreoTutor1;
    private TextView txtTelefonoTutor1;
    private TextView txtNombreTutor2;
    private TextView txtCorreoTutor2;
    private TextView txtTelefonoTutor2;

    private View divider3;
    private View divider4;
    private View divider5;

    private Uri rutaImagen;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        setTitle(R.string.tituloPerfilUsuarioActivity);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        db = FirebaseFirestore.getInstance();

        imgPerfil = (ImageView) findViewById(R.id.imgPerfil);
        btnCambiarImagenPerfil = (Button) findViewById(R.id.btnCambiarImagenPerfil);
        btnEliminarImagenPerfil = (Button) findViewById(R.id.btnEliminarImagenPerfil);

        lblNombreTutor1 = (TextView) findViewById(R.id.lblNombreTutor1);
        lblCorreoTutor1 = (TextView) findViewById(R.id.lblCorreoTutor1);
        lblTelefonoTutor1 = (TextView) findViewById(R.id.lblTelefonoTutor1);
        txtNombreTutor1 = (TextView) findViewById(R.id.txtNombreTutor1);
        txtCorreoTutor1 = (TextView) findViewById(R.id.txtCorreoTutor1);
        txtTelefonoTutor1 = (TextView) findViewById(R.id.txtTelefonoTutor1);

        lblNombreTutor2 = (TextView) findViewById(R.id.lblNombreTutor2);
        lblCorreoTutor2 = (TextView) findViewById(R.id.lblCorreoTutor2);
        lblTelefonoTutor2 = (TextView) findViewById(R.id.lblTelefonoTutor2);
        txtNombreTutor2 = (TextView) findViewById(R.id.txtNombreTutor2);
        txtCorreoTutor2 = (TextView) findViewById(R.id.txtCorreoTutor2);
        txtTelefonoTutor2 = (TextView) findViewById(R.id.txtTelefonoTutor2);

        divider3 = (View) findViewById(R.id.divider3);
        divider4 = (View) findViewById(R.id.divider4);
        divider5 = (View) findViewById(R.id.divider5);

        obtenerExtras();

        if (procedencia.equalsIgnoreCase("infoGrupo")) {
            btnCambiarImagenPerfil.setVisibility(View.GONE);
        }

        if (isDocente && procedencia.equalsIgnoreCase("main")) {
            lblNombreTutor1.setText(R.string.lblNombreDocente);
            lblCorreoTutor1.setText(R.string.lblCorreoDocente);
            lblTelefonoTutor1.setText(R.string.lblTelefonoDocente);
        }

        obtenerInformacion();

        btnCambiarImagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarImagenPerfil();
            }
        });

        btnEliminarImagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preguntarUsuario();
            }
        });

    }

    private void obtenerExtras() {
        if (!getIntent().getExtras().isEmpty()) {
            if (getIntent().getExtras().containsKey("idFamilia")) {
                idFamilia = getIntent().getExtras().getString("idFamilia");
            }

            if (getIntent().getExtras().containsKey("isDocente")) {
                isDocente = getIntent().getExtras().getBoolean("isDocente");
            }

            if (isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaDocente = getIntent().getParcelableExtra("usuarioJava");
            } else if (!isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaFamilia = getIntent().getParcelableExtra("usuarioJava");
            }

            if (getIntent().getExtras().containsKey("procedencia")) {
                procedencia = getIntent().getExtras().getString("procedencia");
            }
        }
    }

    private void obtenerInformacion() {
        if (procedencia.equalsIgnoreCase("infoGrupo")) {
            db.collection("Usuarios").document(idFamilia).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document != null) {
                            familia = new Familia(document.getId(),
                                    document.getString("NombreTutor1"),
                                    document.getString("Apellido1Tutor1"),
                                    document.getString("Apellido2Tutor1"),
                                    document.getString("CorreoTutor1"),
                                    document.getString("TelefonoTutor1"),
                                    document.getString("NombreTutor2"),
                                    document.getString("Apellido1Tutor2"),
                                    document.getString("Apellido2Tutor2"),
                                    document.getString("CorreoTutor2"),
                                    document.getString("TelefonoTutor2"));

                            if (document.contains("extImagen")) {
                                mostrarImagenPerfil(document.getString("extImagen"));
                                nombreImagen = StringUtils.stripAccents(familia.getNombreFamilia()) + "." + document.getString("extImagen");
                            }

                            rellenarInformacion();

                        }
                    } else {
                        Toast.makeText(PerfilUsuarioActivity.this, R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        } else if (procedencia.equalsIgnoreCase("main") && isDocente) {
            db.collection("Docentes").document(usuarioJavaDocente.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.contains("extImagen")) {
                            mostrarImagenPerfil(document.getString("extImagen"));
                            nombreImagen = StringUtils.stripAccents(usuarioJavaDocente.getId()) + "." + document.getString("extImagen");
                        }

                        rellenarInformacion();
                    }
                }
            });
        } else if (procedencia.equalsIgnoreCase("main") && !isDocente)
            db.collection("Usuarios").document(usuarioJavaFamilia.getNombreFamilia()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.contains("extImagen")) {
                            mostrarImagenPerfil(document.getString("extImagen"));
                            nombreImagen = StringUtils.stripAccents(usuarioJavaFamilia.getNombreFamilia()) + "." + document.getString("extImagen");
                        }

                        rellenarInformacion();
                    }
                }
            });
    }

    private void mostrarImagenPerfil(String ext) {
        String ruta = "perfiles/";

        if (isDocente && procedencia.equalsIgnoreCase("main")) {
            ruta = ruta + StringUtils.stripAccents(usuarioJavaDocente.getId()) + "." + ext;
            final String finalRuta = ruta;
            storageReference.child(ruta).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    btnEliminarImagenPerfil.setEnabled(true);
                    GlideApp.with(PerfilUsuarioActivity.this /* context */)
                            .load(storageReference.child(finalRuta))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imgPerfil);
                }
            });
        } else if (isDocente && procedencia.equalsIgnoreCase("infoGrupo")) {
            ruta = ruta + StringUtils.stripAccents(familia.getNombreFamilia()) + "." + ext;
            final String finalRuta1 = ruta;
            storageReference.child(ruta).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    btnEliminarImagenPerfil.setEnabled(true);
                    GlideApp.with(PerfilUsuarioActivity.this /* context */)
                            .load(storageReference.child(finalRuta1))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imgPerfil);
                }
            });
        } else if (!isDocente) {
            ruta = ruta + StringUtils.stripAccents(usuarioJavaFamilia.getNombreFamilia()) + "." + ext;
            final String finalRuta2 = ruta;
            storageReference.child(ruta).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    btnEliminarImagenPerfil.setEnabled(true);
                    GlideApp.with(PerfilUsuarioActivity.this /* context */)
                            .load(storageReference.child(finalRuta2))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imgPerfil);
                }
            });
        }

    }

    private void rellenarInformacion() {
        if (procedencia.equalsIgnoreCase("infoGrupo") && isDocente) {
            txtNombreTutor1.setText(familia.getNombreTutor1() + " " + familia.getApellido1Tutor1() + " " + familia.getApellido2Tutor1());
            txtCorreoTutor1.setText(familia.getCorreoTutor1());
            txtTelefonoTutor1.setText(familia.getTelefonoTutor1());

            if (!familia.getNombreTutor2().equals("")) {
                txtNombreTutor2.setText(familia.getNombreTutor2() + " " + familia.getApellido1Tutor2() + " " + familia.getApellido2Tutor2());
                txtCorreoTutor2.setText(familia.getCorreoTutor2());
                txtTelefonoTutor2.setText(familia.getTelefonoTutor2());
            } else {
                segundaInfoVisible(false);
            }
        } else if (procedencia.equalsIgnoreCase("main") && isDocente){
            txtNombreTutor1.setText(usuarioJavaDocente.getNombre() + " " + usuarioJavaDocente.getApellido1() + " " + usuarioJavaDocente.getApellido2());
            txtCorreoTutor1.setText(usuarioJavaDocente.getCorreo());
            txtTelefonoTutor1.setText(usuarioJavaDocente.getTelefono());

            segundaInfoVisible(false);

        } else if (procedencia.equalsIgnoreCase("main") && !isDocente) {
            txtNombreTutor1.setText(usuarioJavaFamilia.getNombreTutor1() + " " + usuarioJavaFamilia.getApellido1Tutor1() + " " + usuarioJavaFamilia.getApellido2Tutor1());
            txtCorreoTutor1.setText(usuarioJavaFamilia.getCorreoTutor1());
            txtTelefonoTutor1.setText(usuarioJavaFamilia.getTelefonoTutor1());

            if (!usuarioJavaFamilia.getNombreTutor2().equals("")) {
                txtNombreTutor2.setText(usuarioJavaFamilia.getNombreTutor2() + " " + usuarioJavaFamilia.getApellido1Tutor2() + " " + usuarioJavaFamilia.getApellido2Tutor2());
                txtCorreoTutor2.setText(usuarioJavaFamilia.getCorreoTutor2());
                txtTelefonoTutor2.setText(usuarioJavaFamilia.getTelefonoTutor2());
            } else {
                segundaInfoVisible(false);
            }
        }
    }

    private void segundaInfoVisible(boolean visible) {
        if (!visible) {
            lblNombreTutor2.setVisibility(View.GONE);
            lblCorreoTutor2.setVisibility(View.GONE);
            lblTelefonoTutor2.setVisibility(View.GONE);

            divider3.setVisibility(View.GONE);
            divider4.setVisibility(View.GONE);
            divider5.setVisibility(View.GONE);
        }
    }

    private void cambiarImagenPerfil() {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            rutaImagen = data.getData();

            subirImagen();
        }
    }

    private void borrarImagen() {
        StorageReference imgRef = storageReference.child("perfiles/" + nombreImagen);

        imgRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                imgPerfil.setImageResource(R.drawable.usuario64);
                Toast.makeText(PerfilUsuarioActivity.this, R.string.msgBorradoImagenCorrecto, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void subirImagen() {
        if (rutaImagen != null) {
            final StorageReference sRef;
            final ProgressDialog progressDialog = new ProgressDialog(PerfilUsuarioActivity.this);
            progressDialog.setTitle(R.string.msgSubiendoImagen);
            progressDialog.show();

            if (isDocente) {
                sRef = storageReference.child("perfiles/" + StringUtils.stripAccents(usuarioJavaDocente.getId()) + "." + getFileExtension(rutaImagen));
            } else {
                sRef = storageReference.child("perfiles/" + StringUtils.stripAccents(usuarioJavaFamilia.getNombreFamilia()) + "." + getFileExtension(rutaImagen));
            }

            //adding the file to reference
            sRef.putFile(rutaImagen)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), R.string.msgImagenSubida, Toast.LENGTH_LONG).show();

                            actualizarBBDD(getFileExtension(rutaImagen));
                            mostrarImagenPerfil(getFileExtension(rutaImagen));

                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msgErrorBBDD, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Subido " + ((int) progress) + "%...");
                        }
                    });
        }
    }

    private void actualizarBBDD(String ext) {
        if (isDocente) {
            db.collection("Docentes").document(usuarioJavaDocente.getId()).update("extImagen", ext);
        } else {
            db.collection("Usuarios").document(usuarioJavaFamilia.getNombreFamilia()).update("extImagen", ext);
        }
    }

    private void preguntarUsuario() {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle(getString(R.string.lblPreguntaBorrar));
        dialogo1.setMessage(getString(R.string.msgPreguntaBorrar));
        dialogo1.setCancelable(false);

        dialogo1.setPositiveButton(getString(R.string.lblConfirmar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                borrarImagen();
            }
        });

        dialogo1.setNegativeButton(getString(R.string.lblCancelar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

            }
        });

        dialogo1.show();
    }

}
