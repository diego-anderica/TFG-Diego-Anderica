package es.uclm.esi.tfg.colegiapp;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AdaptadorListaMensajes extends RecyclerView.Adapter<AdaptadorListaMensajes.MyViewHolder> {

    private ArrayList<Mensaje> mensajes;
    private Context context;

    public AdaptadorListaMensajes(Context context, ArrayList<Mensaje> items) {
        this.context = context;
        this.mensajes = items;
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder itemsViewHolder, int i) {
        itemsViewHolder.lblRemitente.setText(mensajes.get(i).getNombre() + " " + mensajes.get(i).getApellido1() + " " + mensajes.get(i).getApellido2());
        itemsViewHolder.lblMensaje.setText(mensajes.get(i).getMensaje());
        itemsViewHolder.lblFecha.setText(new SimpleDateFormat("dd-MM-yy").format(mensajes.get(i).getFecha()) + " " +
                new SimpleDateFormat("HH:mm").format(mensajes.get(i).getFecha()));

        if (mensajes.get(i).isDocente()) {
            if (mensajes.get(i).getTono() <= 25.0) {
                itemsViewHolder.lblMensaje.setBackgroundResource(R.drawable.rounded_red_item);
            } else if (mensajes.get(i).getTono() > 25.0 && mensajes.get(i).getTono() <= 50.0) {
                itemsViewHolder.lblMensaje.setBackgroundResource(R.drawable.rounded_orange_item);
            } else if (mensajes.get(i).getTono() > 50.0 && mensajes.get(i).getTono() <= 75.0) {
                itemsViewHolder.lblMensaje.setBackgroundResource(R.drawable.rounded_yellow_item);
                itemsViewHolder.lblMensaje.setTextColor(Color.BLACK);
            } else if (mensajes.get(i).getTono() > 75.0) {
                itemsViewHolder.lblMensaje.setBackgroundResource(R.drawable.rounded_green_item);
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.mensaje_sala_item, viewGroup, false);

        return new MyViewHolder(itemView);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        protected TextView lblRemitente;
        protected TextView lblMensaje;
        protected TextView lblFecha;

        public MyViewHolder(View v) {
            super(v);
            lblRemitente = (TextView) v.findViewById(R.id.lblRemitente);
            lblMensaje = (TextView) v.findViewById(R.id.lblMensaje);
            lblFecha = (TextView) v.findViewById(R.id.lblFecha);
        }
    }
}
