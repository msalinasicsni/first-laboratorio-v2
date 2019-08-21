package ni.gob.minsa.laboratorio.domain.muestra;

import ni.gob.minsa.laboratorio.domain.audit.Auditable;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by FIRSTICT on 12/9/2014.
 */
@Entity
@Table(name = "recepcion_mx", schema = "laboratorio", uniqueConstraints = @UniqueConstraint(columnNames = {"CODUNICOMX","LABORATORIO_RECEP"},name = "RECEPCION_MX_CODUNICO_LAB"))
public class RecepcionMx implements Serializable, Auditable {

    private String idRecepcion;
    private DaTomaMx tomaMx;
    private Timestamp fechaHoraRecepcion;
    private String tipoRecepcionMx;
    private User usuarioRecepcion;
    private String tipoTubo;
    private String calidadMx;
    private boolean cantidadTubosCk;
    private boolean tipoMxCk;
    private String causaRechazo;
    private Laboratorio labRecepcion;
    private String condicionMx;
    private Date fechaRecibido;
    private String horaRecibido;
    private String desTipoRecepcionMx;
    private String desCausaRechazo;
    private String desCondicionMx;
    private String desTipoTubo;
    private String desCalidadMx;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "ID_RECEPCION", nullable = false, insertable = true, updatable = true, length = 36)
    public String getIdRecepcion() {
        return idRecepcion;
    }

    public void setIdRecepcion(String idRecepcion) {
        this.idRecepcion = idRecepcion;
    }

    @Basic
    @Column(name = "FECHAHORA_RECEPCION", nullable = false, insertable = true, updatable = false)
    public Timestamp getFechaHoraRecepcion() {
        return fechaHoraRecepcion;
    }

    public void setFechaHoraRecepcion(Timestamp fechaHoraRecepcion) {
        this.fechaHoraRecepcion = fechaHoraRecepcion;
    }

    @Column(name = "TIPO_RECEPCION", length = 32)
    public String getTipoRecepcionMx() {
        return tipoRecepcionMx;
    }

    public void setTipoRecepcionMx(String tipoRecepcionMx) {
        this.tipoRecepcionMx = tipoRecepcionMx;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "USUARIO_RECEPCION", referencedColumnName = "username")
    @ForeignKey(name = "RECEPCION_USUARIO_FK")
    public User getUsuarioRecepcion() {
        return usuarioRecepcion;
    }

    public void setUsuarioRecepcion(User usuarioRecepcion) {
        this.usuarioRecepcion = usuarioRecepcion;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "CODUNICOMX", referencedColumnName = "CODUNICOMX")
    @ForeignKey(name = "RECEPCION_TOMAMX_FK")
    public DaTomaMx getTomaMx() {
        return tomaMx;
    }

    public void setTomaMx(DaTomaMx tomaMx) {
        this.tomaMx = tomaMx;
    }

    @Column(name = "COD_TIPO_TUBO", length = 32)
    public String getTipoTubo() {
        return tipoTubo;
    }

    public void setTipoTubo(String tipoTubo) {
        this.tipoTubo = tipoTubo;
    }

    @Column(name = "COD_CALIDADMX", length = 32)
    public String getCalidadMx() {
        return calidadMx;
    }

    public void setCalidadMx(String calidadMx) {
        this.calidadMx = calidadMx;
    }

    @Basic
    @Column(name = "CANTUBOS_CK", nullable = true, insertable = true, updatable = true)
    public boolean isCantidadTubosCk() {
        return cantidadTubosCk;
    }

    public void setCantidadTubosCk(boolean cantidadTubosCk) {
        this.cantidadTubosCk = cantidadTubosCk;
    }

    @Basic
    @Column(name = "TIPOMX_CK", nullable = true, insertable = true, updatable = true)
    public boolean isTipoMxCk() {
        return tipoMxCk;
    }

    public void setTipoMxCk(boolean tipoMxCk) {
        this.tipoMxCk = tipoMxCk;
    }

    @Column(name = "CAUSA", nullable = true, length = 32)
    public String getCausaRechazo() {
        return causaRechazo;
    }

    public void setCausaRechazo(String causaRechazo) {
        this.causaRechazo = causaRechazo;
    }

    @ManyToOne(optional = true)
    @JoinColumn(name = "LABORATORIO_RECEP", referencedColumnName = "CODIGO")
    @ForeignKey(name = "RECEPCION_MX_LABORATORIO_FK")
    public Laboratorio getLabRecepcion() {
        return labRecepcion;
    }

    public void setLabRecepcion(Laboratorio labRecepcion) {
        this.labRecepcion = labRecepcion;
    }

    @Column(name = "COD_CONDICIONMX", nullable = true, length = 32)
    public String getCondicionMx() {
        return condicionMx;
    }

    public void setCondicionMx(String condicionMx) {
        this.condicionMx = condicionMx;
    }

    @Basic
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(name = "FECHA_RECIBIDO", nullable = true, insertable = true, updatable = true)
    public Date getFechaRecibido() { return fechaRecibido; }

    public void setFechaRecibido(Date fechaRecibido) { this.fechaRecibido = fechaRecibido; }

    @Basic
    @Column(name = "HORA_RECIBIDO", nullable = true, insertable = true, updatable = true, length = 8)
    public String getHoraRecibido() { return horaRecibido; }

    public void setHoraRecibido(String horaRecibido) { this.horaRecibido = horaRecibido; }

    @Column(name = "DES_TIPO_RECEPCION", nullable = true, length = 100)
    public String getDesTipoRecepcionMx() {
        return desTipoRecepcionMx;
    }

    public void setDesTipoRecepcionMx(String desTipoRecepcionMx) {
        this.desTipoRecepcionMx = desTipoRecepcionMx;
    }

    @Column(name = "DES_CAUSA", nullable = true, length = 100)
    public String getDesCausaRechazo() {
        return desCausaRechazo;
    }

    public void setDesCausaRechazo(String desCausaRechazo) {
        this.desCausaRechazo = desCausaRechazo;
    }

    @Column(name = "DES_CONDICIONMX", nullable = true, length = 100)
    public String getDesCondicionMx() {
        return desCondicionMx;
    }

    public void setDesCondicionMx(String desCondicionMx) {
        this.desCondicionMx = desCondicionMx;
    }

    @Column(name = "DES_TIPO_TUBO", nullable = true, length = 100)
    public String getDesTipoTubo() {
        return desTipoTubo;
    }

    public void setDesTipoTubo(String desTipoTubo) {
        this.desTipoTubo = desTipoTubo;
    }

    @Column(name = "DES_CALIDADMX", nullable = true, length = 100)
    public String getDesCalidadMx() {
        return desCalidadMx;
    }

    public void setDesCalidadMx(String desCalidadMx) {
        this.desCalidadMx = desCalidadMx;
    }

    @Override
    public boolean isFieldAuditable(String fieldname) {
        if (fieldname.matches("tomaMx") || fieldname.matches("fechaHoraRecepcion") || fieldname.matches("tipoRecepcionMx") || fieldname.matches("usuarioRecepcion"))
            return false;
        else
            return true;
    }

    @Override
    public String toString() {
        return "idRecepcion='" + idRecepcion + '\'';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecepcionMx)) return false;

        RecepcionMx that = (RecepcionMx) o;

        if (!idRecepcion.equals(that.idRecepcion)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return idRecepcion.hashCode();
    }
}
