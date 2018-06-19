package es.uclm.esi.tfg.educhat.dominio;

import android.os.Parcel;
import android.os.Parcelable;

public class Docente implements Parcelable{
    private String id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String correo;
    private String telefono;

    public Docente() {

    }

    public Docente(String id, String nombre, String apellido1, String apellido2, String correo,
                   String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.correo = correo;
        this.telefono = telefono;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return "Docente{" +
                "nombre='" + nombre + '\'' +
                ", apellido1='" + apellido1 + '\'' +
                ", apellido2='" + apellido2 + '\'' +
                ", correo='" + correo + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }

    protected Docente(Parcel in) {
        id = in.readString();
        nombre = in.readString();
        apellido1 = in.readString();
        apellido2 = in.readString();
        correo = in.readString();
        telefono = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nombre);
        dest.writeString(apellido1);
        dest.writeString(apellido2);
        dest.writeString(correo);
        dest.writeString(telefono);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Docente> CREATOR = new Parcelable.Creator<Docente>() {
        @Override
        public Docente createFromParcel(Parcel in) {
            return new Docente(in);
        }

        @Override
        public Docente[] newArray(int size) {
            return new Docente[size];
        }
    };
}
