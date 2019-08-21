package ni.gob.minsa.laboratorio.web.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ni.gob.minsa.laboratorio.domain.examen.CatalogoExamenes;
import ni.gob.minsa.laboratorio.domain.muestra.*;
import ni.gob.minsa.laboratorio.domain.concepto.Catalogo_Lista;
import ni.gob.minsa.laboratorio.domain.persona.PersonaTmp;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultado;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultadoFinal;
import ni.gob.minsa.laboratorio.domain.resultados.RespuestaExamen;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import ni.gob.minsa.laboratorio.domain.solicitante.Solicitante;
import ni.gob.minsa.laboratorio.restServices.CallRestServices;
import ni.gob.minsa.laboratorio.restServices.entidades.EntidadesAdtvas;
import ni.gob.minsa.laboratorio.service.*;
import ni.gob.minsa.laboratorio.utilities.ConstantsSecurity;
import ni.gob.minsa.laboratorio.utilities.DateUtil;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FIRSTICT on 1/9/2015.
 */
@Controller
@RequestMapping("resultados")
public class ResultadosController {
    private static final Logger logger = LoggerFactory.getLogger(RecepcionMxController.class);
    @Autowired
    @Qualifier(value = "seguridadService")
    private SeguridadService seguridadService;

    @Autowired
    @Qualifier(value = "recepcionMxService")
    private RecepcionMxService recepcionMxService;

    @Autowired
    @Qualifier(value = "catalogosService")
    private CatalogoService catalogosService;

    @Autowired
    @Qualifier(value = "tomaMxService")
    private TomaMxService tomaMxService;

    @Autowired
    @Qualifier(value = "ordenExamenMxService")
    private OrdenExamenMxService ordenExamenMxService;

    @Autowired
    @Qualifier(value = "respuestasExamenService")
    private RespuestasExamenService respuestasExamenService;

    @Autowired
    @Qualifier(value = "resultadosService")
    private ResultadosService resultadosService;

    @Resource(name = "examenesService")
    private ExamenesService examenesService;

    @Autowired
    @Resource(name = "resultadoFinalService")
    private ResultadoFinalService resultadoFinalService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    ServletContext servletContext;

    @RequestMapping(value = "init", method = RequestMethod.GET)
    public ModelAndView initSearchForm(HttpServletRequest request) throws Exception {
        logger.debug("buscar ordenes para recepcion");
        String urlValidacion="";
        try {
            urlValidacion = seguridadService.validarLogin(request);
            //si la url esta vacia significa que la validaci�n del login fue exitosa
            if (urlValidacion.isEmpty())
                urlValidacion = seguridadService.validarAutorizacionUsuario(request, ConstantsSecurity.SYSTEM_CODE, false);
        }catch (Exception e){
            e.printStackTrace();
            urlValidacion = "404";
        }
        ModelAndView mav = new ModelAndView();
        if (urlValidacion.isEmpty()) {
            List<EntidadesAdtvas> entidadesAdtvases =  CallRestServices.getEntidadesAdtvas();
            List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
            mav.addObject("entidades",entidadesAdtvases);
            mav.addObject("tipoMuestra", tipoMxList);
            mav.setViewName("resultados/searchOrders");
        }else
            mav.setViewName(urlValidacion);

        return mav;
    }

    @RequestMapping(value = "create/{strIdOrdenExamen}", method = RequestMethod.GET)
    public ModelAndView createReceiptForm(HttpServletRequest request, @PathVariable("strIdOrdenExamen")  String strIdOrdenExamen) throws Exception {
        logger.debug("buscar ordenes para recepcion");
        String urlValidacion="";
        try {
            urlValidacion = seguridadService.validarLogin(request);
            //si la url esta vacia significa que la validaci�n del login fue exitosa
            if (urlValidacion.isEmpty())
                urlValidacion = seguridadService.validarAutorizacionUsuario(request, ConstantsSecurity.SYSTEM_CODE, false);
        }catch (Exception e){
            e.printStackTrace();
            urlValidacion = "404";
        }
        ModelAndView mav = new ModelAndView();
        if (urlValidacion.isEmpty()) {
            OrdenExamen ordenExamen = ordenExamenMxService.getOrdenExamenById(strIdOrdenExamen);
            List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
            Date fechaInicioSintomas = null;
            String idTomaMx = "";
            String idSolicitud = "";
            boolean solicitarResFinal = false;
            if (ordenExamen.getSolicitudDx()!=null) {
                fechaInicioSintomas = ordenExamen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getFechaInicioSintomas();
                idTomaMx = ordenExamen.getSolicitudDx().getIdTomaMx().getIdTomaMx();
                idSolicitud = ordenExamen.getSolicitudDx().getIdSolicitudDx();
            }else {
                fechaInicioSintomas = ordenExamen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getFechaInicioSintomas();
                idTomaMx = ordenExamen.getSolicitudEstudio().getIdTomaMx().getIdTomaMx();
                idSolicitud = ordenExamen.getSolicitudEstudio().getIdSolicitudEstudio();
            }
            //s�lo si a�n no tiene resultado final
            if (resultadoFinalService.getDetResActivosBySolicitud(idSolicitud).size()<=0) {
                DaSolicitudEstudio soliE = tomaMxService.getSolicitudEstByIdSolicitud(idSolicitud);
                if (soliE != null) {//si es estudio
                    String codMx = soliE.getIdTomaMx().getCodigoUnicoMx();
                    solicitarResFinal = codMx.contains(".") && codMx.substring(codMx.lastIndexOf(".")+1, codMx.length()).equals("1") && //es muestra inicial
                            ordenExamenMxService.getOrdenesExamenNoAnuladasByIdSolicitud(idSolicitud).size() == 1 //s�lo es un un examen
                            && resultadosService.getDetallesResultadoActivosByExamen(ordenExamen.getIdOrdenExamen()).size() <= 0; //y no tiene resultados el examen
                } else {

                    //solicitarResFinal = ordenExamenMxService.getOrdenesExamenNoAnuladasByIdMx(idTomaMx).size() == 1 //s�lo es un un examen
                    solicitarResFinal = ordenExamenMxService.getOrdenesExamenNoAnuladasByIdSolicitud(idSolicitud).size() == 1 //s�lo es un un examen
                            && resultadosService.getDetallesResultadoActivosByExamen(ordenExamen.getIdOrdenExamen()).size() <= 0; //y no tiene resultados el examen
                }
            }
            //ABRIL2019
            PersonaTmp persona;
            Solicitante solicitante;
            if (ordenExamen.getSolicitudDx()!=null){
                persona = ordenExamen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona();
                solicitante = ordenExamen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getSolicitante();
            }else{
                persona = ordenExamen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona();
                solicitante = ordenExamen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getSolicitante();
            }

            mav.addObject("ordenExamen", ordenExamen);
            mav.addObject("tipoMuestra", tipoMxList);
            mav.addObject("fechaInicioSintomas",fechaInicioSintomas);
            mav.addObject("solicitarResFinal",solicitarResFinal);
            mav.addObject("persona", persona);
            mav.addObject("solicitante", solicitante);
            mav.setViewName("resultados/incomeResult");
        }else
            mav.setViewName(urlValidacion);

        return mav;
    }

    @RequestMapping(value = "searchOrders", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody  String fetchOrdersJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception{
        logger.info("Obteniendo las ordenes de examen pendientes seg�n filtros en JSON");
        FiltroMx filtroMx = jsonToFiltroMx(filtro);
        logger.info("antes searchOrders dx");
        List<OrdenExamen> ordenExamenList = ordenExamenMxService.getOrdenesExamenDxByFiltro(filtroMx);
        logger.info("despu�s searchOrders dx");
        ordenExamenList.addAll(ordenExamenMxService.getOrdenesExamenEstudioByFiltro(filtroMx));
        return ordenesExamenToJson(ordenExamenList);
    }

    private FiltroMx jsonToFiltroMx(String strJson) throws Exception {
        JsonObject jObjectFiltro = new Gson().fromJson(strJson, JsonObject.class);
        FiltroMx filtroMx = new FiltroMx();
        String nombreApellido = null;
        Date fechaInicioTomaMx = null;
        Date fechaFinTomaMx = null;
        Date fechaInicioRecep = null;
        Date fechaFinRecep = null;
        String codSilais = null;
        String codUnidadSalud = null;
        String codTipoMx = null;
        String codigoUnicoMx = null;
        String codTipoSolicitud = null;
        String nombreSolicitud = null;
        String examenResultado = null;

        if (jObjectFiltro.get("nombreApellido") != null && !jObjectFiltro.get("nombreApellido").getAsString().isEmpty())
            nombreApellido = jObjectFiltro.get("nombreApellido").getAsString();
        if (jObjectFiltro.get("fechaInicioTomaMx") != null && !jObjectFiltro.get("fechaInicioTomaMx").getAsString().isEmpty())
            fechaInicioTomaMx = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioTomaMx").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFinTomaMx") != null && !jObjectFiltro.get("fechaFinTomaMx").getAsString().isEmpty())
            fechaFinTomaMx = DateUtil.StringToDate(jObjectFiltro.get("fechaFinTomaMx").getAsString()+" 23:59:59");
        if (jObjectFiltro.get("fechaInicioRecep") != null && !jObjectFiltro.get("fechaInicioRecep").getAsString().isEmpty())
            fechaInicioRecep = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioRecep").getAsString()+" 00:00:00");
        if (jObjectFiltro.get("fechaFinRecepcion") != null && !jObjectFiltro.get("fechaFinRecepcion").getAsString().isEmpty())
            fechaFinRecep =DateUtil. StringToDate(jObjectFiltro.get("fechaFinRecepcion").getAsString()+" 23:59:59");
        if (jObjectFiltro.get("codSilais") != null && !jObjectFiltro.get("codSilais").getAsString().isEmpty())
            codSilais = jObjectFiltro.get("codSilais").getAsString();
        if (jObjectFiltro.get("codUnidadSalud") != null && !jObjectFiltro.get("codUnidadSalud").getAsString().isEmpty())
            codUnidadSalud = jObjectFiltro.get("codUnidadSalud").getAsString();
        if (jObjectFiltro.get("codTipoMx") != null && !jObjectFiltro.get("codTipoMx").getAsString().isEmpty())
            codTipoMx = jObjectFiltro.get("codTipoMx").getAsString();
        if (jObjectFiltro.get("codigoUnicoMx") != null && !jObjectFiltro.get("codigoUnicoMx").getAsString().isEmpty())
            codigoUnicoMx = jObjectFiltro.get("codigoUnicoMx").getAsString();
        if (jObjectFiltro.get("codTipoSolicitud") != null && !jObjectFiltro.get("codTipoSolicitud").getAsString().isEmpty())
            codTipoSolicitud = jObjectFiltro.get("codTipoSolicitud").getAsString();
        if (jObjectFiltro.get("nombreSolicitud") != null && !jObjectFiltro.get("nombreSolicitud").getAsString().isEmpty())
            nombreSolicitud = jObjectFiltro.get("nombreSolicitud").getAsString();
        if (jObjectFiltro.get("examenResultado") != null && !jObjectFiltro.get("examenResultado").getAsString().isEmpty())
            examenResultado = jObjectFiltro.get("examenResultado").getAsString();


        filtroMx.setCodSilais(codSilais);
        filtroMx.setCodUnidadSalud(codUnidadSalud);
        filtroMx.setFechaInicioTomaMx(fechaInicioTomaMx);
        filtroMx.setFechaFinTomaMx(fechaFinTomaMx);
        filtroMx.setFechaInicioRecep(fechaInicioRecep);
        filtroMx.setFechaFinRecep(fechaFinRecep);
        filtroMx.setNombreApellido(nombreApellido);
        filtroMx.setCodTipoMx(codTipoMx);
        filtroMx.setCodTipoSolicitud(codTipoSolicitud);
        filtroMx.setNombreSolicitud(nombreSolicitud);
        filtroMx.setCodEstado("ESTDMX|RCLAB"); // s�lo las recepcionadas en laboratorio
        filtroMx.setCodigoUnicoMx(codigoUnicoMx);
        filtroMx.setResultado(examenResultado);
        filtroMx.setNombreUsuario(seguridadService.obtenerNombreUsuario());
        filtroMx.setIncluirTraslados(true);
        return filtroMx;
    }

    private String ordenesExamenToJson(List<OrdenExamen> ordenesExamen){
        String jsonResponse="";
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice=0;
        Laboratorio labUser = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        for(OrdenExamen orden : ordenesExamen){
            Map<String, String> map = new HashMap<String, String>();
            map.put("idOrdenExamen", orden.getIdOrdenExamen());
            map.put("examen", orden.getCodExamen().getNombre());
            map.put("fechaHoraOrden", DateUtil.DateToString(orden.getFechaHOrden(), "dd/MM/yyyy hh:mm:ss a"));
            boolean agregar = false;
            if (orden.getSolicitudDx()!=null) {
                if (recepcionMxService.validarMuestraRecepcionadaAreaLab(
                        orden.getSolicitudDx().getIdTomaMx().getIdTomaMx(),
                        labUser.getCodigo(),
                        orden.getCodExamen().getArea().getIdArea())) {
                    agregar = true;
                    map.put("idTomaMx", orden.getSolicitudDx().getIdTomaMx().getIdTomaMx());
                    map.put("codigoUnicoMx", orden.getSolicitudDx().getIdTomaMx().getCodigoLab());
                    map.put("fechaHoraDx", DateUtil.DateToString(orden.getSolicitudDx().getFechaHSolicitud(), "dd/MM/yyyy hh:mm:ss a"));
                    map.put("tipoDx", orden.getSolicitudDx().getCodDx().getNombre());
                    map.put("fechaTomaMx", DateUtil.DateToString(orden.getSolicitudDx().getIdTomaMx().getFechaHTomaMx(), "dd/MM/yyyy") +
                            (orden.getSolicitudDx().getIdTomaMx().getHoraTomaMx() != null ? " " + orden.getSolicitudDx().getIdTomaMx().getHoraTomaMx() : ""));
                    if (orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                        map.put("codSilais", orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                    }
                    if (orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                        map.put("codUnidadSalud", orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                    } else {
                        map.put("codUnidadSalud", "");
                    }
                    map.put("tipoMuestra", orden.getSolicitudDx().getIdTomaMx().getCodTipoMx().getNombre());
                    //Si hay fecha de inicio de sintomas se muestra
                    Date fechaInicioSintomas = orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getFechaInicioSintomas();
                    if (fechaInicioSintomas != null)
                        map.put("fechaInicioSintomas", DateUtil.DateToString(fechaInicioSintomas, "dd/MM/yyyy"));
                    else
                        map.put("fechaInicioSintomas", " ");
                    //Si hay persona
                    if (orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona() != null) {
                        /// se obtiene el nombre de la persona asociada a la ficha
                        String nombreCompleto = "";
                        nombreCompleto = orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                        if (orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                            nombreCompleto = nombreCompleto + " " + orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                        nombreCompleto = nombreCompleto + " " + orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                        if (orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                            nombreCompleto = nombreCompleto + " " + orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                        map.put("persona", nombreCompleto);
                    } else if (orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getSolicitante() != null) {
                        map.put("persona", orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getSolicitante().getNombre());
                    } else if (orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                        map.put("persona", orden.getSolicitudDx().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                    } else {
                        map.put("persona", " ");
                    }

                    if (!resultadosService.getDetallesResultadoActivosByExamen(orden.getIdOrdenExamen()).isEmpty()) {
                        map.put("resultadoExamen", "Si");
                    } else {
                        map.put("resultadoExamen", "No");
                    }
                }
            }
            else{
                map.put("idTomaMx", orden.getSolicitudEstudio().getIdTomaMx().getIdTomaMx());
                map.put("codigoUnicoMx", orden.getSolicitudEstudio().getIdTomaMx().getCodigoUnicoMx());
                map.put("fechaHoraDx", DateUtil.DateToString(orden.getSolicitudEstudio().getFechaHSolicitud(), "dd/MM/yyyy hh:mm:ss a"));
                map.put("tipoDx", orden.getSolicitudEstudio().getTipoEstudio().getNombre());
              //  map.put("fechaTomaMx", DateUtil.DateToString(orden.getSolicitudEstudio().getIdTomaMx().getFechaHTomaMx(), "dd/MM/yyyy hh:mm:ss a"));
               // map.put("codSilais", orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getCodSilaisAtencion().getNombre());
                map.put("codUnidadSalud", orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
              //  map.put("tipoMuestra", orden.getSolicitudEstudio().getIdTomaMx().getCodTipoMx().getNombre());
                //Si hay fecha de inicio de sintomas se muestra
                Date fechaInicioSintomas = orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getFechaInicioSintomas();
                if (fechaInicioSintomas != null)
                    map.put("fechaInicioSintomas", DateUtil.DateToString(fechaInicioSintomas, "dd/MM/yyyy"));
                else
                    map.put("fechaInicioSintomas", " ");
                //Si hay persona
                if (orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona() != null) {
                    /// se obtiene el nombre de la persona asociada a la ficha
                    String nombreCompleto = "";
                    nombreCompleto = orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                    if (orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                        nombreCompleto = nombreCompleto + " " + orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                    nombreCompleto = nombreCompleto + " " + orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                    if (orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                        nombreCompleto = nombreCompleto + " " + orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                    map.put("persona", nombreCompleto);
                } else if (orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                	map.put("persona", orden.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                }
                else {
                    map.put("persona", " ");
                }

                if(!resultadosService.getDetallesResultadoActivosByExamen(orden.getIdOrdenExamen()).isEmpty()){
                    map.put("resultadoExamen", "Si");
                }else{
                    map.put("resultadoExamen", "No");
                }
            }
            if (agregar) {
                mapResponse.put(indice, map);
                indice++;
            }
        }
        jsonResponse = new Gson().toJson(mapResponse);
        //escapar caracteres especiales, escape de los caracteres con valor num�rico mayor a 127
        UnicodeEscaper escaper     = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    @RequestMapping(value = "printBC/{strBarCodes}", method = RequestMethod.GET)
    //public String openFile(@RequestParam(value = "path", required = true) String path, @RequestParam(value="objectName", required = true) String objectName,@RequestParam(value="objectType", required = true) String objectType, HttpServletRequest request) {
    public ModelAndView openFile(HttpServletRequest request,@PathVariable("strBarCodes")  String strBarCodes) {
        logger.debug("buscar ordenes para recepcion");
        String urlValidacion="";
        try {
            urlValidacion = seguridadService.validarLogin(request);
            //si la url esta vacia significa que la validaci�n del login fue exitosa
            if (urlValidacion.isEmpty())
                urlValidacion = seguridadService.validarAutorizacionUsuario(request, ConstantsSecurity.SYSTEM_CODE, false);
        }catch (Exception e){
            e.printStackTrace();
            urlValidacion = "404";
        }
        ModelAndView mav = new ModelAndView();
        if (urlValidacion.isEmpty()) {
            strBarCodes = strBarCodes.replaceAll("\\*",".");
            mav.addObject("strBarCodes",strBarCodes);
            mav.setViewName("impresion/print");
        }else
            mav.setViewName(urlValidacion);

        return mav;
    }

    @RequestMapping(value = "getCatalogosListaConceptoByIdExamen", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<Catalogo_Lista> getCatalogoListaConceptoByIdExamen(@RequestParam(value = "idExamen", required = true) String idExamen) throws Exception {
        logger.info("Obteniendo los valores para los conceptos tipo lista asociados a las respueta del examen");
        return respuestasExamenService.getCatalogoListaConceptoByIdExamen(Integer.valueOf(idExamen));
    }

    @RequestMapping(value = "getDetallesResultadoByExamen", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<DetalleResultado> getDetallesResultadoByExamen(@RequestParam(value = "idOrdenExamen", required = true) String idOrdenExamen) throws Exception {
        logger.info("Se obtienen los detalles de resultados activos para la orden");
        return resultadosService.getDetallesResultadoActivosByExamen(idOrdenExamen);
    }

    @RequestMapping(value = "saveResult", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    protected void saveResult(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json;
        String resultado = "";
        String strRespuestas="";
        String idOrdenExamen="";
        Integer cantRespuestas=0;
        boolean examenAgregado= false;
        String esResFinal="";
        String fechaProc ="";
        String horaProc = "";
        Date fechaRegistro = new Date();
        Date fechaHoraPrc = fechaRegistro;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF8"));
            json = br.readLine();
            //Recuperando Json enviado desde el cliente
            JsonObject jsonpObject = new Gson().fromJson(json, JsonObject.class);
            strRespuestas = jsonpObject.get("strRespuestas").toString();
            idOrdenExamen = jsonpObject.get("idOrdenExamen").getAsString();
            cantRespuestas = jsonpObject.get("cantRespuestas").getAsInt();
            esResFinal = jsonpObject.get("esResFinal").getAsString();
            if (jsonpObject.get("fechaProc") != null && !jsonpObject.get("fechaProc").getAsString().isEmpty())
                fechaProc = jsonpObject.get("fechaProc").getAsString();
            if (jsonpObject.get("horaProc") != null && !jsonpObject.get("horaProc").getAsString().isEmpty())
                horaProc = jsonpObject.get("horaProc").getAsString();

            if (!fechaProc.isEmpty()){
                if (horaProc.isEmpty())
                    fechaHoraPrc = DateUtil.StringToDate(fechaProc, "dd/MM/yyyy");
                else
                    fechaHoraPrc = DateUtil.StringToDate(fechaProc+ " "+horaProc, "dd/MM/yyyy hh:mm a");
            }

            OrdenExamen ordenExamen = ordenExamenMxService.getOrdenExamenById(idOrdenExamen);
            long idUsuario = seguridadService.obtenerIdUsuario(request);
            //Usuarios usuario = usuarioService.getUsuarioById((int) idUsuario);
            User usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
            //se obtiene datos de los conceptos a registrar

            JsonObject jObjectRespuestas = new Gson().fromJson(strRespuestas, JsonObject.class);
            for(int i = 0; i< cantRespuestas;i++) {
                String respuesta = jObjectRespuestas.get(String.valueOf(i)).toString();
                JsonObject jsRespuestaObject = new Gson().fromJson(respuesta, JsonObject.class);
                Integer idRespuesta = jsRespuestaObject.get("idRespuesta").getAsInt();
                RespuestaExamen conceptoTmp = respuestasExamenService.getRespuestaById(idRespuesta);
                String valor = jsRespuestaObject.get("valor").getAsString();
                DetalleResultado detalleResultado = new DetalleResultado();
                detalleResultado.setFechahProcesa(new Timestamp(fechaHoraPrc.getTime()));
                detalleResultado.setFechahoraRegistro(new Timestamp(fechaRegistro.getTime()));
                detalleResultado.setValor(valor);
                detalleResultado.setRespuesta(conceptoTmp);
                detalleResultado.setExamen(ordenExamen);
                detalleResultado.setUsuarioRegistro(usuario);

                DetalleResultado resultadoRegistrado = resultadosService.getDetalleResultadoByOrdenExamanAndRespuesta(idOrdenExamen,idRespuesta);
                if (resultadoRegistrado!=null){
                    detalleResultado.setIdDetalle(resultadoRegistrado.getIdDetalle());
                    resultadosService.updateDetalleResultado(detalleResultado);
                  if(addTestVA(request, ordenExamen) ){
                      examenAgregado = true;
                  }
                    guardarResultadoFinal(detalleResultado,ordenExamen,true);
                }else {
                    if (detalleResultado.getValor() != null && !detalleResultado.getValor().isEmpty()) {
                        resultadosService.addDetalleResultado(detalleResultado);

                        //save final result in dengue IGM o PCR, s�lo para rutinas
                        if(ordenExamen.getSolicitudDx()!=null && (ordenExamen.getCodExamen().getNombre().equals("Dengue ELISA IgM") || ordenExamen.getCodExamen().getNombre().equals("Dengue PCR"))){
                            boolean procesado = saveFinalResultToDengue(detalleResultado, ordenExamen, usuario);
                            if (procesado) esResFinal = "NO";

                        }

                        if(addTestVA(request, ordenExamen)){
                            examenAgregado = true;
                        }
                        if(esResFinal.equalsIgnoreCase("SI")){
                            guardarResultadoFinal(detalleResultado,ordenExamen,false);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            ex.printStackTrace();
            resultado =  messageSource.getMessage("msg.result.error.added",null,null);
            resultado=resultado+". \n "+ex.getMessage();

        }finally {
            Map<String, String> map = new HashMap<String, String>();
            map.put("idOrdenExamen",idOrdenExamen);
            map.put("strRespuestas",strRespuestas);
            map.put("mensaje",resultado);
            map.put("examenAgregado", String.valueOf(examenAgregado));
            map.put("cantRespuestas",cantRespuestas.toString());
            map.put("esResFinal",esResFinal);
            String jsonResponse = new Gson().toJson(map);
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        }
    }

    private boolean saveFinalResultToDengue(DetalleResultado detalleResultado, OrdenExamen orden, User usuario) throws Exception {
        boolean procesado = false;
        try {
            //search amount of orders exams
            List<OrdenExamen> ordenes = ordenExamenMxService.getOrdenesExamenNoAnuladasByIdSolicitud(orden.getSolicitudDx().getIdSolicitudDx());
            //in one order case
            if (ordenes.size() == 1) {
                //search response value
                if (detalleResultado != null) {
                    //in positive case

                    if (detalleResultado.getRespuesta() != null) {
                        if (detalleResultado.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                            Integer idLista = Integer.valueOf(detalleResultado.getValor());
                            Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                            //in positive case
                            if (valor.getValor().toLowerCase().trim().equals("positivo")) {
                                //save final Result
                                guardarResultadoFinal(detalleResultado, orden, false);
                                procesado=true;
                            }
                            //in positive case
                        } else if (detalleResultado.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                            if (detalleResultado.getValor().toLowerCase().equals("positivo")) {
                                //save final Result
                                guardarResultadoFinal(detalleResultado, orden, false);
                                procesado=true;
                            }
                        }
                    }
                }

                //in two order case
            } else if (ordenes.size() == 2) {
                //search detail result for the different order to the order to save
                for (OrdenExamen ord : ordenes) {

                        if (orden.getCodExamen().getNombre().equals("Dengue ELISA IgM") || orden.getCodExamen().getNombre().equals("Dengue PCR")) {

                            List<DetalleResultado> det = resultadosService.getDetallesResultadoActivosByExamen(ord.getIdOrdenExamen());

                            for (DetalleResultado de : det) {

                                //setting values
                                DetalleResultado deta = new DetalleResultado();
                                deta.setValor(de.getValor());
                                deta.setRespuesta(de.getRespuesta());
                                deta.setUsuarioRegistro(usuario);

                                if (de.getRespuesta() != null) {
                                    if (de.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                                        Integer idLista = Integer.valueOf(de.getValor());
                                        Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                                        //in positive case
                                        if (valor.getValor().toLowerCase().equals("positivo")) {
                                            guardarResultadoFinal(deta, ord, false);
                                            procesado = true;
                                        }
                                        //in positive case
                                    } else if (de.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                                        if (de.getValor().toLowerCase().equals("positivo")) {
                                            guardarResultadoFinal(deta, ord, false);
                                            procesado = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        } catch (Exception ex) {
            throw new Exception(ex);
        }
        return procesado;
    }

    private void guardarResultadoFinal(DetalleResultado detalleResultado, OrdenExamen ordenExamen, boolean esUpdate) throws Exception {
        try {
            String idSolicitud="";
            DetalleResultadoFinal resultadoFinal = new DetalleResultadoFinal();
            resultadoFinal.setFechahRegistro(new Timestamp(new Date().getTime()));
            resultadoFinal.setValor(detalleResultado.getValor());
            resultadoFinal.setRespuestaExamen(detalleResultado.getRespuesta());
            if (ordenExamen.getSolicitudDx()!=null){
                resultadoFinal.setSolicitudDx(ordenExamen.getSolicitudDx());
                idSolicitud = ordenExamen.getSolicitudDx().getIdSolicitudDx();
            }
            if (ordenExamen.getSolicitudEstudio()!=null) {
                resultadoFinal.setSolicitudEstudio(ordenExamen.getSolicitudEstudio());
                idSolicitud =ordenExamen.getSolicitudEstudio().getIdSolicitudEstudio();
            }
            resultadoFinal.setUsuarioRegistro(detalleResultado.getUsuarioRegistro());
            DetalleResultadoFinal resFinalRegistrado = null;
            //si es un update del detalle resultado se valida si la solicitud tiene la misma respuesta final para actualizarla
            if (esUpdate) resFinalRegistrado = resultadoFinalService.getDetResBySolicitudAndRespuestaExa(idSolicitud, detalleResultado.getRespuesta().getIdRespuesta());
            if (resFinalRegistrado != null) {
                resultadoFinal.setIdDetalle(resFinalRegistrado.getIdDetalle());
                resultadoFinalService.updateDetResFinal(resultadoFinal);
            } else {
                if (resultadoFinal.getValor() != null && !resultadoFinal.getValor().isEmpty() && !esUpdate) {
                    resultadoFinalService.saveDetResFinal(resultadoFinal);
                }
            }
        }catch (Exception ex)
        {
            throw new Exception(ex);
        }
    }

    @RequestMapping(value = "overrideResult", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    protected void overrideResult(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json;
        String resultado = "";
        String idOrdenExamen="";
        String causaAnulacion = "";
        boolean solicitarResFinal = false;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF8"));
            json = br.readLine();
            //Recuperando Json enviado desde el cliente
            JsonObject jsonpObject = new Gson().fromJson(json, JsonObject.class);
            idOrdenExamen = jsonpObject.get("idOrdenExamen").getAsString();
            causaAnulacion = jsonpObject.get("causaAnulacion").getAsString();
            OrdenExamen ordenExamen = ordenExamenMxService.getOrdenExamenById(idOrdenExamen);

            User usuario =seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());

            String idTomaMx = "";
            String idSolicitud = "";
            if (ordenExamen.getSolicitudDx()!=null) {
                idTomaMx = ordenExamen.getSolicitudDx().getIdTomaMx().getIdTomaMx();
                idSolicitud = ordenExamen.getSolicitudDx().getIdSolicitudDx();
            }else {
                idTomaMx = ordenExamen.getSolicitudEstudio().getIdTomaMx().getIdTomaMx();
                idSolicitud = ordenExamen.getSolicitudEstudio().getIdSolicitudEstudio();
            }

            //se obtiene datos de los conceptos a registrar
            List<DetalleResultado> detalleResultadosAct = resultadosService.getDetallesResultadoActivosByExamen(idOrdenExamen);
            for(DetalleResultado detalleResultado : detalleResultadosAct) {
                detalleResultado.setFechahAnulacion(new Timestamp(new Date().getTime()));
                detalleResultado.setExamen(ordenExamen);
                detalleResultado.setUsuarioAnulacion(usuario);
                detalleResultado.setRazonAnulacion(causaAnulacion);
                detalleResultado.setPasivo(true);
                resultadosService.updateDetalleResultado(detalleResultado);
                //se valida si la solicitud tiene la misma respuesta final para anularla
                DetalleResultadoFinal resFinalRegistrado = resultadoFinalService.getDetResBySolicitudAndRespuestaExa(idSolicitud, detalleResultado.getRespuesta().getIdRespuesta());
                if (resFinalRegistrado != null) {
                    resFinalRegistrado.setRazonAnulacion(detalleResultado.getRazonAnulacion());
                    resFinalRegistrado.setFechahAnulacion(detalleResultado.getFechahAnulacion());
                    resFinalRegistrado.setUsuarioAnulacion(detalleResultado.getUsuarioAnulacion());
                    resFinalRegistrado.setPasivo(true);
                    resultadoFinalService.updateDetResFinal(resFinalRegistrado);
                }
            }

            //se determina si luego de anular la respuesta del examen, se debe solicitar resultado final al ingresar nuevo resultado
            //s�lo si a�n no tiene resultado final
            if (resultadoFinalService.getDetResActivosBySolicitud(idSolicitud).size()<=0) {
                DaSolicitudEstudio soliE = tomaMxService.getSolicitudEstByIdSolicitud(idSolicitud);
                if (soliE!=null){//si es estudio
                    String codMx = soliE.getIdTomaMx().getCodigoUnicoMx();
                    solicitarResFinal  = codMx.contains(".") && codMx.substring(codMx.lastIndexOf(".")+1,codMx.length()).equals("1") && //es muestra inicial
                        ordenExamenMxService.getOrdenesExamenNoAnuladasByIdSolicitud(idSolicitud).size() == 1 //s�lo es un un examen
                                && resultadosService.getDetallesResultadoActivosByExamen(ordenExamen.getIdOrdenExamen()).size() <= 0; //y no tiene resultados el examen
                }else {
                    solicitarResFinal = ordenExamenMxService.getOrdenesExamenNoAnuladasByIdSolicitud(idSolicitud).size() == 1 //s�lo es un un examen
                            && resultadosService.getDetallesResultadoActivosByExamen(ordenExamen.getIdOrdenExamen()).size() <= 0; //y no tiene resultados el examen
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            ex.printStackTrace();
            resultado =  messageSource.getMessage("msg.result.error.canceled",null,null);
            resultado=resultado+". \n "+ex.getMessage();

        }finally {
            Map<String, String> map = new HashMap<String, String>();
            map.put("idOrdenExamen",idOrdenExamen);
            map.put("causaAnulacion",causaAnulacion);
            map.put("mensaje",resultado);
            map.put("solicitarResFinal",String.valueOf(solicitarResFinal));
            String jsonResponse = new Gson().toJson(map);
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        }
    }

    protected boolean addTestVA(HttpServletRequest request, OrdenExamen orden) throws Exception {
        boolean examenAgregado = false;
        String gravedad = null;

        //si es un estudio
        if (orden.getSolicitudEstudio() != null) {
            //Comprobar si el examen es de Estudio de Cohorte
            if (orden.getSolicitudEstudio().getTipoEstudio().getIdEstudio().equals(1)) {
                //si es categoria aguda
                String codigo = orden.getSolicitudEstudio().getIdTomaMx().getCodigoUnicoMx();
                if (codigo.contains("."))
                    gravedad = codigo.substring(codigo.lastIndexOf(".") + 1);
                if (gravedad != null) {
                    if (gravedad.equals("1")) {
                        //si es dengue PCR
                        if (orden.getCodExamen().getNombre().equals("Dengue PCR")) {
                            //validar q no exista la orden de examen de Aislamiento Viral de Dengue
                            if(ordenExamenMxService.getOrdExamenNoAnulByIdMxIdEstIdExamen(orden.getSolicitudEstudio().getIdTomaMx().getIdTomaMx(), orden.getSolicitudEstudio().getTipoEstudio().getIdEstudio(), 239).size() <= 0){
                                //Buscar los resultados por el idOrdenExamen
                                List<DetalleResultado> resultado = resultadosService.getDetallesResultadoActivosByExamen(orden.getIdOrdenExamen());
                                long idUsuario = seguridadService.obtenerIdUsuario(request);
                                //Usuarios usuario = usuarioService.getUsuarioById((int) idUsuario);
                                User usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
                                Laboratorio labUsuario = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
                                for (DetalleResultado res : resultado) {
                                    Integer dias = DateUtil.CalcularDiferenciaDiasFechas(orden.getSolicitudEstudio().getIdTomaMx().getFechaHTomaMx(), new Date());
                                    //en caso de ser la respuesta tipo texto buscar texto positivo
                                    if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                                        if (res.getValor().toLowerCase().equals("positivo")) {
                                            //si la mx es menor a tres dias realizar examen aislamiento viral
                                            if (dias <= 3) {
                                                //agregar el examen aislamiento viral
                                                OrdenExamen ordenExamen = new OrdenExamen();
                                                ordenExamen.setSolicitudEstudio(orden.getSolicitudEstudio());
                                                CatalogoExamenes examen = examenesService.getExamenById(239);
                                                ordenExamen.setCodExamen(examen);
                                                ordenExamen.setFechaHOrden(new Timestamp(new Date().getTime()));
                                                ordenExamen.setUsuarioRegistro(usuario);
                                                ordenExamen.setLabProcesa(labUsuario);
                                                try {
                                                    ordenExamenMxService.addOrdenExamen(ordenExamen);
                                                    examenAgregado = true;
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                    logger.error("Error al agregar orden de examen", ex);
                                                }
                                            }
                                        }

                                    } else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                                        Integer idLista = Integer.valueOf(res.getValor());
                                        Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                                        if (valor.getValor().toLowerCase().equals("positivo")) {
                                            if (dias <= 3) {
                                                //agregar examen aislamiento viral
                                                OrdenExamen ordenExamen = new OrdenExamen();
                                                ordenExamen.setSolicitudEstudio(orden.getSolicitudEstudio());
                                                CatalogoExamenes examen = examenesService.getExamenById(239);
                                                ordenExamen.setCodExamen(examen);
                                                ordenExamen.setFechaHOrden(new Timestamp(new Date().getTime()));
                                                ordenExamen.setUsuarioRegistro(usuario);
                                                ordenExamen.setLabProcesa(labUsuario);
                                                try {
                                                    ordenExamenMxService.addOrdenExamen(ordenExamen);
                                                    examenAgregado = true;
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                    logger.error("Error al agregar orden de examen", ex);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return examenAgregado;
    }
}
