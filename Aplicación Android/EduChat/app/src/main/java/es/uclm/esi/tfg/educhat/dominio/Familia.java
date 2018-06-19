package es.uclm.esi.tfg.educhat.dominio;

import android.os.Parcel;
import android.os.Parcelable;

public class Familia implements Parcelable{
    private String nombreFamilia;
    private String nombreTutor1;
    private String apellido1Tutor1;
    private String apellido2Tutor1;
    private String correoTutor1;
    private String telefonoTutor1;
    private String nombreTutor2;
    private String apellido1Tutor2;
    private String apellido2Tutor2;
    private String correoTutor2;
    private String telefonoTutor2;
    private boolean checked;

    public Familia() {

    }

    public Familia(String nombreFamilia, String nombreTutor1, String apellido1Tutor1,
                   String apellido2Tutor1, String correoTutor1, String telefonoTutor1,
                   String nombreTutor2, String apellido1Tutor2, String apellido2Tutor2,
                   String correoTutor2, String telefonoTutor2) {
        this.nombreFamilia = nombreFamilia;
        this.nombreTutor1 = nombreTutor1;
        this.apellido1Tutor1 = apellido1Tutor1;
        this.apellido2Tutor1 = apellido2Tutor1;
        this.correoTutor1 = correoTutor1;
        this.telefonoTutor1 = telefonoTutor1;
        this.nombreTutor2 = nombreTutor2;
        this.apellido1Tutor2 = apellido1Tutor2;
        this.apellido2Tutor2 = apellido2Tutor2;
        this.correoTutor2 = correoTutor2;
        this.telefonoTutor2 = telefonoTutor2;
    }

    public String getNombreFamilia() {
        return nombreFamilia;
    }

    public void setNombreFamilia(String nombreFamilia) {
        this.nombreFamilia = nombreFamilia;
    }

    public String getNombreTutor1() {
        return nombreTutor1;
    }

    public void setNombreTutor1(String nombreTutor1) {
        this.nombreTutor1 = nombreTutor1;
    }

    public String getApellido1Tutor1() {
        return apellido1Tutor1;
    }

    public void setApellido1Tutor1(String apellido1Tutor1) {
        this.apellido1Tutor1 = apellido1Tutor1;
    }

    public String getApellido2Tutor1() {
        return apellido2Tutor1;
    }

    public void setApellido2Tutor1(String apellido2Tutor1) {
        this.apellido2Tutor1 = apellido2Tutor1;
    }

    public String getCorreoTutor1() {
        return correoTutor1;
    }

    public void setCorreoTutor1(String correoTutor1) {
        this.correoTutor1 = correoTutor1;
    }

    public String getTelefonoTutor1() {
        return telefonoTutor1;
    }

    public void setTelefonoTutor1(String telefonoTutor1) {
        this.telefonoTutor1 = telefonoTutor1;
    }

    public String getNombreTutor2() {
        return nombreTutor2;
    }

    public void setNombreTutor2(String nombreTutor2) {
        this.nombreTutor2 = nombreTutor2;
    }

    public String getApellido1Tutor2() {
        return apellido1Tutor2;
    }

    public void setApellido1Tutor2(String apellido1Tutor2) {
        this.apellido1Tutor2 = apellido1Tutor2;
    }

    public String getApellido2Tutor2() {
        return apellido2Tutor2;
    }

    public void setApellido2Tutor2(String apellido2Tutor2) {
        this.apellido2Tutor2 = apellido2Tutor2;
    }

    public String getCorreoTutor2() {
        return correoTutor2;
    }

    public void setCorreoTutor2(String correoTutor2) {
        this.correoTutor2 = correoTutor2;
    }

    public String getTelefonoTutor2() {
        return telefonoTutor2;
    }

    public void setTelefonoTutor2(String telefonoTutor2) {
        this.telefonoTutor2 = telefonoTutor2;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombreFamilia='" + nombreFamilia + '\'' +
                ", nombreTutor1='" + nombreTutor1 + '\'' +
                ", apellido1Tutor1='" + apellido1Tutor1 + '\'' +
                ", apellido2Tutor1='" + apellido2Tutor1 + '\'' +
                ", correoTutor1='" + correoTutor1 + '\'' +
                ", telefonoTutor1='" + telefonoTutor1 + '\'' +
                ", nombreTutor2='" + nombreTutor2 + '\'' +
                ", apellido1Tutor2='" + apellido1Tutor2 + '\'' +
                ", apellido2Tutor2='" + apellido2Tutor2 + '\'' +
                ", correoTutor2='" + correoTutor2 + '\'' +
                ", telefonoTutor2='" + telefonoTutor2 + '\'' +
                ", checked=" + checked +
                '}';
    }

    protected Familia(Parcel in) {
        nombreFamilia = in.readString();
        nombreTutor1 = in.readString();
        apellido1Tutor1 = in.readString();
        apellido2Tutor1 = in.readString();
        correoTutor1 = in.readString();
        telefonoTutor1 = in.readString();
        nombreTutor2 = in.readString();
        apellido1Tutor2 = in.readString();
        apellido2Tutor2 = in.readString();
        correoTutor2 = in.readString();
        telefonoTutor2 = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nombreFamilia);
        dest.writeString(nombreTutor1);
        dest.writeString(apellido1Tutor1);
        dest.writeString(apellido2Tutor1);
        dest.writeString(correoTutor1);
        dest.writeString(telefonoTutor1);
        dest.writeString(nombreTutor2);
        dest.writeString(apellido1Tutor2);
        dest.writeString(apellido2Tutor2);
        dest.writeString(correoTutor2);
        dest.writeString(telefonoTutor2);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Familia> CREATOR = new Parcelable.Creator<Familia>() {
        @Override
        public Familia createFromParcel(Parcel in) {
            return new Familia(in);
        }

        @Override
        public Familia[] newArray(int size) {
            return new Familia[size];
        }
    };

}
