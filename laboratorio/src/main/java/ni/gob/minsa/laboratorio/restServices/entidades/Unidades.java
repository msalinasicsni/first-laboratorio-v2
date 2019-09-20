package ni.gob.minsa.laboratorio.restServices.entidades;

import javax.validation.constraints.NotNull;

/**
 * Created by Miguel Salinas on 22/05/2019.
 * V1.0
 */
public class Unidades implements Comparable<Unidades> {

    public long id;
    public String nombre;
    public String codigo;
    public float latitud;
    public float longitud;
    public boolean pasivo;
    public String usuarioregistro;
    public String fecharegistro;
    public String direccion;
    public EntidadesAdtvas entidadesadtvas;
    public Municipio municipio;
    private TipoEstablecimiento tipoestablecimiento;

    public Unidades(long id, String nombre, String codigo, float latitud, float longitud, boolean pasivo, String usuarioregistro, String fecharegistro, String direccion, EntidadesAdtvas entidadesadtvas, Municipio municipio, TipoEstablecimiento tipoestablecimiento) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.latitud = latitud;
        this.longitud = longitud;
        this.pasivo = pasivo;
        this.usuarioregistro = usuarioregistro;
        this.fecharegistro = fecharegistro;
        this.direccion = direccion;
        this.entidadesadtvas = entidadesadtvas;
        this.municipio = municipio;
        this.tipoestablecimiento = tipoestablecimiento;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public float getLatitud() {
        return latitud;
    }

    public float getLongitud() {
        return longitud;
    }

    public boolean getPasivo() {
        return pasivo;
    }

    public String getUsuarioregistro() {
        return usuarioregistro;
    }

    public String getFecharegistro() {
        return fecharegistro;
    }

    public String getDireccion() {
        return direccion;
    }

    public EntidadesAdtvas getEntidadesadtvas() {
        return entidadesadtvas;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public TipoEstablecimiento getTipoestablecimiento() {
        return tipoestablecimiento;
    }

    @Override
    @NotNull
    public int compareTo(Unidades uni1) {
        return this.nombre.compareToIgnoreCase(uni1.nombre);
    }
}
