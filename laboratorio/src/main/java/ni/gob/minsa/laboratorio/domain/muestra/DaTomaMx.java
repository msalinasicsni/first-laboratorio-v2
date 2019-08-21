package ni.gob.minsa.laboratorio.domain.muestra;

import ni.gob.minsa.laboratorio.domain.audit.Auditable;
import ni.gob.minsa.laboratorio.domain.notificacion.DaNotificacion;
import ni.gob.minsa.laboratorio.domain.portal.Usuarios;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by souyen-ics on 11-05-14.
 */
@Entity
@Table(name = "da_tomamx_v2", schema = "alerta", uniqueConstraints = @UniqueConstraint(columnNames = "CODUNICOMX"))
public class DaTomaMx implements Serializable, Auditable {

    private String idTomaMx;
    private DaNotificacion idNotificacion;
    private TipoMx codTipoMx;
    private Timestamp fechaHTomaMx;//solo tomar en cuenta la fecha
    private String horaRefrigeracion;
    private Integer canTubos;
    private Float volumen;
    private Boolean mxSeparada;
    private String estadoMx;
    private Usuarios usuario;
    private Timestamp fechaRegistro;
    private boolean anulada;
    private Timestamp fechaAnulacion;
    private String codigoUnicoMx;
    private DaEnvioMx envio;
    private String categoriaMx;
    private String codigoLab;
    private Long codSilaisAtencion;
    private Long codUnidadAtencion;
    private String horaTomaMx;
    //Metadata
    private Long idUnidadAtencion;
    private String nombreUnidadAtencion;
    private Long tipoUnidad;
    private Long idMuniUnidadAtencion;
    private Long codMuniUnidadAtencion;
    private String nombreMuniUnidadAtencion;
    private Long idSilaisAtencion;
    private String nombreSilaisAtencion;
    private String desEstadoMx;
    private String desCategoriaMx;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "ID_TOMAMX", nullable = false, insertable = true, updatable = true, length = 36)
    public String getIdTomaMx() {
        return idTomaMx;
    }

    public void setIdTomaMx(String idTomaMx) {
        this.idTomaMx = idTomaMx;
    }


    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_NOTIFICACION", referencedColumnName = "ID_NOTIFICACION", nullable = false)
    @ForeignKey(name = "IDNOTI_FK")
    public DaNotificacion getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(DaNotificacion idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "COD_TIPOMX", referencedColumnName = "ID_TIPOMX", nullable = false)
    @ForeignKey(name = "COD_TIPOMX_FK")
    public TipoMx getCodTipoMx() {
        return codTipoMx;
    }

    public void setCodTipoMx(TipoMx codTipoMx) {
        this.codTipoMx = codTipoMx;
    }

    @Basic
    @Column(name = "FECHAH_TOMAMX", nullable = false, insertable = true, updatable = true)
    public Timestamp getFechaHTomaMx() {
        return fechaHTomaMx;
    }

    public void setFechaHTomaMx(Timestamp fechaHTomaMx) {
        this.fechaHTomaMx = fechaHTomaMx;
    }

    @Basic
    @Column(name = "CANT_TUBOS", nullable = true, insertable = true, updatable = true, precision = 0)
    public Integer getCanTubos() {
        return canTubos;
    }

    public void setCanTubos(Integer canTubos) {
        this.canTubos = canTubos;
    }

    @Column(name = "VOLUMEN", nullable = true)
    public Float getVolumen() {
        return volumen;
    }

    public void setVolumen(Float volumen) {
        this.volumen = volumen;
    }

    @Basic
    @Column(name = "MXSEPARADA", nullable = true, insertable = true, updatable = true)
    public Boolean getMxSeparada() {
        return mxSeparada;
    }

    public void setMxSeparada(Boolean mxSeparada) {
        this.mxSeparada = mxSeparada;
    }


    @ManyToOne(optional = false)
    @JoinColumn(name = "USUARIO_ID", referencedColumnName = "USUARIO_ID")
    @ForeignKey(name = "USUARIO_FK")
    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    @Basic
    @Column(name = "FECHA_REGISTRO", nullable = false, insertable = true, updatable = true)
    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Basic
    @Column(name = "ANULADA", nullable = true, insertable = true, updatable = true)
    public boolean isAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    @Basic
    @Column(name = "FECHA_ANULACION", nullable = true, insertable = true, updatable = true)
    public Timestamp getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(Timestamp fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }


    @Column(name = "COD_ESTADOMX", nullable = false, length = 32)
    public String getEstadoMx() {
        return estadoMx;
    }

    public void setEstadoMx(String estadoMx) {
        this.estadoMx = estadoMx;
    }

    @Basic
    @Column(name = "HORA_REFRIGERACION", nullable = true, insertable = true, updatable = true, length = 50)
    public String getHoraRefrigeracion() {
        return horaRefrigeracion;
    }

    public void setHoraRefrigeracion(String horaRefrigeracion) {
        this.horaRefrigeracion = horaRefrigeracion;
    }

    @Basic
    @Column(name = "CODUNICOMX", nullable = true, insertable = true, updatable = true, length = 12)
    public String getCodigoUnicoMx() {
        return codigoUnicoMx;
    }

    public void setCodigoUnicoMx(String codigoUnicoMx) {
        this.codigoUnicoMx = codigoUnicoMx;
    }

    @ManyToOne(optional = true)
    @JoinColumn(name = "ID_ENVIO", referencedColumnName = "ID_ENVIO")
    @ForeignKey(name = "ENVIOMX_MX_FK")
    public DaEnvioMx getEnvio() {
        return envio;
    }

    public void setEnvio(DaEnvioMx envio) {
        this.envio = envio;
    }

    @Column(name = "COD_CATEGORIA",nullable = true, length = 32)
    public String getCategoriaMx() {
        return categoriaMx;
    }

    public void setCategoriaMx(String categoriaMx) {
        this.categoriaMx = categoriaMx;
    }

    @Basic
    @Column(name = "CODIGO_LAB", nullable = true, insertable = true, updatable = true, length = 16, unique=true)
    public String getCodigoLab() {
        return codigoLab;
    }

    public void setCodigoLab(String codigoLab) {
        this.codigoLab = codigoLab;
    }

    @Column(name = "COD_SILAIS_ATENCION", nullable = true)
    public Long getCodSilaisAtencion() {
        return codSilaisAtencion;
    }

    public void setCodSilaisAtencion(Long codSilaisAtencion) {
        this.codSilaisAtencion = codSilaisAtencion;
    }

    @Column(name = "COD_UNIDAD_ATENCION", nullable = true)
    @ForeignKey(name = "COD_UNIDAD_FK")
    public Long getCodUnidadAtencion() {
        return codUnidadAtencion;
    }

    public void setCodUnidadAtencion(Long codUnidadAtencion) {
        this.codUnidadAtencion = codUnidadAtencion;
    }

    @Basic
    @Column(name = "HORA_TOMAMX", nullable = true, insertable = true, updatable = true, length = 8)
    public String getHoraTomaMx() {
        return horaTomaMx;
    }

    public void setHoraTomaMx(String horaTomaMx) {
        this.horaTomaMx = horaTomaMx;
    }

    //Metadata
    @Basic
    @Column(name = "ID_UNIDAD_ATENCION", nullable = true)
    public Long getIdUnidadAtencion() {
        return idUnidadAtencion;
    }

    public void setIdUnidadAtencion(Long idUnidadAtencion) {
        this.idUnidadAtencion = idUnidadAtencion;
    }

    @Basic
    @Column(name = "NOMBRE_UNIDAD_ATENCION", nullable = true, insertable = true, updatable = true, length = 400)
    public String getNombreUnidadAtencion() {
        return nombreUnidadAtencion;
    }

    public void setNombreUnidadAtencion(String nombreUnidadAtencion) {
        this.nombreUnidadAtencion = nombreUnidadAtencion;
    }

    @Basic
    @Column(name = "TIPO_UNIDAD", nullable = true)
    public Long getTipoUnidad() {
        return this.tipoUnidad;
    }

    public void setTipoUnidad(Long tipoUnidad) {
        this.tipoUnidad = tipoUnidad;
    }

    @Basic
    @Column(name = "ID_MUN_UNIDAD_ATENCION", nullable = true)
    public Long getIdMuniUnidadAtencion() {
        return idMuniUnidadAtencion;
    }

    public void setIdMuniUnidadAtencion(Long idMuniUnidadAtencion) {
        this.idMuniUnidadAtencion = idMuniUnidadAtencion;
    }

    @Basic
    @Column(name = "COD_MUN_UNIDAD_ATENCION", nullable = true)
    public Long getCodMuniUnidadAtencion() {
        return codMuniUnidadAtencion;
    }

    public void setCodMuniUnidadAtencion(Long codMuniUnidadAtencion) {
        this.codMuniUnidadAtencion = codMuniUnidadAtencion;
    }

    @Basic
    @Column(name = "NOM_MUN_UNIDAD_ATENCION", nullable = true)
    public String getNombreMuniUnidadAtencion() {
        return nombreMuniUnidadAtencion;
    }

    public void setNombreMuniUnidadAtencion(String nombreMuniUnidadAtencion) {
        this.nombreMuniUnidadAtencion = nombreMuniUnidadAtencion;
    }

    @Basic
    @Column(name = "ID_SILAIS_ATENCION", nullable = true, insertable = true, updatable = true)
    public Long getIdSilaisAtencion() {
        return idSilaisAtencion;
    }

    public void setIdSilaisAtencion(Long idSilaisAtencion) {
        this.idSilaisAtencion = idSilaisAtencion;
    }

    @Basic
    @Column(name = "NOMBRE_SILAIS_ATENCION", nullable = true, insertable = true, updatable = true, length = 400)
    public String getNombreSilaisAtencion() {
        return nombreSilaisAtencion;
    }

    public void setNombreSilaisAtencion(String nombreSilaisAtencion) {
        this.nombreSilaisAtencion = nombreSilaisAtencion;
    }

    @Basic
    @Column(name = "DES_ESTADOMX", nullable = false, length = 100)
    public String getDesEstadoMx() {
        return desEstadoMx;
    }

    public void setDesEstadoMx(String desEstadoMx) {
        this.desEstadoMx = desEstadoMx;
    }

    @Basic
    @Column(name = "DES_CATEGORIA",nullable = true, length = 100)
    public String getDesCategoriaMx() {
        return desCategoriaMx;
    }

    public void setDesCategoriaMx(String desCategoriaMx) {
        this.desCategoriaMx = desCategoriaMx;
    }

    @Override
    public boolean isFieldAuditable(String fieldname) {
        //if (fieldname.matches("estadoMx") || fieldname.matches("anulada") || fieldname.matches("envio") || fieldname.matches("codigoLab"))
            return true;
        //else
            //return false;
    }

    @Override
    public String toString() {
        return "idTomaMx='" + idTomaMx+ '\'';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DaTomaMx)) return false;

        DaTomaMx tomaMx = (DaTomaMx) o;

        if (idTomaMx != null ? !idTomaMx.equals(tomaMx.idTomaMx) : tomaMx.idTomaMx != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return idTomaMx != null ? idTomaMx.hashCode() : 0;
    }
}
