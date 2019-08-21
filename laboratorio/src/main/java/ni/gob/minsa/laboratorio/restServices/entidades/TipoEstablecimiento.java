package ni.gob.minsa.laboratorio.restServices.entidades;

/**
 * Created by Miguel Salinas on 20/06/2019.
 * V1.0
 */
public class TipoEstablecimiento {

    private long id;
    private String nombre;
    private String codigo;
    private boolean pasivo;

    public TipoEstablecimiento(long id, String nombre, String codigo, boolean pasivo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.pasivo = pasivo;
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

    public boolean getPasivo() {
        return pasivo;
    }
}
