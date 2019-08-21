package ni.gob.minsa.laboratorio.web.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ni.gob.minsa.laboratorio.domain.muestra.*;
import ni.gob.minsa.laboratorio.domain.muestra.traslado.TrasladoMx;
import ni.gob.minsa.laboratorio.domain.concepto.Catalogo_Lista;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultado;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultadoFinal;
import ni.gob.minsa.laboratorio.restServices.CallRestServices;
import ni.gob.minsa.laboratorio.restServices.constantes.CatalogConstants;
import ni.gob.minsa.laboratorio.restServices.entidades.Catalogo;
import ni.gob.minsa.laboratorio.restServices.entidades.EntidadesAdtvas;
import ni.gob.minsa.laboratorio.service.*;
import ni.gob.minsa.laboratorio.utilities.ConstantsSecurity;
import ni.gob.minsa.laboratorio.utilities.DateUtil;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.BaseTable;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.Cell;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.GeneralUtils;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.Row;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by souyen-ics.
 */
@Controller
@RequestMapping("searchMx")
public class SearchMxController {

    private static final Logger logger = LoggerFactory.getLogger(SearchMxController.class);

    @Autowired
    @Qualifier(value = "seguridadService")
    private SeguridadService seguridadService;

    @Autowired
    @Qualifier(value = "catalogosService")
    private CatalogoService catalogosService;

    @Autowired
    @Qualifier(value = "recepcionMxService")
    private RecepcionMxService recepcionMxService;

    @Autowired
    @Qualifier(value = "tomaMxService")
    private TomaMxService tomaMxService;

    @Autowired
    @Qualifier(value = "ordenExamenMxService")
    private OrdenExamenMxService ordenExamenMxService;

    @Autowired
    @Qualifier(value = "resultadoFinalService")
    private ResultadoFinalService resultadoFinalService;

    @Autowired
    @Qualifier(value = "resultadosService")
    private ResultadosService resultadosService;

    @Autowired
    @Qualifier(value = "conceptoService")
    private ConceptoService conceptoService;

    @Autowired
    @Qualifier(value = "trasladosService")
    private TrasladosService trasladosService;

    @Autowired
    MessageSource messageSource;

    String nombreSoli = null;
    String nombrePersona = null;
    String nombreSilais = null;
    String nombreUS = null;
    Integer edad = null;
    String sexo = null;
    String fechaRecepcion = null;
    String fechaToma = null;
    String fis = null;
    String fechaResultado = null;
    String fechaAprobacion = null;
    List<DetalleResultadoFinal> detalleResultado = null;
    String fechaImpresion = null;
    String orderSample = null;


    /**
     * M�todo que se llama al entrar a la opci�n de menu "Recepci�n Mx Vigilancia". Se encarga de inicializar las listas para realizar la b�squeda de envios de Mx
     *
     * @param request para obtener informaci�n de la petici�n del cliente
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "init", method = RequestMethod.GET)
    public ModelAndView initSearchForm(HttpServletRequest request) throws Exception {
        logger.debug("Buscar Mx");
        String urlValidacion;
        try {
            urlValidacion = seguridadService.validarLogin(request);
            //si la url esta vacia significa que la validaci�n del login fue exitosa
            if (urlValidacion.isEmpty())
                urlValidacion = seguridadService.validarAutorizacionUsuario(request, ConstantsSecurity.SYSTEM_CODE, false);
        } catch (Exception e) {
            e.printStackTrace();
            urlValidacion = "404";
        }
        ModelAndView mav = new ModelAndView();
        if (urlValidacion.isEmpty()) {
            List<EntidadesAdtvas> entidadesAdtvases = CallRestServices.getEntidadesAdtvas();
            List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
            mav.addObject("entidades", entidadesAdtvases);
            mav.addObject("tipoMuestra", tipoMxList);
            mav.setViewName("recepcionMx/searchMx");
        } else
            mav.setViewName(urlValidacion);

        return mav;
    }

    /**
     * M�todo para realizar la b�squeda de Mx para recepcionar en Mx Vigilancia general
     *
     * @param filtro JSon con los datos de los filtros a aplicar en la b�squeda(Nombre Apellido, Rango Fec Toma Mx, Tipo Mx, SILAIS, unidad salud)
     * @return String con las Mx encontradas
     * @throws Exception
     */
    @RequestMapping(value = "searchMx", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String fetchOrdersJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception {
        logger.info("Obteniendo las mx seg�n filtros en JSON");
        FiltroMx filtroMx = jsonToFiltroMx(filtro);
        List<DaTomaMx> tomaMxList = tomaMxService.getTomaMxByFiltro(filtroMx);
        return tomaMxToJson(tomaMxList);
    }

    /**
     * M�todo para convertir estructura Json que se recibe desde el cliente a FiltroMx para realizar b�squeda de Mx(Vigilancia) y Recepci�n Mx(Laboratorio)
     *
     * @param strJson String con la informaci�n de los filtros
     * @return FiltroMx
     * @throws Exception
     */
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
        Boolean solicitudAprobada = null;

        if (jObjectFiltro.get("nombreApellido") != null && !jObjectFiltro.get("nombreApellido").getAsString().isEmpty())
            nombreApellido = jObjectFiltro.get("nombreApellido").getAsString();
        if (jObjectFiltro.get("fechaInicioTomaMx") != null && !jObjectFiltro.get("fechaInicioTomaMx").getAsString().isEmpty())
            fechaInicioTomaMx = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioTomaMx").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFinTomaMx") != null && !jObjectFiltro.get("fechaFinTomaMx").getAsString().isEmpty())
            fechaFinTomaMx = DateUtil.StringToDate(jObjectFiltro.get("fechaFinTomaMx").getAsString() + " 23:59:59");
        if (jObjectFiltro.get("fechaInicioRecep") != null && !jObjectFiltro.get("fechaInicioRecep").getAsString().isEmpty())
            fechaInicioRecep = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioRecep").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFinRecepcion") != null && !jObjectFiltro.get("fechaFinRecepcion").getAsString().isEmpty())
            fechaFinRecep = DateUtil.StringToDate(jObjectFiltro.get("fechaFinRecepcion").getAsString() + " 23:59:59");
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
        if (jObjectFiltro.get("solicitudAprobada") != null && !jObjectFiltro.get("solicitudAprobada").getAsString().isEmpty())
            solicitudAprobada = jObjectFiltro.get("solicitudAprobada").getAsBoolean();

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
        filtroMx.setCodigoUnicoMx(codigoUnicoMx);
        filtroMx.setNombreUsuario(seguridadService.obtenerNombreUsuario());
        filtroMx.setSolicitudAprobada(solicitudAprobada);
        filtroMx.setIncluirTraslados(true);

        return filtroMx;
    }

    /**
     * M�todo que convierte una lista de tomaMx a un string con estructura Json
     *
     * @param tomaMxList lista con las tomaMx a convertir
     * @return String
     */
    private String tomaMxToJson(List<DaTomaMx> tomaMxList) throws Exception {
        String jsonResponse;
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice = 0;
        boolean esEstudio;
        Laboratorio laboratorioUsuario = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        //ABRIL2019
        List<Catalogo> estadosMx = CallRestServices.getCatalogos(CatalogConstants.EstadoMx);
        List<Catalogo> calidadesMx = CallRestServices.getCatalogos(CatalogConstants.CalidadMx);
        for (DaTomaMx tomaMx : tomaMxList) {
            esEstudio = tomaMxService.getSolicitudesEstudioByIdTomaMx(tomaMx.getIdTomaMx()).size() > 0;
            Map<String, String> map = new HashMap<String, String>();
            RecepcionMx rec = recepcionMxService.getRecepcionMxByCodUnicoMx(tomaMx.getCodigoUnicoMx(), (laboratorioUsuario.getCodigo() != null ? laboratorioUsuario.getCodigo() : ""));
            map.put("idTomaMx", tomaMx.getIdTomaMx());
            map.put("codigoUnicoMx", esEstudio?tomaMx.getCodigoUnicoMx():(tomaMx.getCodigoLab()!=null?tomaMx.getCodigoLab():(messageSource.getMessage("lbl.not.generated", null, null))));
            map.put("fechaTomaMx", DateUtil.DateToString(tomaMx.getFechaHTomaMx(), "dd/MM/yyyy")+
                    (tomaMx.getHoraTomaMx()!=null?" "+tomaMx.getHoraTomaMx():""));

            if (rec != null) {
                if (rec.getCalidadMx() != null) {
                    map.put("calidad", catalogosService.buscarValorCatalogo( calidadesMx, rec.getCalidadMx()));//ABRIL2019
                } else {
                    map.put("calidad", (messageSource.getMessage("lbl.undefined", null, null)));
                }
            } else {
                map.put("calidad", (messageSource.getMessage("lbl.undefined", null, null)));
            }

            if (tomaMx.getIdNotificacion().getCodSilaisAtencion() != null) {
                map.put("codSilais", tomaMx.getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
            } else {
                map.put("codSilais", "");
            }
            if (tomaMx.getIdNotificacion().getCodUnidadAtencion() != null) {
                map.put("codUnidadSalud", tomaMx.getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
            } else {
                map.put("codUnidadSalud", "");
            }

            //laboratorio y area
             if(tomaMx.getEstadoMx().equals("ESTDMX|ENV")){//ABRIL2019
                map.put("area", (messageSource.getMessage("lbl.not.received", null, null)));
                map.put("laboratorio", (messageSource.getMessage("lbl.not.received", null, null)));
            }else{
                //Search transferred assets
                DaSolicitudEstudio estudio = tomaMxService.getSoliEstByCodigo(tomaMx.getCodigoUnicoMx());
                RecepcionMx lastRecepcion = recepcionMxService.getMaxRecepcionMxByCodUnicoMx(tomaMx.getCodigoUnicoMx());
                TrasladoMx traslado = trasladosService.getTrasladoActivoMx(tomaMx.getIdTomaMx());

                if (estudio != null) {
                    map.put("area", estudio.getTipoEstudio().getArea().getNombre());
                    map.put("laboratorio", estudio.getIdTomaMx().getEnvio().getLaboratorioDestino().getNombre());
                } else {
                    //asset transfers
                    if (traslado != null) {
                        //CC
                        if (traslado.isTrasladoExterno()) {
                            List<DaSolicitudDx> soliPriori = tomaMxService.getSoliDxPrioridadByTomaAndLab(tomaMx.getIdTomaMx(), traslado.getLaboratorioDestino().getCodigo());

                            map.put("laboratorio", traslado.getLaboratorioDestino().getNombre());
                            map.put("area", soliPriori.get(0).getCodDx().getArea().getNombre());

                        } else {
                            //Intern
                            Laboratorio lab = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
                            if(lab != null){
                                List<DaSolicitudDx> soli = tomaMxService.getSoliDxPrioridadByTomaAndLab(tomaMx.getIdTomaMx(), lab.getCodigo());

                                if(soli != null){
                                    map.put("area", soli.get(0).getCodDx().getArea().getNombre());
                                }
                            }

                            if (lastRecepcion != null) {
                                map.put("laboratorio", lastRecepcion.getLabRecepcion().getNombre());
                            }
                        }
                    } else {
                        if (lastRecepcion != null) {
                            map.put("laboratorio", lastRecepcion.getLabRecepcion().getNombre());
                        } else {
                            map.put("laboratorio", "");
                        }

                        DaSolicitudDx soli = tomaMxService.getMaxSoliByToma(tomaMx.getIdTomaMx());

                        if (soli != null) {

                            map.put("area", soli.getCodDx().getArea().getNombre());
                        } else {
                            map.put("area", "");
                        }
                    }
                }
            }

            map.put("tipoMuestra", tomaMx.getCodTipoMx().getNombre());
            map.put("estadoMx", catalogosService.buscarValorCatalogo( estadosMx, tomaMx.getEstadoMx()));//ABRIL2019

            //Si hay persona
            if (tomaMx.getIdNotificacion().getPersona() != null) {
                /// se obtiene el nombre de la persona asociada a la ficha
                String nombreCompleto = "";
                nombreCompleto = tomaMx.getIdNotificacion().getPersona().getPrimerNombre();
                if (tomaMx.getIdNotificacion().getPersona().getSegundoNombre() != null)
                    nombreCompleto = nombreCompleto + " " + tomaMx.getIdNotificacion().getPersona().getSegundoNombre();
                nombreCompleto = nombreCompleto + " " + tomaMx.getIdNotificacion().getPersona().getPrimerApellido();
                if (tomaMx.getIdNotificacion().getPersona().getSegundoApellido() != null)
                    nombreCompleto = nombreCompleto + " " + tomaMx.getIdNotificacion().getPersona().getSegundoApellido();
                map.put("persona", nombreCompleto);
            } else if (tomaMx.getIdNotificacion().getSolicitante() != null) {
                map.put("persona", tomaMx.getIdNotificacion().getSolicitante().getNombre());
            } else if (tomaMx.getIdNotificacion().getCodigoPacienteVIH() != null) {
            	map.put("persona", tomaMx.getIdNotificacion().getCodigoPacienteVIH());
            	if (tomaMx.getIdNotificacion().getEmbarazada()!=null) {
                    map.put("embarazada", (tomaMx.getIdNotificacion().getEmbarazada().equalsIgnoreCase("RESP|S") ?//ABRIL2019
                            messageSource.getMessage("lbl.yes", null, null) : messageSource.getMessage("lbl.no", null, null)));
                }else{
                    map.put("embarazada", "--");
                }
            }else {
                map.put("persona", " ");
            }

            //se arma estructura de diagn�sticos o estudios
            Laboratorio labUser = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
            List<DaSolicitudDx> solicitudDxList = tomaMxService.getSolicitudesDxByIdToma(tomaMx.getIdTomaMx(), labUser.getCodigo());
            List<DaSolicitudEstudio> solicitudEList = tomaMxService.getSolicitudesEstudioByIdTomaMx(tomaMx.getIdTomaMx());

            Map<Integer, Object> mapDxList = new HashMap<Integer, Object>();
            Map<String, String> mapDx = new HashMap<String, String>();
            int subIndice = 0;

            if (!solicitudDxList.isEmpty()) {
                for (DaSolicitudDx solicitudDx : solicitudDxList) {
                    mapDx.put("idSolicitud", solicitudDx.getIdSolicitudDx());
                    mapDx.put("nombre", solicitudDx.getCodDx().getNombre());
                    mapDx.put("fechaSolicitud", DateUtil.DateToString(solicitudDx.getFechaHSolicitud(), "dd/MM/yyyy hh:mm:ss a"));
                    List<DetalleResultadoFinal> detRes = resultadoFinalService.getDetResActivosBySolicitud(solicitudDx.getIdSolicitudDx());

                    if (solicitudDx.getAprobada() != null) {
                        if (solicitudDx.getAprobada().equals(true)) {
                            mapDx.put("estado", (messageSource.getMessage("lbl.approval.result", null, null)));
                        } else {
                            if (!detRes.isEmpty()) {
                                mapDx.put("estado", (messageSource.getMessage("lbl.result.pending.approval", null, null)));
                            } else {
                                mapDx.put("estado", (messageSource.getMessage("lbl.without.result", null, null)));
                            }
                        }
                    } else {
                        if (!detRes.isEmpty()) {
                            mapDx.put("estado", (messageSource.getMessage("lbl.result.pending.approval", null, null)));
                        } else {
                            mapDx.put("estado", (messageSource.getMessage("lbl.without.result", null, null)));
                        }
                    }

                    if(solicitudDx.getControlCalidad().equals(true)){
                        mapDx.put("cc", (messageSource.getMessage("lbl.yes", null, null)));

                    }else{
                        mapDx.put("cc", (messageSource.getMessage("lbl.no", null, null)));

                    }


                    subIndice++;
                    mapDxList.put(subIndice, mapDx);
                    mapDx = new HashMap<String, String>();
                }
            } else {
                for (DaSolicitudEstudio solicitudEstudio : solicitudEList) {
                    mapDx.put("idSolicitud", solicitudEstudio.getIdSolicitudEstudio());
                    mapDx.put("nombre", solicitudEstudio.getTipoEstudio().getNombre());
                    mapDx.put("fechaSolicitud", DateUtil.DateToString(solicitudEstudio.getFechaHSolicitud(), "dd/MM/yyyy hh:mm:ss a"));
                    List<DetalleResultadoFinal> detRes = resultadoFinalService.getDetResActivosBySolicitud(solicitudEstudio.getIdSolicitudEstudio());

                    if (solicitudEstudio.getAprobada() != null) {
                        if (solicitudEstudio.getAprobada().equals(true)) {
                            mapDx.put("estado", (messageSource.getMessage("lbl.approval.result", null, null)));
                        } else {
                            mapDx.put("estado", (messageSource.getMessage("lbl.without.result", null, null)));
                        }
                    } else {
                        if (!detRes.isEmpty()) {
                            mapDx.put("estado", (messageSource.getMessage("lbl.result.pending.approval", null, null)));
                        } else {
                            mapDx.put("estado", (messageSource.getMessage("lbl.without.result", null, null)));
                        }
                    }

                    mapDx.put("cc", (messageSource.getMessage("lbl.not.apply", null, null)));


                    subIndice++;
                    mapDxList.put(subIndice, mapDx);
                    mapDx = new HashMap<String, String>();
                }
            }


            map.put("solicitudes", new Gson().toJson(mapDxList));

            mapResponse.put(indice, map);
            indice++;
        }
        jsonResponse = new Gson().toJson(mapResponse);
        //escapar caracteres especiales, escape de los caracteres con valor num�rico mayor a 127
        UnicodeEscaper escaper = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    @RequestMapping(value = "printResults", method = RequestMethod.GET)
    public
    @ResponseBody
    String getPDF(@RequestParam(value = "codigos", required = true) String codigos, HttpServletRequest request) throws IOException, COSVisitorException, ParseException {
        String res = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        PDDocument doc = new PDDocument();
        List<DaSolicitudDx> solicDx = null;
        List<DaSolicitudEstudio> solicEstudio = null;
        DaSolicitudDx detalleSoliDx = null;
        DaSolicitudEstudio detalleSoliE = null;
        PDPageContentStream stream = null;
        Laboratorio labProcesa = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());

        String nombreExamen = null;

        String[] toma = codigos.split(",");
        boolean reporteCRes = false;

        for (String idToma : toma) {
            solicDx = tomaMxService.getSoliDxAprobByTomaAndUser(idToma, seguridadService.obtenerNombreUsuario());
            solicEstudio = tomaMxService.getSoliEAprobByIdTomaMxOrderCodigo(idToma);


            detalleResultado = null;
            fechaAprobacion = null;
            nombreSoli = null;
            nombrePersona = null;
            nombreSilais = null;
            nombreUS = null;
            edad = null;
            sexo = null;
            fechaRecepcion = null;
            fechaToma = null;
            fis = null;
            fechaResultado = null;
            fechaImpresion = null;
            orderSample = null;

            if (!solicDx.isEmpty() || !solicEstudio.isEmpty()) {
                reporteCRes = true;

                //Obtener las respuestas activas de la solicitud
                if (!solicDx.isEmpty()) {
                    for (DaSolicitudDx solicitudDx : solicDx) {
                        detalleSoliDx = solicitudDx;
                        detalleResultado = resultadoFinalService.getDetResActivosBySolicitud(solicitudDx.getIdSolicitudDx());
                        fechaImpresion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

                        if (solicitudDx.getFechaAprobacion() != null) {
                            fechaAprobacion = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(solicitudDx.getFechaAprobacion());
                        }


                        if (detalleResultado != null) {

                            //Prepare the document.
                            float y = 500;
                            float m1 = 20;

                            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
                            doc.addPage(page);
                            stream = new PDPageContentStream(doc, page);

                            GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                            drawInfoLab(stream, page, labProcesa);
                            drawReportHeader(stream, detalleSoliDx, detalleSoliE);

                            drawInfoSample(stream, detalleSoliDx, detalleSoliE, y);
                            y -= m1;


                            boolean lista = false;
                            String valor = null;
                            String respuesta;
                            String[][] content = new String[detalleResultado.size()][2];


                            int numFila = 0;
                            for (DetalleResultadoFinal resul : detalleResultado) {
                                y = y - 20;
                                if (resul.getRespuesta() != null) {
                                    respuesta = resul.getRespuesta().getNombre();
                                    lista = resul.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                } else {
                                    respuesta = resul.getRespuestaExamen().getNombre();
                                    lista = resul.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                }

                                if (lista) {
                                    Catalogo_Lista catLista = conceptoService.getCatalogoListaById(Integer.valueOf(resul.getValor()));
                                    valor = catLista.getValor();
                                } else {
                                    valor = resul.getValor();
                                }


                                content[numFila][0] = respuesta;
                                content[numFila][1] = valor;
                                numFila++;

                            }

                            drawFinalResultTable(content, doc, page, y);
                            y = y - 140;
                            drawFinalInfo(stream, y, fechaAprobacion, fechaImpresion);
                            stream.close();

                        }
                    }
                } else {
                    for (DaSolicitudEstudio solicitudE : solicEstudio) {
                        fechaImpresion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());


                        List<DaSolicitudEstudio> solicitudes = null;

                        boolean lista = false;
                        String valor = null;
                        String respuesta;


                        if (solicitudE.getTipoEstudio().getIdEstudio().equals(1) || solicitudE.getTipoEstudio().getIdEstudio().equals(2)) {

                            //Prepare document

                            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
                            doc.addPage(page);
                            stream = new PDPageContentStream(doc, page);


                            //draw Page Header
                            GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                            drawInfoLab(stream, page, labProcesa);

                            String codigoUnico = solicitudE.getIdTomaMx().getCodigoUnicoMx();
                          /*  if(codigoAnterior != null) {
                                codigoAnterior = codigoAnterior.substring(0, codigoAnterior.lastIndexOf("."));

                                if (!codigoUnico.contains(codigoAnterior)) {
                                    //solicitudes por codigo
                                    solicitudes = tomaMxService.getSoliEAprobByCodigo(codigoAnterior);
                                    codigoAnterior = codigoUnico;

                                }
                            }else {
                                    String codigo = codigoUnico.substring(0, codigoUnico.lastIndexOf("."));
                                    solicitudes = tomaMxService.getSoliEAprobByCodigo(codigo);

                                }*/
                            //primera muestra
                            String codigo = solicitudE.getIdTomaMx().getCodigoUnicoMx();
                            String secuencia = null;
                            float m = 20;
                            float y = 500;
                            float m1 = 50;
                            float m2 = 10;
                            float m3 = 140;

                            List<DetalleResultado> resultadoExamen = null;
                            List<DetalleResultadoFinal> detalleRF = null;


                            if (codigo.contains("."))
                                secuencia = codigo.substring(codigo.lastIndexOf(".") + 1);

                            if (secuencia.equals("1")) {

                                if (solicitudE.getFechaAprobacion() != null) {
                                    fechaAprobacion = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(solicitudE.getFechaAprobacion());
                                }

                                if (solicitudE.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas() != null) {
                                    fis = String.valueOf(solicitudE.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas());
                                }

                                orderSample = messageSource.getMessage("lbl.first.sample", null, null);
                                detalleRF = resultadoFinalService.getDetResActivosBySolicitud(solicitudE.getIdSolicitudEstudio());

                                fechaToma = new SimpleDateFormat("dd/MM/yyyy").format(solicitudE.getIdTomaMx().getFechaHTomaMx())+
                                        (solicitudE.getIdTomaMx().getHoraTomaMx()!=null?" "+solicitudE.getIdTomaMx().getHoraTomaMx():"");


                                //draw Report Header
                                drawReportHeader(stream, null, solicitudE);
                                //draw info sample1
                                drawInfoSpecialSample(orderSample, y, solicitudE, stream);
                                y -= m;
                                if (y < 320) {
                                    stream.close();
                                    page = new PDPage(PDPage.PAGE_SIZE_A4);
                                    doc.addPage(page);
                                    stream = new PDPageContentStream(doc, page);
                                    GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                    y = 700;
                                }


                                //Obtener ordenes de examen  con resultado segun solicitud
                                FiltroMx filtro = new FiltroMx();
                                filtro.setResultado("Si");
                                filtro.setCodigoUnicoMx(solicitudE.getIdTomaMx().getCodigoUnicoMx());

                                List<OrdenExamen> ordenExamen = ordenExamenMxService.getOrdenesExamenEstudioResultadoByFiltro(filtro);


                                for (OrdenExamen orden : ordenExamen) {

                                    nombreExamen = orden.getCodExamen().getNombre();

                                    //add info test
                                    GeneralUtils.drawTEXT(messageSource.getMessage("lbl.test", null, null), y, 15, stream, 12, PDType1Font.HELVETICA_BOLD);
                                    GeneralUtils.drawTEXT(nombreExamen, y, 80, stream, 12, PDType1Font.HELVETICA);


                                    y -= m2;
                                    if (y < 320) {
                                        stream.close();
                                        page = new PDPage(PDPage.PAGE_SIZE_A4);
                                        doc.addPage(page);
                                        stream = new PDPageContentStream(doc, page);
                                        GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                        y = 700;
                                    }

                                    //Obtener resultado del examen
                                    resultadoExamen = resultadosService.getDetallesResultadoActivosByExamen(orden.getIdOrdenExamen());

                                    String[][] exam = new String[resultadoExamen.size()][2];
                                    float tableHeight = (resultadoExamen.size() + 3) * 15;

                                    int numFila = 0;
                                    for (DetalleResultado resulExa : resultadoExamen) {

                                        if (resulExa.getRespuesta() != null) {
                                            respuesta = resulExa.getRespuesta().getNombre();
                                            lista = resulExa.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                        } else {
                                            respuesta = resulExa.getRespuesta().getNombre();
                                            lista = resulExa.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                        }

                                        if (lista) {
                                            Catalogo_Lista catLista = conceptoService.getCatalogoListaById(Integer.valueOf(resulExa.getValor()));
                                            valor = catLista.getValor();
                                        } else {
                                            valor = resulExa.getValor();
                                        }


                                        exam[numFila][0] = respuesta;
                                        exam[numFila][1] = valor;
                                        numFila++;

                                    }

                                    //draw test result table Sample 1
                                    drawFinalResultTable(exam, doc, page, y);
                                    y -= tableHeight;
                                    if (y < 320) {
                                        stream.close();
                                        page = new PDPage(PDPage.PAGE_SIZE_A4);
                                        doc.addPage(page);
                                        stream = new PDPageContentStream(doc, page);
                                        GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                        y = 700;
                                    }


                                }

                                //Sample 2
                                String codMx = codigoUnico.substring(0, codigoUnico.lastIndexOf("."));

                                //Getting Sample2
                                DaSolicitudEstudio sample2 = tomaMxService.getSoliEstByCodigo(codMx + "." + "2");

                                if (sample2 != null) {
                                    orderSample = messageSource.getMessage("lbl.second.sample", null, null);

                                    //draw info sample2
                                    drawInfoSpecialSample(orderSample, y, sample2, stream);
                                    y -= m;
                                    if (y < 320) {
                                        stream.close();
                                        page = new PDPage(PDPage.PAGE_SIZE_A4);
                                        doc.addPage(page);
                                        stream = new PDPageContentStream(doc, page);
                                        GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                        y = 700;
                                    }

                                    FiltroMx filtro1 = new FiltroMx();
                                    filtro1.setResultado("Si");
                                    filtro1.setCodigoUnicoMx(sample2.getIdTomaMx().getCodigoUnicoMx());

                                    //Obtener ordenes de examen segun solicitud
                                    List<OrdenExamen> ordenExamen2 = ordenExamenMxService.getOrdenesExamenEstudioResultadoByFiltro(filtro1);


                                    for (OrdenExamen orden : ordenExamen2) {

                                        nombreExamen = orden.getCodExamen().getNombre();

                                        //add info test
                                        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.test", null, null), y, 15, stream, 12, PDType1Font.HELVETICA_BOLD);
                                        GeneralUtils.drawTEXT(nombreExamen, y, 80, stream, 12, PDType1Font.HELVETICA);

                                        y -= m;
                                        if (y < 320) {
                                            stream.close();
                                            page = new PDPage(PDPage.PAGE_SIZE_A4);
                                            doc.addPage(page);
                                            stream = new PDPageContentStream(doc, page);
                                            GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                            y = 700;
                                        }

                                        //Obtener resultado del examen
                                        resultadoExamen = resultadosService.getDetallesResultadoActivosByExamen(orden.getIdOrdenExamen());

                                        String[][] exam = new String[resultadoExamen.size()][2];

                                        int numFila = 0;
                                        float tableHeight = (resultadoExamen.size() + 3) * 15;

                                        for (DetalleResultado resulExa : resultadoExamen) {

                                            if (resulExa.getRespuesta() != null) {
                                                respuesta = resulExa.getRespuesta().getNombre();
                                                lista = resulExa.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                            } else {
                                                respuesta = resulExa.getRespuesta().getNombre();
                                                lista = resulExa.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                            }

                                            if (lista) {
                                                Catalogo_Lista catLista = conceptoService.getCatalogoListaById(Integer.valueOf(resulExa.getValor()));
                                                valor = catLista.getValor();
                                            } else {
                                                valor = resulExa.getValor();
                                            }

                                            exam[numFila][0] = respuesta;
                                            exam[numFila][1] = valor;
                                            numFila++;
                                        }

                                        //draw test result table Sample 2
                                        drawFinalResultTable(exam, doc, page, y);
                                        y -= tableHeight;
                                        if (y < 320) {
                                            stream.close();
                                            page = new PDPage(PDPage.PAGE_SIZE_A4);
                                            doc.addPage(page);
                                            stream = new PDPageContentStream(doc, page);
                                            GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                            y = 700;
                                        }
                                    }
                                }


                                //dibujar resultado final

                                boolean listaFinal = false;
                                String valorFinal = null;
                                String respuestaFinal;
                                String[][] content = new String[detalleRF.size()][2];


                                int numFila = 0;
                                for (DetalleResultadoFinal rFinal : detalleRF) {

                                    if (rFinal.getRespuesta() != null) {
                                        respuestaFinal = rFinal.getRespuesta().getNombre();
                                        listaFinal = rFinal.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                    } else {
                                        respuestaFinal = rFinal.getRespuestaExamen().getNombre();
                                        listaFinal = rFinal.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                                    }

                                    if (listaFinal) {
                                        Catalogo_Lista catLista = conceptoService.getCatalogoListaById(Integer.valueOf(rFinal.getValor()));
                                        valorFinal = catLista.getValor();
                                    } else {
                                        valorFinal = rFinal.getValor();
                                    }


                                    content[numFila][0] = respuestaFinal;
                                    content[numFila][1] = valorFinal;
                                    numFila++;
                                }

                                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.final.result1", null, null), y, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
                                y -= m2;
                                if (y < 320) {
                                    stream.close();
                                    page = new PDPage(PDPage.PAGE_SIZE_A4);
                                    doc.addPage(page);
                                    stream = new PDPageContentStream(doc, page);
                                    GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                    y = 700;
                                }
                                drawFinalResultTable(content, doc, page, y);
                                y -= m3;
                                if (y < 320) {
                                    stream.close();
                                    page = new PDPage(PDPage.PAGE_SIZE_A4);
                                    doc.addPage(page);
                                    stream = new PDPageContentStream(doc, page);
                                    GeneralUtils.drawHeaderAndFooter(stream, doc, 750, 590, 80, 600, 70);
                                    y = 700;
                                }
                                drawFinalInfo(stream, y, fechaAprobacion, fechaImpresion);
                                stream.close();


                            } else {
                                detalleSoliE = solicitudE;
                                detalleResultado = resultadoFinalService.getDetResActivosBySolicitud(solicitudE.getIdSolicitudEstudio());
                                if (solicitudE.getFechaAprobacion() != null) {
                                    fechaAprobacion = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(solicitudE.getFechaAprobacion());
                                }
                            }
                        }
                    }
                }

            }

        }
        if (reporteCRes) {
            doc.save(output);
            doc.close();
            // generate the file
            res = Base64.encodeBase64String(output.toByteArray());
        }

        return res;
    }


    private void drawReportHeader(PDPageContentStream stream, DaSolicitudDx soliDx, DaSolicitudEstudio soliE) throws IOException {

        float inY = 610;
        float m = 20;

        if (soliDx != null || soliE != null) {

            if (soliDx != null) {
                if (soliDx.getIdTomaMx().getIdNotificacion().getPersona()!=null) {
                    nombrePersona = soliDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                    if (soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null) {
                        nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                        nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                    } else {
                        nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();

                    }
                    if (soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null) {
                        nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                    }
                }else if (soliDx.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                	nombrePersona = soliDx.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH();
                }else{
                    nombrePersona = soliDx.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre();
                }
                nombreSoli = soliDx.getCodDx().getNombre();

                if (soliDx.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas() != null) {
                    fis = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(soliDx.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas());
                } else {
                    fis = "---------------";
                }

                if (soliDx.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                    nombreSilais = soliDx.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                    nombreUS = soliDx.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019

                } else {
                    nombreSilais = "---------------";
                    nombreUS = "---------------";
                }
/*ABRIL2019
                if (soliDx.getIdTomaMx().getIdNotificacion().getPersona() != null && soliDx.getIdTomaMx().getIdNotificacion().getPersona().getDescSexo() != null) {
                    sexo = soliDx.getIdTomaMx().getIdNotificacion().getPersona().getDescSexo();
                } else {
                    sexo = "----------------";
                }*/

                if (soliDx.getIdTomaMx().getIdNotificacion().getPersona() != null && soliDx.getIdTomaMx().getIdNotificacion().getPersona().getFechaNacimiento() != null) {
                    String fechaformateada = DateUtil.DateToString(soliDx.getIdTomaMx().getIdNotificacion().getPersona().getFechaNacimiento(), "dd/MM/yyyy");
                    edad = DateUtil.edad(fechaformateada);
                }
            } else {
                nombrePersona = soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                nombreSoli = soliE.getTipoEstudio().getNombre();

                if (soliE.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas() != null) {
                    fis = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(soliE.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas());
                } else {
                    fis = "---------------";
                }

                if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null) {
                    nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                    nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                } else {
                    nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();

                }
                if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null) {
                    nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                }

                if (soliE.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                    nombreSilais = soliE.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                    nombreUS = soliE.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019

                } else {
                    nombreSilais = "---------------";
                    nombreUS = "---------------";
                }
/*ABRIL2019
                if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getDescSexo() != null) {
                    sexo = soliE.getIdTomaMx().getIdNotificacion().getPersona().getDescSexo();
                } else {
                    sexo = "----------------";
                }*/

                if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getFechaNacimiento() != null) {
                    String fechaformateada = DateUtil.DateToString(soliE.getIdTomaMx().getIdNotificacion().getPersona().getFechaNacimiento(), "dd/MM/yyyy");
                    edad = DateUtil.edad(fechaformateada);
                }


            }

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.request.name1", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(nombreSoli, inY, 80, stream, 12, PDType1Font.HELVETICA);
            inY -= m;

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.name1", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(nombrePersona, inY, 80, stream, 12, PDType1Font.HELVETICA);

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.age", null, null) + " ", inY, 380, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT((edad!=null?String.valueOf(edad):"-"), inY, 425, stream, 12, PDType1Font.HELVETICA);

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.sex", null, null) + " ", inY, 450, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(sexo, inY, 495, stream, 12, PDType1Font.HELVETICA);
            inY -= m;

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.silais1", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(nombreSilais, inY, 80, stream, 12, PDType1Font.HELVETICA);
            inY -= m;

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.health.unit1", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(nombreUS, inY, 140, stream, 12, PDType1Font.HELVETICA);
            inY -= m;

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.fis", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fis, inY, 210, stream, 12, PDType1Font.HELVETICA);

        }

    }

    private void drawInfoSample(PDPageContentStream stream, DaSolicitudDx solDx, DaSolicitudEstudio solE, float inY) throws IOException {
        float m = 20;
        Laboratorio laboratorioUsuario = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario()); //laboratorio al que pertenece el usuario
        if (solDx != null || solE != null) {
            if (solDx != null) {

                if (solDx.getIdTomaMx().getFechaHTomaMx() != null) {
                    fechaToma = new SimpleDateFormat("dd/MM/yyyy").format(solDx.getIdTomaMx().getFechaHTomaMx())+
                            (solDx.getIdTomaMx().getHoraTomaMx()!=null?" "+solDx.getIdTomaMx().getHoraTomaMx():"");
                }

                RecepcionMx recepcion = recepcionMxService.getRecepcionMxByCodUnicoMx(solDx.getIdTomaMx().getCodigoUnicoMx(), (laboratorioUsuario.getCodigo() != null ? laboratorioUsuario.getCodigo() : ""));
                if (recepcion != null) {
                    fechaRecepcion = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(recepcion.getFechaHoraRecepcion());
                }


                Object fechaResultadoFinal = resultadoFinalService.getFechaResultadoByIdSoli(solDx.getIdSolicitudDx());
                if (fechaResultadoFinal != null) {
                    fechaResultado = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(fechaResultadoFinal);
                }


            } else {

                if (solE.getIdTomaMx().getFechaHTomaMx() != null) {
                    fechaToma = new SimpleDateFormat("dd/MM/yyyy").format(solE.getIdTomaMx().getFechaHTomaMx())+
                            (solE.getIdTomaMx().getHoraTomaMx()!=null?" "+solE.getIdTomaMx().getHoraTomaMx():"");
                }

                RecepcionMx recepcion = recepcionMxService.getRecepcionMxByCodUnicoMx(solE.getIdTomaMx().getCodigoUnicoMx(), (laboratorioUsuario.getCodigo() != null ? laboratorioUsuario.getCodigo() : ""));
                if (recepcion != null) {
                    fechaRecepcion = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(recepcion.getFechaHoraRecepcion());
                }

                Object fechaResultadoFinal = resultadoFinalService.getFechaResultadoByIdSoli(solE.getIdSolicitudEstudio());
                if (fechaResultadoFinal != null) {
                    fechaResultado = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(fechaResultadoFinal);
                }

            }

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.sampling.datetime1", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaToma, inY, 140, stream, 12, PDType1Font.HELVETICA);


            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.reception.datetime", null, null) + " ", inY, 310, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaRecepcion!=null?fechaRecepcion:"", inY, 435, stream, 12, PDType1Font.HELVETICA);
            inY -= m;

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.finalResult.datetime", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaResultado!=null?fechaResultado:"", inY, 180, stream, 12, PDType1Font.HELVETICA);

        }

    }

    private void drawFinalResultTable(String[][] content, PDDocument doc, PDPage page, float y) throws IOException {

        //Initialize table
        float margin = 50;
        float tableWidth = 500;
        float yStartNewPage = y;
        float yStart = yStartNewPage;
        float bottomMargin = 45;
        BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);

        //Create Header row
        Row headerRow = table.createRow(15f);
      /*  Cell cell = headerRow.createCell(100, "");
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFillColor(Color.black);
        cell.setTextColor(Color.WHITE);*/

        table.setHeader(headerRow);

        //Create 2 column row
        Row row;
        Cell cell;

        //Create Fact header row
        Row factHeaderrow = table.createRow(15f);
        cell = factHeaderrow.createCell(50, messageSource.getMessage("lbl.approve.response", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell((50), messageSource.getMessage("lbl.value", null, null));
        cell.setFillColor(Color.lightGray);
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);

        //Add multiple rows with random facts about Belgium

        for (String[] fact : content) {

            row = table.createRow(15f);
            cell = row.createCell(50, fact[0]);
            cell.setFont(PDType1Font.HELVETICA);
            cell.setFontSize(10);


            for (int i = 1; i < fact.length; i++) {
                cell = row.createCell(50, fact[i]);
                cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
                cell.setFontSize(10);
            }
        }
        table.draw();
    }

    private void drawInfoSpecialSample(String orderSample, float inY, DaSolicitudEstudio detSoliE, PDPageContentStream stream) throws IOException {
        if (detSoliE != null) {
            if (detSoliE.getIdTomaMx().getFechaHTomaMx() != null) {
                fechaToma = new SimpleDateFormat("dd/MM/yyyy").format(detSoliE.getIdTomaMx().getFechaHTomaMx())+
                        (detSoliE.getIdTomaMx().getHoraTomaMx()!=null?" "+detSoliE.getIdTomaMx().getHoraTomaMx():"");
            } else {
                fechaToma = "----------";
            }

            GeneralUtils.drawTEXT(orderSample, inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.sampling.datetime1", null, null) + " ", inY, 290, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaToma, inY, 420, stream, 12, PDType1Font.HELVETICA);
        }

    }

    private void drawInfoLab(PDPageContentStream stream, PDPage page, Laboratorio labProcesa) throws IOException {
        float xCenter;

        float inY = 700;
        float m = 20;

        xCenter = GeneralUtils.centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 14, messageSource.getMessage("lbl.minsa", null, null));
        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.minsa", null, null), inY, xCenter, stream, 14, PDType1Font.HELVETICA_BOLD);
        inY -= m;

        if (labProcesa != null) {

            if (labProcesa.getDescripcion() != null) {
                xCenter = GeneralUtils.centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 14, labProcesa.getDescripcion());
                GeneralUtils.drawTEXT(labProcesa.getDescripcion(), inY, xCenter, stream, 14, PDType1Font.HELVETICA_BOLD);
                inY -= m;
            }

            if (labProcesa.getDireccion() != null) {
                xCenter = GeneralUtils.centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getDireccion());
                GeneralUtils.drawTEXT(labProcesa.getDireccion(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                inY -= m;
            }

            if (labProcesa.getTelefono() != null) {

                if (labProcesa.getTelefax() != null) {
                    xCenter = GeneralUtils.centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getTelefono() + " " + labProcesa.getTelefax());
                    GeneralUtils.drawTEXT(labProcesa.getTelefono() + " " + labProcesa.getTelefax(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                } else {
                    xCenter = GeneralUtils.centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getTelefono());
                    GeneralUtils.drawTEXT(labProcesa.getTelefono(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                }
            }
        }
    }

    private void drawFinalInfo(PDPageContentStream stream, float y, String fechaAprobacion, String fechaImpresion) throws IOException {
        //dibujar lineas de firmas
        stream.drawLine(90, y, 250, y);
        stream.drawLine(340, y, 500, y);

        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.analyst", null, null), y - 10, 145, stream, 10, PDType1Font.HELVETICA_BOLD);
        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.director", null, null), y - 10, 400, stream, 10, PDType1Font.HELVETICA_BOLD);

        //info reporte
        if (fechaAprobacion != null) {
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.approval.datetime", null, null), 115, 15, stream, 10, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaAprobacion, 115, 120, stream, 10, PDType1Font.HELVETICA);

        }

        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.print.datetime", null, null), 115, 360, stream, 10, PDType1Font.HELVETICA_BOLD);
        GeneralUtils.drawTEXT(fechaImpresion, 115, 450, stream, 10, PDType1Font.HELVETICA);

    }

}
