package ni.gob.minsa.laboratorio.restServices.entidades;

/**
 * Created by Miguel Salinas on 22/05/2019.
 * V1.0
 */
public class Catalogo {
    public final long id;
    public final long catalogosup;
    public final String codigo;
    public final String valor;
    public final long orden;
    public final long final1;
    public final boolean pasivo;
    public final String usuarioregistro;
    public final String fecharegistro;

    public Catalogo(long id, long catalogosup, String codigo, String valor, long orden, long final1, boolean pasivo, String usuarioregistro, String fecharegistro) {
        this.id = id;
        this.catalogosup = catalogosup;
        this.codigo = codigo;
        this.valor = valor;
        this.orden = orden;
        this.final1 = final1;
        this.pasivo = pasivo;
        this.usuarioregistro = usuarioregistro;
        this.fecharegistro = fecharegistro;
    }

    public long getId() {
        return id;
    }

    public long getCatalogosup() {
        return catalogosup;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getValor() {
        return valor;
    }

    public long getOrden() {
        return orden;
    }

    public long getFinal1() {
        return final1;
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
}
