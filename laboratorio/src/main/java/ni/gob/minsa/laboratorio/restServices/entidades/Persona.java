package ni.gob.minsa.laboratorio.restServices.entidades;

import java.util.List;

public class Persona implements java.io.Serializable {


    private static final long serialVersionUID = 1L;

    private long id;
    private boolean identificada;
    private Identificacion identificacion;
    private String primerNombre;
    private String primerApellido;
    private String segundoNombre;
    private String segundoApellido;
    private Sexo sexo;
    private String fechaNacimiento;
    private Fallecimiento fallecimiento;
    private Paciente paciente;
    private DivisionPolitica divisionPolitica;
    private RedServicio redServicio;

    public Persona(long id, boolean identificada, Identificacion identificacion, String primerNombre, String primerApellido, String segundoNombre, String segundoApellido, Sexo sexo, String fechaNacimiento, Fallecimiento fallecimiento, Paciente paciente, DivisionPolitica divisionPolitica, RedServicio redServicio) {
        this.id = id;
        this.identificada = identificada;
        this.identificacion = identificacion;
        this.primerNombre = primerNombre;
        this.primerApellido = primerApellido;
        this.segundoNombre = segundoNombre;
        this.segundoApellido = segundoApellido;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.fallecimiento = fallecimiento;
        this.paciente = paciente;
        this.divisionPolitica = divisionPolitica;
        this.redServicio = redServicio;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isIdentificada() {
        return identificada;
    }

    public void setIdentificada(boolean identificada) {
        this.identificada = identificada;
    }

    public Identificacion getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(Identificacion identificacion) {
        this.identificacion = identificacion;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Fallecimiento getFallecimiento() {
        return fallecimiento;
    }

    public void setFallecimiento(Fallecimiento fallecimiento) {
        this.fallecimiento = fallecimiento;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public DivisionPolitica getDivisionPolitica() {
        return divisionPolitica;
    }

    public void setDivisionPolitica(DivisionPolitica divisionPolitica) {
        this.divisionPolitica = divisionPolitica;
    }

    public RedServicio getRedServicio() {
        return redServicio;
    }

    public void setRedServicio(RedServicio redServicio) {
        this.redServicio = redServicio;
    }

    public class Identificacion {
        private Long id;
        private String codigo;
        private String nombre;
        private String valor;

        public Identificacion(Long id, String codigo, String nombre, String valor) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.valor = valor;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }

    public class Sexo {
        private Long id;
        private String codigo;
        private String valor;

        public Sexo(Long id, String codigo, String valor) {
            this.id = id;
            this.codigo = codigo;
            this.valor = valor;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }

    public class Fallecimiento {
        private boolean fallecido;

        public Fallecimiento(boolean fallecido) {
            this.fallecido = fallecido;
        }

        public boolean isFallecido() {
            return fallecido;
        }

        public void setFallecido(boolean fallecido) {
            this.fallecido = fallecido;
        }
    }

    public class Paciente {
        private Long id;
        private CodigoExpediente codigoExpediente;

        public Paciente(Long id, CodigoExpediente codigoExpediente) {
            this.id = id;
            this.codigoExpediente = codigoExpediente;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public CodigoExpediente getCodigoExpediente() {
            return codigoExpediente;
        }

        public void setCodigoExpediente(CodigoExpediente codigoExpediente) {
            this.codigoExpediente = codigoExpediente;
        }
    }

    public class CodigoExpediente{
        private Long id;
        private String codigo;
        private String nombre;
        private String valor;

        public CodigoExpediente(Long id, String codigo, String nombre, String valor) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.valor = valor;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }

    public class DivisionPolitica{
        private Nacimiento nacimiento;
        private Residencia residencia;

        public DivisionPolitica(Nacimiento nacimiento, Residencia residencia) {
            this.nacimiento = nacimiento;
            this.residencia = residencia;
        }

        public Nacimiento getNacimiento() {
            return nacimiento;
        }

        public void setNacimiento(Nacimiento nacimiento) {
            this.nacimiento = nacimiento;
        }

        public Residencia getResidencia() {
            return residencia;
        }

        public void setResidencia(Residencia residencia) {
            this.residencia = residencia;
        }
    }

    public class Nacimiento {
        private Dupla municipio;
        private Dupla departamento;
        private Dupla region;
        private Dupla pais;

        Nacimiento(Dupla municipio, Dupla departamento, Dupla region, Dupla pais) {
            this.municipio = municipio;
            this.departamento = departamento;
            this.region = region;
            this.pais = pais;
        }

        public Dupla getMunicipio() {
            return municipio;
        }

        public void setMunicipio(Dupla municipio) {
            this.municipio = municipio;
        }

        public Dupla getDepartamento() {
            return departamento;
        }

        public void setDepartamento(Dupla departamento) {
            this.departamento = departamento;
        }

        public Dupla getRegion() {
            return region;
        }

        public void setRegion(Dupla region) {
            this.region = region;
        }

        public Dupla getPais() {
            return pais;
        }

        public void setPais(Dupla pais) {
            this.pais = pais;
        }
    }

    public class Residencia {
        private Dupla distrito;
        private Dupla comunidad;
        private Dupla municipio;
        private Dupla departamento;
        private Dupla region;
        private String personaDireccion;

        Residencia(Dupla distrito, Dupla comunidad, Dupla municipio, Dupla departamento, Dupla region, String personaDireccion) {
            this.distrito = distrito;
            this.comunidad = comunidad;
            this.municipio = municipio;
            this.departamento = departamento;
            this.region = region;
            this.personaDireccion = personaDireccion;
        }

        public Dupla getDistrito() {
            return distrito;
        }

        public void setDistrito(Dupla distrito) {
            this.distrito = distrito;
        }

        public Dupla getComunidad() {
            return comunidad;
        }

        public void setComunidad(Dupla comunidad) {
            this.comunidad = comunidad;
        }

        public Dupla getMunicipio() {
            return municipio;
        }

        public void setMunicipio(Dupla municipio) {
            this.municipio = municipio;
        }

        public Dupla getDepartamento() {
            return departamento;
        }

        public void setDepartamento(Dupla departamento) {
            this.departamento = departamento;
        }

        public Dupla getRegion() {
            return region;
        }

        public void setRegion(Dupla region) {
            this.region = region;
        }

        public String getPersonaDireccion() {
            return personaDireccion;
        }

        public void setPersonaDireccion(String personaDireccion) {
            this.personaDireccion = personaDireccion;
        }
    }

    public class RedServicio{
        private InfoRedServicio residencia;
        private InfoRedServicio ocurrencia;

        public RedServicio(InfoRedServicio residencia, InfoRedServicio ocurrencia) {
            this.residencia = residencia;
            this.ocurrencia = ocurrencia;
        }

        public InfoRedServicio getResidencia() {
            return residencia;
        }

        public void setResidencia(InfoRedServicio residencia) {
            this.residencia = residencia;
        }

        public InfoRedServicio getOcurrencia() {
            return ocurrencia;
        }

        public void setOcurrencia(InfoRedServicio ocurrencia) {
            this.ocurrencia = ocurrencia;
        }
    }

    public class InfoRedServicio{
        private Dupla sector;
        private Dupla unidadSalud;
        private Dupla entidadAdministrativa;

        public InfoRedServicio(Dupla sector, Dupla unidadSalud, Dupla entidadAdministrativa) {
            this.sector = sector;
            this.unidadSalud = unidadSalud;
            this.entidadAdministrativa = entidadAdministrativa;
        }

        public Dupla getSector() {
            return sector;
        }

        public void setSector(Dupla sector) {
            this.sector = sector;
        }

        public Dupla getUnidadSalud() {
            return unidadSalud;
        }

        public void setUnidadSalud(Dupla unidadSalud) {
            this.unidadSalud = unidadSalud;
        }

        public Dupla getEntidadAdministrativa() {
            return entidadAdministrativa;
        }

        public void setEntidadAdministrativa(Dupla entidadAdministrativa) {
            this.entidadAdministrativa = entidadAdministrativa;
        }
    }

    public class Dupla {
        private long id;
        private String nombre;

        Dupla(long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }


}


