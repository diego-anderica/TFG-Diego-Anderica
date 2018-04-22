package es.uclm.esi.tfg.colegiapp;

import java.util.Date;

public class Mensaje{
    private String remitenteId;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String mensaje;
    private Date fecha;

    public Mensaje(String remitenteId, String nombre, String apellido1, String apellido2, String mensaje, Date fecha) {
        this.remitenteId = remitenteId;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public Mensaje() {

    }

    public String getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

}
