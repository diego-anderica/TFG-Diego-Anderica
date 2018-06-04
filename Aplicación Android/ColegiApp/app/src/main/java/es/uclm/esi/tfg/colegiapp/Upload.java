package es.uclm.esi.tfg.colegiapp;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Upload {

    public String nombre;
    public String url;

    public Upload() {

    }

    public Upload(String nombre, String url) {
        this.nombre = nombre;
        this.url= url;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUrl() {
        return url;
    }
}
