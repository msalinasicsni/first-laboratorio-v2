package ni.gob.minsa.laboratorio.restServices.entidades;

/**
 * Created by Miguel Salinas on 22/04/2019.
 * V1.0
 */
public class Departamento {
    public long id;
    public String nombre;
    public long regionId;
    public String regionNombre;
    public long paisId;
    public String codigo;
    public String codigoiso;
    public String codigocse;
    public float latitud;
    public float longitud;
    public boolean pasivo;
    public String usuarioregistro;
    public String fecharegistro;

    public Departamento(long id, String nombre, long regionId, String regionNombre, long paisId, String codigo, String codigoiso, String codigocse, float latitud, float longitud, boolean pasivo, String usuarioregistro, String fecharegistro) {
        this.id = id;
        this.nombre = nombre;
        this.regionId = regionId;
        this.regionNombre = regionNombre;
        this.paisId = paisId;
        this.codigo = codigo;
        this.codigoiso = codigoiso;
        this.codigocse = codigocse;
        this.latitud = latitud;
        this.longitud = longitud;
        this.pasivo = pasivo;
        this.usuarioregistro = usuarioregistro;
        this.fecharegistro = fecharegistro;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public long getRegionId() {
        return regionId;
    }

    public String getRegionNombre() {
        return regionNombre;
    }

    public long getPaisId() {
        return paisId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCodigoiso() {
        return codigoiso;
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

    public String getCodigoNacional() {
        return codigo;
    }
}
