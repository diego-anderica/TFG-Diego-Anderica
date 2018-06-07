package es.uclm.esi.tfg.colegiapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorListaChats extends ArrayAdapter {
    private Activity context;
    private ArrayList<ChatGrupal> chatsGrupales;

    public AdaptadorListaChats(Activity context, ArrayList<ChatGrupal> chatsGrupales) {
        super(context, R.layout.lst_familias_item, chatsGrupales);
        this.context = context;
        this.chatsGrupales = chatsGrupales;
    }

    @Override
    public int getCount() {
        return chatsGrupales.size();
    }

    @Override
    public Object getItem(int position) {
        return chatsGrupales.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View item = inflater.inflate(R.layout.lst_chats_item, null);

        TextView lblNombreGrupo = (TextView)item.findViewById(R.id.lblNombreGrupo);
        lblNombreGrupo.setText(chatsGrupales.get(position).getNombre());

        return(item);
    }
}
