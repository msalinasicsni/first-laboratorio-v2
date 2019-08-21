package ni.gob.minsa.laboratorio.domain.examen;

import ni.gob.minsa.laboratorio.domain.audit.Auditable;
import ni.gob.minsa.laboratorio.domain.muestra.Catalogo_Estudio;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import org.hibernate.annotations.ForeignKey;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by FIRSTICT on 12/2/2014.
 */
@Entity
@Table(name = "examen_estudio", schema = "laboratorio")
public class Examen_Estudio implements Auditable {

    Integer idExamen_Estudio;
    Catalogo_Estudio estudio;
    CatalogoExamenes examen;
    private boolean pasivo;
    Date fechaRegistro;
    User usuarioRegistro;
    private boolean porDefecto;

    @Id
    @GeneratedValue(strategy= GenerationType.TABLE)
    @Column(name = "ID_EXAMEN_EST", nullable = false, insertable = true, updatable = true)
    public Integer getIdExamen_Estudio() { return idExamen_Estudio; }

    public void setIdExamen_Estudio(Integer idExamen_Estudio) { this.idExamen_Estudio = idExamen_Estudio; }

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_ESTUDIO", referencedColumnName = "ID_ESTUDIO",nullable = false)
    @ForeignKey(name="EXAMENDX_ESTUDIO_FK")
    public Catalogo_Estudio getEstudio() {
        return estudio;
    }

    public void setEstudio(Catalogo_Estudio diagnostico) {
        this.estudio = diagnostico;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_EXAMEN", referencedColumnName = "ID_EXAMEN",nullable = false)
    @ForeignKey(name="EXAMENEST_EXAMEN_FK")
    public CatalogoExamenes getExamen() {
        return examen;
    }

    public void setExamen(CatalogoExamenes examen) {
        this.examen = examen;
    }

    @Basic
    @Column(name = "PASIVO", nullable = false, insertable = true, updatable = true)
    public boolean isPasivo() {
        return pasivo;
    }

    public void setPasivo(boolean pasivo) {
        this.pasivo = pasivo;
    }

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(name = "FECHA_REGISTRO", nullable = false)
    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @ManyToOne()
    @JoinColumn(name="USUARIO_REGISTRO", referencedColumnName="username", nullable=false)
    @ForeignKey(name = "fk_estTMxNoti_usuario")
    public User getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(User usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    @Basic
    @Column(name = "PORDEFECTO", nullable = false, insertable = true, updatable = true)
    public boolean isPorDefecto() {
        return porDefecto;
    }

    public void setPorDefecto(boolean porDefecto) {
        this.porDefecto = porDefecto;
    }
    @Override
    public boolean isFieldAuditable(String fieldname) {
        if (fieldname.matches("fechaRegistro") || fieldname.matches("usuarioRegistro")) return false;
        return  true;
    }

    @Override
    public String toString() {
        return "{" +
                "idExamen_Estudio=" + idExamen_Estudio +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Examen_Estudio)) return false;

        Examen_Estudio that = (Examen_Estudio) o;

        if (!idExamen_Estudio.equals(that.idExamen_Estudio)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return idExamen_Estudio.hashCode();
    }
}
