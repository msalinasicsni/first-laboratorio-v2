package ni.gob.minsa.laboratorio.service;

import ni.gob.minsa.laboratorio.domain.examen.Area;
import ni.gob.minsa.laboratorio.domain.muestra.*;
import ni.gob.minsa.laboratorio.domain.persona.PersonaTmp;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.AutoridadArea;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.AutoridadLaboratorio;
import ni.gob.minsa.laboratorio.domain.solicitante.Solicitante;
import ni.gob.minsa.laboratorio.utilities.DateUtil;
import ni.gob.minsa.laboratorio.utilities.reportes.DatosRecepcionMx;
import org.apache.commons.codec.language.Soundex;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by FIRSTICT on 12/10/2014.
 * V 1.0
 */
@Service("recepcionMxService")
@Transactional
public class RecepcionMxService {

    private Logger logger = LoggerFactory.getLogger(RecepcionMxService.class);

    @Resource(name="sessionFactory")
    private SessionFactory sessionFactory;

    public RecepcionMxService(){}

    /**
     * Agrega una Registro de Recepci�n de muestra
     *
     * @param dto Objeto a agregar
     * @throws Exception
     */
    public String addRecepcionMx(RecepcionMx dto) throws Exception {
        String idMaestro;
        try {
            if (dto != null) {
                Session session = sessionFactory.getCurrentSession();
                idMaestro = (String)session.save(dto);
            }
            else
                throw new Exception("Objeto Recepci�n Muestra es NULL");
        }catch (Exception ex){
            logger.error("Error al agregar recepci�n de muestra",ex);
            throw ex;
        }
        return idMaestro;
    }

    /**
     * Agrega una Registro de Recepci�n de muestra en laboratorio
     *
     * @param dto Objeto a agregar
     * @throws Exception
     */
    public void addRecepcionMxLab(RecepcionMxLab dto) throws Exception {
        try {
            if (dto != null) {
                Session session = sessionFactory.getCurrentSession();
                session.save(dto);
            }
            else
                throw new Exception("Objeto Recepci�n Muestra Lab es NULL");
        }catch (Exception ex){
            logger.error("Error al agregar recepci�n de muestra Lab",ex);
            throw ex;
        }
    }

    /**
     * Actualiza una Registro de Recepci�n de muestra
     *
     * @param dto Objeto a actualizar
     * @throws Exception
     */
    public void updateRecepcionMx(RecepcionMx dto) throws Exception {
        try {
            if (dto != null) {
                Session session = sessionFactory.getCurrentSession();
                session.update(dto);
            }
            else
                throw new Exception("Objeto Recepci�n Muestra es NULL");
        }catch (Exception ex){
            logger.error("Error al actualizar recepci�n de muestra",ex);
            throw ex;
        }
    }

    public RecepcionMx getRecepcionMx(String idRecepcion){
        String query = "from RecepcionMx as a where idRecepcion= :idRecepcion";

        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(query);
        q.setString("idRecepcion", idRecepcion);
        return  (RecepcionMx)q.uniqueResult();
    }

    public RecepcionMx getRecepcionMxByCodUnicoMx(String codigoUnicoMx, String codLaboratorio){
        try {
            String query = "select a from RecepcionMx as a inner join a.tomaMx as t where (t.codigoUnicoMx= :codigoUnicoMx or t.codigoLab = :codigoUnicoMx) " +
                    "and a.labRecepcion.codigo = :codLaboratorio";

            Session session = sessionFactory.getCurrentSession();
            Query q = session.createQuery(query);
            q.setString("codigoUnicoMx", codigoUnicoMx);
            q.setString("codLaboratorio",codLaboratorio);
            return  (RecepcionMx)q.uniqueResult();
        }catch (Exception ex){
            throw  ex;
        }

    }

    public DatosRecepcionMx getRecepcionMxByCodUnicoMxV2(String codigoUnicoMx, String codLaboratorio){
        try {
            String query = "select a.fechaHoraRecepcion as fechaHoraRecepcion, a.fechaRecibido as fechaRecibido, a.horaRecibido as horaRecibido, " +
                    //"coalesce((select c.valor from CalidadMx c where c.codigo = a.calidadMx.codigo ), null) as calidadMx   " +
                    //"coalesce((select c.valor from CalidadMx c where c.codigo = a.calidadMx ), null) as calidadMx   " +
                    "a.calidadMx as calidadMx   " +
                    "from RecepcionMx as a inner join a.tomaMx as t where (t.codigoUnicoMx= :codigoUnicoMx or t.codigoLab = :codigoUnicoMx) " +
                    "and a.labRecepcion.codigo = :codLaboratorio";

            Session session = sessionFactory.getCurrentSession();
            Query q = session.createQuery(query);
            q.setString("codigoUnicoMx", codigoUnicoMx);
            q.setString("codLaboratorio",codLaboratorio);
            q.setResultTransformer(Transformers.aliasToBean(DatosRecepcionMx.class));
            return  (DatosRecepcionMx)q.uniqueResult();
        }catch (Exception ex){
            throw  ex;
        }

    }

    public RecepcionMx getMaxRecepcionMxByCodUnicoMx(String codigoUnicoMx) {
        Session session = sessionFactory.getCurrentSession();
        String query = "select  re from RecepcionMx as re  inner join re.tomaMx as t " +
                "where t.codigoUnicoMx= :codigoUnicoMx and re.fechaHoraRecepcion= (SELECT MAX(remax.fechaHoraRecepcion)" +
                "FROM RecepcionMx as remax where remax.tomaMx.codigoUnicoMx = :codigoUnicoMx)";
        Query q = session.createQuery(query);
        q.setParameter("codigoUnicoMx", codigoUnicoMx);
        return (RecepcionMx)q.uniqueResult();
    }

    public List<RecepcionMx> getRecepcionesByFiltro(FiltroMx filtro){
        Session session = sessionFactory.getCurrentSession();
        Soundex varSoundex = new Soundex();
        Criteria crit = session.createCriteria(RecepcionMx.class, "recepcion");
        crit.createAlias("recepcion.tomaMx","tomaMx");
        //crit.createAlias("tomaMx.estadoMx","estado");
        //crit.createAlias("orden.idTomaMx", "tomaMx");
        crit.createAlias("tomaMx.idNotificacion", "notifi");
        //siempre se tomam las muestras que no estan anuladas
        crit.add( Restrictions.and(
                        Restrictions.eq("tomaMx.anulada", false))
        );//y las ordenes en estado seg�n filtro
        if (filtro.getCodEstado()!=null) {
            if (filtro.getIncluirTraslados()){
                crit.add(Restrictions.or(
                        //Restrictions.eq("estado.codigo", filtro.getCodEstado()).ignoreCase()).
                        Restrictions.eq("tomaMx.estadoMx", filtro.getCodEstado()).ignoreCase()).
                        add(Restrictions.or(
                        //Restrictions.eq("estado.codigo", "ESTDMX|TRAS"))));
                                Restrictions.eq("tomaMx.estadoMx", "ESTDMX|TRAS"))));
            }else {
                crit.add(Restrictions.and(
                        //Restrictions.eq("estado.codigo", filtro.getCodEstado().toLowerCase()).ignoreCase()));
                        Restrictions.eq("tomaMx.estadoMx", filtro.getCodEstado().toLowerCase()).ignoreCase()));
            }
        }

        // se filtra por nombre y apellido persona
        if (filtro.getNombreApellido()!=null) {
            //crit.createAlias("notifi.persona", "person");
            String[] partes = filtro.getNombreApellido().split(" ");
            String[] partesSnd = filtro.getNombreApellido().split(" ");
            for (int i = 0; i < partes.length; i++) {
                try {
                    partesSnd[i] = varSoundex.encode(partes[i]);
                } catch (IllegalArgumentException e) {
                    partesSnd[i] = "0000";
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < partes.length; i++) {
                Junction conditGroup = Restrictions.disjunction();
                //ABRIL2019
                conditGroup.add(Subqueries.propertyIn("notifi.persona.idDatosPersona", DetachedCriteria.forClass(PersonaTmp.class,"person")
                        .add(Restrictions.or(Restrictions.ilike("person.primerNombre", "%" + partes[i] + "%"))
                        .add(Restrictions.or(Restrictions.ilike("person.primerApellido", "%" + partes[i] + "%"))
                        .add(Restrictions.or(Restrictions.ilike("person.segundoNombre", "%" + partes[i] + "%"))
                        .add(Restrictions.or(Restrictions.ilike("person.segundoApellido", "%" + partes[i] + "%"))
                        //.add(Restrictions.or(Restrictions.ilike("person.sndNombre", "%" + partesSnd[i] + "%")))
                        ))))
                        .setProjection(Property.forName("idDatosPersona"))))
                        .add(Subqueries.propertyIn("notifi.solicitante.idSolicitante", DetachedCriteria.forClass(Solicitante.class,"solicitante")
                                .add(Restrictions.ilike("solicitante.nombre", "%" + partes[i] + "%"))
                                .setProjection(Property.forName("idSolicitante"))));

                crit.add(conditGroup);
            }
        }
        //se filtra por SILAIS
        //ABRIL2019
        if (filtro.getCodSilais()!=null){
            //crit.createAlias("notifi.codSilaisAtencion","silais");
            crit.add( Restrictions.and(
                            Restrictions.eq("notifi.codSilaisAtencion", Long.valueOf(filtro.getCodSilais())))
            );
        }
        //se filtra por unidad de salud
        //ABRIL2019
        if (filtro.getCodUnidadSalud()!=null){
            //crit.createAlias("notifi.codUnidadAtencion","unidadS");
            crit.add( Restrictions.and(
                            Restrictions.eq("notifi.codUnidadAtencion", Long.valueOf(filtro.getCodUnidadSalud())))
            );
        }
        //Se filtra por rango de fecha de toma de muestra
        if (filtro.getFechaInicioTomaMx()!=null && filtro.getFechaFinTomaMx()!=null){
            crit.add( Restrictions.and(
                            Restrictions.between("tomaMx.fechaHTomaMx", filtro.getFechaInicioTomaMx(),filtro.getFechaFinTomaMx()))
            );
        }
        //Se filtra por rango de fecha de recepci�n
        if (filtro.getFechaInicioRecep()!=null && filtro.getFechaFinRecep()!=null){
            crit.add( Restrictions.and(
                            Restrictions.between("recepcion.fechaHoraRecepcion", filtro.getFechaInicioRecep(),filtro.getFechaFinRecep()))
            );
        }
        // se filtra por tipo de muestra
        if (filtro.getCodTipoMx()!=null){
            crit.add( Restrictions.and(
                            Restrictions.eq("tomaMx.codTipoMx.idTipoMx", Integer.valueOf(filtro.getCodTipoMx())))
            );
        }
        //se filtra por area que procesa
        /*if (filtro.getIdAreaProcesa()!=null){
            crit.createAlias("orden.codExamen", "examen");
            crit.add( Restrictions.and(
                            Restrictions.eq("examen.area.idArea", Integer.valueOf(filtro.getIdAreaProcesa())))
            );
        }*/

        //Se filtra por rango de fecha de recepcion en laboratorio
        if (filtro.getFechaInicioRecepLab()!=null && filtro.getFechaFinRecepLab()!=null){
            crit.add(Subqueries.propertyIn("recepcion.idRecepcion", DetachedCriteria.forClass(RecepcionMxLab.class)
                    .createAlias("recepcionMx", "recepcionGral")
                    .add(Restrictions.between("fechaHoraRecepcion", filtro.getFechaInicioRecepLab(),filtro.getFechaFinRecepLab()))
                    .setProjection(Property.forName("recepcionGral.idRecepcion"))));
        }

        if(filtro.getIncluirMxInadecuada()!=null && filtro.getIncluirMxInadecuada()){
            //crit.add(Restrictions.or(Restrictions.isNull("recepcion.calidadMx.codigo")).add(Restrictions.or(Restrictions.ne("recepcion.calidadMx.codigo", "CALIDMX|IDC"))));
            crit.add(Restrictions.or(Restrictions.isNull("recepcion.calidadMx")).add(Restrictions.or(Restrictions.ne("recepcion.calidadMx", "CALIDMX|IDC"))));
        }
        if(filtro.getCodigoUnicoMx()!=null){
            crit.add(Restrictions.or(
                            Restrictions.eq("tomaMx.codigoUnicoMx", filtro.getCodigoUnicoMx())).add(Restrictions.or(Restrictions.eq("tomaMx.codigoLab", filtro.getCodigoUnicoMx())))
            );
        }

        //se filtra por tipo de solicitud
        if(filtro.getCodTipoSolicitud()!=null){
            if(filtro.getCodTipoSolicitud().equals("Estudio")){
                crit.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                        .add(Restrictions.eq("anulado", false))
                        .createAlias("idTomaMx", "toma")
                        .setProjection(Property.forName("toma.idTomaMx"))));
            }else{
                crit.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                        .createAlias("idTomaMx", "toma")
                        .add(Restrictions.eq("anulado", false))
                        .add(Subqueries.propertyIn("labProcesa.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                                .createAlias("laboratorio", "labautorizado")
                                .createAlias("user", "usuario")
                                .add(Restrictions.eq("pasivo", false)) //autoridad laboratorio activa
                                .add(Restrictions.and(Restrictions.eq("usuario.username", filtro.getNombreUsuario()))) //usuario
                                .setProjection(Property.forName("labautorizado.codigo"))))
                        .setProjection(Property.forName("toma.idTomaMx"))));
            }
        }

        //nombre solicitud
        if (filtro.getNombreSolicitud() != null) {
            if (filtro.getCodTipoSolicitud() != null) {
                if (filtro.getCodTipoSolicitud().equals("Estudio")) {
                    crit.add(Subqueries.propertyIn("solicitudtomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                            .add(Restrictions.eq("anulado", false))
                            .createAlias("tipoEstudio", "estudio")
                            .add(Restrictions.ilike("estudio.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                            .createAlias("idTomaMx", "toma")
                            .setProjection(Property.forName("toma.idTomaMx"))));
                } else {
                    crit.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                            .add(Restrictions.eq("anulado", false))
                            .createAlias("codDx", "dx")
                            .add(Restrictions.ilike("dx.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                            .createAlias("idTomaMx", "toma")
                            .setProjection(Property.forName("toma.idTomaMx"))));
                }
            } else {

                Junction conditGroup = Restrictions.disjunction();
                conditGroup.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                        .add(Restrictions.eq("anulado", false))
                        .createAlias("tipoEstudio", "estudio")
                        .add(Restrictions.ilike("estudio.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                        .createAlias("idTomaMx", "toma")
                        .setProjection(Property.forName("toma.idTomaMx"))))
                        .add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                                .add(Restrictions.eq("anulado", false))
                                .createAlias("codDx", "dx")
                                .add(Restrictions.ilike("dx.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                                .createAlias("idTomaMx", "toma")
                                .setProjection(Property.forName("toma.idTomaMx"))));

                crit.add(conditGroup);
            }
        }
        //se filtra que usuario tenga autorizado laboratorio al que se envio la muestra desde ALERTA
        /*if (filtro.getNombreUsuario()!=null) {
            crit.createAlias("tomaMx.envio","envioMx");
            crit.add(Subqueries.propertyIn("envioMx.laboratorioDestino.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                    .createAlias("laboratorio", "labautorizado")
                    .createAlias("user", "usuario")
                    .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                    .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                    .setProjection(Property.forName("labautorizado.codigo"))));

        }*/
            if(filtro.getCodEstado() != null){
                if (filtro.getCodEstado().equalsIgnoreCase("ESTDMX|EPLAB")){ //significa que es recepci�n en laboratorio
                    //Se filtra que el �rea a la que pertenece la solicitud este asociada al usuario autenticado
                    Junction conditGroup = Restrictions.disjunction();

                    conditGroup.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                            .add(Restrictions.eq("anulado", false))
                            .createAlias("tipoEstudio", "estudio")
                            .createAlias("estudio.area", "area")
                            .add(Subqueries.propertyIn("area.idArea", DetachedCriteria.forClass(AutoridadArea.class)
                                    .add(Restrictions.eq("pasivo", false)) //autoridad area activa
                                    .add(Restrictions.and(Restrictions.eq("user.username", filtro.getNombreUsuario()))) //usuario
                                    .setProjection(Property.forName("area.idArea"))))
                            .createAlias("idTomaMx", "toma")
                            .setProjection(Property.forName("idTomaMx.idTomaMx"))))
                            .add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                                    .add(Restrictions.eq("anulado", false))
                                    .createAlias("codDx", "dx")
                                    .createAlias("dx.area","area")
                                    .add(Subqueries.propertyIn("area.idArea", DetachedCriteria.forClass(AutoridadArea.class)
                                            .add(Restrictions.eq("pasivo", false)) //autoridad area activa
                                            .add(Restrictions.and(Restrictions.eq("user.username", filtro.getNombreUsuario()))) //usuario
                                            .setProjection(Property.forName("area.idArea"))))
                                    .createAlias("idTomaMx", "toma")
                                    .setProjection(Property.forName("toma.idTomaMx"))));

                    crit.add(conditGroup);
                    DetachedCriteria maxDateQuery = DetachedCriteria.forClass(RecepcionMx.class);
                    maxDateQuery.createAlias("tomaMx", "mx");
                    maxDateQuery.add(Restrictions.eqProperty("mx.idTomaMx", "tomaMx.idTomaMx"));
                    maxDateQuery.setProjection(Projections.max("fechaHoraRecepcion"));
                    crit.add(Property.forName("fechaHoraRecepcion").eq(maxDateQuery));

                }else if (filtro.getCodEstado().equalsIgnoreCase("ESTDMX|RCP")){
                    DetachedCriteria maxDateQuery = DetachedCriteria.forClass(RecepcionMx.class);
                    maxDateQuery.createAlias("tomaMx", "mx");
                    maxDateQuery.add(Restrictions.eqProperty("mx.idTomaMx", "tomaMx.idTomaMx"));
                    maxDateQuery.setProjection(Projections.max("fechaHoraRecepcion"));
                    crit.add(Property.forName("fechaHoraRecepcion").eq(maxDateQuery));
                    //crit.add(Restrictions.isNull("calidadMx.codigo"));//si no tiene calidad significa que no ha sido procesada. esto es por los traslados CC
                    crit.add(Restrictions.isNull("calidadMx"));//si no tiene calidad significa que no ha sido procesada. esto es por los traslados CC
                }
                //sólo la última recepción de cada muestra, cuando es para envio al area que procesa
                if (filtro.getCodEstado().equalsIgnoreCase("ESTDMX|RCP")) {
                    DetachedCriteria maxDateQuery = DetachedCriteria.forClass(RecepcionMx.class);
                    maxDateQuery.createAlias("tomaMx", "mx");
                    maxDateQuery.add(Restrictions.eqProperty("mx.idTomaMx", "tomaMx.idTomaMx"));
                    maxDateQuery.setProjection(Projections.max("fechaHoraRecepcion"));
                    crit.add(Property.forName("fechaHoraRecepcion").eq(maxDateQuery));
                }
            }


        //filtro que las rutinas pertenezcan al laboratorio del usuario que consulta
        crit.createAlias("recepcion.labRecepcion","labRecep");
        crit.add(Subqueries.propertyIn("labRecep.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                        .createAlias("laboratorio", "labautorizado")
                        .createAlias("user", "usuario")
                        .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                        .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                        .setProjection(Property.forName("labautorizado.codigo"))));

        //s�lo la �ltima recepci�n de cada muestra
/*
        DetachedCriteria maxDateQuery = DetachedCriteria.forClass(RecepcionMx.class);
        maxDateQuery.createAlias("tomaMx","mx");
        maxDateQuery.add(Restrictions.eqProperty("mx.idTomaMx","tomaMx.idTomaMx"));
        maxDateQuery.setProjection(Projections.max("fechaHoraRecepcion"));
        crit.add(Property.forName("fechaHoraRecepcion").eq(maxDateQuery));*/

        //filtro s�lo control calidad en el laboratio del usuario
        if (filtro.getControlCalidad()!=null) {
            if (filtro.getControlCalidad()){  //si hay filtro por control de calidad y es "Si", s�lo incluir rutinas
                crit.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                        .add(Restrictions.eq("controlCalidad", filtro.getControlCalidad()))
                        .createAlias("idTomaMx", "toma")
                        .add(Subqueries.propertyIn("labProcesa.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                                .createAlias("laboratorio", "labautorizado")
                                .createAlias("user", "usuario")
                                .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                                .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                                .setProjection(Property.forName("labautorizado.codigo"))))
                        .setProjection(Property.forName("toma.idTomaMx"))));
            }else { //si hay filtro por control de calidad y es "No", siempre incluir los estudios
                Junction conditGroup = Restrictions.disjunction();
                conditGroup.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                        .add(Restrictions.eq("anulado", false))
                        .add(Restrictions.eq("controlCalidad", filtro.getControlCalidad()))
                        .createAlias("idTomaMx", "toma")
                        .add(Subqueries.propertyIn("labProcesa.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                                .createAlias("laboratorio", "labautorizado")
                                .createAlias("user", "usuario")
                                .add(Restrictions.eq("pasivo", false)) //autoridad laboratorio activa
                                .add(Restrictions.and(Restrictions.eq("usuario.username", filtro.getNombreUsuario()))) //usuario
                                .setProjection(Property.forName("labautorizado.codigo"))))
                        .setProjection(Property.forName("toma.idTomaMx"))))
                        .add(Restrictions.or(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                                .add(Restrictions.eq("anulado", false))
                                .createAlias("idTomaMx", "idTomaMx")
                                .setProjection(Property.forName("idTomaMx.idTomaMx")))));
                crit.add(conditGroup);
            }
        }
        //filtro para s�lo solicitudes aprobadas
        if (filtro.getSolicitudAprobada() != null) {
            Junction conditGroup = Restrictions.disjunction();
            conditGroup.add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                    .add(Restrictions.eq("anulado", false))
                    .add(Restrictions.eq("aprobada", filtro.getSolicitudAprobada()))
                    .createAlias("idTomaMx", "toma")
                    .setProjection(Property.forName("toma.idTomaMx"))))
                    .add(Subqueries.propertyIn("tomaMx.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                            .add(Restrictions.eq("aprobada", filtro.getSolicitudAprobada()))
                            //.add(Restrictions.eq("controlCalidad",false)) ���������?????????????
                            .createAlias("idTomaMx", "toma")
                            .setProjection(Property.forName("toma.idTomaMx"))));


            crit.add(conditGroup);

        }

        return crit.list();
    }

    public RecepcionMxLab getRecepcionMxLabByIdRecepGral(String idRecepcion){
        RecepcionMxLab resultado = null;
        String query = "select a from RecepcionMxLab as a inner join a.recepcionMx as t where t.idRecepcion= :idRecepcion order by a.fechaHoraRecepcion desc";
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(query);
        q.setString("idRecepcion", idRecepcion);
        List<RecepcionMxLab> recepcionMxLabList = q.list();
        if (recepcionMxLabList!=null && recepcionMxLabList.size()>0){
            resultado = recepcionMxLabList.get(0);
        }
        return  resultado;
    }

    public RecepcionMxLab getLastRecepcionMxLabByIdTomaMx(String idTomaMx){
        RecepcionMxLab resultado = null;
        String query = "select a from RecepcionMxLab as a inner join a.recepcionMx as t inner join t.tomaMx mx where mx.idTomaMx = :idTomaMx order by a.fechaHoraRecepcion desc";
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(query);
        q.setString("idTomaMx", idTomaMx);
        List<RecepcionMxLab> recepcionMxLabList = q.list();
        if (recepcionMxLabList!=null && recepcionMxLabList.size()>0){
            resultado = recepcionMxLabList.get(0);
        }
        return  resultado;
    }

    public Boolean validarMuestraRecepcionadaAreaLab(String idTomaMx, String codigoLab, Integer idArea){
        RecepcionMxLab resultado = null;
        String query = "select ar from RecepcionMxLab as a inner join a.recepcionMx as t inner join t.tomaMx mx inner join a.area as ar where mx.idTomaMx = :idTomaMx" +
                " and t.labRecepcion.codigo = :codigoLab and ar.idArea = :idArea order by a.fechaHoraRecepcion desc";
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(query);
        q.setString("idTomaMx", idTomaMx);
        q.setString("codigoLab", codigoLab);
        q.setParameter("idArea",idArea);
        return q.list().size()>0;
    }


    /**
     * M�todo que genera
     * @param codigoLaboratorio codigo del laboratorio que recepciona la muestra
     * @param intento cantidad numeros a sumar al siguiente codigo a obtener. Intentos por que si falla la llamada, intenta obtener el siguiente mediante llamada recursiva
     * @return String
     */
    public String obtenerCodigoLab(String codigoLaboratorio, int intento){
        String codigoLab=null;
        String anioActual = DateUtil.DateToString(new Date(), "yyyy");
        String query = "select concat(to_char((count(a.idRecepcion)+"+String.valueOf(intento)+")),concat('-',to_char(current_date,'YY'))) " +
                //"from RecepcionMx as a where a.labRecepcion.codigo = :codLab and a.tipoRecepcionMx.codigo = 'TPRECPMX|VRT' and to_char(a.fechaHoraRecepcion,'YYYY') =:anio "+
                "from RecepcionMx as a where a.labRecepcion.codigo = :codLab and a.tipoRecepcionMx = 'TPRECPMX|VRT' and to_char(a.fechaHoraRecepcion,'YYYY') =:anio "+
                "and a.tomaMx.codigoLab like '"+codigoLaboratorio+"-%'";
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(query);
        q.setParameter("codLab", codigoLaboratorio);
        q.setParameter("anio", anioActual);
        Object oNumero  = q.uniqueResult();
        if (oNumero!=null){
            codigoLab = oNumero.toString();
            codigoLab = codigoLaboratorio.concat("-").concat(codigoLab);
            if (existeCodigoLab(codigoLab))
                codigoLab = obtenerCodigoLab(codigoLaboratorio,intento+1);
        }

        return codigoLab;
    }

    private boolean existeCodigoLab(String codigoLab){
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("select codigoUnicoMx from DaTomaMx where codigoLab = :codigoLab");
        q.setParameter("codigoLab", codigoLab);
        Object oExiste  = q.uniqueResult();
        return oExiste!=null;
    }
}
