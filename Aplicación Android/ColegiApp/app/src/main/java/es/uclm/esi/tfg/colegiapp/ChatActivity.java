package es.uclm.esi.tfg.colegiapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private DocumentReference dbChat;
    private CollectionReference dbMensajes;

    private String chatID;
    private boolean isDocente;
    private Docente usuarioJavaDocente;
    private Familia usuarioJavaFamilia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        obtenerExtras();

        db = FirebaseFirestore.getInstance();

        dbChat = db.collection("ChatsGrupales").document(chatID);
        dbMensajes = dbChat.collection("Mensajes");

        obtenerMensajes();
    }

    private void obtenerMensajes() {
        dbMensajes
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("Mensajes", document.getId() + " => " + document.get("mensaje"));
                            }
                        } else {
                            Log.d("Mensajes", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void obtenerExtras() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("isDocente")) {
                isDocente = getIntent().getBooleanExtra("isDocente", false);
            }

            if (getIntent().getExtras().containsKey("chatID")) {
                chatID = getIntent().getExtras().getString("chatID", "");
            }

            if (isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaDocente = getIntent().getParcelableExtra("usuarioJava");
            } else if (!isDocente && getIntent().getExtras().containsKey("usuarioJava")) {
                usuarioJavaFamilia = getIntent().getParcelableExtra("usuarioJava");
            }
        }
    }
}
