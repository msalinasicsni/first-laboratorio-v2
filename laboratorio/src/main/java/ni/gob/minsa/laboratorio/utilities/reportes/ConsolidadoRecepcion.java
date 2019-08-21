package ni.gob.minsa.laboratorio.utilities.reportes;

/**
 * Created by Miguel Salinas on 01/07/2019.
 * V1.0
 */
public class ConsolidadoRecepcion {

    private Long total;
    private Long idConsolida;
    private String nombreConsolida;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getIdConsolida() {
        return idConsolida;
    }

    public void setIdConsolida(Long idConsolida) {
        this.idConsolida = idConsolida;
    }

    public String getNombreConsolida() {
        return nombreConsolida;
    }

    public void setNombreConsolida(String nombreConsolida) {
        this.nombreConsolida = nombreConsolida;
    }
}
