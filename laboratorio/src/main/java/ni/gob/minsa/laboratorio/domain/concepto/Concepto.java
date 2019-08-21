package ni.gob.minsa.laboratorio.domain.concepto;

import ni.gob.minsa.laboratorio.domain.audit.Auditable;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import org.hibernate.annotations.ForeignKey;
import javax.persistence.*;
import java.sql.Timestamp;
import java.io.Serializable;

/**
 * Created by souyen-ics.
 */
@Entity
@Table(name = "concepto", schema = "laboratorio")
public class Concepto implements Serializable, Auditable {

    Integer idConcepto;
    String nombre;
    boolean pasivo;
    String tipo;
    User usuarioRegistro;
    Timestamp fechahRegistro;
    String desTipo;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "ID_CONCEPTO", nullable = false, insertable = true, updatable = false)
    public Integer getIdConcepto() {
        return idConcepto;
    }

    public void setIdConcepto(Integer idConcepto) {
        this.idConcepto = idConcepto;
    }

    @Basic
    @Column(name= "NOMBRE", nullable = false, insertable = true, updatable = true, length = 50)
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    @Basic
    @Column(name= "PASIVO", nullable = true, insertable = true, updatable = true)
    public boolean isPasivo() {
        return pasivo;
    }

    public void setPasivo(boolean pasivo) {
        this.pasivo = pasivo;
    }

    @Basic
    @Column(name = "TIPO_DATO_CATALOGO", nullable = true, length = 32)
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }


    @ManyToOne(optional = true)
    @JoinColumn(name = "USUARIO_REGISTRO", referencedColumnName = "username")
    @ForeignKey(name = "CONCEPTO_USUARIO_REG_FK")
    public User getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(User usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    @Basic
    @Column(name = "FECHAH_REGISTRO", nullable = false, insertable = true, updatable = false)
    public Timestamp getFechahRegistro() {
        return fechahRegistro;
    }

    public void setFechahRegistro(Timestamp fechahRegistro) {
        this.fechahRegistro = fechahRegistro;
    }

    @Basic
    @Column(name = "DES_TIPO_DATO_CATALOGO", nullable = true, length = 100)
    public String getDesTipo() {
        return desTipo;
    }

    public void setDesTipo(String desTipo) {
        this.desTipo = desTipo;
    }

    @Override
    public boolean isFieldAuditable(String fieldname) {
        if (fieldname.matches("fechahRegistro") || fieldname.matches("usuarioRegistro"))
            return false;
        else
            return true;
    }

    @Override
    public String toString() {
        return "{idConcepto=" + idConcepto +
                ", nombre='" + nombre + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Concepto)) return false;

        Concepto concepto = (Concepto) o;

        if (!idConcepto.equals(concepto.idConcepto)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return idConcepto.hashCode();
    }
}
