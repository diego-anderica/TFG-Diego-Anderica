package es.uclm.esi.tfg.educhat;

import java.util.ArrayList;

public class ChatGrupal {
    private String id;
    private String nombre;
    private Docente administrador;
    private ArrayList<Familia> familias;
    private ArrayList<Mensaje> mensajes;

    public ChatGrupal() {

    }

    public ChatGrupal(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public ChatGrupal(String id, String nombre, Docente administrador, ArrayList<Familia> familias) {
        this.id = id;
        this.nombre = nombre;
        this.administrador = administrador;
        this.familias = familias;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Docente getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Docente administrador) {
        this.administrador = administrador;
    }

    public ArrayList<Familia> getFamilias() {
        return familias;
    }

    public void setFamilias(ArrayList<Familia> familias) {
        this.familias = familias;
    }
}
