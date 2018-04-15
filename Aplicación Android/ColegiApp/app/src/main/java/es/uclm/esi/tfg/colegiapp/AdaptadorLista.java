package es.uclm.esi.tfg.colegiapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorLista extends ArrayAdapter {
    private Activity context;
    private ArrayList<Usuario> usuarios;

    public AdaptadorLista(Activity context, ArrayList<Usuario> usuarios) {
        super(context, R.layout.lst_usuarios_item, usuarios);
        this.context = context;
        this.usuarios = usuarios;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();

        View item = inflater.inflate(R.layout.lst_usuarios_item, null);

        TextView lblFamilia = (TextView) item.findViewById(R.id.lblFamilia);
        lblFamilia.setText("Familia " + usuarios.get(position).getApellido1Tutor1() + " " + usuarios.get(position).getApellido1Tutor2());

        TextView lblTutor1 = (TextView) item.findViewById(R.id.lblTutor1);
        lblTutor1.setText(usuarios.get(position).getNombreTutor1() + " " + usuarios.get(position).getApellido1Tutor1() + " " + usuarios.get(position).getApellido2Tutor1());

        TextView lblTutor2 = (TextView) item.findViewById(R.id.lblTutor2);

        if (!usuarios.get(position).getNombreTutor2().equalsIgnoreCase("")) {
            lblTutor2.setText(usuarios.get(position).getNombreTutor2() + " " + usuarios.get(position).getApellido1Tutor2() + " " + usuarios.get(position).getApellido2Tutor2());
        } else {
            lblTutor2.setVisibility(View.GONE);
        }

        return (item);
    }

}
