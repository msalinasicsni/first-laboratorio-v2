package ni.gob.minsa.laboratorio.domain.persona;


import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.Field;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name="datos_personas", schema="alerta")
public class PersonaTmp implements java.io.Serializable {


    private static final long serialVersionUID = 1L;

    private String idDatosPersona;
    private long personaId;
    private boolean personaIdentificada;
    private String codigoExpUnico;
    private String idTipoIdentificacion;
    private String nombreTipoIdentificacion;
    private String identificacion;
    private String primerNombre;
    private String primerApellido;
    private String segundoNombre;
    private String segundoApellido;
    private Date fechaNacimiento;
    private String codigoSexo;
    private String descSexo;
    private Long fallecida;
    private Long idPaisNacimiento;
    private String nombrePaisNacimiento;
    private Long idRegionNacimiento;
    private String nombreRegionNacimiento;
    private Long idDepartamentoNacimiento;
    private String nombreDepartamentoNacimiento;
    private Long idMunicipioNacimiento;
    private String nombreMunicipioNacimiento;
    private Long idPaisResidencia;
    private String nombrePaisResidencia;
    private Long idRegionResidencia;
    private String nombreRegionResidencia;
    private Long idDepartamentoResidencia;
    private String nombreDepartamentoResidencia;
    private Long idMunicipioResidencia;
    private String nombreMunicipioResidencia;
    private Long idComunidadResidencia;
    private String nombreComunidadResidencia;
    private String direccionResidencia;
    private String telefonoResidencia;
    private String telefonoMovil;
    private Timestamp fechaRegistro;
    private String usuarioRegistro;
    private int pasivo;

    public PersonaTmp() {
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "ID_DATOS_PERSONA", nullable = false, insertable = true, updatable = true, length = 36)
    public String getIdDatosPersona() {
        return idDatosPersona;
    }

    public void setIdDatosPersona(String idDatosPersona) {
        this.idDatosPersona = idDatosPersona;
    }

    @Column(name="PERSONA_ID", nullable=false, precision=10, scale=0)
    public long getPersonaId() {
        return this.personaId;
    }
    
    public void setPersonaId(long personaId) {
        this.personaId = personaId;
    }

    @Basic
    @Column(name = "IDENTIFICADA", nullable = false, insertable = true, updatable = true)
    public boolean isPersonaIdentificada() {
        return personaIdentificada;
    }

    public void setPersonaIdentificada(boolean personaIdentificada) {
        this.personaIdentificada = personaIdentificada;
    }

    @Column(name="CODIGO_EXP_UNICO", length=20)
    public String getCodigoExpUnico() {
        return codigoExpUnico;
    }

    public void setCodigoExpUnico(String codigoExpUnico) {
        this.codigoExpUnico = codigoExpUnico;
    }

    @Column(name="IDENTIFICACION", length=32)
    public String getIdentificacion() {
        return this.identificacion;
    }
    
    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    @Column(name="CODIGO_TIPOIDENTIFICACION", length=20)
    public String getIdTipoIdentificacion() {
        return idTipoIdentificacion;
    }

    public void setIdTipoIdentificacion(String idTipoIdentificacion) {
        this.idTipoIdentificacion = idTipoIdentificacion;
    }

    @Column(name="NOMBRE_TIPOIDENTIFICACION", length=32)
    public String getNombreTipoIdentificacion() {
        return nombreTipoIdentificacion;
    }

    public void setNombreTipoIdentificacion(String nombreTipoIdentificacion) {
        this.nombreTipoIdentificacion = nombreTipoIdentificacion;
    }

    @Field
    @Column(name="PRIMER_NOMBRE", nullable=false, length=50)
    public String getPrimerNombre() {
        return this.primerNombre;
    }
    
    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }
    
    @Field
    @Column(name="PRIMER_APELLIDO", nullable=false, length=50)
    public String getPrimerApellido() {
        return this.primerApellido;
    }
    
    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }
    
    @Field
    @Column(name="SEGUNDO_NOMBRE", length=50)
    public String getSegundoNombre() {
        return this.segundoNombre;
    }
    
    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }
    
    @Field
    @Column(name="SEGUNDO_APELLIDO", length=50)
    public String getSegundoApellido() {
        return this.segundoApellido;
    }
    
    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    @Temporal(TemporalType.DATE)
    @Column(name="FECHA_NACIMIENTO")
    public Date getFechaNacimiento() {
        return this.fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    @Column(name="CODIGO_SEXO", length=20)
    public String getCodigoSexo() {
        return codigoSexo;
    }

    public void setCodigoSexo(String codigoSexo) {
        this.codigoSexo = codigoSexo;
    }

    @Column(name="NOMBRE_SEXO", length=20)
    public String getDescSexo() {
        return descSexo;
    }

    public void setDescSexo(String descSexo) {
        this.descSexo = descSexo;
    }

    @Column(name="FALLECIDA",precision=1)
    public Long getFallecida() {
        return fallecida;
    }

    public void setFallecida(Long fallecida) {
        this.fallecida = fallecida;
    }

    @Column(name="ID_PAIS_NACIMIENTO")
    public Long getIdPaisNacimiento() {
        return idPaisNacimiento;
    }

    public void setIdPaisNacimiento(Long idPais) {
        this.idPaisNacimiento = idPais;
    }

    @Column(name="NOMBRE_PAIS_NACIMIENTO", length = 100)
    public String getNombrePaisNacimiento() {
        return nombrePaisNacimiento;
    }

    public void setNombrePaisNacimiento(String nombrePais) {
        this.nombrePaisNacimiento = nombrePais;
    }

    @Column(name="ID_REGION_NACIMIENTO")
    public Long getIdRegionNacimiento() {
        return idRegionNacimiento;
    }

    public void setIdRegionNacimiento(Long idRegionNacimiento) {
        this.idRegionNacimiento = idRegionNacimiento;
    }

    @Column(name="NOMBRE_REGION_NACIMIENTO", length = 100)
    public String getNombreRegionNacimiento() {
        return nombreRegionNacimiento;
    }

    public void setNombreRegionNacimiento(String nombreRegionNacimiento) {
        this.nombreRegionNacimiento = nombreRegionNacimiento;
    }

    @Column(name="ID_DEPARTAMENTE_NACIMIENTO")
    public Long getIdDepartamentoNacimiento() {
        return idDepartamentoNacimiento;
    }

    public void setIdDepartamentoNacimiento(Long idDepartamentoNacimiento) {
        this.idDepartamentoNacimiento = idDepartamentoNacimiento;
    }

    @Column(name="NOMBRE_DEPARTAMENTO_NACIMIENTO", length = 100)
    public String getNombreDepartamentoNacimiento() {
        return nombreDepartamentoNacimiento;
    }

    public void setNombreDepartamentoNacimiento(String nombreDepartamentoNacimiento) {
        this.nombreDepartamentoNacimiento = nombreDepartamentoNacimiento;
    }

    @Column(name="ID_MUNICIPIO_NACIMIENTO")
    public Long getIdMunicipioNacimiento() {
        return idMunicipioNacimiento;
    }

    public void setIdMunicipioNacimiento(Long idMunicipioNacimiento) {
        this.idMunicipioNacimiento = idMunicipioNacimiento;
    }

    @Column(name="NOMBRE_MUNICIPIO_NACIMIENTO", length = 100)
    public String getNombreMunicipioNacimiento() {
        return nombreMunicipioNacimiento;
    }

    public void setNombreMunicipioNacimiento(String nombreMunicipioNacimiento) {
        this.nombreMunicipioNacimiento = nombreMunicipioNacimiento;
    }

    @Column(name="ID_PAIS_RESIDENCIA")
    public Long getIdPaisResidencia() {
        return idPaisResidencia;
    }

    public void setIdPaisResidencia(Long idPaisResidencia) {
        this.idPaisResidencia = idPaisResidencia;
    }

    @Column(name="NOMBRE_PAIS_RESIDENCIA", length = 100)
    public String getNombrePaisResidencia() {
        return nombrePaisResidencia;
    }

    public void setNombrePaisResidencia(String nombrePaisResidencia) {
        this.nombrePaisResidencia = nombrePaisResidencia;
    }

    @Column(name="ID_REGION_RESIDENCIA")
    public Long getIdRegionResidencia() {
        return idRegionResidencia;
    }

    public void setIdRegionResidencia(Long idRegionResidencia) {
        this.idRegionResidencia = idRegionResidencia;
    }

    @Column(name="NOMBRE_REGION_RESIDENCIA", length = 100)
    public String getNombreRegionResidencia() {
        return nombreRegionResidencia;
    }

    public void setNombreRegionResidencia(String nombreRegionResidencia) {
        this.nombreRegionResidencia = nombreRegionResidencia;
    }

    @Column(name="ID_DEPARTAMENTO_RESIDENCIA")
    public Long getIdDepartamentoResidencia() {
        return idDepartamentoResidencia;
    }

    public void setIdDepartamentoResidencia(Long idDepartamentoResidencia) {
        this.idDepartamentoResidencia = idDepartamentoResidencia;
    }

    @Column(name="NOMBRE_DEPARTAMENTO_RESIDENCIA", length = 100)
    public String getNombreDepartamentoResidencia() {
        return nombreDepartamentoResidencia;
    }

    public void setNombreDepartamentoResidencia(String nombreDepartamentoResidencia) {
        this.nombreDepartamentoResidencia = nombreDepartamentoResidencia;
    }

    @Column(name="ID_MUNICIPIO_RESIDENCIA")
    public Long getIdMunicipioResidencia() {
        return idMunicipioResidencia;
    }

    public void setIdMunicipioResidencia(Long idMunicipioResidencia) {
        this.idMunicipioResidencia = idMunicipioResidencia;
    }

    @Column(name="NOMBRE_MUNICIPIO_RESIDENCIA", length = 100)
    public String getNombreMunicipioResidencia() {
        return nombreMunicipioResidencia;
    }

    public void setNombreMunicipioResidencia(String nombreMunicipioResidencia) {
        this.nombreMunicipioResidencia = nombreMunicipioResidencia;
    }

    @Column(name="ID_COMUNIDAD_RESIDENCIA")
    public Long getIdComunidadResidencia() {
        return idComunidadResidencia;
    }

    public void setIdComunidadResidencia(Long idComunidadResidencia) {
        this.idComunidadResidencia = idComunidadResidencia;
    }

    @Column(name="NOMBRE_COMUNIDAD_RESIDENCIA", length = 150)
    public String getNombreComunidadResidencia() {
        return nombreComunidadResidencia;
    }

    public void setNombreComunidadResidencia(String nombreComunidadResidencia) {
        this.nombreComunidadResidencia = nombreComunidadResidencia;
    }

    @Column(name="DIRECCION_RESIDENCIA", length=250)
    public String getDireccionResidencia() {
        return this.direccionResidencia;
    }

    public void setDireccionResidencia(String direccionResidencia) {
        this.direccionResidencia = direccionResidencia;
    }

    @Column(name="TELEFONO_RESIDENCIA", length=20)
    public String getTelefonoResidencia() {
        return this.telefonoResidencia;
    }

    public void setTelefonoResidencia(String telefonoResidencia) {
        this.telefonoResidencia = telefonoResidencia;
    }

    @Column(name="TELEFONO_MOVIL", length=20)
    public String getTelefonoMovil() {
        return this.telefonoMovil;
    }

    public void setTelefonoMovil(String telefonoMovil) {
        this.telefonoMovil = telefonoMovil;
    }

    @Column(name="FECHA_REGISTRO", nullable=false)
    public Timestamp getFechaRegistro() {
        return this.fechaRegistro;
    }
    
    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    @Column(name="USUARIO_REGISTRO", nullable=false, length=100)
    public String getUsuarioRegistro() {
        return this.usuarioRegistro;
    }
    
    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

	@Column(name = "PASIVO", nullable = false, length = 1)
	public int getPasivo() {
		return pasivo;
	}

	public void setPasivo(int pasivo) {
		this.pasivo = pasivo;
	}

	@Transient
    public boolean isSexoFemenino() {
        boolean resultado = false;
                
        if(this.codigoSexo!=null){
            if(this.codigoSexo.equals("SEXO|F")){
                resultado = true;
            }
        }
        
        return resultado;
    }
    
    @Transient
    public boolean isSexoMasculino() {
        boolean resultado = false;
                
        if(this.codigoSexo!=null){
            if(this.codigoSexo.equals("SEXO|M")){
                resultado = true;
            }
        }
        
        return resultado;
    }
           
    
}


