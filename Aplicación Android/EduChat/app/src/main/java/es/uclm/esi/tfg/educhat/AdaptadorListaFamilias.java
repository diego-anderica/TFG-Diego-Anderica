package es.uclm.esi.tfg.educhat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import es.uclm.esi.tfg.educhat.dominio.Familia;

public class AdaptadorListaFamilias extends ArrayAdapter {
    private Activity context;
    private ArrayList<Familia> usuarios;

    public AdaptadorListaFamilias(Activity context, ArrayList<Familia> usuarios) {
        super(context, R.layout.lst_familias_item, usuarios);
        this.context = context;
        this.usuarios = usuarios;
    }

    @Override
    public int getCount() {
        return usuarios.size();
    }

    @Override
    public Object getItem(int position) {
        return usuarios.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean isChecked(int position) {
        return usuarios.get(position).isChecked();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        CrearGrupoActivity.ViewHolder viewHolder = new CrearGrupoActivity.ViewHolder();

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.lst_familias_item, null);

            viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.itemCheckBox);
            viewHolder.familia = (TextView) rowView.findViewById(R.id.lblFamilia);
            viewHolder.tutor1 = (TextView) rowView.findViewById(R.id.lblTutor1);
            viewHolder.tutor2 = (TextView) rowView.findViewById(R.id.lblTutor2);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (CrearGrupoActivity.ViewHolder) rowView.getTag();
        }

        viewHolder.checkBox.setChecked(usuarios.get(position).isChecked());
        viewHolder.familia.setText("Familia " + usuarios.get(position).getApellido1Tutor1() + " " + usuarios.get(position).getApellido1Tutor2());
        viewHolder.tutor1.setText(usuarios.get(position).getNombreTutor1() + " " + usuarios.get(position).getApellido1Tutor1() + " " + usuarios.get(position).getApellido2Tutor1());

        if (!usuarios.get(position).getNombreTutor2().equalsIgnoreCase("")) {
            viewHolder.tutor2.setText(usuarios.get(position).getNombreTutor2() + " " + usuarios.get(position).getApellido1Tutor2() + " " + usuarios.get(position).getApellido2Tutor2());
        } else {
            viewHolder.tutor2.setVisibility(View.GONE);
        }

        viewHolder.checkBox.setTag(position);

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean newState = !usuarios.get(position).isChecked();
                usuarios.get(position).setChecked(newState);
            }
        });

        viewHolder.checkBox.setChecked(isChecked(position));

        return (rowView);
    }

}
