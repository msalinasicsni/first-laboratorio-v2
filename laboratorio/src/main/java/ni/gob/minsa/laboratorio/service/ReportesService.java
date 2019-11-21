package ni.gob.minsa.laboratorio.service;

import ni.gob.minsa.laboratorio.domain.concepto.Catalogo_Lista;
import ni.gob.minsa.laboratorio.domain.muestra.DaSolicitudDx;
import ni.gob.minsa.laboratorio.domain.muestra.DaSolicitudEstudio;
import ni.gob.minsa.laboratorio.domain.muestra.FiltroMx;
import ni.gob.minsa.laboratorio.domain.muestra.RecepcionMx;
import ni.gob.minsa.laboratorio.domain.muestra.traslado.HistoricoEnvioMx;
import ni.gob.minsa.laboratorio.domain.muestra.traslado.TrasladoMx;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultadoFinal;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.AutoridadLaboratorio;
import ni.gob.minsa.laboratorio.utilities.FiltrosReporte;
import ni.gob.minsa.laboratorio.utilities.enumeration.HealthUnitType;
import ni.gob.minsa.laboratorio.utilities.reportes.ConsolidadoRecepcion;
import ni.gob.minsa.laboratorio.utilities.reportes.ResultadoVigilancia;
import ni.gob.minsa.laboratorio.utilities.reportes.ConsolidadoRecepcion;
import ni.gob.minsa.laboratorio.utilities.reportes.ResultadoVigilancia;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by souyen-ics.
 */
@Service("reportesService")
@Transactional
public class ReportesService {

    private Logger logger = LoggerFactory.getLogger(ReportesService.class);

    @Resource(name="sessionFactory")
    private SessionFactory sessionFactory;

    @Resource(name="resultadoFinalService")
    private ResultadoFinalService resultadoFinalService;

    @Resource(name = "respuestasExamenService")
    private RespuestasExamenService respuestasExamenService;

    private static final String sqlRutina = " and dx.codDx.idDiagnostico = :idDx ";
    private static final String sqlEstudio = " and dx.tipoEstudio.idEstudio = :idDx ";
    private static final String sqlFechasProcRut = " and dx.idSolicitudDx in (select r.solicitudDx.idSolicitudDx  from DetalleResultadoFinal r where r.pasivo = false and r.fechahRegistro between :fechaInicio and :fechaFin) "; //" and mx.fechaHTomaMx between :fechaInicio and :fechaFin ";
    private static final String sqlFechasAproRut =  " and dx.fechaAprobacion between :fechaInicio and :fechaFin ";
    private static final String sqlLab = " and dx.labProcesa.codigo = :codigoLab ";
    private static final String sqlFIS = " and noti.fechaInicioSintomas between :fechaInicio and :fechaFin ";

    @SuppressWarnings("unchecked")
    public List<RecepcionMx> getReceivedSamplesByFiltro(FiltroMx filtro) throws UnsupportedEncodingException {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = session.createCriteria(RecepcionMx.class, "recepcion");
        crit.createAlias("recepcion.tomaMx", "toma");
        crit.createAlias("toma.idNotificacion", "notifi");


        if(filtro.getNombreSolicitud()!= null){
            filtro.setNombreSolicitud(URLDecoder.decode(filtro.getNombreSolicitud(), "utf-8"));
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
        //Se filtra por rango de fecha de recepcion
        if (filtro.getFechaInicioRecep()!=null && filtro.getFechaFinRecep()!=null){
            crit.add( Restrictions.and(
                            Restrictions.between("fechaHoraRecepcion", filtro.getFechaInicioRecep(),filtro.getFechaFinRecep()))
            );
        }
        // se filtra por tipo de muestra
        if (filtro.getCodTipoMx()!=null){
            crit.createAlias("toma.codTipoMx", "tipoMx");
            crit.add( Restrictions.and(

                            Restrictions.eq("tipoMx.idTipoMx", Integer.valueOf(filtro.getCodTipoMx())))
            );
        }

        //se filtra por tipo de solicitud
        if(filtro.getCodTipoSolicitud()!=null){
            if(filtro.getCodTipoSolicitud().equals("Estudio")){
                crit.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                        .add(Restrictions.eq("anulado",false))
                        .createAlias("idTomaMx", "idTomaMx")
                        .setProjection(Property.forName("idTomaMx.idTomaMx"))));
            }else{
                crit.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                        .add(Restrictions.eq("anulado",false))
                        .createAlias("idTomaMx", "idTomaMx")
                        .setProjection(Property.forName("idTomaMx.idTomaMx"))));
            }

        }

        //nombre solicitud
        if (filtro.getNombreSolicitud() != null) {
            if (filtro.getCodTipoSolicitud() != null) {
                if (filtro.getCodTipoSolicitud().equals("Estudio")) {
                    crit.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                            .add(Restrictions.eq("anulado",false))
                            .createAlias("tipoEstudio", "estudio")
                            .add(Restrictions.ilike("estudio.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                            .setProjection(Property.forName("idTomaMx.idTomaMx"))));
                } else {
                    crit.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                            .add(Restrictions.eq("anulado",false))
                            .createAlias("codDx", "dx")
                            .add(Restrictions.ilike("dx.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                            .setProjection(Property.forName("idTomaMx.idTomaMx"))));
                }
            } else {

                Junction conditGroup = Restrictions.disjunction();
                conditGroup.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudEstudio.class)
                        .add(Restrictions.eq("anulado",false))
                        .createAlias("tipoEstudio", "estudio")
                        .add(Restrictions.ilike("estudio.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                        .setProjection(Property.forName("idTomaMx.idTomaMx"))))
                        .add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                                .add(Restrictions.eq("anulado",false))
                                .createAlias("codDx", "dx")
                                .add(Restrictions.ilike("dx.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                                .setProjection(Property.forName("idTomaMx.idTomaMx"))));

                crit.add(conditGroup);
            }
        }
        //se filtra que usuario tenga autorizado laboratorio al que se envio la muestra desde ALERTA
        if (filtro.getNombreUsuario()!=null) {
            /*crit.createAlias("tomaMx.envio","envioMx");
            crit.add(Subqueries.propertyIn("envioMx.laboratorioDestino.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                    .createAlias("laboratorio", "labautorizado")
                    .createAlias("user", "usuario")
                    .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                    .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                    .setProjection(Property.forName("labautorizado.codigo"))));*/
            //filtro que las rutinas pertenezcan al laboratorio del usuario que consulta
            crit.createAlias("recepcion.labRecepcion","labRecep");
            crit.add(Subqueries.propertyIn("labRecep.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                    .createAlias("laboratorio", "labautorizado")
                    .createAlias("user", "usuario")
                    .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                    .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                    .setProjection(Property.forName("labautorizado.codigo"))));
        }

        crit.addOrder(Order.asc("recepcion.fechaHoraRecepcion"));

        return crit.list();
    }


    @SuppressWarnings("unchecked")
    public List<DaSolicitudDx> getPositiveRoutineRequestByFilter(FiltroMx filtro) throws UnsupportedEncodingException {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = session.createCriteria(DaSolicitudDx.class, "rutina");
        crit.createAlias("rutina.idTomaMx", "toma");
        crit.createAlias("toma.idNotificacion", "notif");
        crit.createAlias("rutina.codDx", "dx");

        if(filtro.getNombreSolicitud()!= null){
            filtro.setNombreSolicitud(URLDecoder.decode(filtro.getNombreSolicitud(), "utf-8"));
        }
        //se filtra por SILAIS
        //ABRIL2019
        if (filtro.getCodSilais()!=null){
            //crit.createAlias("notif.codSilaisAtencion","silais");
            crit.add( Restrictions.and(
                            Restrictions.eq("notif.codSilaisAtencion", Long.valueOf(filtro.getCodSilais())))
            );
        }
        //se filtra por unidad de salud
        //ABRIL2019
        if (filtro.getCodUnidadSalud()!=null){
            //crit.createAlias("notif.codUnidadAtencion","unidadS");
            crit.add( Restrictions.and(
                            Restrictions.eq("notif.codUnidadAtencion", Long.valueOf(filtro.getCodUnidadSalud())))
            );
        }
        //Se filtra por rango de fecha de aprobacion
        if (filtro.getFechaInicioAprob()!=null && filtro.getFechaFinAprob()!=null){
            crit.add( Restrictions.and(
                            Restrictions.between("fechaAprobacion", filtro.getFechaInicioAprob(),filtro.getFechaFinAprob()))
            );
        }

        //nombre solicitud
        if (filtro.getNombreSolicitud() != null) {
            crit.add(Restrictions.ilike("dx.nombre", "%" + filtro.getNombreSolicitud() + "%"));
        }

        //filtro de resultados finales aprobados
        crit.add(Restrictions.and(
                        Restrictions.eq("rutina.aprobada", true))
        );
        //filtro de solicitud que no sea control de calidad
        crit.add(Restrictions.and(
                        Restrictions.eq("rutina.controlCalidad", false))
        );

        //filtro de resultado final positivo
        crit.add(Subqueries.propertyIn("rutina.idSolicitudDx", DetachedCriteria.forClass(DetalleResultadoFinal.class)
                .setProjection(Property.forName("solicitudDx.idSolicitudDx"))));

        crit.addOrder(Order.asc("fechaAprobacion"));

        if (!filtro.isNivelCentral()) {//si no es nivel central, filtrar solo el laboratorio al que pertenece el usuario o seleccionado en pantalla
            crit.createAlias("rutina.labProcesa", "labProcesa");
            if (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")) {
                crit.add(Restrictions.and(
                        Restrictions.eq("labProcesa.codigo", filtro.getCodLaboratio())));
            }else {
                crit.add(Subqueries.propertyIn("labProcesa.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                        .createAlias("laboratorio", "labautorizado")
                        .createAlias("user", "usuario")
                        .add(Restrictions.eq("pasivo", false)) //autoridad laboratorio activa
                        .add(Restrictions.and(Restrictions.eq("usuario.username", filtro.getNombreUsuario()))) //usuario
                        .setProjection(Property.forName("labautorizado.codigo"))));
            }
        }else {
            //se filtra por laboratorio que procesa
            if (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")) {
                // and dx.labProcesa.codigo = :codigoLab
                crit.createAlias("rutina.labProcesa", "labProcesa");
                crit.add(Restrictions.and(
                        Restrictions.eq("labProcesa.codigo", filtro.getCodLaboratio())));
            }
        }

        //filtro x area
        if(filtro.getArea() != null){
            crit.createAlias("dx.area","area");
            crit.add( Restrictions.and(
                    Restrictions.eq("area.nombre",(filtro.getArea()))));
        }
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    public List<DaSolicitudEstudio> getPositiveStudyRequestByFilter(FiltroMx filtro) throws UnsupportedEncodingException {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = session.createCriteria(DaSolicitudEstudio.class, "estudio");
        crit.createAlias("estudio.idTomaMx", "toma");
        crit.createAlias("toma.idNotificacion", "notif");
        crit.createAlias("estudio.tipoEstudio", "tEstudio");
        crit.add(Restrictions.eq("estudio.anulado", false));
        if(filtro.getNombreSolicitud()!= null){
            filtro.setNombreSolicitud(URLDecoder.decode(filtro.getNombreSolicitud(), "utf-8"));
        }

        //se filtra por SILAIS
        //ABRIL2019
        if (filtro.getCodSilais()!=null){
            //crit.createAlias("notif.codSilaisAtencion","silais");
            crit.add( Restrictions.and(
                            Restrictions.eq("notif.codSilaisAtencion", Long.valueOf(filtro.getCodSilais())))
            );
        }
        //se filtra por unidad de salud
        //ABRIL2019
        if (filtro.getCodUnidadSalud()!=null){
            //crit.createAlias("notif.codUnidadAtencion","unidadS");
            crit.add( Restrictions.and(
                            Restrictions.eq("notif.codUnidadAtencion", Long.valueOf(filtro.getCodUnidadSalud())))
            );
        }
        //Se filtra por rango de fecha de aprobacion
        if (filtro.getFechaInicioAprob()!=null && filtro.getFechaFinAprob()!=null){
            crit.add( Restrictions.and(
                            Restrictions.between("fechaAprobacion", filtro.getFechaInicioAprob(),filtro.getFechaFinAprob()))
            );
        }

        //nombre solicitud
        if (filtro.getNombreSolicitud() != null) {
            //nombre solicitud
            if (filtro.getNombreSolicitud() != null) {
                crit.add(Restrictions.ilike("tEstudio.nombre", "%" + filtro.getNombreSolicitud() + "%"));
            }
        }
        if (!filtro.isNivelCentral()) {//si no es nivel central, filtrar solo el laboratorio al que pertenece el usuario
            //se filtra que usuario tenga autorizado laboratorio al que se envio la muestra desde ALERTA
            if (filtro.getNombreUsuario() != null) {
                crit.createAlias("toma.envio", "envioMx");
            /*crit.add(Subqueries.propertyIn("envioMx.laboratorioDestino.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                    .createAlias("laboratorio", "labautorizado")
                    .createAlias("user", "usuario")
                    .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                    .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                    .setProjection(Property.forName("labautorizado.codigo"))));*/

                //se filtra que laboratorio destino o si es traslado haya historico de envio al laboratario este autorizado al usuario
                Junction conditGroup = Restrictions.disjunction();
                conditGroup.add(Subqueries.propertyIn("envioMx.laboratorioDestino.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                        .createAlias("laboratorio", "labautorizado")
                        .createAlias("user", "usuario")
                        .add(Restrictions.eq("pasivo", false)) //autoridad laboratorio activa
                        .add(Restrictions.and(Restrictions.eq("usuario.username", filtro.getNombreUsuario()))) //usuario
                        .setProjection(Property.forName("labautorizado.codigo"))))
                        .add(Restrictions.or(Subqueries.propertyIn("idTomaMx", DetachedCriteria.forClass(HistoricoEnvioMx.class)
                                .createAlias("tomaMx", "toma")
                                .createAlias("envioMx", "envio")
                                .createAlias("envio.laboratorioDestino", "destino")
                                .add(Subqueries.propertyIn("destino.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                                        .add(Restrictions.eq("pasivo", false)) //autoridad lab activa
                                        .createAlias("laboratorio", "labautorizadoEnv")
                                        .createAlias("user", "usuario")
                                        .add(Restrictions.and(Restrictions.eq("usuario.username", filtro.getNombreUsuario()))) //usuario
                                        .setProjection(Property.forName("labautorizadoEnv.codigo"))))
                                .setProjection(Property.forName("toma.idTomaMx")))));
                crit.add(conditGroup);
            }
        }

        //filtro de resultados finales aprobados
        crit.add(Restrictions.and(
                        Restrictions.eq("estudio.aprobada", true))
        );

        //filtro de resultado final positivo
        crit.add(Subqueries.propertyIn("estudio.idSolicitudEstudio", DetachedCriteria.forClass(DetalleResultadoFinal.class)
                .setProjection(Property.forName("solicitudEstudio.idSolicitudEstudio"))));

        crit.addOrder(Order.asc("fechaAprobacion"));

        //filtro x area
        if(filtro.getArea() != null){
            crit.createAlias("tEstudio.area","area");
            crit.add( Restrictions.and(
                    Restrictions.eq("area.nombre",(filtro.getArea()))));
        }

        return crit.list();
    }

    @SuppressWarnings("unchecked")
    public List<DaSolicitudDx> getQCRoutineRequestByFilter(FiltroMx filtro) throws UnsupportedEncodingException {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = session.createCriteria(DaSolicitudDx.class, "rutina");
        crit.add(Restrictions.eq("anulado",false));
        crit.createAlias("rutina.idTomaMx", "toma");
        crit.createAlias("toma.idNotificacion", "notif");
        crit.createAlias("rutina.codDx", "dx");

        if(filtro.getNombreSolicitud()!= null){
            filtro.setNombreSolicitud(URLDecoder.decode(filtro.getNombreSolicitud(), "utf-8"));
        }

        //se filtra por SILAIS
        //ABRIL2019
        if (filtro.getCodSilais()!=null){
            //crit.createAlias("notif.codSilaisAtencion","silais");
            crit.add( Restrictions.and(
                            Restrictions.eq("notif.codSilaisAtencion", Long.valueOf(filtro.getCodSilais())))
            );
        }
        //se filtra por unidad de salud
        //ABRIL2019
        if (filtro.getCodUnidadSalud()!=null){
            //crit.createAlias("notif.codUnidadAtencion","unidadS");
            crit.add( Restrictions.and(
                            Restrictions.eq("notif.codUnidadAtencion", Long.valueOf(filtro.getCodUnidadSalud())))
            );
        }
        //Se filtra por rango de fecha de aprobacion
        if (filtro.getFechaInicioAprob()!=null && filtro.getFechaFinAprob()!=null){
            crit.add( Restrictions.and(
                            Restrictions.between("fechaAprobacion", filtro.getFechaInicioAprob(),filtro.getFechaFinAprob()))
            );
        }

        //se filtra por tipo de solicitud
        if(filtro.getCodTipoSolicitud()!=null){
            if(filtro.getCodTipoSolicitud().equals("Rutina")){
                crit.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                        .createAlias("idTomaMx", "idTomaMx")
                        .setProjection(Property.forName("idTomaMx.idTomaMx"))));
            }

        }

        //nombre solicitud
        if (filtro.getNombreSolicitud() != null) {
            if (filtro.getCodTipoSolicitud() != null) {
                if (filtro.getCodTipoSolicitud().equals("Rutina")) {
                    crit.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(DaSolicitudDx.class)
                            .createAlias("codDx", "dx")
                            .add(Restrictions.ilike("dx.nombre", "%" + filtro.getNombreSolicitud() + "%"))
                            .setProjection(Property.forName("idTomaMx.idTomaMx"))));
                }
            }
        }
        //se filtra que usuario tenga autorizado laboratorio al que se envio la muestra desde ALERTA
        if (filtro.getNombreUsuario()!=null) {
            crit.createAlias("toma.envio","envioMx");
            crit.add(Subqueries.propertyIn("envioMx.laboratorioDestino.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                    .createAlias("laboratorio", "labautorizado")
                    .createAlias("user", "usuario")
                    .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                    .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                    .setProjection(Property.forName("labautorizado.codigo"))));
        }

        crit.add(Restrictions.and(
                        Restrictions.eq("rutina.controlCalidad", true))
        );

        //filtro de resultados finales aprobados
        crit.add(Restrictions.and(
                        Restrictions.eq("rutina.aprobada", true))
        );

        // se filtra por tipo de muestra
        if (filtro.getCodTipoMx()!=null){
            crit.createAlias("toma.codTipoMx", "tipoMx");
            crit.add( Restrictions.and(

                            Restrictions.eq("tipoMx.idTipoMx", Integer.valueOf(filtro.getCodTipoMx())))
            );
        }
        //filtro de resultado final positivo
        /*crit.add(Subqueries.propertyIn("rutina.idSolicitudDx", DetachedCriteria.forClass(DetalleResultadoFinal.class)
                .setProjection(Property.forName("solicitudDx.idSolicitudDx"))));*/

        //crit.addOrder(Order.asc("fechaAprobacion"));

        if (filtro.getCodLaboratio()!=null){
            crit.add(Subqueries.propertyIn("toma.idTomaMx", DetachedCriteria.forClass(TrasladoMx.class)
                    .createAlias("tomaMx", "tomaMx")
                    .add(Restrictions.eq("laboratorioOrigen.codigo",filtro.getCodLaboratio()))
                    .setProjection(Property.forName("tomaMx.idTomaMx"))));
            //crit.add(Restrictions.eq("labProcesa.codigo",filtro.getCodLaboratio()));
        }

        /*crit.add(Subqueries.propertyIn("labProcesa.codigo", DetachedCriteria.forClass(AutoridadLaboratorio.class)
                .createAlias("laboratorio", "labautorizado")
                .createAlias("user", "usuario")
                .add(Restrictions.eq("pasivo",false)) //autoridad laboratorio activa
                .add(Restrictions.and(Restrictions.eq("usuario.username",filtro.getNombreUsuario()))) //usuario
                .setProjection(Property.forName("labautorizado.codigo"))));*/

        //filtro x area
        /*if(filtro.getArea() != null){
            crit.createAlias("dx.area","area");
            crit.add( Restrictions.and(
                    Restrictions.eq("area.nombre",(filtro.getArea()))));
        }*/

        return crit.list();
    }

    public List<ConsolidadoRecepcion> getResumenRecepcionMuestrasSILAIS(String laboratorio, Date fechaInicio, Date fechaFin, boolean nivelCentral){
        Session session = sessionFactory.getCurrentSession();
        String sQuery = "select coalesce(count(r.idRecepcion),0) as total, mx.idSilaisAtencion as idConsolida, mx.nombreSilaisAtencion as nombreConsolida " +
                "from RecepcionMx as r inner join r.tomaMx as mx " +
                "where r.fechaHoraRecepcion between :fechaInicio and :fechaFin ";
        if (!nivelCentral){
            sQuery += "and r.labRecepcion.codigo = :laboratorio ";
        }
        sQuery += "group by mx.idSilaisAtencion, mx.nombreSilaisAtencion";

        Query q = session.createQuery(sQuery);
        if (!nivelCentral) {
            q.setParameter("laboratorio", laboratorio);
        }
        q.setParameter("fechaInicio", fechaInicio);
        q.setParameter("fechaFin", fechaFin);

        q.setResultTransformer(Transformers.aliasToBean(ConsolidadoRecepcion.class));
        return q.list();
    }

    /**
     * Consolida las recepciones de muestras por municipios de un SILAIS
     * @param laboratorio
     * @param fechaInicio
     * @param fechaFin
     * @return List<Object[]>
     */
    public List<ConsolidadoRecepcion> getResumenRecepcionMuestrasMunSILAIS(String laboratorio, Date fechaInicio, Date fechaFin, Long codSilais){
        Session session = sessionFactory.getCurrentSession();
        String sQuery = "select distinct " +
                "coalesce((select count(distinct r.idRecepcion) " +
                "from RecepcionMx as r inner join r.tomaMx as mx inner join mx.idNotificacion as noti " +
                "where r.labRecepcion.codigo = :laboratorio and noti.idMuniUnidadAtencion = notifi.idMuniUnidadAtencion " +
                "and r.fechaHoraRecepcion between :fechaInicio and :fechaFin " +
                "group by noti.idMuniUnidadAtencion), 0) as total , notifi.idMuniUnidadAtencion as idConsolida, notifi.nombreMuniUnidadAtencion as nombreConsolida " +
                "from DaNotificacion notifi " +
                "where notifi.idSilaisAtencion = :codSilais ";

        Query q = session.createQuery(sQuery);

        q.setParameter("laboratorio",laboratorio);
        q.setParameter("fechaInicio", fechaInicio);
        q.setParameter("fechaFin", fechaFin);
        q.setParameter("codSilais", codSilais);
        q.setResultTransformer(Transformers.aliasToBean(ConsolidadoRecepcion.class));
        return q.list();
    }


    public List<Object[]> getResumenRecepcionMuestrasSolicitud(String laboratorio, Date fechaInicio, Date fechaFin){
        Session session = sessionFactory.getCurrentSession();
        String sQuery = "select count(r.idRecepcion) as total, dx.idDiagnostico, dx.nombre " +
                "from RecepcionMx as r, DaSolicitudDx as sdx inner join sdx.idTomaMx as mx " +
                "inner join sdx.codDx as dx " +
                "where r.tomaMx.idTomaMx = mx.idTomaMx and sdx.anulado = false and sdx.inicial = true and sdx.labProcesa.codigo = :laboratorio " +
                " and r.fechaHoraRecepcion between :fechaInicio and :fechaFin " +
                "group by dx.idDiagnostico, dx.nombre";

        String sQuery2 = "select count(r.idRecepcion) as total, es.idEstudio, es.nombre " +
                "from RecepcionMx as r, DaSolicitudEstudio as sde inner join sde.idTomaMx as mx " +
                "inner join sde.tipoEstudio as es " +
                "where r.tomaMx.idTomaMx = mx.idTomaMx and sde.anulado = false and mx.envio.laboratorioDestino.codigo = :laboratorio " +
                " and r.fechaHoraRecepcion between :fechaInicio and :fechaFin " +
                "group by es.idEstudio, es.nombre";


        Query q = session.createQuery(sQuery);

        q.setParameter("laboratorio",laboratorio);
        q.setParameter("fechaInicio", fechaInicio);
        q.setParameter("fechaFin", fechaFin);

        List<Object[]> resumenMxSolicitud= (List<Object[]>)q.list();

        q = session.createQuery(sQuery2);
        q.setParameter("laboratorio",laboratorio);
        q.setParameter("fechaInicio", fechaInicio);
        q.setParameter("fechaFin", fechaFin);

        resumenMxSolicitud.addAll(q.list());

        return resumenMxSolicitud;
    }

    public List<DaSolicitudDx> getDiagnosticosAprobadosByFiltro(FiltrosReporte filtro){
        Session session = sessionFactory.getCurrentSession();
        Query queryNotiDx = null;
        if (filtro.getCodArea().equals("AREAREP|PAIS")) {
            queryNotiDx = session.createQuery(" select dx from DaSolicitudDx dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti " +
                    "where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true and dx.controlCalidad = false "+ sqlLab + sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut));
        }else if (filtro.getCodArea().equals("AREAREP|SILAIS")) {
            queryNotiDx = session.createQuery(" select dx from DaSolicitudDx dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti " +
                    "where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true and dx.controlCalidad = false "+ sqlLab + sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) +
                    "  and noti.idSilaisAtencion =:codSilais ");
            queryNotiDx.setParameter("codSilais", filtro.getCodSilais());
        } else if (filtro.getCodArea().equals("AREAREP|UNI")) {
            queryNotiDx = session.createQuery(" select dx from DaSolicitudDx dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti " +
                    "where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true and dx.controlCalidad = false "+ sqlLab + sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) +
                    "  and noti.idUnidadAtencion =:codUnidad ");
            queryNotiDx.setParameter("codUnidad", filtro.getCodUnidad());
        }

        queryNotiDx.setParameter("codigoLab", filtro.getCodLaboratio());
        queryNotiDx.setParameter("idDx", filtro.getIdDx());
        queryNotiDx.setParameter("fechaInicio", filtro.getFechaInicio());
        queryNotiDx.setParameter("fechaFin", filtro.getFechaFin());
        return queryNotiDx.list();
    }

    public List<ResultadoVigilancia> getDiagnosticosAprobadosByFiltroV2(FiltrosReporte filtro){
        Session session = sessionFactory.getCurrentSession();
        Query queryNotiDx = null;
        if (filtro.getCodArea().equals("AREAREP|PAIS")) {
            queryNotiDx = session.createQuery(" select cast(p.personaId as string) as codigoExpUnico, p.primerNombre as primerNombre, p.segundoNombre as segundoNombre, p.primerApellido as primerApellido, p.segundoApellido as segundoApellido, p.fechaNacimiento as fechaNacimiento, p.codigoSexo as sexo, " +
                    " p.direccionResidencia as direccionResidencia, p.telefonoResidencia as telefonoResidencia, p.telefonoMovil as telefonoMovil, p.nombreComunidadResidencia as comunidadResidencia, " +
                    " noti.idNotificacion as idNotificacion, noti.semanasEmbarazo as semanasEmbarazo, noti.fechaInicioSintomas as fechaInicioSintomas, noti.desUrgente as urgente, noti.desEmbarazada as embarazada, " +
                    " noti.codSilaisAtencion as codigoSilaisNoti, noti.nombreSilaisAtencion as nombreSilaisNoti, " +
                    " noti.codUnidadAtencion as codigoUnidadNoti, noti.nombreUnidadAtencion as nombreUnidadNoti, " +
                    " noti.codMuniUnidadAtencion as codigoMuniNoti, noti.nombreMuniUnidadAtencion as nombreMuniNoti, " +
                    " noti.municipioResidencia as codigoMuniResid, noti.nombreMunicipioResidencia as nombreMuniResid, " + //de la persona
                    " noti.codSilaisResid as codigoSilaisResid, noti.nombreSilaisResid as nombreSilaisResid, " + //de la persona
                    " mx.codSilaisAtencion as codigoSilaisMx, mx.nombreSilaisAtencion as nombreSilaisMx, " +
                    " mx.codUnidadAtencion as codigoUnidadMx, mx.nombreUnidadAtencion as nombreUnidadMx, " +
                    " mx.codMuniUnidadAtencion as codigoMuniMx, mx.nombreMuniUnidadAtencion as nombreMuniMx, " +
                    " mx.idTomaMx as idTomaMx, mx.fechaHTomaMx as fechaTomaMx, mx.codigoLab as codigoMx, mx.codigoUnicoMx as codUnicoMx, mx.codTipoMx.idTipoMx as idTipoMx, mx.codTipoMx.nombre as nombreTipoMx, dx.idSolicitudDx as idSolicitud, dx.fechaAprobacion as fechaAprobacion  " +
                    " from DaSolicitudDx dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti inner join noti.persona p  " +
                    " where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true and dx.controlCalidad = false "+ sqlLab + sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut));

        }else if (filtro.getCodArea().equals("AREAREP|SILAIS")) {
            queryNotiDx = session.createQuery(" select cast(p.personaId as string) as codigoExpUnico, p.primerNombre as primerNombre, p.segundoNombre as segundoNombre, p.primerApellido as primerApellido, p.segundoApellido as segundoApellido, p.fechaNacimiento as fechaNacimiento, p.codigoSexo as sexo, " +
                    " p.direccionResidencia as direccionResidencia, p.telefonoResidencia as telefonoResidencia, p.telefonoMovil as telefonoMovil, p.nombreComunidadResidencia as comunidadResidencia, " +
                    " noti.idNotificacion as idNotificacion, noti.semanasEmbarazo as semanasEmbarazo, noti.fechaInicioSintomas as fechaInicioSintomas, noti.desUrgente as urgente, noti.desEmbarazada as embarazada, " +
                    " noti.codSilaisAtencion as codigoSilaisNoti, noti.nombreSilaisAtencion as nombreSilaisNoti, " +
                    " noti.codUnidadAtencion as codigoUnidadNoti, noti.nombreUnidadAtencion as nombreUnidadNoti, " +
                    " noti.codMuniUnidadAtencion as codigoMuniNoti, noti.nombreMuniUnidadAtencion as nombreMuniNoti, " +
                    " noti.municipioResidencia as codigoMuniResid, noti.nombreMunicipioResidencia as nombreMuniResid, " +
                    " noti.codSilaisResid as codigoSilaisResid, noti.nombreSilaisResid as nombreSilaisResid, " +
                    " mx.codSilaisAtencion as codigoSilaisMx, mx.nombreSilaisAtencion as nombreSilaisMx, " +
                    " mx.codUnidadAtencion as codigoUnidadMx, mx.nombreUnidadAtencion as nombreUnidadMx, " +
                    " mx.codMuniUnidadAtencion as codigoMuniMx, mx.nombreMuniUnidadAtencion as nombreMuniMx, " +
                    " mx.idTomaMx as idTomaMx, mx.fechaHTomaMx as fechaTomaMx, mx.codigoLab as codigoMx, mx.codigoUnicoMx as codUnicoMx, mx.codTipoMx.idTipoMx as idTipoMx, mx.codTipoMx.nombre as nombreTipoMx, dx.idSolicitudDx as idSolicitud, dx.fechaAprobacion as fechaAprobacion  " +
                    " from DaSolicitudDx dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti inner join noti.persona p " +
                    " where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true and dx.controlCalidad = false "+ sqlLab + sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) +
                    " and noti.idSilaisAtencion =:codSilais ");
            queryNotiDx.setParameter("codSilais", filtro.getCodSilais());

        } else if (filtro.getCodArea().equals("AREAREP|UNI")) {
            queryNotiDx = session.createQuery(" select cast(p.personaId as string) as codigoExpUnico, p.primerNombre as primerNombre, p.segundoNombre as segundoNombre, p.primerApellido as primerApellido, p.segundoApellido as segundoApellido, p.fechaNacimiento as fechaNacimiento, p.codigoSexo as sexo, " +
                    " p.direccionResidencia as direccionResidencia, p.telefonoResidencia as telefonoResidencia, p.telefonoMovil as telefonoMovil, p.nombreComunidadResidencia as comunidadResidencia, " +
                    " noti.idNotificacion as idNotificacion, noti.semanasEmbarazo as semanasEmbarazo, noti.fechaInicioSintomas as fechaInicioSintomas, noti.desUrgente as urgente, noti.desEmbarazada as embarazada, " +
                    " noti.codSilaisAtencion as codigoSilaisNoti, noti.nombreSilaisAtencion as nombreSilaisNoti, " +
                    " noti.codUnidadAtencion as codigoUnidadNoti, noti.nombreUnidadAtencion as nombreUnidadNoti, " +
                    " noti.codMuniUnidadAtencion as codigoMuniNoti, noti.nombreMuniUnidadAtencion as nombreMuniNoti, " +
                    " noti.municipioResidencia as codigoMuniResid, noti.nombreMunicipioResidencia as nombreMuniResid, " +
                    " noti.codSilaisResid as codigoSilaisResid, noti.nombreSilaisResid as nombreSilaisResid, " +
                    " mx.codSilaisAtencion as codigoSilaisMx, mx.nombreSilaisAtencion as nombreSilaisMx, " +
                    " mx.codUnidadAtencion as codigoUnidadMx, mx.nombreUnidadAtencion as nombreUnidadMx, " +
                    " mx.codMuniUnidadAtencion as codigoMuniMx, mx.nombreMuniUnidadAtencion as nombreMuniMx, " +
                    " mx.idTomaMx as idTomaMx, mx.fechaHTomaMx as fechaTomaMx, mx.codigoLab as codigoMx, mx.codigoUnicoMx as codUnicoMx, mx.codTipoMx.idTipoMx as idTipoMx, mx.codTipoMx.nombre as nombreTipoMx, dx.idSolicitudDx as idSolicitud, dx.fechaAprobacion as fechaAprobacion  " +
                    " from DaSolicitudDx dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti inner join noti.persona p " +
                    " where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true and dx.controlCalidad = false "+ sqlLab + sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) +
                    " and noti.idUnidadAtencion =:codUnidad ");
            queryNotiDx.setParameter("codUnidad", filtro.getCodUnidad());
        }

        queryNotiDx.setParameter("codigoLab", filtro.getCodLaboratio());
        queryNotiDx.setParameter("idDx", filtro.getIdDx());
        queryNotiDx.setParameter("fechaInicio", filtro.getFechaInicio());
        queryNotiDx.setParameter("fechaFin", filtro.getFechaFin());

        queryNotiDx.setResultTransformer(Transformers.aliasToBean(ResultadoVigilancia.class));
        return queryNotiDx.list();
    }


    /**
     * M?todo que retornar la informaci?n para generar reporte y gr?fico de notificaciones por tipo de resultado (positivo, negativo, sin resultado y % positividad)
     * 04-09-2018 Andrea solocita que sea por fecha de aprobaci�n que se filtre el reporte
     * @param filtro indicando el nivel (pais, silais, departamento, municipio, unidad salud), tipo notificaci?n, rango de fechas, factor tasas de poblaci?n
     * @return Lista de objetos a mostrar
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getDataDxResultReport(FiltrosReporte filtro) {
        // Retrieve session from Hibernate
        List<Object[]> resTemp1 = new ArrayList<Object[]>();
        List<Object[]> resTemp2 = new ArrayList<Object[]>();

        List<Object[]> resFinal = new ArrayList<Object[]>();
        Session session = sessionFactory.getCurrentSession();
        Query queryNotiDx = null;
        Query queryIdNoti = null;

        if (filtro.getCodArea().equals("AREAREP|PAIS")) {

            if (filtro.isPorSilais()) {
                queryNotiDx = session.createQuery(" select coalesce(ent.codSilaisAtencion, 0) , coalesce(ent.nombreSilaisAtencion, 'SIN SILAIS'), " + //TOTAL RUTINAS
                        " (select coalesce(sum(count(noti.idNotificacion)),0) from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion and noti.codSilaisAtencion = ent.codSilaisAtencion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx and noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.controlCalidad = false " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " group by noti.codSilaisAtencion) as dx, " +
                        " coalesce( " + //TOTAL RUTINAS CON RESULTADO
                        " (select sum(case dx.aprobada when true then 1 else 0 end) " +
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx" +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        " and  noti.codSilaisAtencion = ent.codSilaisAtencion " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                        " and mx.anulada = false),0) as conresultado, " +
                        " coalesce( " + //TOTAL RUTINAS SIN RESULTADO
                        " (select  sum(case dx.aprobada when false then 1 else 0 end) " +
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and  noti.codSilaisAtencion = ent.codSilaisAtencion " +
                        " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                        " and mx.anulada = false),0) as sinresultado " +
                        " from DaNotificacion ent " + (!filtro.isNivelCentral()?", EntidadAdtvaLaboratorio entlab ":"") +
                        " where ent.pasivo = false " + (!filtro.isNivelCentral()?" and ent.codSilaisAtencion = entlab.entidadAdtva and entlab.laboratorio.codigo = :laboratorio ":"") +
                        " group by ent.codSilaisAtencion , ent.nombreSilaisAtencion " +
                        " order by ent.codSilaisAtencion ");

                queryIdNoti = session.createQuery(" select noti.codSilaisAtencion, dx.idSolicitudDx, r.valor " +
                        //", coalesce((select rr.concepto.tipo.codigo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                        //", coalesce((select rr.concepto.tipo.codigo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                        ", coalesce((select rr.concepto.tipo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                        ", coalesce((select rr.concepto.tipo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx, DetalleResultadoFinal r " +
                        " where noti.idNotificacion = mx.idNotificacion " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        " and dx.idSolicitudDx = r.solicitudDx.idSolicitudDx and r.pasivo = false " +
                        " and noti.pasivo = false and dx.anulado = false " +
                        " and mx.anulada = false " +
                        " and dx.aprobada = true and dx.controlCalidad = false " +
                        " and noti.codSilaisAtencion is not null " +
                        " order by noti.codSilaisAtencion");

            }else{
                queryNotiDx = session.createQuery(" select div.divisionpoliticaId, div.nombre, " +
                        " (select coalesce(sum(count(noti.idNotificacion)),0) from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx" +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion" +
                        " and noti.codUnidadAtencion.municipio.dependencia.divisionpoliticaId = div.divisionpoliticaId " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false and mx.anulada = false " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " group by noti.codUnidadAtencion.municipio.dependencia.divisionpoliticaId) as dx, " +
                        " coalesce( " +
                        " (select sum(case dx.aprobada when true then 1 else 0 end) " +
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        " and  noti.codUnidadAtencion.municipio.dependencia.divisionpoliticaId = div.divisionpoliticaId " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                        " and mx.anulada = false),0) as conresultado, " +
                        " coalesce( " +
                        " (select  sum(case dx.aprobada when false then 1 else 0 end) " +
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and  noti.codUnidadAtencion.municipio.dependencia.divisionpoliticaId = div.divisionpoliticaId " +
                        " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                        " and mx.anulada = false),0) as sinresultado " +
                        " from Divisionpolitica div " +
                        "where div.dependencia is null and div.pasivo = '0'" +
                        " order by div.divisionpoliticaId ");

                queryIdNoti = session.createQuery(" select noti.codUnidadAtencion.municipio.dependencia.divisionpoliticaId, dx.idSolicitudDx, r.valor " +
                        //", coalesce((select rr.concepto.tipo.codigo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                        //", coalesce((select rr.concepto.tipo.codigo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                        ", coalesce((select rr.concepto.tipo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                        ", coalesce((select rr.concepto.tipo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx, DetalleResultadoFinal r " +
                        " where noti.idNotificacion = mx.idNotificacion " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        " and dx.idSolicitudDx = r.solicitudDx.idSolicitudDx and r.pasivo = false " +
                        " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                        " and mx.anulada = false " +
                        " and dx.aprobada = true " +
                        " and noti.codUnidadAtencion is not null " +
                        " order by noti.codUnidadAtencion.municipio.dependencia.divisionpoliticaId");

            }

            //rutinas
            queryIdNoti.setParameter("idDx", filtro.getIdDx());
            queryIdNoti.setParameter("fechaInicio", filtro.getFechaInicio());
            queryIdNoti.setParameter("fechaFin", filtro.getFechaFin());
            //if (!filtro.isNivelCentral()) {
            if (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")) {
                queryNotiDx.setParameter("codigoLab", filtro.getCodLaboratio());
                queryIdNoti.setParameter("codigoLab", filtro.getCodLaboratio());
            }
            if (!filtro.isNivelCentral()){
                queryNotiDx.setParameter("laboratorio", filtro.getCodLaboratio());
            }
            resTemp2.addAll(queryIdNoti.list());

        } else if (filtro.getCodArea().equals("AREAREP|SILAIS")) {

            queryNotiDx = session.createQuery(" select distinct uni.idMuniUnidadAtencion, uni.nombreMuniUnidadAtencion, " +
                    " (select coalesce(sum(count(noti.idNotificacion)),0) from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion" +
                    " and noti.idMuniUnidadAtencion = uni.idMuniUnidadAtencion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false and mx.anulada = false " +
                    sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " group by noti.idSilaisAtencion), " +
                    " coalesce( " +
                    " (select sum(case dx.aprobada when true then 1 else 0 end) " +
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    " and noti.idMuniUnidadAtencion = uni.idMuniUnidadAtencion " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                    " and mx.anulada = false),0) as conresultado, " +
                    " coalesce( " +
                    " (select  sum(case dx.aprobada when false then 1 else 0 end) " +
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    " and noti.idMuniUnidadAtencion = uni.idMuniUnidadAtencion " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                    " and mx.anulada = false),0) as sinresultado " +
                    " from DaNotificacion as uni " +
                    " where uni.pasivo=false " +
                    " and uni.codSilaisAtencion = :codSilais " +
                    " order by uni.idMuniUnidadAtencion ");

            queryIdNoti = session.createQuery(" select coalesce(noti.idMuniUnidadAtencion,0) , dx.idSolicitudDx, r.valor " +
                    //", coalesce((select rr.concepto.tipo.codigo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                    //", coalesce((select rr.concepto.tipo.codigo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                    ", coalesce((select rr.concepto.tipo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                    ", coalesce((select rr.concepto.tipo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx, DetalleResultadoFinal r " +
                    " where noti.idNotificacion = mx.idNotificacion " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    " and dx.idSolicitudDx = r.solicitudDx.idSolicitudDx and r.pasivo = false " +
                    " and noti.pasivo = false and dx.anulado = false " +
                    " and mx.anulada = false " +
                    " and dx.aprobada = true and dx.controlCalidad = false " +
                    " and noti.codSilaisAtencion= :codSilais " +
                    " order by noti.idMuniUnidadAtencion ");

            queryNotiDx.setParameter("codSilais", filtro.getCodSilais());

            //rutinas
            queryIdNoti.setParameter("idDx", filtro.getIdDx());
            queryIdNoti.setParameter("fechaInicio", filtro.getFechaInicio());
            queryIdNoti.setParameter("fechaFin", filtro.getFechaFin());
            queryIdNoti.setParameter("codSilais", filtro.getCodSilais());
            if (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")) {
                queryNotiDx.setParameter("codigoLab", filtro.getCodLaboratio());
                queryIdNoti.setParameter("codigoLab", filtro.getCodLaboratio());
            }
            resTemp2.addAll(queryIdNoti.list());

        } else if (filtro.getCodArea().equals("AREAREP|MUNI")) {
            queryNotiDx = session.createQuery(" select distinct uni.idUnidadAtencion, uni.nombreUnidadAtencion, " +
                    " (select coalesce(sum(count(noti.idNotificacion)),0) from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion" +
                    " and noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx and noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.controlCalidad = false " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " group by  noti.idUnidadAtencion) as dx, " +
                    " coalesce( " +
                    " (select sum(case dx.aprobada when true then 1 else 0 end) " +
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    " and  noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                    " and mx.anulada = false),0) as conresultado, " +
                    " coalesce( " +
                    " (select  sum(case dx.aprobada when false then 1 else 0 end) " +
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and  noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                    " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                    " and mx.anulada = false),0) as sinresultado " +
                    "FROM DaNotificacion uni " +
                    "where uni.idMuniUnidadAtencion = :codMunicipio" +
                    " and uni.codSilaisAtencion = :codSilais" +
                    " and uni.tipoUnidad in ("+ HealthUnitType.UnidadesPrimHosp.getDiscriminator()+") " +
                    " order by uni.idUnidadAtencion ");


            queryIdNoti = session.createQuery(" select noti.idUnidadAtencion, dx.idSolicitudDx, r.valor " +
                    //", coalesce((select rr.concepto.tipo.codigo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                    //", coalesce((select rr.concepto.tipo.codigo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                    ", coalesce((select rr.concepto.tipo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                    ", coalesce((select rr.concepto.tipo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx, DetalleResultadoFinal r  " +
                    " where noti.idNotificacion = mx.idNotificacion " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    " and dx.idSolicitudDx = r.solicitudDx.idSolicitudDx and r.pasivo = false " +
                    " and noti.pasivo = false and dx.anulado = false " +
                    " and mx.anulada = false " +
                    " and dx.aprobada = true and dx.controlCalidad = false " +
                    " and noti.idMuniUnidadAtencion = :codMunicipio " +
                    " and noti.codSilaisAtencion = :codSilais " +
                    " order by noti.idUnidadAtencion ");

            queryNotiDx.setParameter("codMunicipio", filtro.getCodMunicipio());
            queryNotiDx.setParameter("codSilais", filtro.getCodSilais());

            //rutinas
            queryIdNoti.setParameter("idDx", filtro.getIdDx());
            queryIdNoti.setParameter("fechaInicio", filtro.getFechaInicio());
            queryIdNoti.setParameter("fechaFin", filtro.getFechaFin());
            queryIdNoti.setParameter("codMunicipio", filtro.getCodMunicipio());
            queryIdNoti.setParameter("codSilais", filtro.getCodSilais());
            if (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")) {
                queryNotiDx.setParameter("codigoLab", filtro.getCodLaboratio());
                queryIdNoti.setParameter("codigoLab", filtro.getCodLaboratio());
            }
            resTemp2.addAll(queryIdNoti.list());

        } else if (filtro.getCodArea().equals("AREAREP|UNI")) {

            //ABRIL2019
            /*if(filtro.isSubunidades()){
                queryNotiDx = session.createQuery(" select uni.unidadId, uni.nombre, " +
                        " (select coalesce(sum(count(noti.idNotificacion)),0) from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx" +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion" +
                        " and noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false and mx.anulada = false " +
                        sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " group by  noti.idUnidadAtencion) as dx, " +
                        " coalesce( " +
                        " (select sum(case dx.aprobada when true then 1 else 0 end) " +
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        " and  noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                        sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                        " and mx.anulada = false),0) as conresultado, " +
                        " coalesce( " +
                        " (select  sum(case dx.aprobada when false then 1 else 0 end) " +
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                        " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and  noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                        " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                        " and mx.anulada = false),0) as sinresultado " +
                        "FROM DaNotificacion uni " +
                        "where (uni.unidadId = :codUnidad" +
                        " or uni.unidadAdtva in (select u.codigo from Unidades u where u.unidadId = :codUnidad )) " +
                        " and uni.tipoUnidad in ("+ HealthUnitType.UnidadesPrimHosp.getDiscriminator()+") " +
                        " order by uni.unidadId ");

                queryIdNoti = session.createQuery(" select noti.idUnidadAtencion, dx.idSolicitudDx, r.valor " +
                        ", coalesce((select rr.concepto.tipo.codigo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                        ", coalesce((select rr.concepto.tipo.codigo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                        " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx, DetalleResultadoFinal r " +
                        " where noti.idNotificacion = mx.idNotificacion " +
                        sqlRutina + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                        " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                        " and dx.idSolicitudDx = r.solicitudDx.idSolicitudDx and r.pasivo = false " +
                        " and noti.pasivo = false and dx.anulado = false " +
                        " and mx.anulada = false " +
                        " and dx.aprobada = true and dx.controlCalidad = false " +
                        " and (noti.idUnidadAtencion = :codUnidad " +
                        " or noti.codUnidadAtencion.unidadAdtva in (select u.codigo from Unidades u where u.unidadId = :codUnidad )) " +
                        " order by noti.idUnidadAtencion ");

            }else{*/
            queryNotiDx = session.createQuery(" select distinct uni.idUnidadAtencion, uni.nombreUnidadAtencion, " +
                    " (select coalesce(sum(count(noti.idNotificacion)),0) from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx " +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion" +
                    " and noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false and mx.anulada = false " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " group by  noti.codUnidadAtencion) as dx, " +
                    " coalesce( " +
                    " (select sum(case dx.aprobada when true then 1 else 0 end) " +
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx" +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    " and  noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                    " and mx.anulada = false),0) as conresultado, " +
                    " coalesce( " +
                    " (select  sum(case dx.aprobada when false then 1 else 0 end) " +
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx" +
                    " where noti.idNotificacion = mx.idNotificacion.idNotificacion " +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and  noti.codUnidadAtencion =  uni.codUnidadAtencion " +
                    " and noti.pasivo = false and dx.anulado = false and dx.controlCalidad = false " +
                    " and mx.anulada = false),0) as sinresultado " +
                    "FROM DaNotificacion uni " +
                    "where uni.codUnidadAtencion = :codUnidad" +
                    " and uni.tipoUnidad in ("+ HealthUnitType.UnidadesPrimHosp.getDiscriminator()+") " +
                    " order by uni.idUnidadAtencion ");

            queryIdNoti = session.createQuery(" select noti.idUnidadAtencion, dx.idSolicitudDx, r.valor " +
                    //", coalesce((select rr.concepto.tipo.codigo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                    //", coalesce((select rr.concepto.tipo.codigo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                    ", coalesce((select rr.concepto.tipo from RespuestaSolicitud rr where rr.idRespuesta = r.respuesta.idRespuesta),'NULL')"+
                    ", coalesce((select rr.concepto.tipo from RespuestaExamen rr where rr.idRespuesta = r.respuestaExamen.idRespuesta),'NULL') "+
                    " from DaNotificacion noti, DaTomaMx mx, DaSolicitudDx dx, DetalleResultadoFinal r " +
                    " where noti.idNotificacion = mx.idNotificacion " +
                    sqlRutina +(filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) + (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")?sqlLab:"") +
                    " and mx.idTomaMx = dx.idTomaMx.idTomaMx " +
                    " and dx.idSolicitudDx = r.solicitudDx.idSolicitudDx and r.pasivo = false " +
                    " and noti.pasivo = false and dx.anulado = false " +
                    " and mx.anulada = false " +
                    " and dx.aprobada = true and dx.controlCalidad = false " +
                    " and noti.codUnidadAtencion = :codUnidad " +
                    " order by noti.idUnidadAtencion ");
            //}

            queryNotiDx.setParameter("codUnidad", filtro.getCodUnidad());

            //rutinas
            queryIdNoti.setParameter("idDx", filtro.getIdDx());
            queryIdNoti.setParameter("fechaInicio", filtro.getFechaInicio());
            queryIdNoti.setParameter("fechaFin", filtro.getFechaFin());
            queryIdNoti.setParameter("codUnidad", filtro.getCodUnidad());
            if (!filtro.getCodLaboratio().equalsIgnoreCase("ALL")) {
                queryNotiDx.setParameter("codigoLab", filtro.getCodLaboratio());
                queryIdNoti.setParameter("codigoLab", filtro.getCodLaboratio());
            }
            resTemp2.addAll(queryIdNoti.list());

        }

        queryNotiDx.setParameter("idDx", filtro.getIdDx());
        queryNotiDx.setParameter("fechaInicio", filtro.getFechaInicio());
        queryNotiDx.setParameter("fechaFin", filtro.getFechaFin());
        resTemp1.addAll(queryNotiDx.list());
        for (Object[] reg : resTemp1) {
            Object[] reg1 = new Object[8];
            reg1[0] = reg[1]; //Nombre Silais
            reg1[1] = reg[2]; //Cantidad Notificaciones (NO SE USA)
            reg1[2] = (Long) reg[2]; //Cantidad Dx
            if (!filtro.getCodArea().equals("AREAREP|MUNI") || (filtro.getCodArea().equals("AREAREP|MUNI") && (Long) reg[2]>0)) {
                int pos = 0;
                int neg = 0;
                int inadecuada = 0;
                String idSolicitud = "";
                for (Object[] sol : resTemp2) {
                    //identidad
                    if (sol[0].equals(reg[0]) && !sol[0].equals(idSolicitud)) {

                        if (!sol[3].toString().equalsIgnoreCase("NULL")) {
                            if (sol[3].toString().equalsIgnoreCase("TPDATO|LIST")) {
                                Integer idLista = Integer.valueOf(sol[2].toString());
                                String valor = null;
                                try {
                                    valor = respuestasExamenService.getValorCatalogoListaConceptoByIdLista(idLista);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (valor != null) {
                                    if (valor.trim().toLowerCase().contains("negativo")
                                            || valor.trim().toLowerCase().contains("no reactor")
                                            || valor.trim().toLowerCase().contains("no detectado")
                                            || valor.trim().toUpperCase().contains("MTB-ND")) {
                                        neg++;
                                        idSolicitud = sol[1].toString();
                                    } else if (valor.trim().toLowerCase().equals("mx inadecuada")){
                                        inadecuada++;
                                        idSolicitud = sol[1].toString();
                                    } else if (valor.trim().toLowerCase().contains("positivo")
                                            || valor.trim().toLowerCase().contains("reactor")
                                            || valor.trim().toLowerCase().contains("detectado")
                                            || valor.trim().toUpperCase().contains("MTB-DET")
                                            || (!valor.trim().toLowerCase().contains("negativo") && !valor.trim().toLowerCase().contains("indetermin"))) {
                                        pos++;
                                        idSolicitud = sol[1].toString();
                                    }
                                }

                            } else if (sol[3].toString().equalsIgnoreCase("TPDATO|TXT")) {
                                if (sol[2].toString().trim().toLowerCase().contains("negativo")
                                        || sol[2].toString().trim().toLowerCase().contains("no reactor")
                                        || sol[2].toString().trim().toLowerCase().contains("no detectado")
                                        || sol[2].toString().trim().toUpperCase().contains("MTB-ND")) {
                                    neg++;
                                    idSolicitud = sol[1].toString();
                                } else if (sol[2].toString().trim().toLowerCase().contains("mx inadecuada")) {
                                    inadecuada++;
                                    idSolicitud = sol[1].toString();
                                } else if (sol[2].toString().trim().toLowerCase().contains("positivo")
                                        || sol[2].toString().trim().toLowerCase().contains("reactor")
                                        || sol[2].toString().trim().toLowerCase().contains("detectado")
                                        || sol[2].toString().trim().toUpperCase().contains("MTB-DET")
                                        || (!sol[2].toString().trim().toLowerCase().contains("negativo") && !sol[2].toString().trim().toLowerCase().contains("indetermin"))) {
                                    pos++;
                                    idSolicitud = sol[1].toString();
                                }
                            }
                        } else if (!sol[4].toString().equalsIgnoreCase("NULL")) {
                            if (sol[4].toString().equalsIgnoreCase("TPDATO|LIST")) {
                                Integer idLista = Integer.valueOf(sol[2].toString());
                                String valor = null;
                                try {
                                    valor = respuestasExamenService.getValorCatalogoListaConceptoByIdLista(idLista);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (valor != null) {
                                    if (valor.trim().toLowerCase().contains("negativo")
                                            || valor.trim().toLowerCase().contains("no reactor")
                                            || valor.trim().toLowerCase().contains("no detectado")
                                            || valor.trim().toUpperCase().contains("MTB-ND")) {
                                        neg++;
                                        idSolicitud = sol[1].toString();
                                    } else if (valor.trim().toLowerCase().equals("mx inadecuada")){
                                        inadecuada++;
                                        idSolicitud = sol[1].toString();
                                    } else if (valor.trim().toLowerCase().contains("positivo")
                                            || valor.trim().toLowerCase().contains("reactor")
                                            || valor.trim().toLowerCase().contains("detectado")
                                            || valor.trim().toUpperCase().contains("MTB-DET")
                                            || (!valor.trim().toLowerCase().contains("negativo") && !valor.trim().toLowerCase().contains("indetermin"))) {
                                        pos++;
                                        idSolicitud = sol[1].toString();
                                    }
                                }


                            } else if (sol[4].toString().equalsIgnoreCase("TPDATO|TXT")) {
                                if (sol[2].toString().trim().toLowerCase().contains("negativo")
                                        || sol[2].toString().trim().toLowerCase().contains("no reactor")
                                        || sol[2].toString().trim().toLowerCase().contains("no detectado")
                                        || sol[2].toString().trim().toUpperCase().contains("MTB-ND")) {
                                    neg++;
                                    idSolicitud = sol[1].toString();
                                } else if (sol[2].toString().trim().toLowerCase().contains("mx inadecuada")) {
                                    inadecuada++;
                                    idSolicitud = sol[1].toString();
                                } else if (sol[2].toString().trim().toLowerCase().contains("positivo")
                                        || sol[2].toString().trim().toLowerCase().contains("reactor")
                                        || sol[2].toString().trim().toLowerCase().contains("detectado")
                                        || sol[2].toString().trim().toUpperCase().contains("MTB-DET")
                                        || (!sol[2].toString().trim().toLowerCase().contains("negativo") && !sol[2].toString().trim().toLowerCase().contains("indetermin"))) {
                                    pos++;
                                    idSolicitud = sol[1].toString();
                                }

                            }
                        }
                    }
                }

                reg1[3] = pos; // Positivo
                reg1[4] = neg; // Negativo
                reg1[5] = (Long) reg[4]; // Sin Resultado dx
                Long totalConySinResultado = (Long) reg1[2];
                reg1[6] = (totalConySinResultado != 0 ? (double) Math.round(Integer.valueOf(reg1[3].toString()).doubleValue() / totalConySinResultado * 100 * 100) / 100 : 0);
                reg1[7] = inadecuada; //muestras inadecuadas
                resFinal.add(reg1);
            }
        }
        return resFinal;
    }

    public List<ResultadoVigilancia> getEstudiosAprobadosByFiltroV2(FiltrosReporte filtro){
        Session session = sessionFactory.getCurrentSession();
        Query queryNotiDx = null;
        if (filtro.getCodArea().equals("AREAREP|PAIS")) {
            queryNotiDx = session.createQuery(" select cast(p.personaId as string) as codigoExpUnico, p.primerNombre as primerNombre, p.segundoNombre as segundoNombre, p.primerApellido as primerApellido, p.segundoApellido as segundoApellido, p.fechaNacimiento as fechaNacimiento, p.codigoSexo as sexo, " +
                    " p.direccionResidencia as direccionResidencia, p.telefonoResidencia as telefonoResidencia, p.telefonoMovil as telefonoMovil, p.nombreComunidadResidencia as comunidadResidencia, " +
                    " noti.idNotificacion as idNotificacion, noti.semanasEmbarazo as semanasEmbarazo, noti.fechaInicioSintomas as fechaInicioSintomas, noti.desUrgente as urgente, noti.desEmbarazada as embarazada, " +
                    " noti.codSilaisAtencion as codigoSilaisNoti, noti.nombreSilaisAtencion as nombreSilaisNoti, " +
                    " noti.codUnidadAtencion as codigoUnidadNoti, noti.nombreUnidadAtencion as nombreUnidadNoti, " +
                    " noti.codMuniUnidadAtencion as codigoMuniNoti, noti.nombreMuniUnidadAtencion as nombreMuniNoti, " +
                    " noti.municipioResidencia as codigoMuniResid, noti.nombreMunicipioResidencia as nombreMuniResid, " + //de la persona
                    " noti.codSilaisResid as codigoSilaisResid, noti.nombreSilaisResid as nombreSilaisResid, " +
                    " mx.codSilaisAtencion as codigoSilaisMx, mx.nombreSilaisAtencion as nombreSilaisMx, " +
                    " mx.codUnidadAtencion as codigoUnidadMx, mx.nombreUnidadAtencion as nombreUnidadMx, " +
                    " mx.codMuniUnidadAtencion as codigoMuniMx, mx.nombreMuniUnidadAtencion as nombreMuniMx, " +
                    " mx.idTomaMx as idTomaMx, mx.fechaHTomaMx as fechaTomaMx, mx.codigoLab as codigoMx, mx.codigoUnicoMx as codUnicoMx, mx.codTipoMx.idTipoMx as idTipoMx, mx.codTipoMx.nombre as nombreTipoMx, dx.idSolicitudEstudio as idSolicitud, dx.fechaAprobacion as fechaAprobacion  " +
                    " from DaSolicitudEstudio dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti inner join noti.persona p  " +
                    " where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true " + sqlEstudio + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut));

        }else if (filtro.getCodArea().equals("AREAREP|SILAIS")) {
            queryNotiDx = session.createQuery(" select cast(p.personaId as string) as codigoExpUnico, p.primerNombre as primerNombre, p.segundoNombre as segundoNombre, p.primerApellido as primerApellido, p.segundoApellido as segundoApellido, p.fechaNacimiento as fechaNacimiento, p.codigoSexo as sexo, " +
                    " p.direccionResidencia as direccionResidencia, p.telefonoResidencia as telefonoResidencia, p.telefonoMovil as telefonoMovil, p.nombreComunidadResidencia as comunidadResidencia, " +
                    " noti.idNotificacion as idNotificacion, noti.semanasEmbarazo as semanasEmbarazo, noti.fechaInicioSintomas as fechaInicioSintomas, noti.desUrgente as urgente, noti.desEmbarazada as embarazada, " +
                    " noti.codSilaisAtencion as codigoSilaisNoti, noti.nombreSilaisAtencion as nombreSilaisNoti, " +
                    " noti.codUnidadAtencion as codigoUnidadNoti, noti.nombreUnidadAtencion as nombreUnidadNoti, " +
                    " noti.codMuniUnidadAtencion as codigoMuniNoti, noti.nombreMuniUnidadAtencion as nombreMuniNoti, " +
                    " noti.municipioResidencia as codigoMuniResid, noti.nombreMunicipioResidencia as nombreMuniResid, " + //de la persona
                    " noti.codSilaisResid as codigoSilaisResid, noti.nombreSilaisResid as nombreSilaisResid, " +
                    " mx.codSilaisAtencion as codigoSilaisMx, mx.nombreSilaisAtencion as nombreSilaisMx, " +
                    " mx.codUnidadAtencion as codigoUnidadMx, mx.nombreUnidadAtencion as nombreUnidadMx, " +
                    " mx.codMuniUnidadAtencion as codigoMuniMx, mx.nombreMuniUnidadAtencion as nombreMuniMx, " +
                    " mx.idTomaMx as idTomaMx, mx.fechaHTomaMx as fechaTomaMx, mx.codigoLab as codigoMx, mx.codigoUnicoMx as codUnicoMx, mx.codTipoMx.idTipoMx as idTipoMx, mx.codTipoMx.nombre as nombreTipoMx, dx.idSolicitudEstudio as idSolicitud, dx.fechaAprobacion as fechaAprobacion  " +
                    " from DaSolicitudEstudio dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti inner join noti.persona p " +
                    " where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true " + sqlEstudio + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) +
                    " and noti.idSilaisAtencion =:codSilais ");
            queryNotiDx.setParameter("codSilais", filtro.getCodSilais());

        } else if (filtro.getCodArea().equals("AREAREP|UNI")) {
            queryNotiDx = session.createQuery(" select cast(p.personaId as string) as codigoExpUnico, p.primerNombre as primerNombre, p.segundoNombre as segundoNombre, p.primerApellido as primerApellido, p.segundoApellido as segundoApellido, p.fechaNacimiento as fechaNacimiento, p.codigoSexo as sexo, " +
                    " p.direccionResidencia as direccionResidencia, p.telefonoResidencia as telefonoResidencia, p.telefonoMovil as telefonoMovil, p.nombreComunidadResidencia as comunidadResidencia, " +
                    " noti.idNotificacion as idNotificacion, noti.semanasEmbarazo as semanasEmbarazo, noti.fechaInicioSintomas as fechaInicioSintomas, noti.desUrgente as urgente, noti.desEmbarazada as embarazada, " +
                    " noti.codSilaisAtencion as codigoSilaisNoti, noti.nombreSilaisAtencion as nombreSilaisNoti, " +
                    " noti.codUnidadAtencion as codigoUnidadNoti, noti.nombreUnidadAtencion as nombreUnidadNoti, " +
                    " noti.codMuniUnidadAtencion as codigoMuniNoti, noti.nombreMuniUnidadAtencion as nombreMuniNoti, " +
                    " noti.municipioResidencia as codigoMuniResid, noti.nombreMunicipioResidencia as nombreMuniResid, " + //de la persona
                    " noti.codSilaisResid as codigoSilaisResid, noti.nombreSilaisResid as nombreSilaisResid, " +
                    " mx.codSilaisAtencion as codigoSilaisMx, mx.nombreSilaisAtencion as nombreSilaisMx, " +
                    " mx.codUnidadAtencion as codigoUnidadMx, mx.nombreUnidadAtencion as nombreUnidadMx, " +
                    " mx.codMuniUnidadAtencion as codigoMuniMx, mx.nombreMuniUnidadAtencion as nombreMuniMx, " +
                    " mx.idTomaMx as idTomaMx, mx.fechaHTomaMx as fechaTomaMx, mx.codigoLab as codigoMx, mx.codigoUnicoMx as codUnicoMx, mx.codTipoMx.idTipoMx as idTipoMx, mx.codTipoMx.nombre as nombreTipoMx, dx.idSolicitudEstudio as idSolicitud, dx.fechaAprobacion as fechaAprobacion  " +
                    " from DaSolicitudEstudio dx inner join dx.idTomaMx mx inner join mx.idNotificacion noti inner join noti.persona p " +
                    " where noti.pasivo = false and dx.anulado = false and mx.anulada = false and dx.aprobada = true " + sqlEstudio + (filtro.getConsolidarPor().equalsIgnoreCase("FIS")? sqlFIS : sqlFechasAproRut) +
                    " and noti.idUnidadAtencion =:codUnidad ");
            queryNotiDx.setParameter("codUnidad", filtro.getCodUnidad());
        }

        //queryNotiDx.setParameter("codigoLab", filtro.getCodLaboratio());
        queryNotiDx.setParameter("idDx", filtro.getIdEstudio());
        queryNotiDx.setParameter("fechaInicio", filtro.getFechaInicio());
        queryNotiDx.setParameter("fechaFin", filtro.getFechaFin());

        queryNotiDx.setResultTransformer(Transformers.aliasToBean(ResultadoVigilancia.class));
        return queryNotiDx.list();
    }

}
