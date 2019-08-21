package ni.gob.minsa.laboratorio.restServices.entidades;

import javax.validation.constraints.NotNull;

/**
 * Created by Miguel Salinas on 22/05/2019.
 * V1.0
 */
public class Municipio implements Comparable<Municipio> {

    public long id;
    public String nombre;
    public long deptoId;
    public String codigo;
    public String codigocsereg;
    public String codigocse;
    public float latitud;
    public float longitud;
    public boolean pasivo;
    public String usuarioregistro;
    public String fecharegistro;
    public Departamento departamento;

    public Municipio(long id, String nombre, long deptoId, String codigo, String codigocsereg, String codigocse, float latitud, float longitud, boolean pasivo, String usuarioregistro, String fecharegistro, Departamento departamento) {
        this.id = id;
        this.nombre = nombre;
        this.deptoId = deptoId;
        this.codigo = codigo;
        this.codigocsereg = codigocsereg;
        this.codigocse = codigocse;
        this.latitud = latitud;
        this.longitud = longitud;
        this.pasivo = pasivo;
        this.usuarioregistro = usuarioregistro;
        this.fecharegistro = fecharegistro;
        this.departamento = departamento;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public long getDeptoId() {
        return deptoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCodigocsereg() {
        return codigocsereg;
    }

    public String getCodigocse() {
        return codigocse;
    }

    public float getLatitud() {
        return latitud;
    }

    public float getLongitud() {
        return longitud;
    }

    public boolean isPasivo() {
        return pasivo;
    }

    public String getUsuarioregistro() {
        return usuarioregistro;
    }

    public String getFecharegistro() {
        return fecharegistro;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    //Necesario en las vistas para no modificar más codigo
    public String getCodigoNacional() {
        return codigo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Municipio)) return false;

        Municipio municipio = (Municipio) o;

        if (id != municipio.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    @NotNull
    public int compareTo(Municipio mun1) {
        return this.nombre.compareToIgnoreCase(mun1.nombre);
    }

}
