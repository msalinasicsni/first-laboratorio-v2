package ni.gob.minsa.laboratorio.restServices.entidades;

/**
 * Created by Miguel Salinas on 22/05/2019.
 * V1.0
 */
public class EntidadesAdtvas implements Comparable<EntidadesAdtvas> {
    public final long id;
    public final String nombre;
    public final String codigo;
    public final String telefono;
    public final String fax;
    public final String direccion;
    public final float latitud;
    public final float longitud;
    public final boolean pasivo;
    public final String usuarioregistro;
    public final String fecharegistro;
    private Municipio municipio;

    public EntidadesAdtvas(long id, String nombre, String codigo, String telefono, String fax, String direccion, float latitud, float longitud, boolean pasivo, String usuarioregistro, String fecharegistro, Municipio municipio) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.telefono = telefono;
        this.fax = fax;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.pasivo = pasivo;
        this.usuarioregistro = usuarioregistro;
        this.fecharegistro = fecharegistro;
        this.municipio = municipio;
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

    public String getTelefono() {
        return telefono;
    }

    public String getFax() {
        return fax;
    }

    public String getDireccion() {
        return direccion;
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

    public Municipio getMunicipio() {
        return municipio;
    }

    @Override
    public int compareTo(EntidadesAdtvas o) {
        return this.nombre.compareToIgnoreCase(o.nombre);
    }
}
