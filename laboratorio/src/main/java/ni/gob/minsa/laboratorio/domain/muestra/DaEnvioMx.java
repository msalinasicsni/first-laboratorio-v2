package ni.gob.minsa.laboratorio.domain.muestra;

import ni.gob.minsa.laboratorio.domain.portal.Usuarios;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by FIRSTICT on 11/21/2014.
 */
@Entity
@Table(name = "da_envio_mx", schema = "alerta")
public class DaEnvioMx {
    private String idEnvio;
    private String nombreTransporta;
    private Float temperaturaTermo;
    private Timestamp fechaHoraEnvio;
    private long tiempoEspera;
    private Laboratorio laboratorioDestino;
    private Usuarios usarioRegistro;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "ID_ENVIO", nullable = false, insertable = true, updatable = true, length = 36)
    public String getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(String idEnvio) {
        this.idEnvio = idEnvio;
    }

    @Basic
    @Column(name = "NOMBRE_TRANSPORTA", nullable = true, insertable = true, updatable = true, length = 100)
    public String getNombreTransporta() {
        return nombreTransporta;
    }

    public void setNombreTransporta(String nombreTransporta) {
        this.nombreTransporta = nombreTransporta;
    }

    @Basic
    @Column(name = "FECHAHORA_ENVIO", nullable = false, insertable = true, updatable = false)
    public Timestamp getFechaHoraEnvio() {
        return fechaHoraEnvio;
    }

    public void setFechaHoraEnvio(Timestamp fechaHoraEnvio) {
        this.fechaHoraEnvio = fechaHoraEnvio;
    }

    @Column(name = "TEMPERATURA", nullable = true)
    public Float getTemperaturaTermo() {
        return temperaturaTermo;
    }

    public void setTemperaturaTermo(Float temperaturaTermo) {
        this.temperaturaTermo = temperaturaTermo;
    }

    @Column(name = "TIEMPO_ESPERA", nullable = true)
    public long getTiempoEspera() {
        return tiempoEspera;
    }

    public void setTiempoEspera(long tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "LABORATORIO_DEST", referencedColumnName = "CODIGO")
    @ForeignKey(name = "ENVIO_ORDEN_LABORATORIO_FK")
    public Laboratorio getLaboratorioDestino() {
        return laboratorioDestino;
    }

    public void setLaboratorioDestino(Laboratorio laboratorioDestino) {
        this.laboratorioDestino = laboratorioDestino;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "USUARIO", referencedColumnName = "USUARIO_ID")
    @ForeignKey(name = "ENVIO_ORDEN_USUARIO_FK")
    public Usuarios getUsarioRegistro() {
        return usarioRegistro;
    }

    public void setUsarioRegistro(Usuarios usarioRegistro) {
        this.usarioRegistro = usarioRegistro;
    }

    @Override
    public String toString() {
        return "idEnvio='" + idEnvio + '\'';

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DaEnvioMx)) return false;

        DaEnvioMx envioMx = (DaEnvioMx) o;

        if (idEnvio != null ? !idEnvio.equals(envioMx.idEnvio) : envioMx.idEnvio != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return idEnvio != null ? idEnvio.hashCode() : 0;
    }
}
