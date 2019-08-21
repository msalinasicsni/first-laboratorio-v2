package ni.gob.minsa.laboratorio.domain.muestra;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by FIRSTICT on 12/9/2014.
 */
@Entity
@Table(name = "alicuotas_catalogo", schema = "laboratorio")
public class Alicuota implements Serializable {


    private static final long serialVersionUID = -2629993117114280146L;
    private Integer idAlicuota;
    private String alicuota;
    private String volumen;
    private String etiquetaPara;
    private TipoMx tipoMuestra;
    private String tipoRecepcionMx;
    private Catalogo_Estudio estudio;
    private Catalogo_Dx diagnostico;
    boolean pasivo;
    private String desTipoRecepcionMx;

    @Id
    @GeneratedValue(strategy= GenerationType.TABLE)
    @Column(name = "ID_ALICUOTA", nullable = false, updatable = true, insertable = true, precision = 0)
    public Integer getIdAlicuota() {
        return idAlicuota;
    }

    public void setIdAlicuota(Integer idAlicuota) {
        this.idAlicuota = idAlicuota;
    }

    @Basic
    @Column(name = "ALICUOTA", nullable = false, insertable = true, updatable = true, length = 24)
    public String getAlicuota() {
        return alicuota;
    }

    public void setAlicuota(String alicuota) {
        this.alicuota = alicuota;
    }

    @Column(name = "VOLUMEN", nullable = true, length = 24)
    public String getVolumen() { return volumen; }

    public void setVolumen(String volumen) { this.volumen = volumen; }

    @Basic
    @Column(name = "ETIQUETA_PARA", nullable = false, insertable = true, updatable = true, length = 100)
    public String getEtiquetaPara() {
        return etiquetaPara;
    }

    public void setEtiquetaPara(String etiquetaPara) {
        this.etiquetaPara = etiquetaPara;
    }

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "TIPO_MUESTRA", referencedColumnName = "ID_TIPOMX", nullable = false)
    @ForeignKey(name = "ALICUOTA_TIPOMX_FK")
    public TipoMx getTipoMuestra() {
        return tipoMuestra;
    }

    public void setTipoMuestra(TipoMx tipoMuestra) {
        this.tipoMuestra = tipoMuestra;
    }

    @Column(name = "TIPO_RECEPCION", nullable = false, length = 32)
    public String getTipoRecepcionMx() {
        return tipoRecepcionMx;
    }

    public void setTipoRecepcionMx(String tipoRecepcionMx) {
        this.tipoRecepcionMx = tipoRecepcionMx;
    }

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "ESTUDIO", referencedColumnName = "ID_ESTUDIO", nullable = true)
    @ForeignKey(name = "EST_FK")
    public Catalogo_Estudio getEstudio() {
        return estudio;
    }

    public void setEstudio(Catalogo_Estudio estudio) {
        this.estudio = estudio;
    }

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DIAGNOSTICO", referencedColumnName = "ID_DIAGNOSTICO", nullable = true)
    @ForeignKey(name = "ALICUOTA_TIPOEST_FK")

    public Catalogo_Dx getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(Catalogo_Dx diagnostico) {
        this.diagnostico = diagnostico;
    }

    @Basic
    @Column(name = "PASIVO", nullable = true, insertable = true, updatable = true)
    public boolean isPasivo() {
        return pasivo;
    }

    public void setPasivo(boolean pasivo) {
        this.pasivo = pasivo;
    }

    @Column(name = "DES_TIPO_RECEPCION", nullable = false, length = 100)
    public String getDesTipoRecepcionMx() {
        return desTipoRecepcionMx;
    }

    public void setDesTipoRecepcionMx(String desTipoRecepcionMx) {
        this.desTipoRecepcionMx = desTipoRecepcionMx;
    }
}
