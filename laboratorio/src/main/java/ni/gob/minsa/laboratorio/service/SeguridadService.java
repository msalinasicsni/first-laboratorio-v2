package ni.gob.minsa.laboratorio.service;

import ni.gob.minsa.laboratorio.domain.examen.Area;
import ni.gob.minsa.laboratorio.domain.muestra.Laboratorio;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import ni.gob.minsa.laboratorio.utilities.ConstantsSecurity;
import ni.gob.minsa.laboratorio.utilities.UtilityProperties;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Miguel Salinas on 10/28/2014.
 * v 1.0
 */
@Service("seguridadService")
@Transactional
public class SeguridadService {
    @Resource(name = "sessionFactory")
    private SessionFactory sessionFactory;

    UtilityProperties utilityProperties = new UtilityProperties();

    /**
     * Retorna valor de constante que indica que se habilita o no la seguridad en el sistema
     * @return True: seguridad habilitada, False: Seguridad deshabilitada
     */
    public boolean seguridadHabilitada(){
        return ConstantsSecurity.ENABLE_SECURITY;
    }

    /**
     * M�todo que ejecuta el servicio del portal para obtener la url de inicio del portal del MINSA
     * @return String con url de incio del MINSA
     */
    public String obtenerUrlPortal() {
       return "login";
    }

    /**
     * M�todo que valida si es correcto el login en el sistema
     * @param request petici�n actual
     * @return String vacio "" si login es correcto, en caso contrario url de login del portal del minsa
     */
    public String validarLogin(HttpServletRequest request){
        return "";
    }

    /**
     * M�todo que valida si el usuario logueado tiene acceso a la vista solicitada
     * @param request reques actual
     * @param codSistema c�digo de sistema actual
     * @param hayParametro TRUE indica que en el contextPath el �ltimo elemento es un p�rametro de spring, FALSE no hay par�metro
     * @return String vacio "" si tiene autorizaci�n, si no tiene retorna url de acceso denegado
     */
    public String validarAutorizacionUsuario(HttpServletRequest request, String codSistema, boolean hayParametro){
        return "";
    }

    /**
     * M�todo que determina si la sesi�n que contiene la informaci�n del usuario aunticada existe, es decir hay usuario autenticado
     * @param session sesi�n actual
     * @return TRUE si existe sessi�n, False en caso contrario
     */
    private boolean esUsuarioAutenticado(HttpSession session) {
        return session.getAttribute("infoSesionActual")!=null;
    }

    /**
     * M�todo que consume el portal de seguridad para determinar si un usario determinado tiene autorizaci�n se ingresar a una vista determinada, en el sistema actual
     * @param pUsuarioId id del usuario autenticado
     * @param pSistema c�digo del sistema actual, ALERTA
     * @param pViewId url de la vista solicitada
     * @return True si el usuario tiene permiso, False en caso contrario
     */
    private boolean esUsuarioAutorizado(long pUsuarioId, String pSistema, String pViewId) {
        return true;
    }

    /**
     * M�todo que determina si un usuario determinado esta configurado como usario de nivel central en el sistema
     * @param pUsuarioId id del usuario autenticado
     * @param pSistema c�digo del sistema actual, ALERTA
     * @return TRUE: si es de nivel central  o la seguridad esta deshabilitada, FALSE: no es nivel central o sucedi� un error
     */
    public boolean esUsuarioNivelCentral(long pUsuarioId, String pSistema) {
        return true;
    }

    /**
     * M�todo que consulta la sessi�n con informaci�n del usuario y obtiene el id el usuario auntenticado
     * @param request petici�n actual
     * @return long con Id del usuario almacenado en sesi�n o O si no se encontr�
     */
    public long obtenerIdUsuario(HttpServletRequest request){
        return 1L;
    }

    /**
     *  M�todo que consulta la sessi�n con informaci�n del usuario y obtiene el nombre el usuario auntenticado
     * @param request petici�n actual
     * @return String con el nombre del usuario auntenticado, "" si no se encontr�
     */
    public String obtenerNombreUsuario(HttpServletRequest request){
        return "usuariosis1";
    }

    /**
     * M�todo que obtiene el �rbol del menu del sistema seg�n la configuraci�n en la seguridad, luego se arma el men� en un string
     * @param request petici�n actual
     * @return String que contiene el html de todas las opciones de menu
     */
    public String obtenerMenu(HttpServletRequest request){
        return "";
    }

    /**
     * M�todo que se ejecuta cuando se selecciona la opci�n "Salir" del sistema
     * @param session sesi�n actual para limpiarla
     */
    public void logOut(HttpSession session) {
        session.removeAttribute("infoSesionActual");
        session.removeAttribute("menuSistema");
        session.invalidate();
    }

    /**
     * M�todo que obtiene las entidades administrativas (SILAIS) a las que tiene autorizaci�n el usuario en el sistema
     * @param pUsuarioId id del usuario autenticado
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @return List<EntidadesAdtvas>
     */
    /*public List<EntidadesAdtvas> obtenerEntidadesPorUsuario(Integer pUsuarioId, String pCodigoSis){
        List<EntidadesAdtvas> entidadesAdtvasList = new ArrayList<EntidadesAdtvas>();
        try {
            String query = "select ent from EntidadesAdtvas ent, UsuarioEntidad usuent, Usuarios usu, Sistema sis " +
                    "where ent.id = usuent.entidadAdtva.entidadAdtvaId and usu.usuarioId = usuent.usuario.usuarioId and usuent.sistema.id = sis.id " +
                    "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and ent.pasivo = :pasivo order by ent.nombre";
            Query qrUsuarioEntidad = sessionFactory.getCurrentSession().createQuery(query);
            qrUsuarioEntidad.setParameter("pUsuarioId", pUsuarioId);
            qrUsuarioEntidad.setParameter("pCodigoSis", pCodigoSis);
            qrUsuarioEntidad.setParameter("pasivo", '0');
            entidadesAdtvasList = qrUsuarioEntidad.list();
        }catch (Exception e){
            e.printStackTrace();
        }
        return entidadesAdtvasList;
    }*/

    /**
     * M�todo que valida si el usuario logueado tiene autorizaci�n sobre una entidad administrativa determinada
     * @param pUsuarioId id del usuario autenticado
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @param pCodEntidad c�digo de la entidad a validar
     * @return TRUE: si tiena autorizaci�n o la seguridad esta deshabilitada, FALSE: no tiene autorizaci�n
     */
    /*public boolean esUsuarioAutorizadoEntidad(Integer pUsuarioId, String pCodigoSis, long pCodEntidad){
        if (seguridadHabilitada()) {
            List<EntidadesAdtvas> entidadesAdtvasList = new ArrayList<EntidadesAdtvas>();
            try {
                String query = "select ent from EntidadesAdtvas ent, UsuarioEntidad usuent, Usuarios usu, Sistema sis " +
                        "where ent.id = usuent.entidadAdtva.entidadAdtvaId and usu.usuarioId = usuent.usuario.usuarioId and usuent.sistema.id = sis.id " +
                        "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and ent.codigo = :pCodEntidad and ent.pasivo = :pasivo order by ent.nombre";
                Query qrUsuarioEntidad = sessionFactory.getCurrentSession().createQuery(query);
                qrUsuarioEntidad.setParameter("pUsuarioId", pUsuarioId);
                qrUsuarioEntidad.setParameter("pCodigoSis", pCodigoSis);
                qrUsuarioEntidad.setParameter("pCodEntidad", pCodEntidad);
                qrUsuarioEntidad.setParameter("pasivo", '0');
                entidadesAdtvasList = qrUsuarioEntidad.list();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return entidadesAdtvasList.size() > 0;
        }else return true;
    }*/

    /**
     * M�todo que obtiene todas las unidades de salud a las que tiene autorizaci�n el usuario en el sistema
     * @param pUsuarioId id del usuario autenticado
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @param tipoUnidades tipos de unidades a carga. Eje: Primarias , Primarias+Hospitales
     * @return List<Unidades>
     */
    /*public List<Unidades> obtenerUnidadesPorUsuario(Integer pUsuarioId, String pCodigoSis, String tipoUnidades){
        List<Unidades> unidadesList = new ArrayList<Unidades>();
        try {
            String query = "select uni from Unidades uni, UsuarioUnidad usuni, Usuarios usu, Sistema sis " +
                    "where uni.unidadId = usuni.unidad.unidadId and usu.usuarioId = usuni.usuario.usuarioId and usuni.sistema.id = sis.id " +
                    "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and uni.pasivo = :pasivo and uni.tipoUnidad in ("+tipoUnidades+") " +
                    "order by uni.nombre";
            Query qrUsuarioUnidad = sessionFactory.getCurrentSession().createQuery(query);
            qrUsuarioUnidad.setParameter("pUsuarioId",pUsuarioId);
            qrUsuarioUnidad.setParameter("pCodigoSis",pCodigoSis);
            qrUsuarioUnidad.setParameter("pasivo", '0');
            unidadesList = qrUsuarioUnidad.list();
        }catch (Exception e){
            e.printStackTrace();
        }
        return unidadesList;
    }*/

    /**
     * M�todo que valida si el usuario logueado tiene autorizaci�n sobre una unidad de salud determinada
     * @param pUsuarioId id del usuario autenticado
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @param pCodUnidad c�digo de la unidad a validar
     * @return TRUE: si tiena autorizaci�n o la seguridad esta deshabilitada, FALSE: no tiene autorizaci�n
     */
    /*public boolean esUsuarioAutorizadoUnidad(Integer pUsuarioId, String pCodigoSis, long pCodUnidad){
        if (seguridadHabilitada()) {
            List<Unidades> unidadesList = new ArrayList<Unidades>();
            try {
                String query = "select uni from Unidades uni, UsuarioUnidad usuni, Usuarios usu, Sistema sis " +
                        "where uni.unidadId = usuni.unidad.unidadId and usu.usuarioId = usuni.usuario.usuarioId and usuni.sistema.id = sis.id " +
                        "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and uni.codigo = :pCodUnidad and uni.pasivo = :pasivo " +
                        "order by uni.nombre";
                Query qrUsuarioUnidad = sessionFactory.getCurrentSession().createQuery(query);
                qrUsuarioUnidad.setParameter("pUsuarioId", pUsuarioId);
                qrUsuarioUnidad.setParameter("pCodigoSis", pCodigoSis);
                qrUsuarioUnidad.setParameter("pCodUnidad", pCodUnidad);
                qrUsuarioUnidad.setParameter("pasivo", '0');
                unidadesList = qrUsuarioUnidad.list();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return unidadesList.size() > 0;
        }else return true;
    }*/

    /**
     * M�todo que obtiene todas las unidades de salud a las que tiene autorizaci�n el usuario en el sistema seg�n el SILAIS y el tipo de Unidad
     * @param pUsuarioId id del usuario autenticado
     * @param pCodSilais C�digo del silais a filtrar
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @param tipoUnidades tipos de unidades a carga. Eje: Primarias , Primarias+Hospitales
     * @return List<Unidades>
     */
    /*public List<Unidades> obtenerUnidadesPorUsuarioEntidad(Integer pUsuarioId, long pCodSilais, String pCodigoSis, String tipoUnidades){
        List<Unidades> unidadesList = new ArrayList<Unidades>();
        try {
            String query = "select uni from Unidades uni, UsuarioUnidad usuni, Usuarios usu, Sistema sis " +
                    "where uni.unidadId = usuni.unidad.unidadId and usu.usuarioId = usuni.usuario.usuarioId and usuni.sistema.id = sis.id " +
                    "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and uni.pasivo = :pasivo and uni.tipoUnidad in ("+tipoUnidades+")" +
                    "and uni.entidadAdtva.codigo = :pCodSilais order by uni.nombre";
            Query qrUsuarioUnidad = sessionFactory.getCurrentSession().createQuery(query);
            qrUsuarioUnidad.setParameter("pUsuarioId",pUsuarioId);
            qrUsuarioUnidad.setParameter("pCodigoSis",pCodigoSis);
            qrUsuarioUnidad.setParameter("pasivo", '0');
            qrUsuarioUnidad.setParameter("pCodSilais", pCodSilais);
            unidadesList = qrUsuarioUnidad.list();
        }catch (Exception e){
            e.printStackTrace();
        }
        return unidadesList;
    }*/

    /**
     * M�todo que obtiene todas las unidades de salud a las que tiene autorizaci�n el usuario en el sistema seg�n el SILAIS
     * @param pUsuarioId id del usuario autenticado
     * @param pCodSilais C�digo del silais a filtrar
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @return List<Unidades>
     */
    /*public List<Unidades> obtenerUnidadesPorUsuarioEntidad(Integer pUsuarioId, long pCodSilais, String pCodigoSis){
        List<Unidades> unidadesList = new ArrayList<Unidades>();
        try {
            String query = "select uni from Unidades uni, UsuarioUnidad usuni, Usuarios usu, Sistema sis " +
                    "where uni.unidadId = usuni.unidad.unidadId and usu.usuarioId = usuni.usuario.usuarioId and usuni.sistema.id = sis.id " +
                    "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and uni.pasivo = :pasivo " +
                    "and uni.entidadAdtva.codigo = :pCodSilais order by uni.nombre";
            Query qrUsuarioUnidad = sessionFactory.getCurrentSession().createQuery(query);
            qrUsuarioUnidad.setParameter("pUsuarioId",pUsuarioId);
            qrUsuarioUnidad.setParameter("pCodigoSis",pCodigoSis);
            qrUsuarioUnidad.setParameter("pasivo", '0');
            qrUsuarioUnidad.setParameter("pCodSilais", pCodSilais);
            unidadesList = qrUsuarioUnidad.list();
        }catch (Exception e){
            e.printStackTrace();
        }
        return unidadesList;
    }*/

    /**
     * M�todo que obtiene todas las unidades de salud a las que tiene autorizaci�n el usuario en el sistema seg�n el SILAIS y municipio
     * @param pUsuarioId id del usuario autenticado
     * @param pCodSilais C�digo del silais a filtrar
     * @param pCodMunicipio C�digo del municio a filtrar
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @param tipoUnidades tipos de unidades a carga. Eje: Primarias , Primarias+Hospitales
     * @return List<Unidades>
     */
    /*public List<Unidades> obtenerUnidadesPorUsuarioEntidadMunicipio(Integer pUsuarioId, long pCodSilais, String pCodMunicipio, String pCodigoSis, String tipoUnidades){
        List<Unidades> unidadesList = new ArrayList<Unidades>();
        try {
            String query = "select uni from Unidades uni, UsuarioUnidad usuni, Usuarios usu, Sistema sis " +
                    "where uni.unidadId = usuni.unidad.unidadId and usu.usuarioId = usuni.usuario.usuarioId and usuni.sistema.id = sis.id " +
                    "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and uni.pasivo = :pasivo and uni.tipoUnidad in ("+tipoUnidades+")" +
                    "and uni.entidadAdtva.codigo = :pCodSilais and uni.municipio.codigoNacional = :pCodMunicipio order by uni.nombre";
            Query qrUsuarioUnidad = sessionFactory.getCurrentSession().createQuery(query);
            qrUsuarioUnidad.setParameter("pUsuarioId",pUsuarioId);
            qrUsuarioUnidad.setParameter("pCodigoSis",pCodigoSis);
            qrUsuarioUnidad.setParameter("pasivo", '0');
            qrUsuarioUnidad.setParameter("pCodSilais", pCodSilais);
            qrUsuarioUnidad.setParameter("pCodMunicipio",pCodMunicipio);
            unidadesList = qrUsuarioUnidad.list();
        }catch (Exception e){
            e.printStackTrace();
        }
        return unidadesList;
    }*/

    /**
     * M�todo que obtiene los municipios autorizados en el sistema para el usuario seg�n el SILAIS, las unidades autorizadas y el tipo de Unidad
     * @param pUsuarioId id del usuario autenticado
     * @param pCodSilais C�digo del silais a filtrar
     * @param pCodigoSis c�digo del sistema, ALERTA
     * @return List<Divisionpolitica>
     */
    /*public List<Divisionpolitica> obtenerMunicipiosPorUsuarioEntidad(Integer pUsuarioId, long pCodSilais, String pCodigoSis){
        String query = "select distinct muni from Divisionpolitica as muni, Unidades uni, UsuarioUnidad usuni, Usuarios usu, Sistema sis " +
                "where uni.unidadId = usuni.unidad.unidadId and usu.usuarioId = usuni.usuario.usuarioId and usuni.sistema.id = sis.id " +
                "and sis.codigo = :pCodigoSis and usu.usuarioId = :pUsuarioId and uni.pasivo = :pasivo " +
                "and muni.pasivo = :pasivo and  uni.entidadAdtva.codigo = :pCodSilais and uni.municipio.codigoNacional = muni.codigoNacional " +
                "order by muni.nombre";
         Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(query);
        q.setParameter("pCodSilais", pCodSilais);
        q.setParameter("pCodigoSis",pCodigoSis);
        q.setParameter("pUsuarioId",pUsuarioId);
        q.setParameter("pasivo",'0');
        return q.list();
    }*/

    public void obtenerRolesUsuarioAutenticado() {
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>)
                SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        boolean hasRole = false;
        for (GrantedAuthority authority : authorities) {
            hasRole = authority.getAuthority().equals("ROLE");
            if (hasRole) {
                break;
            }
        }
    }

    public String obtenerNombreUsuario(){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }

    public boolean esDirector(String userName){
        List<User> userList = new ArrayList<User>();
        try {
            String query = "select usu from AutoridadDireccion as audir inner join audir.user as usu " +
                    "where audir.pasivo = false and usu.username = :username";
            Query q = sessionFactory.getCurrentSession().createQuery(query);
            q.setParameter("username", userName);
            userList = q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList.size() > 0;
    }

    public boolean esJefeDepartamento(String userName){
        List<User> userList = new ArrayList<User>();
        try {
            String query = "select usu from AutoridadDepartamento as audep inner join audep.user as usu " +
                    "where audep.pasivo = false and usu.username = :username";
            Query q = sessionFactory.getCurrentSession().createQuery(query);
            q.setParameter("username", userName);
            userList = q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList.size() > 0;
    }

    public Laboratorio getLaboratorioUsuario(String userName){
        Laboratorio laboratorio = new Laboratorio();
        try {
            String query = "select aulab.laboratorio from AutoridadLaboratorio as aulab inner join aulab.user as usu " +
                    "where aulab.pasivo = false and usu.username = :username";
            Query q = sessionFactory.getCurrentSession().createQuery(query);
            q.setParameter("username", userName);
            laboratorio = (Laboratorio)q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return laboratorio;
    }

    public User getUsuario(String userName){
        String query = "from User as usu " +
                "where usu.username = :username";
        Query q = sessionFactory.getCurrentSession().createQuery(query);
        q.setParameter("username", userName);
        return  (User)q.uniqueResult();
    }

    public boolean usuarioAutorizadoArea(String userName, int idArea){
        List<Area> areaList = new ArrayList<Area>();
        try {
            String query = "select usu from AutoridadArea as auarea inner join auarea.user as usu inner join auarea.area as are " +
                    "where auarea.pasivo = false and usu.username = :username and are.idArea = :idArea";
            Query q = sessionFactory.getCurrentSession().createQuery(query);
            q.setParameter("username", userName);
            q.setParameter("idArea",idArea);
            areaList = q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return areaList.size() > 0;
    }

    public boolean usuarioAutorizadoLaboratorio(String userName, String codigoLab){
        List<Laboratorio> laboratorioList = new ArrayList<Laboratorio>();
        try {
            String query = "select usu from AutoridadLaboratorio as aulab inner join aulab.user as usu inner join aulab.laboratorio as lab " +
                    "where aulab.pasivo = false and usu.username = :username and lab.codigo = :codigoLab";
            Query q = sessionFactory.getCurrentSession().createQuery(query);
            q.setParameter("username", userName);
            q.setParameter("codigoLab",codigoLab);
            laboratorioList = q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return laboratorioList.size() > 0;
    }

    public boolean esUsuarioNivelCentral(String username){
        String query = "from User as usu " +
                "where usu.username = :username";
        Query q = sessionFactory.getCurrentSession().createQuery(query);
        q.setParameter("username", username);
        User usuario = (User)q.uniqueResult();
        if (usuario!=null) return usuario.getNivelCentral();
        else return  false;
    }

}
