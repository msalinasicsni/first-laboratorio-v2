package ni.gob.minsa.laboratorio.service;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.naming.InitialContext;

import ni.gob.minsa.laboratorio.domain.persona.*;

import ni.gob.minsa.laboratorio.utilities.ConstantsSecurity;
import ni.gob.minsa.ciportal.dto.InfoResultado;
import ni.gob.minsa.ejbPersona.dto.Persona;
import ni.gob.minsa.ejbPersona.servicios.PersonaBTMService;
import ni.gob.minsa.ejbPersona.servicios.PersonaUTMService;
import ni.gob.minsa.laboratorio.utilities.DateUtil;
import org.apache.commons.codec.language.Soundex;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("personaService")
@Transactional
public class PersonaService {

    @Resource(name="sessionFactory")
    private SessionFactory sessionFactory;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PersonaService.class);
    private PersonaUTMService personaUTMService;
    private InitialContext initialContext;
    public static final Pattern DIACRITICS_AND_FRIENDS
            = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    private int rowCount = 0;
    private static String stripDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        return str;
    }

    @SuppressWarnings("unchecked")
    //ABRIL2019
    /*
    public List<SisPersona> getPersonas(String filtro){
        try {
            filtro = URLDecoder.decode(filtro, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Session session = sessionFactory.getCurrentSession();
        if(filtro.matches("[0-9]*")){
            return session.createCriteria(SisPersona.class)
                    .add( Restrictions.or(
                                    Restrictions.eq("telefonoResidencia", filtro),
                                    Restrictions.eq("telefonoMovil", filtro))
                    )
                    .list();
        }else if(filtro.matches("[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]*")){
            Soundex varSoundex = new Soundex();

            Criteria crit = session.createCriteria(SisPersona.class);
            String[] partes = filtro.split(" ");
            String[] partesSnd = filtro.split(" ");
            for(int i=0;i<partes.length;i++){
                try{
                    String noDiacri = stripDiacritics(partes[i]);
                    partesSnd[i] = varSoundex.encode(noDiacri);
                }
                catch (IllegalArgumentException e){
                    partesSnd[i] = "0000";
                    e.printStackTrace();
                }
            }
            for(int i=0;i<partes.length;i++){
                Junction conditionGroup = Restrictions.disjunction();
                conditionGroup.add(Restrictions.ilike("primerNombre" , "%"+partes[i]+"%" ))
                        .add(Restrictions.ilike( "primerApellido" , "%"+partes[i]+"%"  ))
                        .add(Restrictions.ilike( "segundoNombre" , "%"+partes[i]+"%"  ))
                        .add(Restrictions.ilike( "segundoApellido" , "%"+partes[i]+"%"  ))
                        .add(Restrictions.ilike("sndNombre", "%"+partesSnd[i]+"%"));
                crit.add(conditionGroup);
            }

            return crit.list();
        }
        else{
            return session.createCriteria(SisPersona.class)
                    .add( Restrictions.or(
                                    Restrictions.eq("identificacionHse", filtro).ignoreCase(),
                                    Restrictions.eq("identificacion", filtro).ignoreCase())
                    )
                    .list();
        }
    }

    public List<SisPersona> getPersonas(int pPaginaActual,int pRegistrosPorPagina,String pFiltro){
        List<SisPersona> personaList = null;
        try {
            pFiltro = URLDecoder.decode(pFiltro, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try{
            if(pFiltro!=null){
                if(pFiltro.matches("[0-9]*")){
                    personaList = getPersonasPorNumeroTelefono(pPaginaActual, pRegistrosPorPagina, pFiltro,null);
                }else if(pFiltro.matches("[a-zA-ZáéíóúÑñÁÉÍÓÚÜü\\s]*")){
                    personaList = getPersonasPorNombre(pPaginaActual, pRegistrosPorPagina, pFiltro,null);
                }else{
                    personaList = getPersonasPorIdentificacion(pPaginaActual, pRegistrosPorPagina, pFiltro,null);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        return personaList;
    }

    public List<SisPersona> getPersonasPorNombre(int pFilaActual,int pRegistrosPorPagina,String pNombrePersona,String pSexo){

        List<SisPersona> personaList = null;
        pNombrePersona = pNombrePersona.trim().toUpperCase();
        String queryString = "";

        try{
            Session session = sessionFactory.getCurrentSession();

            //Criteria crit = session.createCriteria(SisPersona.class);
            String[] partes = pNombrePersona.split(" ");
            queryString = "select {per.*} from sis.sis_personas {per} where catsearch(snd_nombre,";
            String codigoNombre = "";
            for(int i=0;i<partes.length;i++){
                if (!partes[i].isEmpty()) {//por si hay más de un espacio entre las partes del nombre
                    String codigo = ((String) session.createSQLQuery("select sis.PKG_SND.soundesp(sis.PKG_SND.remover_acentos(TRIM(REPLACE(REPLACE(REPLACE(REPLACE('" + partes[i].trim() + "','DE LOS ',''),'DE LA ',''),'LA ',''),'DE ','')))) from dual").list().get(0)).toString();
                    if (i == 0) {
                        codigoNombre = codigoNombre.concat(codigo);
                    } else {
                        codigoNombre = codigoNombre.concat(" " + codigo);
                    }
                }
            }
            queryString = queryString.concat("'" + codigoNombre + "',null) > 0");
            if(pSexo!=null) queryString = queryString.concat(" AND codigo_sexo='"+pSexo+"'");
            
            String queryConteo = queryString.replaceAll("\\{per.\\*}", "count(\\*)");
            queryConteo = queryConteo.replaceAll("\\{per}","");
            this.rowCount = ((BigDecimal)session.createSQLQuery(queryConteo).list().get(0)).intValue();
            if(rowCount <= pFilaActual) pFilaActual -= rowCount;
            pFilaActual = rowCount <= pFilaActual ? 0 : pFilaActual;
            personaList = (List<SisPersona>)session.createSQLQuery(queryString).addEntity("per", SisPersona.class).setFirstResult(pFilaActual).setMaxResults(pRegistrosPorPagina).list();

            if(personaList == null){
                this.rowCount=0;
            }

        }catch(Exception e){
            this.rowCount = 0;
            throw  e;
        }

        return personaList;
    }

    public List<SisPersona> getPersonasPorNumeroTelefono(int pPaginaActual,int pRegistrosPorPagina,String pNumeroTelefono,String pSexo){

        List<SisPersona> personaList = null;
        Session session = sessionFactory.getCurrentSession();
        try{
            if(!pNumeroTelefono.trim().equals("")){
                Criteria critPersonas = session.createCriteria(SisPersona.class);
                critPersonas.add(Restrictions.or(Restrictions.eq("telefonoResidencia",  pNumeroTelefono.trim().toUpperCase()), Restrictions.eq("telefonoMovil",  pNumeroTelefono.trim().toUpperCase())));
                if(pSexo!=null) critPersonas.add(Restrictions.eq("codigoSexo", pSexo));

                this.rowCount = ((Number) critPersonas.setProjection(Projections.rowCount()).uniqueResult()).intValue();
                if(rowCount <= pPaginaActual) pPaginaActual-= rowCount;
                pPaginaActual = rowCount <= pPaginaActual ? 0 : pPaginaActual;
                critPersonas.setProjection(null);
                critPersonas.setFirstResult(pPaginaActual);
                critPersonas.setMaxResults(pRegistrosPorPagina);
                critPersonas.addOrder(Order.asc("primerApellido"));
                personaList = (List<SisPersona>) critPersonas.list();

                if(this.rowCount < 1){
                    personaList = null;
                }
            }

        }catch(Exception e){
            this.rowCount = 0;
            throw e;
        }

        return personaList;
    }

    public List<SisPersona> getPersonasPorIdentificacion(int pPaginaActual,int pRegistrosPorPagina,String pIdentificacion,String pSexo){

        List<SisPersona> personaList = null;
        Session session = sessionFactory.getCurrentSession();
        try{
            if(!pIdentificacion.trim().equals("")){
                Criteria critPersonas = session.createCriteria(SisPersona.class);
                critPersonas.add(Restrictions.eq("identificacion",  pIdentificacion.trim().toUpperCase()));
                if(pSexo!=null) critPersonas.add(Restrictions.eq("codigoSexo", pSexo));

                this.rowCount = ((Number) critPersonas.setProjection(Projections.rowCount()).uniqueResult()).intValue();
                if(rowCount <= pPaginaActual) pPaginaActual-= rowCount;
                pPaginaActual = rowCount <= pPaginaActual ? 0 : pPaginaActual;
                critPersonas.setProjection(null);
                critPersonas.setFirstResult(pPaginaActual);
                critPersonas.setMaxResults(pRegistrosPorPagina);
                critPersonas.addOrder(Order.asc("primerApellido"));
                personaList = (List<SisPersona>) critPersonas.list();

                if(this.rowCount < 1){
                    personaList = null;
                }
            }
        }catch(Exception e){
            this.rowCount = 0;
            throw e;
        }

        return personaList;
    }

    public SisPersona getPersona(long idPerson){
        // Retrieve session from Hibernate
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM SisPersona p where p.personaId = :idPerson");
        query.setParameter("idPerson", idPerson);
        SisPersona persona = (SisPersona) query.uniqueResult();
        return persona;

    }
*/
    public void iniciarTransaccion() throws Exception {
        this.initialContext = new InitialContext();
        this.personaUTMService = (PersonaUTMService) initialContext.lookup(ConstantsSecurity.EJB_BIN_PERSON_UTM);
        this.personaUTMService.iniciarTransaccion();
        logger.info("Se inicia transacción personaUTMService");
    }

    public void commitTransaccion() throws Exception {
        this.personaUTMService.commitTransaccion();
    }

    public void rollbackTransaccion() throws Exception {
        this.personaUTMService.rollbackTransaccion();
        logger.info("Se hiso rollback personaUTMService");
    }

    public void remover() throws Exception {
        this.personaUTMService.remover();
        this.initialContext.close();
        logger.info("Se cierra conexión personaUTMService");
    }

    public InfoResultado guardarPersona(Persona pPersona, String pUsuarioRegistra) {
        logger.info("Se guardar persona mediante componente");
        return this.personaUTMService.guardarPersona(pPersona, pUsuarioRegistra);
    }

    public Persona buscarPorId(long pIdPersona) throws Exception {

        InfoResultado infoResultado;
        InitialContext ctx;
        Persona persona = null;
        try{
            ctx = new InitialContext();
            PersonaBTMService servicio = (PersonaBTMService) ctx.lookup(ConstantsSecurity.EJB_BIN_PERSON_BTM);

            infoResultado = servicio.obtenerPorId(pIdPersona);
            if(infoResultado.isOk() && infoResultado.getObjeto()!=null){
                persona = (Persona) infoResultado.getObjeto();
            }else{
                throw new Exception("No se encontro persona"+infoResultado.getMensaje()+infoResultado.getMensajeDetalle());
            }
            ctx.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

        return persona;
    }
    //ABRIL2019
    /*
     public Persona ensamblarObjetoPersona(SisPersona pPersona){
        Persona persona = null;

        if(pPersona!=null){
            persona = new Persona();

            persona.setPersonaId(pPersona.getPersonaId());
            persona.setFechaNacimiento(pPersona.getFechaNacimiento());
            persona.setPrimerNombre(pPersona.getPrimerNombre());
            persona.setPrimerApellido(pPersona.getPrimerApellido());
            persona.setSegundoNombre(pPersona.getSegundoNombre());
            persona.setSegundoApellido(pPersona.getSegundoApellido());
            persona.setDireccionResi(pPersona.getDireccionResidencia());
            persona.setIdentHse(pPersona.getIdentificacionHse());
            persona.setIdentNumero(pPersona.getIdentificacion());
            persona.setAseguradoNumero(pPersona.getNumeroAsegurado());
            persona.setTelefonoResi(pPersona.getTelefonoResidencia());
            persona.setTelefonoMovil(pPersona.getTelefonoMovil());
            //persona.setSexoCodigo(pPersona.getSexo()!=null ? pPersona.getSexo().getCodigo() : null);
            //persona.setEtniaCodigo(pPersona.getEtnia()!=null ?pPersona.getEtnia().getCodigo() : null);
            //persona.setOcupacionCodigo(pPersona.getOcupacion()!=null ? pPersona.getOcupacion().getCodigo() : null);
            //persona.setTipoAsegCodigo(pPersona.getTipoAsegurado()!=null? pPersona.getTipoAsegurado().getCodigo() : null);
            //persona.setIdentCodigo(pPersona.getTipoIdentificacion()!=null ? pPersona.getTipoIdentificacion().getCodigo() : null);
            //persona.setEscolaridadCodigo(pPersona.getEscolaridad()!=null ? pPersona.getEscolaridad().getCodigo() : null);
            //persona.setEstadoCivilCodigo(pPersona.getEstadoCivil()!=null ? pPersona.getEstadoCivil().getCodigo() : null);
            //persona.setPaisNacCodigoAlfados(pPersona.getPaisNacimiento()!=null ? pPersona.getPaisNacimiento().getCodigoAlfados() : null);
            //persona.setMuniNacCodigoNac(pPersona.getMunicipioNacimiento()!=null ? pPersona.getMunicipioNacimiento().getCodigoNacional() : null);
            //persona.setMuniResiCodigoNac(pPersona.getMunicipioResidencia()!=null ? pPersona.getMunicipioResidencia().getCodigoNacional() : null);
            //persona.setComuResiCodigo(pPersona.getComunidadResidencia()!=null ? pPersona.getComunidadResidencia().getCodigo() : null);
        }

        return persona;
    }
    */

    public PersonaTmp parsePersonaMinsaToDatosPersona(ni.gob.minsa.laboratorio.restServices.entidades.Persona persona) throws Exception{
        PersonaTmp personaTmp = new PersonaTmp();
        if (persona !=null) {

            personaTmp.setPersonaId(persona.getId());
            personaTmp.setPersonaIdentificada(persona.isIdentificada());
            if (persona.getPaciente() !=null && persona.getPaciente().getCodigoExpediente()!=null) {
               personaTmp.setCodigoExpUnico(persona.getPaciente().getCodigoExpediente().getValor());
            }
            if (persona.getIdentificacion()!=null) {
                personaTmp.setIdTipoIdentificacion(persona.getIdentificacion().getCodigo());
                personaTmp.setNombreTipoIdentificacion(persona.getIdentificacion().getNombre());
                personaTmp.setIdentificacion(persona.getIdentificacion().getValor());
            }
            personaTmp.setPrimerNombre(persona.getPrimerNombre());
            personaTmp.setPrimerApellido(persona.getPrimerApellido());
            personaTmp.setSegundoNombre(persona.getSegundoNombre());
            personaTmp.setSegundoApellido(persona.getSegundoApellido());
            if (persona.getFechaNacimiento()!=null)
                personaTmp.setFechaNacimiento(DateUtil.StringToDate(persona.getFechaNacimiento(), "yyyy-MM-dd"));
            if (persona.getSexo()!=null) {
                personaTmp.setCodigoSexo(persona.getSexo().getCodigo());
                personaTmp.setDescSexo(persona.getSexo().getValor());
            }
            if (persona.getFallecimiento()!=null)
                personaTmp.setFallecida(persona.getFallecimiento().isFallecido()?1L:0L);

            if (persona.getDivisionPolitica() != null) {
                if (persona.getDivisionPolitica().getNacimiento() != null) {
                    personaTmp.setIdPaisNacimiento(persona.getDivisionPolitica().getNacimiento().getPais().getId());
                    personaTmp.setNombrePaisNacimiento(persona.getDivisionPolitica().getNacimiento().getPais().getNombre());
                    personaTmp.setIdRegionNacimiento(persona.getDivisionPolitica().getNacimiento().getRegion().getId());
                    personaTmp.setNombreRegionNacimiento(persona.getDivisionPolitica().getNacimiento().getRegion().getNombre());
                    personaTmp.setIdDepartamentoNacimiento(persona.getDivisionPolitica().getNacimiento().getDepartamento().getId());
                    personaTmp.setNombreDepartamentoNacimiento(persona.getDivisionPolitica().getNacimiento().getDepartamento().getNombre());
                    personaTmp.setIdMunicipioNacimiento(persona.getDivisionPolitica().getNacimiento().getMunicipio().getId());
                    personaTmp.setNombreMunicipioNacimiento(persona.getDivisionPolitica().getNacimiento().getMunicipio().getNombre());
                }
                if (persona.getDivisionPolitica().getResidencia() != null) {
                    //personaTmp.setIdPaisResidencia();
                    //personaTmp.setNombrePaisResidencia();
                    personaTmp.setIdRegionResidencia(persona.getDivisionPolitica().getResidencia().getRegion().getId());
                    personaTmp.setNombreRegionResidencia(persona.getDivisionPolitica().getResidencia().getRegion().getNombre());
                    personaTmp.setIdDepartamentoResidencia(persona.getDivisionPolitica().getResidencia().getDepartamento().getId());
                    personaTmp.setNombreDepartamentoResidencia(persona.getDivisionPolitica().getResidencia().getDepartamento().getNombre());
                    personaTmp.setIdMunicipioResidencia(persona.getDivisionPolitica().getResidencia().getMunicipio().getId());
                    personaTmp.setNombreMunicipioResidencia(persona.getDivisionPolitica().getResidencia().getMunicipio().getNombre());
                    personaTmp.setIdComunidadResidencia(persona.getDivisionPolitica().getResidencia().getComunidad().getId());
                    personaTmp.setNombreComunidadResidencia(persona.getDivisionPolitica().getResidencia().getComunidad().getNombre());
                    personaTmp.setDireccionResidencia(persona.getDivisionPolitica().getResidencia().getPersonaDireccion());
                }
            }


            //personaTmp.telefonoResidencia;
            //personaTmp.telefonoMovil;
            personaTmp.setFechaRegistro(new Timestamp(new Date().getTime()));
            //personaTmp.setUsuarioRegistro();
            personaTmp.setPasivo(0);
        }
        return personaTmp;
    }

    public void saveOrUpdateDatosPersona(PersonaTmp obj) throws Exception{
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(obj);
    }
}