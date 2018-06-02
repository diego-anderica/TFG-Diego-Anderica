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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class InfoGrupoActivity extends AppCompatActivity {

    private String chatID;
    private boolean isDocente;
    private String nombreChat;

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
        final EditText taskEditText = new EditText(this);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.lblCambiarNombre);
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialog.setPositiveButton(R.string.lblConfirmar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dbChat.update("Nombre", input.getText().toString());
                    }
                });

        alertDialog.setNegativeButton(R.string.lblCancelar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        input.setLayoutParams(lp);

        alertDialog.setView(input);
        alertDialog.show();
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
        }
    }

}
