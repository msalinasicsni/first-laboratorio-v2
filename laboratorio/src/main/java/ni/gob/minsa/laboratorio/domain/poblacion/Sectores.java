package ni.gob.minsa.laboratorio.domain.poblacion;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;


@Entity
@Table(name = "sectores", schema = "general")
public class Sectores implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer sectorId;
	private String nombre;
	private String referencias;
	private Long unidad;
	private String municipio;
	private String codigo;
	private char sede = 0;
	private char pasivo = 0;
	private Date fechaRegistro;
	private String usuarioRegistro;
	

	public Sectores() {
	}

	
	@Id
	@Column(name = "SECTOR_ID", nullable = false)
	public Integer getSectorId() {
		return this.sectorId;
	}

	public void setSectorId(Integer sectorId) {
		this.sectorId = sectorId;
	}

	@Column(name = "NOMBRE", nullable = false, length = 100)
	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Column(name = "REFERENCIAS", nullable = true, length = 500)
	public String getReferencias() {
		return this.referencias;
	}

	public void setReferencias(String referencias) {
		this.referencias = referencias;
	}

	@Column(name="UNIDAD", nullable=true)
	public Long getUnidad() {
		return unidad;
	}

	public void setUnidad(Long unidad) {
		this.unidad = unidad;
	}

	@Column(name="MUNICIPIO", nullable=true)
	public String getMunicipio() {
		return municipio;
	}


	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	@Column(name = "CODIGO", nullable = false, length = 7)
	public String getCodigo() {
		return this.codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	@Column(name = "SEDE", nullable = false, length = 1)
	public char getSede() {
		return sede;
	}


	public void setSede(char sede) {
		this.sede = sede;
	}


	@Column(name = "PASIVO", nullable = false, length = 1)
	public char getPasivo() {
		return this.pasivo;
	}

	public void setPasivo(char pasivo) {
		this.pasivo = pasivo;
	}

	@Column(name = "USUARIO_REGISTRO", nullable = false, length = 100)
	public String getUsuarioRegistro() {
		return this.usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	@Column(name="FECHA_REGISTRO")
	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

}
