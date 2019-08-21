package ni.gob.minsa.laboratorio.domain.examen;

import ni.gob.minsa.laboratorio.domain.audit.Auditable;
import ni.gob.minsa.laboratorio.domain.muestra.Catalogo_Dx;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import org.hibernate.annotations.ForeignKey;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by FIRSTICT on 12/2/2014.
 */
@Entity
@Table(name = "examen_dx", schema = "laboratorio")
public class Examen_Dx implements Auditable {

    Integer idExamen_Dx;
    Catalogo_Dx diagnostico;
    CatalogoExamenes examen;
    private boolean pasivo;
    Date fechaRegistro;
    User usuarioRegistro;
    private boolean porDefecto;

    @Id
    @GeneratedValue(strategy= GenerationType.TABLE)
    @Column(name = "ID_EXAMEN_DX", nullable = false, insertable = true, updatable = true)
    public Integer getIdExamen_Dx() {
        return idExamen_Dx;
    }

    public void setIdExamen_Dx(Integer idExamen_Dx) {
        this.idExamen_Dx = idExamen_Dx;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_DIAGNOSTICO", referencedColumnName = "ID_DIAGNOSTICO",nullable = false)
    @ForeignKey(name="EXAMENDX_DX_FK")
    public Catalogo_Dx getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(Catalogo_Dx diagnostico) {
        this.diagnostico = diagnostico;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_EXAMEN", referencedColumnName = "ID_EXAMEN",nullable = false)
    @ForeignKey(name="EXAMENDX_EXAMEN_FK")
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
                "idExamen_Dx=" + idExamen_Dx +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Examen_Dx)) return false;

        Examen_Dx examen_dx = (Examen_Dx) o;

        if (!idExamen_Dx.equals(examen_dx.idExamen_Dx)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return idExamen_Dx.hashCode();
    }
}
