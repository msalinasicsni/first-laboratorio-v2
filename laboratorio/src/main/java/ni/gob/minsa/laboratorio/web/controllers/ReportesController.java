package ni.gob.minsa.laboratorio.web.controllers;

import com.google.common.base.Predicate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ni.gob.minsa.laboratorio.domain.examen.Area;
import ni.gob.minsa.laboratorio.domain.muestra.*;
import ni.gob.minsa.laboratorio.domain.muestra.traslado.TrasladoMx;
import ni.gob.minsa.laboratorio.domain.concepto.Catalogo_Lista;
import ni.gob.minsa.laboratorio.domain.parametros.Parametro;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultadoFinal;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import ni.gob.minsa.laboratorio.restServices.CallRestServices;
import ni.gob.minsa.laboratorio.restServices.constantes.CatalogConstants;
import ni.gob.minsa.laboratorio.restServices.entidades.Catalogo;
import ni.gob.minsa.laboratorio.restServices.entidades.EntidadesAdtvas;
import ni.gob.minsa.laboratorio.restServices.entidades.Unidades;
import ni.gob.minsa.laboratorio.service.*;
import ni.gob.minsa.laboratorio.utilities.DateUtil;
import ni.gob.minsa.laboratorio.utilities.Email.Attachment;
import ni.gob.minsa.laboratorio.utilities.Email.EmailUtil;
import ni.gob.minsa.laboratorio.utilities.Email.SessionData;
import ni.gob.minsa.laboratorio.utilities.FiltrosReporte;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.BaseTable;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.Cell;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.GeneralUtils;
import ni.gob.minsa.laboratorio.utilities.pdfUtils.Row;
import ni.gob.minsa.laboratorio.utilities.reportes.ConsolidadoRecepcion;
import ni.gob.minsa.laboratorio.utilities.reportes.FilterLists;
import ni.gob.minsa.laboratorio.utilities.reportes.ResultadoSolicitud;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by souyen-ics.
 */
@Controller
@RequestMapping("reports")
public class ReportesController {

    private static final Logger logger = LoggerFactory.getLogger(ReportesController.class);

    @Autowired
    @Qualifier(value = "seguridadService")
    private SeguridadService seguridadService;

    @Autowired
    @Qualifier(value = "catalogosService")
    private CatalogoService catalogosService;

    @Autowired
    @Qualifier(value = "tomaMxService")
    private TomaMxService tomaMxService;

    @Autowired
    @Qualifier(value = "recepcionMxService")
    private RecepcionMxService recepcionMxService;

    @Autowired
    @Qualifier(value = "reportesService")
    private ReportesService reportesService;

    @Autowired
    @Qualifier(value = "resultadoFinalService")
    private ResultadoFinalService resultadoFinalService;

    @Autowired
    @Qualifier(value = "respuestasExamenService")
    private RespuestasExamenService respuestasExamenService;

    @Autowired
    @Qualifier (value = "areaService")
    private AreaService areaService;

    @Autowired
    @Qualifier(value = "laboratoriosService")
    private LaboratoriosService laboratoriosService;

    @Autowired
    @Qualifier(value = "trasladosService")
    private TrasladosService trasladosService;

    @Autowired
    @Qualifier(value = "conceptoService")
    private ConceptoService conceptoService;

    @Autowired
    @Qualifier(value = "associationSR")
    private AssociationSamplesRequestService associationSamplesRequestService;

    @Resource(name = "parametrosService")
    private ParametrosService parametrosService;

    @Autowired
    MessageSource messageSource;

    User usuario = null;
    Laboratorio laboratorio = null;


    /**
     * M�todo que se llama al entrar a la opci�n de menu de Reportes "Reporte Recepcion Mx".
     *
     * @param request para obtener informaci�n de la petici�n del cliente
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "/reception/init", method = RequestMethod.GET)
    public ModelAndView initSearchForm(HttpServletRequest request) throws Exception {
        logger.debug("Iniciando Reporte de Recepci�n");

        ModelAndView mav = new ModelAndView();
        List<EntidadesAdtvas> entidadesAdtvases = CallRestServices.getEntidadesAdtvas();//ABRIL2019
        List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
        mav.addObject("entidades", entidadesAdtvases);
        mav.addObject("tipoMuestra", tipoMxList);
        mav.setViewName("reportes/receptionReport");

        return mav;
    }


    /**
     * M�todo para realizar la b�squeda de Mx recepcionadas
     *
     * @param filtro JSon con los datos de los filtros a aplicar en la b�squeda(Rango Fec Recepcion, Tipo Mx, SILAIS, unidad salud, tipo solicitud, descripcion)
     * @return String con las Mx encontradas
     * @throws Exception
     */
    @RequestMapping(value = "/reception/searchSamples", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String fetchOrdersJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception {
        logger.info("Obteniendo las mx recepcionadas seg�n filtros en JSON");
        FiltroMx filtroMx = jsonToFiltroMx(filtro);
        List<RecepcionMx> receivedList = reportesService.getReceivedSamplesByFiltro(filtroMx);
        return receivedToJson(receivedList);
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

        Date fechaInicioRecepcion = null;
        Date fechaFinRecepcion = null;
        Date fechaInicioAprob = null;
        Date fechaFinAprob = null;
        String codSilais = null;
        String codUnidadSalud = null;
        String codTipoMx = null;
        String codTipoSolicitud = null;
        String nombreSolicitud = null;
        String area = null;
        String finalRes = null;
        String codLaboratorio = null;

        if (jObjectFiltro.get("fechaInicioRecepcion") != null && !jObjectFiltro.get("fechaInicioRecepcion").getAsString().isEmpty())
            fechaInicioRecepcion = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioRecepcion").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFinRecepcion") != null && !jObjectFiltro.get("fechaFinRecepcion").getAsString().isEmpty())
            fechaFinRecepcion = DateUtil.StringToDate(jObjectFiltro.get("fechaFinRecepcion").getAsString() + " 23:59:59");
        if (jObjectFiltro.get("codSilais") != null && !jObjectFiltro.get("codSilais").getAsString().isEmpty())
            codSilais = jObjectFiltro.get("codSilais").getAsString();
        if (jObjectFiltro.get("codUnidadSalud") != null && !jObjectFiltro.get("codUnidadSalud").getAsString().isEmpty())
            codUnidadSalud = jObjectFiltro.get("codUnidadSalud").getAsString();
        if (jObjectFiltro.get("codTipoMx") != null && !jObjectFiltro.get("codTipoMx").getAsString().isEmpty())
            codTipoMx = jObjectFiltro.get("codTipoMx").getAsString();
        if (jObjectFiltro.get("codTipoSolicitud") != null && !jObjectFiltro.get("codTipoSolicitud").getAsString().isEmpty())
            codTipoSolicitud = jObjectFiltro.get("codTipoSolicitud").getAsString();
        if (jObjectFiltro.get("nombreSolicitud") != null && !jObjectFiltro.get("nombreSolicitud").getAsString().isEmpty())
            nombreSolicitud = jObjectFiltro.get("nombreSolicitud").getAsString();
        if (jObjectFiltro.get("fechaInicioAprob") != null && !jObjectFiltro.get("fechaInicioAprob").getAsString().isEmpty())
            fechaInicioAprob = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioAprob").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFinAprob") != null && !jObjectFiltro.get("fechaFinAprob").getAsString().isEmpty())
            fechaFinAprob = DateUtil.StringToDate(jObjectFiltro.get("fechaFinAprob").getAsString() + " 23:59:59");
        if (jObjectFiltro.get("area") != null && !jObjectFiltro.get("area").getAsString().isEmpty())
            area = jObjectFiltro.get("area").getAsString();
        if (jObjectFiltro.get("finalRes") != null && !jObjectFiltro.get("finalRes").getAsString().isEmpty())
            finalRes = jObjectFiltro.get("finalRes").getAsString();
        if (jObjectFiltro.get("laboratorio") != null && !jObjectFiltro.get("laboratorio").getAsString().isEmpty())
            codLaboratorio = jObjectFiltro.get("laboratorio").getAsString();

        filtroMx.setCodSilais(codSilais);
        filtroMx.setCodUnidadSalud(codUnidadSalud);
        filtroMx.setFechaInicioRecep(fechaInicioRecepcion);
        filtroMx.setFechaFinRecep(fechaFinRecepcion);
        filtroMx.setCodTipoMx(codTipoMx);
        filtroMx.setCodTipoSolicitud(codTipoSolicitud);
        filtroMx.setNombreSolicitud(nombreSolicitud);
        User usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
        filtroMx.setNombreUsuario(usuario.getUsername());
        filtroMx.setNivelCentral(usuario.getNivelCentral());
        filtroMx.setFechaInicioAprob(fechaInicioAprob);
        filtroMx.setFechaFinAprob(fechaFinAprob);
        filtroMx.setArea(area);
        filtroMx.setResultadoFinal(finalRes);
        filtroMx.setCodLaboratio(codLaboratorio);

        return filtroMx;
    }


    /**
     * M�todo que convierte una lista de mx a un string con estructura Json
     *
     * @param receivedList lista con las mx recepcionadas a convertir
     * @return String
     */
    private String receivedToJson(List<RecepcionMx> receivedList) throws Exception{
        String jsonResponse;
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice = 0;
        boolean esEstudio;
        List<Catalogo> calidades = CallRestServices.getCatalogos(CatalogConstants.CalidadMx);//ABRIL2019
        for (RecepcionMx receivedMx : receivedList) {
            esEstudio = tomaMxService.getSolicitudesEstudioByIdTomaMx( receivedMx.getTomaMx().getIdTomaMx()).size() > 0;
            Map<String, String> map = new HashMap<String, String>();
            map.put("codigoUnicoMx", esEstudio?receivedMx.getTomaMx().getCodigoUnicoMx():receivedMx.getTomaMx().getCodigoLab());
            map.put("fechaRecepcion", DateUtil.DateToString(receivedMx.getFechaHoraRecepcion(), "dd/MM/yyyy hh:mm:ss a"));

            if (receivedMx.getCalidadMx() != null) {
                map.put("calidad", catalogosService.buscarValorCatalogo( calidades, receivedMx.getCalidadMx()));//ABRIL2019
            } else {
                map.put("calidad", "");
            }

            if (receivedMx.getTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                map.put("codSilais", receivedMx.getTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
            } else {
                map.put("codSilais", "");
            }
            if (receivedMx.getTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                map.put("codUnidadSalud", receivedMx.getTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
            } else {
                map.put("codUnidadSalud", "");
            }
            map.put("tipoMuestra", receivedMx.getTomaMx().getCodTipoMx().getNombre());


            //Si hay persona
            if (receivedMx.getTomaMx().getIdNotificacion().getPersona() != null) {
                /// se obtiene el nombre de la persona asociada a la ficha
                String nombreCompleto = "";
                nombreCompleto = receivedMx.getTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                if (receivedMx.getTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                    nombreCompleto = nombreCompleto + " " + receivedMx.getTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                nombreCompleto = nombreCompleto + " " + receivedMx.getTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                if (receivedMx.getTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                    nombreCompleto = nombreCompleto + " " + receivedMx.getTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                map.put("persona", nombreCompleto);
            } else if (receivedMx.getTomaMx().getIdNotificacion().getSolicitante() != null){
                map.put("persona",receivedMx.getTomaMx().getIdNotificacion().getSolicitante().getNombre());
            } else if (receivedMx.getTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null){
                map.put("persona",receivedMx.getTomaMx().getIdNotificacion().getCodigoPacienteVIH());
            } else {
                map.put("persona", " ");
            }

            //se arma estructura de diagn�sticos o estudios
            Laboratorio labUser = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
            List<DaSolicitudDx> solicitudDxList = tomaMxService.getSolicitudesDxByIdToma(receivedMx.getTomaMx().getIdTomaMx(), labUser.getCodigo());
            DaSolicitudEstudio solicitudE = tomaMxService.getSoliEstByCodigo(receivedMx.getTomaMx().getCodigoUnicoMx());
            String dxs = "";
            if (!solicitudDxList.isEmpty()) {
                int cont = 0;
                for (DaSolicitudDx solicitudDx : solicitudDxList) {
                    cont++;
                    if (cont == solicitudDxList.size()) {
                        dxs += solicitudDx.getCodDx().getNombre()+(solicitudDx.getControlCalidad()?"("+messageSource.getMessage("lbl.cc", null, null)+")":"");
                    } else {
                        dxs += solicitudDx.getCodDx().getNombre()+(solicitudDx.getControlCalidad()?"("+messageSource.getMessage("lbl.cc", null, null)+")":"") + "," + " ";
                    }

                }
                map.put("solicitudes", dxs);
            }
            if(solicitudE != null){
                map.put("solicitudes",(dxs.isEmpty()?solicitudE.getTipoEstudio().getNombre():dxs+", "+solicitudE.getTipoEstudio().getNombre()));
            }else{
                map.put("solicitudes", dxs);
            }

            map.put("usuario", receivedMx.getUsuarioRecepcion().getCompleteName());
            mapResponse.put(indice, map);
            indice++;
        }
        jsonResponse = new Gson().toJson(mapResponse);
        //escapar caracteres especiales, escape de los caracteres con valor num�rico mayor a 127
        UnicodeEscaper escaper = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }


    @RequestMapping(value = "/reception/expToPDF", method = RequestMethod.GET)
    public
    @ResponseBody
    String expToPDF(@RequestParam(value = "codes", required = true) String codes, @RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate", required = false) String toDate, HttpServletRequest request) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PDDocument doc = new PDDocument();
        Laboratorio labProcesa = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        String res = null;
        String fechaImpresion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        List<Catalogo> calidades = CallRestServices.getCatalogos(CatalogConstants.CalidadMx);//ABRIL2019

        if (!codes.isEmpty()) {

            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
            page.setRotation(90);
            doc.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
            float xCenter;

            GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);
            String pageNumber = String.valueOf(doc.getNumberOfPages());
            GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);
            drawInfoLab(stream, page, labProcesa);

            float y = 400;
            float m = 20;

            //nombre del reporte
            xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 12, messageSource.getMessage("lbl.reception.report", null, null).toUpperCase());
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.reception.report", null, null).toUpperCase(), y, xCenter, stream, 12, PDType1Font.HELVETICA_BOLD);
            y = y - 10;
            //Rango de Fechas
            if (!fromDate.equals("") && !toDate.equals("")) {
                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.from", null, null), y, 55, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(fromDate, y, 100, stream, 12, PDType1Font.HELVETICA_BOLD);

                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.to", null, null), y, 660, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(toDate, y, 720, stream, 12, PDType1Font.HELVETICA_BOLD);
                y -= m;
            }


            String[] codigosArray = codes.replaceAll("\\*", "-").split(",");
            List<String[]> recList = new ArrayList<String[]>();

            int numFila = 0;

            for (String codigoUnico : codigosArray) {
                String[] content = null;

                RecepcionMx recepcion = recepcionMxService.getRecepcionMxByCodUnicoMx(codigoUnico, labProcesa.getCodigo());

                if (recepcion != null) {
                    String nombreSolitud = null;
                    String nombrePersona = null;

                    List<DaSolicitudDx> listDx = tomaMxService.getSolicitudesDxCodigo(recepcion.getTomaMx().getCodigoUnicoMx(), seguridadService.obtenerNombreUsuario());
                    DaSolicitudEstudio soliE = tomaMxService.getSoliEstByCodigo(recepcion.getTomaMx().getCodigoUnicoMx());

                    if (!listDx.isEmpty()) {
                        int cont = 0;
                        String dxs = "";
                        for (DaSolicitudDx sol : listDx) {
                            cont++;
                            if (cont == listDx.size()) {
                                dxs += sol.getCodDx().getNombre();
                            } else {
                                dxs += sol.getCodDx().getNombre() + "," + " ";
                            }

                        }
                        content = new String[8];

                        nombreSolitud = dxs;

                        if (recepcion.getTomaMx().getIdNotificacion().getPersona()!=null) {
                            nombrePersona = recepcion.getTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                nombrePersona = nombrePersona + " " + recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                            nombrePersona = nombrePersona + " " + recepcion.getTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            if (recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                nombrePersona = nombrePersona + " " + recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                        }else if (recepcion.getTomaMx().getIdNotificacion().getCodigoPacienteVIH()!=null) { 
                        	nombrePersona = recepcion.getTomaMx().getIdNotificacion().getCodigoPacienteVIH();
                        }
                        else if (recepcion.getTomaMx().getIdNotificacion().getSolicitante()!=null) {
                            nombrePersona = recepcion.getTomaMx().getIdNotificacion().getSolicitante().getNombre();
                        }
                        content[0] = recepcion.getTomaMx().getCodigoLab() != null ? recepcion.getTomaMx().getCodigoLab() : recepcion.getTomaMx().getCodigoUnicoMx();
                        content[1] = recepcion.getTomaMx().getCodTipoMx() != null ? recepcion.getTomaMx().getCodTipoMx().getNombre() : "";
                        content[2] = recepcion.getFechaHoraRecepcion() != null ? DateUtil.DateToString(recepcion.getFechaHoraRecepcion(), "dd/MM/yyyy hh:mm:ss a") : "";
                        content[3] = recepcion.getCalidadMx() != null ? catalogosService.buscarValorCatalogo( calidades, recepcion.getCalidadMx()) : "";
                        content[4] = recepcion.getTomaMx().getIdNotificacion().getCodSilaisAtencion() != null ? recepcion.getTomaMx().getIdNotificacion().getNombreSilaisAtencion() : "";//ABRIL2019
                        content[5] = recepcion.getTomaMx().getIdNotificacion().getCodUnidadAtencion() != null ? recepcion.getTomaMx().getIdNotificacion().getNombreUnidadAtencion() : "";//ABRIL2019
                        content[6] = nombrePersona;
                        content[7] = nombreSolitud != null ? nombreSolitud : "";

                        recList.add(content);
                    }

                    if (soliE != null) {
                        content = new String[8];

                        nombreSolitud = soliE.getTipoEstudio().getNombre();

                        if (recepcion.getTomaMx().getIdNotificacion().getPersona()!=null) {
	                        nombrePersona = recepcion.getTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
	                        if (recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
	                            nombrePersona = nombrePersona + " " + recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
	                        nombrePersona = nombrePersona + " " + recepcion.getTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
	                        if (recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
	                            nombrePersona = nombrePersona + " " + recepcion.getTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                        }
                        else if (recepcion.getTomaMx().getIdNotificacion().getCodigoPacienteVIH()!=null) {
                        	nombrePersona = recepcion.getTomaMx().getIdNotificacion().getCodigoPacienteVIH();
                        }
                        else {
                        	nombrePersona = "";
                        }

                        content[0] = recepcion.getTomaMx().getCodigoUnicoMx() != null ? recepcion.getTomaMx().getCodigoUnicoMx() : "";
                        content[1] = recepcion.getTomaMx().getCodTipoMx() != null ? recepcion.getTomaMx().getCodTipoMx().getNombre() : "";
                        content[2] = recepcion.getFechaHoraRecepcion() != null ? DateUtil.DateToString(recepcion.getFechaHoraRecepcion(), "dd/MM/yyyy hh:mm:ss a") : "";
                        content[3] = recepcion.getCalidadMx() != null ? catalogosService.buscarValorCatalogo( calidades, recepcion.getCalidadMx()) : "";
                        content[4] = recepcion.getTomaMx().getIdNotificacion().getCodSilaisAtencion() != null ? recepcion.getTomaMx().getIdNotificacion().getNombreSilaisAtencion() : "";//ABRIL2019
                        content[5] = recepcion.getTomaMx().getIdNotificacion().getCodUnidadAtencion() != null ? recepcion.getTomaMx().getIdNotificacion().getNombreUnidadAtencion() : "";//ABRIL2019
                        content[6] = nombrePersona != null ? nombrePersona : "";
                        content[7] = nombreSolitud != null ? nombreSolitud : "";

                        recList.add(content);
                    }

                }
            }

            //drawTable

            //Initialize table
            float margin = 50;
            float tableWidth = 730;
            float yStartNewPage = y;
            float yStart = yStartNewPage;
            float bottomMargin = 45;
            BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);

            //Create Header row
            Row headerRow = table.createRow(15f);
            table.setHeader(headerRow);

            //Create 2 column row

            Cell cell;
            Row row;


            //Create Fact header row
            Row factHeaderrow = table.createRow(15f);
            cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.lab.code.mx", null, null));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            cell.setFillColor(Color.LIGHT_GRAY);

            cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.sample.type", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(9, messageSource.getMessage("lbl.receipt.dateTime", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(11, messageSource.getMessage("lbl.sample.quality", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.silais", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.health.unit", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(9, messageSource.getMessage("lbl.request.large", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            y -= 15;

            //Add multiple rows with random facts about Belgium
            for (String[] fact : recList) {

                if (y < 260) {
                    table.draw();
                    stream.close();
                    page = new PDPage(PDPage.PAGE_SIZE_A4);
                    page.setRotation(90);
                    doc.addPage(page);
                    stream = new PDPageContentStream(doc, page);
                    stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
                    y = 470;
                    GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);
                    pageNumber = String.valueOf(doc.getNumberOfPages());
                    GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);


                    table = new BaseTable(y, y, bottomMargin, tableWidth, margin, doc, page, true, true);

                    //Create Header row
                    headerRow = table.createRow(15f);
                    table.setHeader(headerRow);

                    //Create Fact header row
                    factHeaderrow = table.createRow(15f);
                    cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.lab.code.mx", null, null));
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    cell.setFillColor(Color.LIGHT_GRAY);

                    cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.sample.type", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(9, messageSource.getMessage("lbl.receipt.dateTime", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(11, messageSource.getMessage("lbl.sample.quality", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.silais", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.health.unit", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(9, messageSource.getMessage("lbl.request.large", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    y -= 15;


                }

                row = table.createRow(15);
                cell = row.createCell(13, fact[0]);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setFontSize(10);
                y -= 15;

                for (int i = 1; i < fact.length; i++) {
                    if (i == 1) {
                        cell = row.createCell(10, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 2) {
                        cell = row.createCell(9, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 5) {
                        cell = row.createCell(16, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 7) {
                        cell = row.createCell(9, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 4) {
                        cell = row.createCell(16, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 6) {
                        cell = row.createCell(16, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);


                    } else {
                        cell = row.createCell(11, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    }

                }
            }
            table.draw();

            //fecha impresi�n
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.print.datetime", null, null), 100, 605, stream, 10, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaImpresion, 100, 710, stream, 10, PDType1Font.HELVETICA);
            stream.close();

            doc.save(output);
            doc.close();
            // generate the file
            res = Base64.encodeBase64String(output.toByteArray());

        }

        return res;
    }


    private void drawInfoLab(PDPageContentStream stream, PDPage page, Laboratorio labProcesa) throws IOException {
        float xCenter;

        float inY = 490;
        float m = 20;

        xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 14, messageSource.getMessage("lbl.minsa", null, null));
        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.minsa", null, null), inY, xCenter, stream, 14, PDType1Font.HELVETICA_BOLD);
        inY -= m;

        if (labProcesa != null) {

            if (labProcesa.getDescripcion() != null) {
                xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 14, labProcesa.getDescripcion());
                GeneralUtils.drawTEXT(labProcesa.getDescripcion(), inY, xCenter, stream, 14, PDType1Font.HELVETICA_BOLD);
                inY -= m;
            }

            if (labProcesa.getDireccion() != null) {
                xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getDireccion());
                GeneralUtils.drawTEXT(labProcesa.getDireccion(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                inY -= m;
            }

            if (labProcesa.getTelefono() != null) {

                if (labProcesa.getTelefax() != null) {
                    xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getTelefono() + " " + labProcesa.getTelefax());
                    GeneralUtils.drawTEXT(labProcesa.getTelefono() + " " + labProcesa.getTelefax(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                } else {
                    xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getTelefono());
                    GeneralUtils.drawTEXT(labProcesa.getTelefono(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                }
            }
        }
    }


    public static float centerTextPositionX(PDPage page, PDFont font, float fontSize, String texto) throws IOException {
        float titleWidth = font.getStringWidth(texto) / 1000 * fontSize;
        return (page.getMediaBox().getHeight() - titleWidth) / 2;
    }

    /**
     * M�todo que se llama al entrar a la opci�n de menu de Reportes "Reporte Resultados Positivos".
     *
     * @param request para obtener informaci�n de la petici�n del cliente
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "/positiveResults/init", method = RequestMethod.GET)
    public ModelAndView initForm(HttpServletRequest request) throws Exception {
        logger.debug("Iniciando Reporte de Resultados Positivos");
        usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
        ModelAndView mav = new ModelAndView();
        List<EntidadesAdtvas> entidadesAdtvases = new ArrayList<EntidadesAdtvas>();
        List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
        List<Area> areas = areaService.getAreas();
        List<Laboratorio> laboratorios = null;
        if (usuario.getNivelCentral()!=null && usuario.getNivelCentral()) {
            laboratorios = laboratoriosService.getLaboratoriosRegionales();
            //areas.add(catalogosService.getAreaRep("AREAREP|PAIS"));
            entidadesAdtvases = CallRestServices.getEntidadesAdtvas();
        }else{
            laboratorio = seguridadService.getLaboratorioUsuario(usuario.getUsername());
            if (laboratorio!=null) {
                entidadesAdtvases = laboratoriosService.getEntidadesAdtvasAsignadasLab(laboratorio.getCodigo());//ABRIL2019
                laboratorios = new ArrayList<Laboratorio>();
                laboratorios.add(laboratorio);
            }
        }
        mav.addObject("laboratorios", laboratorios);
        mav.addObject("entidades", entidadesAdtvases);
        mav.addObject("areas", areas);
        mav.setViewName("reportes/positiveResultsReport");

        return mav;
    }

    /**
     * M�todo para realizar la b�squeda de Resultados positivos
     *
     * @param filtro JSon con los datos de los filtros a aplicar en la b�squeda(Rango Fec Aprob, SILAIS, unidad salud, tipo solicitud, descripcion)
     * @return String con las solicitudes encontradas
     * @throws Exception
     */
    @RequestMapping(value = "/positiveResults/searchRequest", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String fetchRequestJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception {
        logger.info("Obteniendo las solicitudes positivas seg�n filtros en JSON");
        FiltroMx filtroMx = jsonToFiltroMx(filtro);
        List<DaSolicitudDx> positiveRoutineReqList = null;
        List<DaSolicitudEstudio> positiveStudyReqList = null;

        if (filtroMx.getCodTipoSolicitud() != null) {
            if (filtroMx.getCodTipoSolicitud().equals("Estudio")) {
                positiveStudyReqList = reportesService.getPositiveStudyRequestByFilter(filtroMx);
            } else {
                positiveRoutineReqList = reportesService.getPositiveRoutineRequestByFilter(filtroMx);
            }

        } else {
            positiveRoutineReqList = reportesService.getPositiveRoutineRequestByFilter(filtroMx);
            positiveStudyReqList = reportesService.getPositiveStudyRequestByFilter(filtroMx);
        }

        return reqPositiveToJson(positiveRoutineReqList, positiveStudyReqList);
    }

    /**
     * M�todo que convierte una lista de solicitudes a un string con estructura Json
     *
     * @param positiveRoutineReqList lista con las mx recepcionadas a convertir
     * @return String
     */
    private String reqPositiveToJson(List<DaSolicitudDx> positiveRoutineReqList, List<DaSolicitudEstudio> positiveStudyReqList) throws Exception {
        String jsonResponse;
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice = 0;


        if (positiveRoutineReqList != null) {
            for (DaSolicitudDx soli : positiveRoutineReqList) {
                boolean mostrar = false;

                //search positive results from list
                //get Response for each request
                List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soli.getIdSolicitudDx());
                for (DetalleResultadoFinal res : finalRes) {

                    if (res.getRespuesta() != null) {
                        if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                            Integer idLista = Integer.valueOf(res.getValor());
                            Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                            if (valor.getValor().trim().toLowerCase().equals("positivo")
                                    || (valor.getValor().trim().toLowerCase().contains("reactor") && !valor.getValor().trim().toLowerCase().contains("no reactor"))
                                    || (valor.getValor().trim().toLowerCase().contains("detectado") && !valor.getValor().trim().toLowerCase().contains("no detectado"))
                                    || (valor.getValor().trim().toUpperCase().contains("MTB-DET") && !valor.getValor().trim().toUpperCase().contains("MTB-ND"))
                                    && (!valor.getValor().trim().toLowerCase().contains("negativo") && !valor.getValor().trim().toLowerCase().contains("indetermin") && !valor.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                                mostrar = true;
                            }

                        } else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                            if (res.getValor().trim().toLowerCase().equals("positivo")
                                    || (res.getValor().trim().toLowerCase().contains("reactor") && !res.getValor().trim().toLowerCase().contains("no reactor"))
                                    || (res.getValor().trim().toLowerCase().contains("detectado") && !res.getValor().trim().toLowerCase().contains("no detectado"))
                                    && (res.getValor().trim().toUpperCase().contains("MTB-DET") && !res.getValor().trim().toUpperCase().contains("MTB-ND"))) {
                                mostrar = true;
                            }
                        }
                    } else if (res.getRespuestaExamen() != null) {
                        if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                            Integer idLista = Integer.valueOf(res.getValor());
                            Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                            if (valor.getValor().trim().toLowerCase().equals("positivo")
                                    || (valor.getValor().trim().toLowerCase().contains("reactor") && !valor.getValor().trim().toLowerCase().contains("no reactor"))
                                    || (valor.getValor().trim().toLowerCase().contains("detectado") && !valor.getValor().trim().toLowerCase().contains("no detectado"))
                                    || (valor.getValor().trim().toUpperCase().contains("MTB-DET") && !valor.getValor().trim().toUpperCase().contains("MTB-ND"))
                                    && (!valor.getValor().trim().toLowerCase().contains("negativo") && !valor.getValor().trim().toLowerCase().contains("indetermin") && !valor.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                                mostrar = true;
                            }

                        } else if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                            if (res.getValor().trim().toLowerCase().equals("positivo")
                                    || (res.getValor().trim().toLowerCase().contains("reactor") && !res.getValor().trim().toLowerCase().contains("no reactor"))
                                    || (res.getValor().trim().toLowerCase().contains("detectado") && !res.getValor().trim().toLowerCase().contains("no detectado"))
                                    && (res.getValor().trim().toUpperCase().contains("MTB-DET") && !res.getValor().trim().toUpperCase().contains("MTB-ND"))) {
                                mostrar = true;
                            }
                        }

                    }
                }

                if (mostrar) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("solicitud", soli.getCodDx().getNombre());
                    map.put("idSolicitud", soli.getIdSolicitudDx());
                    map.put("codigoUnicoMx", soli.getIdTomaMx().getCodigoLab() != null ? soli.getIdTomaMx().getCodigoLab() : soli.getIdTomaMx().getCodigoUnicoMx());
                    map.put("fechaAprobacion", DateUtil.DateToString(soli.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a"));

                    if (soli.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                        map.put("codSilais", soli.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                    } else {
                        map.put("codSilais", "");
                    }
                    if (soli.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                        map.put("codUnidadSalud", soli.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                    } else {
                        map.put("codUnidadSalud", "");
                    }

                    //Si hay persona
                    if (soli.getIdTomaMx().getIdNotificacion().getPersona() != null) {
                        /// se obtiene el nombre de la persona asociada a la ficha
                        String nombreCompleto = "";
                        nombreCompleto = soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                        if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                            nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                        nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                        if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                            nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                        map.put("persona", nombreCompleto);
                    } else if (soli.getIdTomaMx().getIdNotificacion().getSolicitante() != null){
                        map.put("persona",soli.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre());
                    } else if (soli.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null){
                        map.put("persona",soli.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                    }else {
                        map.put("persona", " ");
                    }

                    mapResponse.put(indice, map);
                    indice++;
                }
            }

        }
        if (positiveStudyReqList != null) {

            for (DaSolicitudEstudio soliE : positiveStudyReqList) {
                boolean mostrar = false;

                //search positive results from list
                //get Response for each request
                List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soliE.getIdSolicitudEstudio());
                for (DetalleResultadoFinal res : finalRes) {

                    if (res.getRespuesta() != null) {
                        if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                            Integer idLista = Integer.valueOf(res.getValor());
                            Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                            if (valor.getValor().trim().toLowerCase().equals("positivo")) {
                                mostrar = true;
                            }

                        } else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                            if (res.getValor().trim().toLowerCase().equals("positivo")) {
                                mostrar = true;
                            }
                        }
                    } else if (res.getRespuestaExamen() != null) {
                        if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                            Integer idLista = Integer.valueOf(res.getValor());
                            Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                            if (valor.getValor().trim().toLowerCase().equals("positivo")) {
                                mostrar = true;
                            }

                        } else if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                            if (res.getValor().trim().toLowerCase().equals("positivo")) {
                                mostrar = true;
                            }
                        }
                    }
                }

                if (mostrar) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("solicitud", soliE.getTipoEstudio().getNombre());
                    map.put("idSolicitud", soliE.getIdSolicitudEstudio());
                    map.put("codigoUnicoMx", soliE.getIdTomaMx().getCodigoUnicoMx());
                    map.put("fechaAprobacion", DateUtil.DateToString(soliE.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a"));

                    if (soliE.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                        map.put("codSilais", soliE.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                    } else {
                        map.put("codSilais", "");
                    }
                    if (soliE.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                        map.put("codUnidadSalud", soliE.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                    } else {
                        map.put("codUnidadSalud", "");
                    }

                    //Si hay persona
                    if (soliE.getIdTomaMx().getIdNotificacion().getPersona() != null) {
                        /// se obtiene el nombre de la persona asociada a la ficha
                        String nombreCompleto = "";
                        nombreCompleto = soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                        if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                            nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                        nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                        if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                            nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                        map.put("persona", nombreCompleto);
                    } else if (soliE.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                    	map.put("persona", soliE.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                    } else {
                        map.put("persona", " ");
                    }

                    mapResponse.put(indice, map);
                    indice++;
                }
            }
        }
        jsonResponse = new Gson().toJson(mapResponse);
        //escapar caracteres especiales, escape de los caracteres con valor num�rico mayor a 127
        UnicodeEscaper escaper = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }


    @RequestMapping(value = "/positiveResults/positiveRequestToPDF", method = RequestMethod.GET)
    public
    @ResponseBody
    String positiveRequestToPDF(@RequestParam(value = "codes", required = true) String codes, @RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate", required = false) String toDate, HttpServletRequest request) throws IOException, COSVisitorException, ParseException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PDDocument doc = new PDDocument();
        Laboratorio labProcesa = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        String res = null;
        String fechaImpresion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());


        if (!codes.isEmpty()) {

            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
            page.setRotation(90);
            doc.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
            float xCenter;

            GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

            String pageNumber = String.valueOf(doc.getNumberOfPages());
            GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

            drawInfoLab(stream, page, labProcesa);

            float y = 400;
            float m = 20;

            //nombre del reporte
            xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 12, messageSource.getMessage("lbl.positiveResultReport", null, null).toUpperCase());
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.positiveResultReport", null, null).toUpperCase(), y, xCenter, stream, 12, PDType1Font.HELVETICA_BOLD);
            y = y - 10;
            //Rango de Fechas
            if (!fromDate.equals("") && !toDate.equals("")) {
                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.from", null, null), y, 55, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(fromDate, y, 100, stream, 12, PDType1Font.HELVETICA_BOLD);

                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.to", null, null), y, 660, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(toDate, y, 720, stream, 12, PDType1Font.HELVETICA_BOLD);
                y -= m;
            }


            String[] idSoli = codes.split(",");
            List<String[]> reqList = new ArrayList<String[]>();


            for (String idSolicitud : idSoli) {
                String nombreSolitud = null;
                String nombrePersona = null;
                String fechaAprob = null;
                String silais = null;
                String unidadSalud = null;
                String[] content = null;

               DaSolicitudDx soli = tomaMxService.getSolicitudDxByIdSolicitudUser(idSolicitud, seguridadService.obtenerNombreUsuario());
               DaSolicitudEstudio soliE = tomaMxService.getSolicitudEstByIdSolicitud(idSolicitud);


               if(soli != null){

                    content = new String[6];
                    nombreSolitud = soli.getCodDx().getNombre();

                    nombrePersona = soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                    if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                        nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                    nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                    if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                        nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();

                    if (soli.getFechaAprobacion() != null) {
                        fechaAprob = DateUtil.DateToString(soli.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a");
                    }

                    if (soli.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                        silais = soli.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                    }

                    if (soli.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                        unidadSalud = soli.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019
                    }

                   content[0] = soli.getIdTomaMx() != null ? soli.getIdTomaMx().getCodigoLab()!=null?soli.getIdTomaMx().getCodigoLab():soli.getIdTomaMx().getCodigoUnicoMx() : "";
                    content[1] = fechaAprob != null ? fechaAprob : "";
                    content[2] = silais != null ? silais : "";
                    content[3] = unidadSalud != null ? unidadSalud : "";
                    content[4] = nombrePersona != null ? nombrePersona : "";
                    content[5] = nombreSolitud != null ? nombreSolitud : "";
                    reqList.add(content);

                }

                if (soliE != null) {
                    content = new String[6];
                    nombreSolitud = soliE.getTipoEstudio().getNombre();

                    if (soliE.getIdTomaMx().getIdNotificacion().getPersona()!=null) {
                        nombrePersona = soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                        if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                            nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                        nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                        if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                            nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                    }else{
                        nombrePersona = soliE.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre();
                    }
                    if (soliE.getFechaAprobacion() != null) {
                        fechaAprob = DateUtil.DateToString(soliE.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a");
                    }

                    if (soliE.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                        silais = soliE.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                    }

                    if (soliE.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                        unidadSalud = soliE.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019
                    }

                    content[0] = soliE.getIdTomaMx() != null ? soliE.getIdTomaMx().getCodigoUnicoMx() : "";
                    content[1] = fechaAprob != null ? fechaAprob : "";
                    content[2] = silais != null ? silais : "";
                    content[3] = unidadSalud != null ? unidadSalud : "";
                    content[4] = nombrePersona != null ? nombrePersona : "";
                    content[5] = nombreSolitud != null ? nombreSolitud : "";
                    reqList.add(content);


                }
            }

            //drawTable

            //Initialize table
            float margin = 50;
            float tableWidth = 730;
            float yStartNewPage = y;
            float yStart = yStartNewPage;
            float bottomMargin = 45;
            BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);

            //Create Header row
            Row headerRow = table.createRow(15f);
            table.setHeader(headerRow);

            //Create 2 column row
            Cell cell;
            Row row;

            //Create Fact header row
            Row factHeaderrow = table.createRow(15f);
            cell = factHeaderrow.createCell(12, messageSource.getMessage("lbl.lab.code.mx", null, null));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            cell.setFillColor(Color.LIGHT_GRAY);

            cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.approve.date", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(17, messageSource.getMessage("lbl.silais", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(20, messageSource.getMessage("lbl.health.unit", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(19, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(15, messageSource.getMessage("lbl.request.large", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            y -= 15;

            //Add multiple rows with random facts about Belgium
            for (String[] fact : reqList) {

                if (y < 260) {
                    table.draw();
                    stream.close();
                    page = new PDPage(PDPage.PAGE_SIZE_A4);
                    page.setRotation(90);
                    doc.addPage(page);
                    stream = new PDPageContentStream(doc, page);
                    stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
                    y = 470;
                    GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

                    pageNumber = String.valueOf(doc.getNumberOfPages());
                    GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

                    table = new BaseTable(y, y, bottomMargin, tableWidth, margin, doc, page, true, true);

                    //Create Header row
                    headerRow = table.createRow(15f);
                    table.setHeader(headerRow);

                    //Create Fact header row
                    factHeaderrow = table.createRow(15f);
                    cell = factHeaderrow.createCell(15, messageSource.getMessage("lbl.lab.code.mx", null, null));
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    cell.setFillColor(Color.LIGHT_GRAY);

                    cell = factHeaderrow.createCell(15, messageSource.getMessage("lbl.approve.date", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(15, messageSource.getMessage("lbl.silais", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(20, messageSource.getMessage("lbl.health.unit", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(19, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(15, messageSource.getMessage("lbl.request.large", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    y -= 15;

                }

                row = table.createRow(15f);
                cell = row.createCell(12, fact[0]);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setFontSize(10);
                y -= 15;

                for (int i = 1; i < fact.length; i++) {
                    if (i == 1) {
                        cell = row.createCell(16, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 2) {
                        cell = row.createCell(17, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 3) {
                        cell = row.createCell(20, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 4) {
                        cell = row.createCell(19, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 5) {
                        cell = row.createCell(15, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    }
                }
            }
            table.draw();

            //fecha impresi�n
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.print.datetime", null, null), 100, 605, stream, 10, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaImpresion, 100, 710, stream, 10, PDType1Font.HELVETICA);

            stream.close();

            doc.save(output);
            doc.close();
            // generate the file
            res = Base64.encodeBase64String(output.toByteArray());

        }

        return res;
    }


    /**
     * M�todo que se llama al entrar a la opci�n de menu de Reportes "Reporte Resultados Positivos y Negativos".
     *
     * @param request para obtener informaci�n de la petici�n del cliente
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "/posNegResults/init", method = RequestMethod.GET)
    public ModelAndView initReportForm(HttpServletRequest request) throws Exception {
        logger.debug("Iniciando Reporte de Resultados Positivos y Negativos");
        usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
        ModelAndView mav = new ModelAndView();
        List<EntidadesAdtvas> entidadesAdtvases = new ArrayList<EntidadesAdtvas>();
        //List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
        List<Area> areas = areaService.getAreas();
        List<Laboratorio> laboratorios = null;
        if (usuario.getNivelCentral()!=null && usuario.getNivelCentral()) {
            laboratorios = laboratoriosService.getLaboratoriosRegionales();
            //areas.add(catalogosService.getAreaRep("AREAREP|PAIS"));
            entidadesAdtvases = CallRestServices.getEntidadesAdtvas();
        }else{
            laboratorio = seguridadService.getLaboratorioUsuario(usuario.getUsername());
            if (laboratorio!=null) {
                entidadesAdtvases = laboratoriosService.getEntidadesAdtvasAsignadasLab(laboratorio.getCodigo());
                laboratorios = new ArrayList<Laboratorio>();
                laboratorios.add(laboratorio);
            }
        }
        mav.addObject("laboratorios", laboratorios);
        mav.addObject("entidades", entidadesAdtvases);
        mav.addObject("areas", areas);
        mav.setViewName("reportes/positiveNegativeResults");

        return mav;
    }


    /**
     * M�todo para realizar la b�squeda de Resultados positivos
     *
     * @param filtro JSon con los datos de los filtros a aplicar en la b�squeda(Rango Fec Aprob, SILAIS, unidad salud, tipo solicitud, descripcion)
     * @return String con las solicitudes encontradas
     * @throws Exception
     */
    @RequestMapping(value = "/posNegResults/searchPosNegRequest", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String fetchPosNegRequestJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception {
        logger.info("Obteniendo las solicitudes positivas y negativas seg�n filtros en JSON");
        FiltroMx filtroMx = jsonToFiltroMx(filtro);
        List<DaSolicitudDx> positiveRoutineReqList = null;
        List<DaSolicitudEstudio> positiveStudyReqList = null;

        if (filtroMx.getCodTipoSolicitud() != null) {
            if (filtroMx.getCodTipoSolicitud().equals("Estudio")) {
                positiveStudyReqList = reportesService.getPositiveStudyRequestByFilter(filtroMx);
            } else {
                positiveRoutineReqList = reportesService.getPositiveRoutineRequestByFilter(filtroMx);
            }

        } else {
            positiveRoutineReqList = reportesService.getPositiveRoutineRequestByFilter(filtroMx);
            positiveStudyReqList = reportesService.getPositiveStudyRequestByFilter(filtroMx);
        }

        return requestPositiveNegativeToJson(positiveRoutineReqList, positiveStudyReqList, filtroMx.getResultadoFinal());
    }

    /**
     * M�todo que convierte una lista de solicitudes a un string con estructura Json
     *
     * @param posNegRoutineReqList lista con las mx recepcionadas a convertir
     * @return String
     */
    private String requestPositiveNegativeToJson(List<DaSolicitudDx> posNegRoutineReqList, List<DaSolicitudEstudio> posNegStudyReqList, String filtroResu) throws Exception {
        String jsonResponse;
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice = 0;


        if (posNegRoutineReqList != null) {
            for (DaSolicitudDx soli : posNegRoutineReqList) {
                boolean mostrar = false;
                String valorResultado = null;
                String content = null;

                //search positive results from list
                //get Response for each request
                List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soli.getIdSolicitudDx());
                for (DetalleResultadoFinal res : finalRes) {

                    if(filtroResu != null){
                        if(filtroResu.equals("Positivo")){
                            content = getPositiveResult(res);
                        }else{
                            content = getNegativeResult(res);
                        }

                    }else{
                        content = getResult(res);
                    }

                    String[] arrayContent = content.split(",");
                    valorResultado = arrayContent[0];
                    mostrar = Boolean.parseBoolean(arrayContent[1]);

                    if (mostrar) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("solicitud", soli.getCodDx().getNombre());
                        map.put("idSolicitud", soli.getIdSolicitudDx());
                        map.put("codigoUnicoMx", soli.getIdTomaMx().getCodigoLab()!=null?soli.getIdTomaMx().getCodigoLab():soli.getIdTomaMx().getCodigoUnicoMx());
                        map.put("fechaAprobacion", DateUtil.DateToString(soli.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a"));
                        map.put("resultado", valorResultado);

                        if (soli.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                            map.put("codSilais", soli.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                        } else {
                            map.put("codSilais", "");
                        }
                        if (soli.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                            map.put("codUnidadSalud", soli.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                        } else {
                            map.put("codUnidadSalud", "");
                        }

                        //Si hay persona
                        if (soli.getIdTomaMx().getIdNotificacion().getPersona() != null) {
                            /// se obtiene el nombre de la persona asociada a la ficha
                            String nombreCompleto = "";
                            nombreCompleto = soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                            nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                            map.put("persona", nombreCompleto);
                        } else if (soli.getIdTomaMx().getIdNotificacion().getSolicitante()!=null){
                            map.put("persona", soli.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre());
                        } else if (soli.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH()!=null){
                            map.put("persona", soli.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                        } else {
                            map.put("persona", " ");
                        }

                        mapResponse.put(indice, map);
                        indice++;
                        break;
                    }

                }


            }

        }
        if (posNegStudyReqList != null) {

            for (DaSolicitudEstudio soliE : posNegStudyReqList) {
                boolean mostrar = false;
                String valorResultado = null;
                String content = null;

                //search positive results from list
                //get Response for each request
                List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soliE.getIdSolicitudEstudio());
                for (DetalleResultadoFinal res : finalRes) {


                    if(filtroResu != null){
                        if(filtroResu.equals("Positivo")){
                            content = getPositiveResult(res);
                        }else{
                            content = getNegativeResult(res);
                        }

                    }else{
                        content = getResult(res);
                    }

                    String[] arrayContent = content.split(",");
                    valorResultado = arrayContent[0];
                    mostrar = Boolean.parseBoolean(arrayContent[1]);

                    if (mostrar) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("solicitud", soliE.getTipoEstudio().getNombre());
                        map.put("idSolicitud", soliE.getIdSolicitudEstudio());
                        map.put("codigoUnicoMx", soliE.getIdTomaMx().getCodigoUnicoMx());
                        map.put("fechaAprobacion", DateUtil.DateToString(soliE.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a"));
                        map.put("resultado", valorResultado);

                        if (soliE.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                            map.put("codSilais", soliE.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                        } else {
                            map.put("codSilais", "");
                        }
                        if (soliE.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                            map.put("codUnidadSalud", soliE.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                        } else {
                            map.put("codUnidadSalud", "");
                        }

                        //Si hay persona
                        if (soliE.getIdTomaMx().getIdNotificacion().getPersona() != null) {
                            /// se obtiene el nombre de la persona asociada a la ficha
                            String nombreCompleto = "";
                            nombreCompleto = soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                            nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                            map.put("persona", nombreCompleto);
                        } else if (soliE.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                        	map.put("persona", soliE.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                        } else {
                            map.put("persona", " ");
                        }

                        mapResponse.put(indice, map);
                        indice++;
                        break;
                    }
                }


            }
        }
        jsonResponse = new Gson().toJson(mapResponse);
        //escapar caracteres especiales, escape de los caracteres con valor num�rico mayor a 127
        UnicodeEscaper escaper = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }


    private String getResult(DetalleResultadoFinal res) throws Exception {
        boolean mostrar= false;
        String valorResultado = null;

        if (res.getRespuesta() != null) {
            if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                Integer idLista = Integer.valueOf(res.getValor());
                Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                if (valor.getValor().trim().toLowerCase().equals("positivo") ||valor.getValor().trim().toLowerCase().equals("negativo")
                        || valor.getValor().trim().toLowerCase().contains("reactor") || valor.getValor().trim().toLowerCase().contains("detectado")
                        || valor.getValor().trim().toUpperCase().contains("MTB-")
                        || (!valor.getValor().trim().toLowerCase().contains("indetermin") && !valor.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = valor.getValor();
                }

            } else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                if (res.getValor().trim().toLowerCase().equals("positivo") || res.getValor().trim().toLowerCase().equals("negativo")
                        || res.getValor().trim().toLowerCase().contains("reactor") || res.getValor().trim().toLowerCase().contains("detectado")
                        || res.getValor().trim().toUpperCase().contains("MTB-")
                        || (!res.getValor().trim().toLowerCase().contains("indetermin") && !res.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = res.getValor();
                }
            }
        } else if (res.getRespuestaExamen() != null) {
            if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                Integer idLista = Integer.valueOf(res.getValor());
                Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                if (valor.getValor().trim().toLowerCase().equals("positivo") || valor.getValor().trim().toLowerCase().equals("negativo")
                        || valor.getValor().trim().toLowerCase().contains("reactor") || valor.getValor().trim().toLowerCase().contains("detectado")
                        || valor.getValor().trim().toUpperCase().contains("MTB-")
                        || (!valor.getValor().trim().toLowerCase().contains("indetermin") && !valor.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = valor.getValor();
                }

            } else if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                if (res.getValor().trim().toLowerCase().equals("positivo") || res.getValor().trim().toLowerCase().equals("negativo")
                        || res.getValor().trim().toLowerCase().contains("reactor") || res.getValor().trim().toLowerCase().contains("detectado")
                        || res.getValor().trim().toUpperCase().contains("MTB-")
                        || (!res.getValor().trim().toLowerCase().contains("indetermin") && !res.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = res.getValor();
                }
            }

        }
        return valorResultado + "," + mostrar;
    }

    private String getNegativeResult(DetalleResultadoFinal res) throws Exception {
        boolean mostrar= false;
        String valorResultado = null;

        if (res.getRespuesta() != null) {
            if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                Integer idLista = Integer.valueOf(res.getValor());
                Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                if (valor.getValor().toLowerCase().equals("negativo")
                        || valor.getValor().trim().toLowerCase().contains("no reactor")
                        || valor.getValor().trim().toLowerCase().contains("no detectado")
                        || valor.getValor().trim().toUpperCase().contains("MTB-ND")) {
                    mostrar = true;
                    valorResultado = valor.getValor();
                }

            } else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                if (res.getValor().toLowerCase().equals("negativo")
                        || res.getValor().trim().toLowerCase().contains("no reactor")
                        || res.getValor().trim().toLowerCase().contains("no detectado")
                        || res.getValor().trim().toUpperCase().contains("MTB-ND")) {
                    mostrar = true;
                    valorResultado = res.getValor();
                }
            }
        } else if (res.getRespuestaExamen() != null) {
            if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                Integer idLista = Integer.valueOf(res.getValor());
                Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                if (valor.getValor().toLowerCase().equals("negativo")
                        || valor.getValor().trim().toLowerCase().contains("no reactor")
                        || valor.getValor().trim().toLowerCase().contains("no detectado")
                        || valor.getValor().trim().toUpperCase().contains("MTB-ND")) {
                    mostrar = true;
                    valorResultado = valor.getValor();
                }

            } else if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                if (res.getValor().toLowerCase().equals("negativo")
                        || res.getValor().trim().toLowerCase().contains("no reactor")
                        || res.getValor().trim().toLowerCase().contains("no detectado")
                        || res.getValor().trim().toUpperCase().contains("MTB-ND")) {
                    mostrar = true;
                    valorResultado = res.getValor();
                }
            }

        }
        return valorResultado + "," + mostrar;
    }

    private String getPositiveResult(DetalleResultadoFinal res) throws Exception {
        boolean mostrar= false;
        String valorResultado = null;

        if (res.getRespuesta() != null) {
            if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                Integer idLista = Integer.valueOf(res.getValor());
                Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                if (valor.getValor().trim().toLowerCase().contains("positivo")
                        || (valor.getValor().trim().toLowerCase().contains("reactor") && !valor.getValor().trim().toLowerCase().contains("no reactor"))
                        || (valor.getValor().trim().toLowerCase().contains("detectado") && !valor.getValor().trim().toLowerCase().contains("no detectado"))
                        || (valor.getValor().trim().toUpperCase().contains("MTB-DET") && !valor.getValor().trim().toUpperCase().contains("MTB-ND"))
                        && (!valor.getValor().trim().toLowerCase().contains("negativo") && !valor.getValor().trim().toLowerCase().contains("indetermin") && !valor.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = valor.getValor();
                }

            } else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                if (res.getValor().trim().toLowerCase().equals("positivo")
                        || (res.getValor().trim().toLowerCase().contains("reactor") && !res.getValor().trim().toLowerCase().contains("no reactor"))
                        || (res.getValor().trim().toLowerCase().contains("detectado") && !res.getValor().trim().toLowerCase().contains("no detectado"))
                        || (res.getValor().trim().toUpperCase().contains("MTB-DET") && !res.getValor().trim().toUpperCase().contains("MTB-ND"))
                        && (!res.getValor().trim().toLowerCase().contains("negativo") && !res.getValor().trim().toLowerCase().contains("indetermin") && !res.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = res.getValor();
                }
            }
        } else if (res.getRespuestaExamen() != null) {
            if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                Integer idLista = Integer.valueOf(res.getValor());
                Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(idLista);

                if (valor.getValor().trim().toLowerCase().equals("positivo")
                        || (valor.getValor().trim().toLowerCase().contains("reactor") && !valor.getValor().trim().toLowerCase().contains("no reactor"))
                        || (valor.getValor().trim().toLowerCase().contains("detectado") && !valor.getValor().trim().toLowerCase().contains("no detectado"))
                        || (valor.getValor().trim().toUpperCase().contains("MTB-DET") && !valor.getValor().trim().toUpperCase().contains("MTB-ND"))
                        && (!valor.getValor().trim().toLowerCase().contains("negativo") && !valor.getValor().trim().toLowerCase().contains("indetermin") && !valor.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = valor.getValor();
                }

            } else if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|TXT")) {//ABRIL2019
                if (res.getValor().trim().toLowerCase().equals("positivo")
                        || (res.getValor().trim().toLowerCase().contains("reactor") && !res.getValor().trim().toLowerCase().contains("no reactor"))
                        || (res.getValor().trim().toLowerCase().contains("detectado") && !res.getValor().trim().toLowerCase().contains("no detectado"))
                        || (res.getValor().trim().toUpperCase().contains("MTB-DET") && !res.getValor().trim().toUpperCase().contains("MTB-ND"))
                        && (!res.getValor().trim().toLowerCase().contains("negativo") && !res.getValor().trim().toLowerCase().contains("indetermin") && !res.getValor().trim().toLowerCase().equals("mx inadecuada"))) {
                    mostrar = true;
                    valorResultado = res.getValor();
                }
            }

        }
        return valorResultado + "," + mostrar;
    }

    @RequestMapping(value = "/posNegResults/posNegRequestToPDF", method = RequestMethod.GET)
    public
    @ResponseBody
    String posNegRequestToPDF(@RequestParam(value = "codes", required = true) String codes, @RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate", required = false) String toDate, HttpServletRequest request) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PDDocument doc = new PDDocument();
        Laboratorio labProcesa = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        String res = null;
        String fechaImpresion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());


        if (!codes.isEmpty()) {

            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
            page.setRotation(90);
            doc.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
            float xCenter;

            GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

            String pageNumber = String.valueOf(doc.getNumberOfPages());
            GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

            drawInfoLab(stream, page, labProcesa);

            float y = 400;
            float m = 20;

            //nombre del reporte
            xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 12, messageSource.getMessage("lbl.positiveResultReport", null, null).toUpperCase());
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.posNegReport", null, null).toUpperCase(), y, xCenter, stream, 12, PDType1Font.HELVETICA_BOLD);
            y = y - 10;
            //Rango de Fechas
            if (!fromDate.equals("") && !toDate.equals("")) {
                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.from", null, null), y, 55, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(fromDate, y, 100, stream, 12, PDType1Font.HELVETICA_BOLD);

                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.to", null, null), y, 660, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(toDate, y, 720, stream, 12, PDType1Font.HELVETICA_BOLD);
                y -= m;
            }


            String[] idSoli = codes.split(",");
            List<String[]> reqList = new ArrayList<String[]>();


            for (String idSolicitud : idSoli) {
                String nombreSolitud = null;
                String nombrePersona = null;
                String fechaAprob = null;
                String silais = null;
                String unidadSalud = null;
                String[] content = null;

                DaSolicitudDx soli = tomaMxService.getSolicitudDxByIdSolicitudUser(idSolicitud, seguridadService.obtenerNombreUsuario());
                DaSolicitudEstudio soliE = tomaMxService.getSolicitudEstByIdSolicitud(idSolicitud);


                if(soli != null){
                String cont = null;
                String valorResultado= null;
                boolean mostrar = false;

                    List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soli.getIdSolicitudDx());
                    for (DetalleResultadoFinal resu : finalRes) {

                        cont = getResult(resu);
                        String[] arrayContent = cont.split(",");
                        valorResultado = arrayContent[0];
                        mostrar = Boolean.parseBoolean(arrayContent[1]);

                        if(mostrar){
                            content = new String[7];
                            nombreSolitud = soli.getCodDx().getNombre();

                            if (soli.getIdTomaMx().getIdNotificacion().getPersona()!=null) {
                                nombrePersona = soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                                if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                    nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                                nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                                if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                    nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                            }else{
                                nombrePersona = soli.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre();
                            }
                            if (soli.getFechaAprobacion() != null) {
                                fechaAprob = DateUtil.DateToString(soli.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a");
                            }

                            if (soli.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                                silais = soli.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                            }

                            if (soli.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                                unidadSalud = soli.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019
                            }

                            content[0] = soli.getIdTomaMx() != null ? (soli.getIdTomaMx().getCodigoLab()!=null?soli.getIdTomaMx().getCodigoLab():soli.getIdTomaMx().getCodigoUnicoMx()) : "";
                            content[1] = fechaAprob != null ? fechaAprob : "";
                            content[2] = silais != null ? silais : "";
                            content[3] = unidadSalud != null ? unidadSalud : "";
                            content[4] = nombrePersona != null ? nombrePersona : "";
                            content[5] = nombreSolitud != null ? nombreSolitud : "";
                            content[6] = valorResultado != null ? valorResultado : "";
                            reqList.add(content);
                            break;
                        }

                    }

                }

                if (soliE != null) {
                    String cont = null;
                    String valorResultado = null;
                    boolean mostrar = false;

                    List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soliE.getIdSolicitudEstudio());
                    for (DetalleResultadoFinal resu : finalRes) {

                        cont = getResult(resu);
                        String[] arrayContent = cont.split(",");
                        valorResultado = arrayContent[0];
                        mostrar = Boolean.parseBoolean(arrayContent[1]);

                        if(mostrar){
                            content = new String[7];
                            nombreSolitud = soliE.getTipoEstudio().getNombre();

                            nombrePersona = soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                            nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();

                            if (soliE.getFechaAprobacion() != null) {
                                fechaAprob = DateUtil.DateToString(soliE.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a");
                            }

                            if (soliE.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                                silais = soliE.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                            }

                            if (soliE.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                                unidadSalud = soliE.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019
                            }

                            content[0] = soliE.getIdTomaMx() != null ? soliE.getIdTomaMx().getCodigoUnicoMx() : "";
                            content[1] = fechaAprob != null ? fechaAprob : "";
                            content[2] = silais != null ? silais : "";
                            content[3] = unidadSalud != null ? unidadSalud : "";
                            content[4] = nombrePersona != null ? nombrePersona : "";
                            content[5] = nombreSolitud != null ? nombreSolitud : "";
                            content[6] = valorResultado != null? valorResultado: "";
                            reqList.add(content);
                            break;
                        }
                    }

                }
            }

            //drawTable

            //Initialize table
            float margin = 50;
            float tableWidth = 730;
            float yStartNewPage = y;
            float yStart = yStartNewPage;
            float bottomMargin = 45;
            BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);

            //Create Header row
            Row headerRow = table.createRow(15f);
            table.setHeader(headerRow);

            //Create 2 column row
            Cell cell;
            Row row;

            //Create Fact header row
            Row factHeaderrow = table.createRow(15f);
            cell = factHeaderrow.createCell(12, messageSource.getMessage("lbl.lab.code.mx", null, null));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            cell.setFillColor(Color.LIGHT_GRAY);

            cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.approve.date", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(17, messageSource.getMessage("lbl.silais", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(20, messageSource.getMessage("lbl.health.unit", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.request.large", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.final.result", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            y -= 15;

            //Add multiple rows with random facts about Belgium
            for (String[] fact : reqList) {

                if (y < 300) {
                    table.draw();
                    stream.close();
                    page = new PDPage(PDPage.PAGE_SIZE_A4);
                    page.setRotation(90);
                    doc.addPage(page);
                    stream = new PDPageContentStream(doc, page);
                    stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
                    y = 470;
                    GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

                    pageNumber = String.valueOf(doc.getNumberOfPages());
                    GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

                    table = new BaseTable(y, y, bottomMargin, tableWidth, margin, doc, page, true, true);

                    //Create Header row
                    headerRow = table.createRow(15f);
                    table.setHeader(headerRow);

                    //Create Fact header row
                    factHeaderrow = table.createRow(15f);
                    cell = factHeaderrow.createCell(12, messageSource.getMessage("lbl.lab.code.mx", null, null));
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    cell.setFillColor(Color.LIGHT_GRAY);

                    cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.approve.date", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(17, messageSource.getMessage("lbl.silais", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(20, messageSource.getMessage("lbl.health.unit", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.request.large", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.final.result", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    y -= 15;

                }

                row = table.createRow(15f);
                cell = row.createCell(12, fact[0]);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setFontSize(10);
                y -= 15;

                for (int i = 1; i < fact.length; i++) {
                    if (i == 1) {
                        cell = row.createCell(16, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 2) {
                        cell = row.createCell(17, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 3) {
                        cell = row.createCell(20, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 4) {
                        cell = row.createCell(13, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 5) {
                        cell = row.createCell(10, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 6) {
                        cell = row.createCell(10, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    }
                }
            }
            table.draw();

            //fecha impresi�n
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.print.datetime", null, null), 100, 605, stream, 10, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaImpresion, 100, 710, stream, 10, PDType1Font.HELVETICA);

            stream.close();

            doc.save(output);
            doc.close();
            // generate the file
            res = Base64.encodeBase64String(output.toByteArray());

        }

        return res;
    }

    /**
     * M�todo que se llama al entrar a la opci�n de menu de Reportes "Control de Calidad".
     *
     * @param request para obtener informaci�n de la petici�n del cliente
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "/qualityControl/init", method = RequestMethod.GET)
    public ModelAndView initSearchQCForm(HttpServletRequest request) throws Exception {
        logger.debug("Iniciando Reporte de Recepci�n");
        ModelAndView mav = new ModelAndView();
        List<EntidadesAdtvas> entidadesAdtvases = CallRestServices.getEntidadesAdtvas();
        List<Laboratorio> laboratorios = laboratoriosService.getLaboratoriosRegionales();
        List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
        laboratorio = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        boolean esCNDR = true;
        //solo cuÃ¡ndo no es usuario del CNDR se setea el laboratorio
        if (laboratorio!=null && !laboratorio.getCodigo().equalsIgnoreCase("CNDR"))
            esCNDR = false;
        mav.addObject("entidades", entidadesAdtvases);
        mav.addObject("tipoMuestra", tipoMxList);
        mav.addObject("laboratorios",laboratorios);
        mav.addObject("esCNDR", esCNDR);
        mav.setViewName("reportes/qualityControlReport");
        return mav;
    }

    /**
     * M�todo para realizar la b�squeda de Mx recepcionadas
     *
     * @param filtro JSon con los datos de los filtros a aplicar en la b�squeda(Rango Fec Recepcion, Tipo Mx, SILAIS, unidad salud, tipo solicitud, descripcion)
     * @return String con las Mx encontradas
     * @throws Exception
     */
    @RequestMapping(value = "searchSamplesQC", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String fetchMxQCJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception {
        logger.info("Obteniendo las mx recepcionadas seg�n filtros en JSON");
        FiltroMx filtroMx = jsonToFiltroMx(filtro);
        filtroMx.setControlCalidad(true);
        laboratorio = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        //solo cuÃ¡ndo no es usuario del CNDR se setea el laboratorio
        if (laboratorio!=null && !laboratorio.getCodigo().equalsIgnoreCase("CNDR")) filtroMx.setCodLaboratio(laboratorio.getCodigo());
        List<DaSolicitudDx> receivedList = reportesService.getQCRoutineRequestByFilter(filtroMx);
       return solicitudesDxToJson(receivedList, true);
    }

    private  String solicitudesDxToJson(List<DaSolicitudDx> solicitudDxList, boolean incluirResultados) throws Exception {
        String jsonResponse="";
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice=0;
        List<Catalogo> tiposNotificacion = CallRestServices.getCatalogos(CatalogConstants.TipoNotificacion);//ABRIL2019
        for(DaSolicitudDx diagnostico : solicitudDxList){
            Map<String, String> map = new HashMap<String, String>();
            map.put("codigoUnicoMx", diagnostico.getIdTomaMx().getCodigoUnicoMx());
            map.put("codigoLab", diagnostico.getIdTomaMx().getCodigoLab());
            map.put("idTomaMx", diagnostico.getIdTomaMx().getIdTomaMx());
            map.put("fechaTomaMx",DateUtil.DateToString(diagnostico.getIdTomaMx().getFechaHTomaMx(),"dd/MM/yyyy")+
                    (diagnostico.getIdTomaMx().getHoraTomaMx()!=null?" "+diagnostico.getIdTomaMx().getHoraTomaMx():""));
            if (diagnostico.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion()!=null) {
                map.put("codSilais", diagnostico.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
            }else{
                map.put("codSilais","");
            }
            if (diagnostico.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion()!=null) {
                map.put("codUnidadSalud", diagnostico.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
            }else{
                map.put("codUnidadSalud","");
            }
            map.put("tipoMuestra", diagnostico.getIdTomaMx().getCodTipoMx().getNombre());
            map.put("tipoNotificacion", catalogosService.buscarValorCatalogo( tiposNotificacion, diagnostico.getIdTomaMx().getIdNotificacion().getCodTipoNotificacion()));//ABRIL2019
            //Si hay fecha de inicio de sintomas se muestra
            Date fechaInicioSintomas = diagnostico.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas();
            if (fechaInicioSintomas!=null)
                map.put("fechaInicioSintomas",DateUtil.DateToString(fechaInicioSintomas,"dd/MM/yyyy"));
            else
                map.put("fechaInicioSintomas"," ");

            //Si hay persona
            if (diagnostico.getIdTomaMx().getIdNotificacion().getPersona()!=null){
                /// se obtiene el nombre de la persona asociada a la ficha
                String nombreCompleto = "";
                nombreCompleto = diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                if (diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre()!=null)
                    nombreCompleto = nombreCompleto +" "+ diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                nombreCompleto = nombreCompleto+" "+ diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                if (diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido()!=null)
                    nombreCompleto = nombreCompleto +" "+ diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                map.put("persona",nombreCompleto);
            }else{
                map.put("persona"," ");
            }

            map.put("solicitud", diagnostico.getCodDx().getNombre());
            map.put("idSolicitud", diagnostico.getIdSolicitudDx());
            map.put("fechaSolicitud",DateUtil.DateToString(diagnostico.getFechaHSolicitud(),"dd/MM/yyyy hh:mm:ss a"));

            if(diagnostico.getAprobada()!=null && diagnostico.getAprobada() && diagnostico.getFechaAprobacion()!=null){
                map.put("fechaAprobacion",DateUtil.DateToString(diagnostico.getFechaAprobacion(),"dd/MM/yyyy hh:mm:ss a"));
            }else {
                map.put("fechaAprobacion","");
            }
            TrasladoMx trasladoMxCC = trasladosService.getTrasladoCCMx(diagnostico.getIdTomaMx().getIdTomaMx());
            if (trasladoMxCC!=null) {
                map.put("laboratorio", trasladoMxCC.getLaboratorioOrigen().getNombre());
            }else {
                map.put("laboratorio", "-");
            }

            if (incluirResultados){
                //detalle resultado final solicitud
                List<ResultadoSolicitud> detalleResultadoCC = resultadoFinalService.getDetResActivosBySolicitudV2(diagnostico.getIdSolicitudDx());
                String resCC = parseResultDetailsV2(detalleResultadoCC, false);
                map.put("resultadocc", resCC);
                //DaSolicitudDx solicitudNoCC = tomaMxService.getSolicitudDxByMxDxNoCC(diagnostico.getIdTomaMx().getIdTomaMx(),diagnostico.getCodDx().getIdDiagnostico());
                //List<ResultadoSolicitud> detalleResultadoNoCC = resultadoFinalService.getDetResActivosBySolicitudV2(solicitudNoCC.getIdSolicitudDx());
                String idSolicitudNoCC = tomaMxService.getIdSolicitudDxByMxDxNoCC(diagnostico.getIdTomaMx().getIdTomaMx(), diagnostico.getCodDx().getIdDiagnostico());
                List<ResultadoSolicitud> detalleResultadoNoCC = resultadoFinalService.getDetResActivosBySolicitudV2(idSolicitudNoCC);
                String resNoCC = parseResultDetailsV2(detalleResultadoNoCC, false);
                map.put("resultado",resNoCC);
                map.put("coincide", (resCC.equalsIgnoreCase(resNoCC)?messageSource.getMessage("lbl.yes",null,null):messageSource.getMessage("lbl.no",null,null)));
            }
            mapResponse.put(indice, map);
            indice ++;
        }

        jsonResponse = new Gson().toJson(mapResponse);
        UnicodeEscaper escaper     = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    @RequestMapping(value = "expQCToPDF", method = RequestMethod.GET)
    public
    @ResponseBody
    String expQCToPDF(@RequestParam(value = "codigos", required = true) String codigos, HttpServletRequest request) throws IOException, COSVisitorException, ParseException, Exception {
        String res = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        List<DetalleResultadoFinal> detalleResultado = null;
        List<DetalleResultadoFinal> detalleResultadoNoCC = null;
        //String fechaImpresion = null;
        PDDocument doc = new PDDocument();
        List<DaSolicitudDx> solicitudDxList = null;
        PDPageContentStream stream = null;
        Laboratorio labProcesa = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        List<Catalogo> tiposNotificacion = CallRestServices.getCatalogos(CatalogConstants.TipoNotificacion);//ABRIL2019
        String[] tomasArray = codigos.split(",");
        boolean reporteCRes = false;

        //Prepare the document.
        float y = 400;
        float m1 = 20;

        PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        page.setRotation(90);
        doc.addPage(page);
        stream = new PDPageContentStream(doc, page);
        stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
        GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);
        drawInfoLab(stream, page, labProcesa);
        float xCenter;

        xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 14, messageSource.getMessage("lbl.qualityControl.report", null, null));
        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.qualityControl.report", null, null), y, xCenter, stream, 14, PDType1Font.HELVETICA_BOLD);
        y = y - 10;
        for (String codigoMx : tomasArray) {
            solicitudDxList = tomaMxService.getSolicitudesDxQCAprobByToma(codigoMx);

            //Obtener las respuestas activas de la solicitud
            if (solicitudDxList.size()>0) {
                reporteCRes = true;
                String[][] contentTable1 = new String[solicitudDxList.size()][8];
                int iteracion=0;

                for (DaSolicitudDx solicitudDx : solicitudDxList) {
                    DaSolicitudDx solicitudNoCC = tomaMxService.getSolicitudDxByMxDxNoCC(solicitudDx.getIdTomaMx().getIdTomaMx(),solicitudDx.getCodDx().getIdDiagnostico());
                    detalleResultado = resultadoFinalService.getDetResActivosBySolicitud(solicitudDx.getIdSolicitudDx());
                    detalleResultadoNoCC = resultadoFinalService.getDetResActivosBySolicitud(solicitudNoCC.getIdSolicitudDx());
                    //fechaImpresion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

                    if (detalleResultado != null) {
                        String fechaAprobacion = "";
                        String labOrigen = "";
                        String nombrePersona = "";
                        String fechaSolicitud = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(solicitudDx.getFechaHSolicitud());
                        String codigoLabMx = solicitudDx.getIdTomaMx().getCodigoLab();
                        String tipoMx = solicitudDx.getIdTomaMx().getCodTipoMx().getNombre();
                        String tipoNoti = catalogosService.buscarValorCatalogo( tiposNotificacion, solicitudDx.getIdTomaMx().getIdNotificacion().getCodTipoNotificacion());//ABRIL2019

                        String nombreSoli = solicitudDx.getCodDx().getNombre();

                        if (solicitudDx.getFechaAprobacion() != null) {
                            fechaAprobacion = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(solicitudDx.getFechaAprobacion());
                        }

                        if (solicitudDx.getIdTomaMx().getIdNotificacion().getPersona()!=null) {
                            nombrePersona = solicitudDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (solicitudDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null) {
                                nombrePersona = nombrePersona + " " + solicitudDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                                nombrePersona = nombrePersona + " " + solicitudDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            } else {
                                nombrePersona = nombrePersona + " " + solicitudDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();

                            }
                            if (solicitudDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null) {
                                nombrePersona = nombrePersona + " " + solicitudDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                            }
                        }else{
                            nombrePersona = solicitudDx.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre();
                        }

                        TrasladoMx trasladoMxCC = trasladosService.getTrasladoCCMx(solicitudDx.getIdTomaMx().getIdTomaMx());
                        if (trasladoMxCC!=null) {
                            labOrigen = trasladoMxCC.getLaboratorioOrigen().getNombre();
                        }
                        contentTable1[iteracion][0]=nombreSoli;
                        contentTable1[iteracion][1]=fechaSolicitud;
                        contentTable1[iteracion][2]=fechaAprobacion;
                        contentTable1[iteracion][3]=codigoLabMx;
                        contentTable1[iteracion][4]=tipoMx;
                        contentTable1[iteracion][5]=tipoNoti;
                        contentTable1[iteracion][6]=nombrePersona;
                        contentTable1[iteracion][7]=labOrigen;
                        boolean coincide = false;

                        String pageNumber = String.valueOf(doc.getNumberOfPages());
                        GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);
                        //y -= m1;

                        float tableHeight = drawSolicitudTable(contentTable1, doc, page, y);
                        y -=tableHeight;
                        if (y < 100) {
                            stream.close();
                            page = new PDPage(PDPage.PAGE_SIZE_A4);
                            page.setRotation(90);
                            doc.addPage(page);
                            stream = new PDPageContentStream(doc, page);
                            stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
                            y = 470;
                            GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

                            pageNumber = String.valueOf(doc.getNumberOfPages());
                            GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);
                        }
                        boolean lista = false;
                        String valorOrigen = "";
                        String valorCC = "";
                        String respuesta;
                        String[][] content = new String[detalleResultado.size()][4];


                        int numFila = 0;
                        for (DetalleResultadoFinal resul : detalleResultado) {
                            if (resul.getRespuesta() != null) {
                                respuesta = resul.getRespuesta().getNombre();
                                lista = resul.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                            } else {
                                respuesta = resul.getRespuestaExamen().getNombre();
                                lista = resul.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST");//ABRIL2019
                            }

                            for (DetalleResultadoFinal resultadoFinalNoCC : detalleResultadoNoCC){
                                if (!lista) {
                                    valorOrigen = resultadoFinalNoCC.getValor();
                                    valorCC = resul.getValor();
                                    if (resul.getValor().equalsIgnoreCase(resultadoFinalNoCC.getValor())) {
                                        coincide = true;
                                        break;
                                    }
                                }else {
                                    Catalogo_Lista catLista = conceptoService.getCatalogoListaById(Integer.valueOf(resul.getValor()));
                                    Catalogo_Lista catListaNoCC = conceptoService.getCatalogoListaById(Integer.valueOf(resultadoFinalNoCC.getValor()));
                                    if (catLista!=null && catListaNoCC !=null) {
                                        valorOrigen = catListaNoCC.getValor();
                                        valorCC = catLista.getValor();
                                        if (catLista.getValor().equalsIgnoreCase(catListaNoCC.getValor())) {
                                            coincide = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            content[numFila][0] = respuesta;
                            content[numFila][1] = valorOrigen;
                            content[numFila][2] = valorCC;
                            content[numFila][3] = (coincide?messageSource.getMessage("lbl.yes",null,null):messageSource.getMessage("lbl.no",null,null)); //valor;
                            numFila++;
                        }

                        tableHeight = drawResultTable(content, doc, page, y);
                        y -=tableHeight;
                        if (y < 100) {
                            stream.close();
                            page = new PDPage(PDPage.PAGE_SIZE_A4);
                            page.setRotation(90);
                            doc.addPage(page);
                            stream = new PDPageContentStream(doc, page);
                            stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
                            y = 470;
                            GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

                            pageNumber = String.valueOf(doc.getNumberOfPages());
                            GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);
                        }
                    }
                    iteracion++;
                }
            }
        }
        stream.close();
        if (reporteCRes) {
            doc.save(output);
            doc.close();
            // generate the file
            res = Base64.encodeBase64String(output.toByteArray());
        }

        return res;
    }

    private float drawSolicitudTable(String[][] content, PDDocument doc, PDPage page, float y) throws IOException {

        //Initialize table
        float height = 0;
        float margin = 40;
        float tableWidth = 770;
        float yStartNewPage = y;
        float yStart = yStartNewPage;
        float bottomMargin = 45;
        BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);

        //Create Header row
        //Row headerRow = table.createRow(15f);
        //table.setHeader(headerRow);

        //Create 2 column row
        Row row;
        Cell cell;

        //Create Fact header row
        Row factHeaderrow = table.createRow(15f);

        cell = factHeaderrow.createCell(15, messageSource.getMessage("lbl.request.name1", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell(11, messageSource.getMessage("lbl.request.date", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell(12, messageSource.getMessage("lbl.approve.date", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.unique.code.mx", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell(14, messageSource.getMessage("lbl.sample.type1", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell(11, messageSource.getMessage("lbl.notification", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell(15, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell(12, messageSource.getMessage("lbl.transfer.origin.lab", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        height = factHeaderrow.getHeight();
        //Add multiple rows with random facts about Belgium

        for (String[] fact : content) {
            row = table.createRow(15f);
            for (int i = 0; i < fact.length; i++) {
                switch (i) {
                    case 0:
                    case 6: {
                        cell = row.createCell(15, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    case 1:
                    case 5: {
                        cell = row.createCell(11, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    case 2:
                    case 7: {
                        cell = row.createCell(12, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    case 3 :{
                        cell = row.createCell(10, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    default: {
                        cell = row.createCell(14, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                }
            }
            height += row.getHeight();
        }
        table.draw();
        return  height;
    }

    private float drawResultTable(String[][] content, PDDocument doc, PDPage page, float y) throws IOException {

        float height = 0;
        //Initialize table
        float margin = 40;
        float tableWidth = 770;
        float yStartNewPage = y;
        float yStart = yStartNewPage;
        float bottomMargin = 45;
        BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);

        //Create Header row
        Row headerRow = table.createRow(15f);
        table.setHeader(headerRow);

        //Create 2 column row
        Row row;
        Cell cell;

        //Create Fact header row
        Row factHeaderrow = table.createRow(15f);

        cell = factHeaderrow.createCell(38, messageSource.getMessage("lbl.approve.response", null, null));
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);
        cell.setFillColor(Color.LIGHT_GRAY);

        cell = factHeaderrow.createCell((24), messageSource.getMessage("lbl.val1.cc", null, null));
        cell.setFillColor(Color.lightGray);
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);

        cell = factHeaderrow.createCell((26), messageSource.getMessage("lbl.val2.cc", null, null));
        cell.setFillColor(Color.lightGray);
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);

        cell = factHeaderrow.createCell((12), messageSource.getMessage("lbl.match", null, null));
        cell.setFillColor(Color.lightGray);
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(10);

        //Add multiple rows with random facts about Belgium
        height = factHeaderrow.getHeight();
        for (String[] fact : content) {
            row = table.createRow(15f);
            for (int i = 0; i < fact.length; i++) {
                switch (i) {
                    case 0: {
                        cell = row.createCell(38, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    case 1: {
                        cell = row.createCell(24, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    case 2: {
                        cell = row.createCell(26, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    case 3: {
                        cell = row.createCell(12, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                    default: {
                        cell = row.createCell(10, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                        break;
                    }
                }
            }
            height += row.getHeight();
        }
        table.draw();
        return height;
    }

    private void drawReportHeader(PDPageContentStream stream, DaSolicitudDx soliDx, DaSolicitudEstudio soliE) throws IOException {
        String nombreSoli = null;
        String nombrePersona = null;
        String nombreSilais = null;
        String nombreUS = null;
        int edad = 0;
        String sexo = null;
        String fis = null;
        String labOrigen = "----------------";
        float inY = 610;
        float m = 20;

        if (soliDx != null || soliE != null) {

            if (soliDx != null) {
                nombrePersona = soliDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                nombreSoli = soliDx.getCodDx().getNombre();

                if (soliDx.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas() != null) {
                    fis = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(soliDx.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas());
                } else {
                    fis = "---------------";
                }

                if (soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null) {
                    nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                    nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                } else {
                    nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();

                }
                if (soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null) {
                    nombrePersona = nombrePersona + " " + soliDx.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                }

                if (soliDx.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                    nombreSilais = soliDx.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                    nombreUS = soliDx.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019

                } else {
                    nombreSilais = "---------------";
                    nombreUS = "---------------";
                }

                /*ABRIL2019
                if (soliDx.getIdTomaMx().getIdNotificacion().getPersona().getDescSexo() != null) {
                    sexo = soliDx.getIdTomaMx().getIdNotificacion().getPersona().getDescSexo();
                } else {
                    sexo = "----------------";
                }*/

                if (soliDx.getIdTomaMx().getIdNotificacion().getPersona().getFechaNacimiento() != null) {
                    String fechaformateada = DateUtil.DateToString(soliDx.getIdTomaMx().getIdNotificacion().getPersona().getFechaNacimiento(), "dd/MM/yyyy");
                    edad = DateUtil.edad(fechaformateada);
                }

                TrasladoMx trasladoMxCC = trasladosService.getTrasladoCCMx(soliDx.getIdTomaMx().getIdTomaMx());
                if (trasladoMxCC!=null) {
                     labOrigen = trasladoMxCC.getLaboratorioOrigen().getNombre();
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

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.full.origin.lab", null, null) + ": ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(labOrigen, inY, 160, stream, 12, PDType1Font.HELVETICA);
            inY -= m;

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.request.name1", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(nombreSoli.toUpperCase(), inY, 80, stream, 12, PDType1Font.HELVETICA);
            inY -= m;

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.name1", null, null) + " ", inY, 15, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(nombrePersona, inY, 80, stream, 12, PDType1Font.HELVETICA);

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.age", null, null) + " ", inY, 380, stream, 14, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(String.valueOf(edad), inY, 425, stream, 12, PDType1Font.HELVETICA);

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

    /**
     * M�todo que se llama al entrar a la opci�n de menu de Reportes "Reporte General de Resultados".
     *
     * @param request para obtener informaci�n de la petici�n del cliente
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "/general/init", method = RequestMethod.GET)
    public ModelAndView init(HttpServletRequest request) throws Exception {
        logger.debug("Iniciando Reporte General de Resultados");
        usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
        ModelAndView mav = new ModelAndView();
        List<EntidadesAdtvas> entidadesAdtvases = new ArrayList<EntidadesAdtvas>();
        List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
        List<Area> areas = areaService.getAreas();
        List<Laboratorio> laboratorios = null;
        if (usuario.getNivelCentral()!=null && usuario.getNivelCentral()) {
            laboratorios = laboratoriosService.getLaboratoriosRegionales();
            //areas.add(catalogosService.getAreaRep("AREAREP|PAIS"));
            entidadesAdtvases = CallRestServices.getEntidadesAdtvas();
        }else{
            laboratorio = seguridadService.getLaboratorioUsuario(usuario.getUsername());
            if (laboratorio!=null) {
                entidadesAdtvases = laboratoriosService.getEntidadesAdtvasAsignadasLab(laboratorio.getCodigo());
                laboratorios = new ArrayList<Laboratorio>();
                laboratorios.add(laboratorio);
            }
        }
        mav.addObject("laboratorios", laboratorios);
        mav.addObject("areas", areas);
        mav.addObject("entidades", entidadesAdtvases);
        mav.addObject("tipoMuestra", tipoMxList);
        mav.setViewName("reportes/generalReportResults");

        return mav;
    }

    /**
     * M�todo para realizar la b�squeda de Resultados positivos
     *
     * @param filtro JSon con los datos de los filtros a aplicar en la b�squeda(Rango Fec Aprob, SILAIS, unidad salud, tipo solicitud, descripcion)
     * @return String con las solicitudes encontradas
     * @throws Exception
     */
    @RequestMapping(value = "/general/searchRequestGR", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String fetchReqJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception {
        logger.info("Obteniendo las solicitudes seg�n filtros en JSON");
        FiltroMx filtroMx = jsonToFiltroMx(filtro);
        List<DaSolicitudDx> reqList = null;
        List<DaSolicitudEstudio> studiesList = null;

        if (filtroMx.getCodTipoSolicitud() != null) {
            if (filtroMx.getCodTipoSolicitud().equals("Estudio")) {
                studiesList = reportesService.getPositiveStudyRequestByFilter(filtroMx);
            } else {
                reqList = reportesService.getPositiveRoutineRequestByFilter(filtroMx);
            }

        } else {
            reqList = reportesService.getPositiveRoutineRequestByFilter(filtroMx);
            studiesList = reportesService.getPositiveStudyRequestByFilter(filtroMx);
        }

        return reqToJson(reqList, studiesList);
    }

    /**
     * M�todo que convierte una lista de solicitudes a un string con estructura Json
     *
     * @param reqList     lista con las mx recepcionadas a convertir
     * @param studiesList lista con las mx estudio recepcionadas a convertir
     * @return String
     */
    private String reqToJson(List<DaSolicitudDx> reqList, List<DaSolicitudEstudio> studiesList) throws Exception {
        String jsonResponse;
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice = 0;


        if (reqList != null || studiesList != null) {
            if (reqList != null) {
                for (DaSolicitudDx soli : reqList) {

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("solicitud", soli.getCodDx().getNombre());
                    map.put("idSolicitud", soli.getIdSolicitudDx());
                    map.put("codigoUnicoMx", soli.getIdTomaMx().getCodigoLab()!=null?soli.getIdTomaMx().getCodigoLab():soli.getIdTomaMx().getCodigoUnicoMx());
                    map.put("fechaAprobacion", DateUtil.DateToString(soli.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a"));


                    if (soli.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                        map.put("codSilais", soli.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                    } else {
                        map.put("codSilais", "");
                    }
                    if (soli.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                        map.put("codUnidadSalud", soli.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                    } else {
                        map.put("codUnidadSalud", "");
                    }

                    //Si hay persona
                    if (soli.getIdTomaMx().getIdNotificacion().getPersona() != null) {
                        /// se obtiene el nombre de la persona asociada a la ficha
                        String nombreCompleto = "";
                        nombreCompleto = soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                        if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                            nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                        nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                        if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                            nombreCompleto = nombreCompleto + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                        map.put("persona", nombreCompleto);
                    } else if (soli.getIdTomaMx().getIdNotificacion().getSolicitante() != null) {
                        map.put("persona", soli.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre());
                    } else if (soli.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                    	map.put("persona", soli.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                    } else {
                        map.put("persona", " ");
                    }
                    List<DetalleResultadoFinal> resFinal = resultadoFinalService.getDetResActivosBySolicitud(soli.getIdSolicitudDx());
                    map.put("resultado",parseResultDetails(resFinal));
                    map.put("procesa", (resFinal.size()>0?resFinal.get(0).getUsuarioRegistro().getUsername():""));
                    map.put("aprueba", (soli.getUsuarioAprobacion()!=null?soli.getUsuarioAprobacion().getUsername():""));
                    mapResponse.put(indice, map);
                    indice++;
                }
            }

            if (studiesList != null) {

                for (DaSolicitudEstudio soliE : studiesList) {

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("solicitud", soliE.getTipoEstudio().getNombre());
                    map.put("idSolicitud", soliE.getIdSolicitudEstudio());
                    map.put("codigoUnicoMx", soliE.getIdTomaMx().getCodigoUnicoMx());
                    map.put("fechaAprobacion", DateUtil.DateToString(soliE.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a"));

                    if (soliE.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                        map.put("codSilais", soliE.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                    } else {
                        map.put("codSilais", "");
                    }
                    if (soliE.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                        map.put("codUnidadSalud", soliE.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                    } else {
                        map.put("codUnidadSalud", "");
                    }

                    //Si hay persona
                    if (soliE.getIdTomaMx().getIdNotificacion().getPersona() != null) {
                        /// se obtiene el nombre de la persona asociada a la ficha
                        String nombreCompleto = "";
                        nombreCompleto = soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                        if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                            nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                        nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                        if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                            nombreCompleto = nombreCompleto + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                        map.put("persona", nombreCompleto);
                    } else if (soliE.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH()!=null){
                        map.put("persona", soliE.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                    } else {
                        map.put("persona", " ");
                    }

                    List<DetalleResultadoFinal> resFinal = resultadoFinalService.getDetResActivosBySolicitud(soliE.getIdSolicitudEstudio());
                    map.put("resultado",parseResultDetails(resFinal));
                    map.put("procesa", (resFinal.size()>0?resFinal.get(0).getUsuarioRegistro().getUsername():""));
                    map.put("aprueba", (soliE.getUsuarioAprobacion()!=null?soliE.getUsuarioAprobacion().getUsername():""));
                    mapResponse.put(indice, map);
                    indice++;
                }
            }
        }

        jsonResponse = new Gson().toJson(mapResponse);
        //escapar caracteres especiales, escape de los caracteres con valor num�rico mayor a 127
        UnicodeEscaper escaper = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);

    }

    private String parseResultDetails(List<DetalleResultadoFinal> resultList){
        String resultados="";
        for(DetalleResultadoFinal res: resultList){
            if (res.getRespuesta()!=null) {
                resultados+=(resultados.isEmpty()?res.getRespuesta().getNombre():", "+res.getRespuesta().getNombre());
                if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                    Catalogo_Lista cat_lista = resultadoFinalService.getCatalogoLista(res.getValor());
                    resultados+=": "+cat_lista.getValor();
                }else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LOG")) {//ABRIL2019
                    String valorBoleano = (Boolean.valueOf(res.getValor())?"lbl.yes":"lbl.no");
                    resultados+=": "+valorBoleano;
                } else {
                    resultados+=": "+res.getValor();
                }
            }else if (res.getRespuestaExamen()!=null){
                resultados+=(resultados.isEmpty()?res.getRespuestaExamen().getNombre():", "+res.getRespuestaExamen().getNombre());
                if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                    Catalogo_Lista cat_lista = resultadoFinalService.getCatalogoLista(res.getValor());
                    resultados+=": "+cat_lista.getValor();
                } else if (res.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LOG")) {//ABRIL2019
                    String valorBoleano = (Boolean.valueOf(res.getValor())?"lbl.yes":"lbl.no");
                    resultados+=": "+valorBoleano;
                }else {
                    resultados+=": "+res.getValor();
                }
            }
        }
        return resultados;
    }

    private String parseResultDetailsV2(List<ResultadoSolicitud> resultList, boolean incluirAuxiliares){
        String resultados="";
        for(ResultadoSolicitud res: resultList){
            if (res.getRespuesta()!=null) {
                if (res.getTipo().equals("TPDATO|LIST")) {
                    resultados+=(resultados.isEmpty()?res.getRespuesta():", "+res.getRespuesta());
                    Catalogo_Lista cat_lista = resultadoFinalService.getCatalogoLista(res.getValor());
                    resultados+=": "+cat_lista.getValor();
                }else if (res.getTipo().equals("TPDATO|LOG")) {
                    resultados+=(resultados.isEmpty()?res.getRespuesta():", "+res.getRespuesta());
                    String valorBoleano = (Boolean.valueOf(res.getValor())?"lbl.yes":"lbl.no");
                    resultados+=": "+valorBoleano;
                } else {
                    if (incluirAuxiliares){
                        resultados+=(resultados.isEmpty()?res.getRespuesta():", "+res.getRespuesta());
                        resultados+=": "+res.getValor();
                    }
                }
            }else if (res.getRespuestaExamen()!=null){
                if (res.getTipoExamen().equals("TPDATO|LIST")) {
                    resultados+=(resultados.isEmpty()?res.getRespuestaExamen():", "+res.getRespuestaExamen());
                    Catalogo_Lista cat_lista = resultadoFinalService.getCatalogoLista(res.getValor());
                    resultados+=": "+cat_lista.getValor();
                } else if (res.getTipoExamen().equals("TPDATO|LOG")) {
                    resultados+=(resultados.isEmpty()?res.getRespuestaExamen():", "+res.getRespuestaExamen());
                    String valorBoleano = (Boolean.valueOf(res.getValor())?"lbl.yes":"lbl.no");
                    resultados+=": "+valorBoleano;
                }else {
                    if (incluirAuxiliares){
                        resultados+=(resultados.isEmpty()?res.getRespuestaExamen():", "+res.getRespuestaExamen());
                        resultados+=": "+res.getValor();
                    }
                }
            }
        }
        return resultados;
    }


    @RequestMapping(value = "/general/generalRepToPDF", method = RequestMethod.GET)
    public
    @ResponseBody
    String generalRepToPDF(@RequestParam(value = "codes", required = true) String codes, @RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate", required = false) String toDate, HttpServletRequest request) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PDDocument doc = new PDDocument();
        Laboratorio labProcesa = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        String res = null;
        String fechaImpresion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());


        if (!codes.isEmpty()) {

            PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
            page.setRotation(90);
            doc.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
            float xCenter;

            GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

            String pageNumber = String.valueOf(doc.getNumberOfPages());
            GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

            drawInfoLab(stream, page, labProcesa);

            float y = 400;
            float m = 20;

            //nombre del reporte
            xCenter = centerTextPositionX(page, PDType1Font.HELVETICA_BOLD, 12, messageSource.getMessage("lbl.general.report.results", null, null).toUpperCase());
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.general.report.results", null, null).toUpperCase(), y, xCenter, stream, 12, PDType1Font.HELVETICA_BOLD);
            y = y - 10;
            //Rango de Fechas
            if (!fromDate.equals("") && !toDate.equals("")) {
                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.from", null, null), y, 55, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(fromDate, y, 100, stream, 12, PDType1Font.HELVETICA_BOLD);

                GeneralUtils.drawTEXT(messageSource.getMessage("lbl.to", null, null), y, 660, stream, 12, PDType1Font.HELVETICA_BOLD);
                GeneralUtils.drawTEXT(toDate, y, 720, stream, 12, PDType1Font.HELVETICA_BOLD);
                y -= m;
            }


            String[] idSoli = codes.split(",");
            List<String[]> reqList = new ArrayList<String[]>();


            for (String idSolicitud : idSoli) {
                String nombreSolitud = null;
                String nombrePersona = null;
                String fechaAprob = null;
                String silais = null;
                String unidadSalud = null;
                String[] content = null;
                String valorResultado = null;

                DaSolicitudDx soli = tomaMxService.getSolicitudDxByIdSolicitudUser(idSolicitud, seguridadService.obtenerNombreUsuario());
                DaSolicitudEstudio soliE = tomaMxService.getSolicitudEstByIdSolicitud(idSolicitud);


                if(soli != null){

                    List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soli.getIdSolicitudDx());

                            valorResultado = loadFinalResult(finalRes);

                            content = new String[7];
                            nombreSolitud = soli.getCodDx().getNombre();

                            if (soli.getIdTomaMx().getIdNotificacion().getPersona()!=null) {
                                nombrePersona = soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                                if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                    nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                                nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                                if (soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                    nombrePersona = nombrePersona + " " + soli.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                            }else{
                                nombrePersona = soli.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre();
                            }
                            if (soli.getFechaAprobacion() != null) {
                                fechaAprob = DateUtil.DateToString(soli.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a");
                            }

                            if (soli.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                                silais = soli.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                            }

                            if (soli.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                                unidadSalud = soli.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019
                            }

                            content[0] = soli.getIdTomaMx() != null ? (soli.getIdTomaMx().getCodigoLab()!=null?soli.getIdTomaMx().getCodigoLab():soli.getIdTomaMx().getCodigoUnicoMx()) : "";
                            content[1] = fechaAprob != null ? fechaAprob : "";
                            content[2] = silais != null ? silais : "";
                            content[3] = unidadSalud != null ? unidadSalud : "";
                            content[4] = nombrePersona != null ? nombrePersona : "";
                            content[5] = nombreSolitud != null ? nombreSolitud : "";
                            content[6] = valorResultado != null ? valorResultado : "";
                            reqList.add(content);
                }

                if (soliE != null) {

                    String valorResu = null;

                    List<DetalleResultadoFinal> finalRes = resultadoFinalService.getDetResActivosBySolicitud(soliE.getIdSolicitudEstudio());

                            valorResu = loadFinalResult(finalRes);
                            content = new String[7];
                            nombreSolitud = soliE.getTipoEstudio().getNombre();

                            nombrePersona = soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                            nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            if (soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                nombrePersona = nombrePersona + " " + soliE.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();

                            if (soliE.getFechaAprobacion() != null) {
                                fechaAprob = DateUtil.DateToString(soliE.getFechaAprobacion(), "dd/MM/yyyy hh:mm:ss a");
                            }

                            if (soliE.getIdTomaMx().getIdNotificacion().getCodSilaisAtencion() != null) {
                                silais = soliE.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion();//ABRIL2019
                            }

                            if (soliE.getIdTomaMx().getIdNotificacion().getCodUnidadAtencion() != null) {
                                unidadSalud = soliE.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion();//ABRIL2019
                            }

                            content[0] = soliE.getIdTomaMx() != null ? soliE.getIdTomaMx().getCodigoUnicoMx() : "";
                            content[1] = fechaAprob != null ? fechaAprob : "";
                            content[2] = silais != null ? silais : "";
                            content[3] = unidadSalud != null ? unidadSalud : "";
                            content[4] = nombrePersona != null ? nombrePersona : "";
                            content[5] = nombreSolitud != null ? nombreSolitud : "";
                            content[6] = valorResu != null? valorResu: "";
                            reqList.add(content);
                }
            }

            //drawTable

            //Initialize table
            float margin = 50;
            float tableWidth = 730;
            float yStartNewPage = y;
            float yStart = yStartNewPage;
            float bottomMargin = 45;
            BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);

            //Create Header row
            Row headerRow = table.createRow(15f);
            table.setHeader(headerRow);

            //Create 2 column row
            Cell cell;
            Row row;

            //Create Fact header row
            Row factHeaderrow = table.createRow(15f);
            cell = factHeaderrow.createCell(12, messageSource.getMessage("lbl.lab.code.mx", null, null));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            cell.setFillColor(Color.LIGHT_GRAY);

            cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.approve.date", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(17, messageSource.getMessage("lbl.silais", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(20, messageSource.getMessage("lbl.health.unit", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.request.large", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.final.result", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            y -= 15;

            //Add multiple rows with random facts about Belgium
            for (String[] fact : reqList) {

                if (y < 300) {
                    table.draw();
                    stream.close();
                    page = new PDPage(PDPage.PAGE_SIZE_A4);
                    page.setRotation(90);
                    doc.addPage(page);
                    stream = new PDPageContentStream(doc, page);
                    stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
                    y = 470;
                    GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

                    pageNumber = String.valueOf(doc.getNumberOfPages());
                    GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

                    table = new BaseTable(y, y, bottomMargin, tableWidth, margin, doc, page, true, true);

                    //Create Header row
                    headerRow = table.createRow(15f);
                    table.setHeader(headerRow);

                    //Create Fact header row
                    factHeaderrow = table.createRow(15f);
                    cell = factHeaderrow.createCell(12, messageSource.getMessage("lbl.lab.code.mx", null, null));
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    cell.setFillColor(Color.LIGHT_GRAY);

                    cell = factHeaderrow.createCell(16, messageSource.getMessage("lbl.approve.date", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(17, messageSource.getMessage("lbl.silais", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(20, messageSource.getMessage("lbl.health.unit", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.receipt.person.applicant.name", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.request.large", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell(10, messageSource.getMessage("lbl.final.result", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    y -= 15;

                }

                row = table.createRow(15f);
                cell = row.createCell(12, fact[0]);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setFontSize(10);
                y -= 15;

                for (int i = 1; i < fact.length; i++) {
                    if (i == 1) {
                        cell = row.createCell(16, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 2) {
                        cell = row.createCell(17, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 3) {
                        cell = row.createCell(20, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 4) {
                        cell = row.createCell(13, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);

                    } else if (i == 5) {
                        cell = row.createCell(10, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    } else if (i == 6) {
                        cell = row.createCell(10, fact[i]);
                        cell.setFont(PDType1Font.HELVETICA);
                        cell.setFontSize(10);
                    }
                }
            }
            table.draw();

            //fecha impresi�n
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.print.datetime", null, null), 100, 605, stream, 10, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaImpresion, 100, 710, stream, 10, PDType1Font.HELVETICA);

            stream.close();

            doc.save(output);
            doc.close();
            // generate the file
            res = Base64.encodeBase64String(output.toByteArray());


        }

        return res;
    }

    private String loadFinalResult(List<DetalleResultadoFinal> finalRes) throws Exception {
        int cont = 0;
        String valorResultado = null;
        for (DetalleResultadoFinal det : finalRes) {
            cont++;
            //first record
            if (cont == 1) {
                //single record result
                if (det.getRespuesta() != null) {
                    if (det.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                        Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(Integer.valueOf(det.getValor()));
                        valorResultado = det.getRespuesta().getNombre() + ":" + " " + valor.getValor();

                    } else {
                        valorResultado = det.getRespuesta().getNombre() + ":" + " " + det.getValor();
                    }
                } else {
                    if (det.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                        Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(Integer.valueOf(det.getValor()));
                        valorResultado = det.getRespuestaExamen().getNombre() + ":" + " " + valor.getValor();

                    } else {
                        valorResultado = det.getRespuestaExamen().getNombre() + ":" + " " + det.getValor();
                    }
                }

                //no first record
            } else {
                if (det.getRespuesta() != null) {
                    if (det.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                        Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(Integer.valueOf(det.getValor()));
                        valorResultado += ","+ " " +det.getRespuesta().getNombre() + ":" + " " + valor.getValor();

                    } else {
                        valorResultado += ","+ " " + det.getRespuesta().getNombre() + ":" + " " + det.getValor();
                    }
                } else {
                    if (det.getRespuestaExamen().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                        Catalogo_Lista valor = respuestasExamenService.getCatalogoListaConceptoByIdLista(Integer.valueOf(det.getValor()));
                        valorResultado += ","+ " " + det.getRespuestaExamen().getNombre() + ":" + " " + valor.getValor();

                    } else {
                        valorResultado += ","+ " " + det.getRespuestaExamen().getNombre() + ":" + " " + det.getValor();
                    }
                }
            }

        }
        return valorResultado;
    }

    /**
     * M�todo que se llama al entrar a la opci�n de menu de Reportes "Consolidado de Recepcion Mx".
     *
     * @param request para obtener informaci�n de la petici�n del cliente
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "/consolidated/init", method = RequestMethod.GET)
    public ModelAndView initConsolidatedReceipt(HttpServletRequest request) throws Exception {
        logger.debug("Iniciando Reporte Consolidad de recepción de mx");
        ModelAndView mav = new ModelAndView();
        User usuActual = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
        List<EntidadesAdtvas> entidadesAdtvases = null;
        if (usuActual.getNivelCentral()!=null && usuActual.getNivelCentral()){
            entidadesAdtvases = CallRestServices.getEntidadesAdtvas();
        }else {
            Laboratorio laboratorio = seguridadService.getLaboratorioUsuario(usuActual.getUsername());
            if (laboratorio != null)
                entidadesAdtvases = laboratoriosService.getEntidadesAdtvasAsignadasLab(laboratorio.getCodigo());
        }
        List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
        List<Area> areas = areaService.getAreas();
        mav.addObject("areas", areas);
        mav.addObject("entidades", entidadesAdtvases);
        mav.addObject("tipoMuestra", tipoMxList);
        mav.setViewName("reportes/consolidatedReception");

        return mav;
    }

    @RequestMapping(value = "getResumenRecepcionMxSILAIS", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String getResumenMuestrasSILAIS(@RequestParam(value = "fechaInicio", required = false) String fechaInicio, @RequestParam(value = "fechaFin", required = false) String fechaFin) throws Exception {
        String jsonResponse = "";
        User usuActual = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
        Laboratorio laboratorio = seguridadService.getLaboratorioUsuario(usuActual.getUsername());
        List<EntidadesAdtvas> silais = CallRestServices.getEntidadesAdtvas();
        if (laboratorio!=null) {

            List<ConsolidadoRecepcion> resumen = reportesService.getResumenRecepcionMuestrasSILAIS(laboratorio.getCodigo(), DateUtil.StringToDate(fechaInicio, "dd/MM/yyyy"), DateUtil.StringToDate(fechaFin+" 23:59:59", "dd/MM/yyyy HH:mm:ss"), usuActual.getNivelCentral());
            Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
            Integer indice = 0;
            for (final EntidadesAdtvas entidad : silais){
                //para dx ifi, filtrar si tiene biologia molecular esa muestra
                Predicate<ConsolidadoRecepcion> bySilais = new Predicate<ConsolidadoRecepcion>() {
                    @Override
                    public boolean apply(ConsolidadoRecepcion consolidado) {
                        return consolidado.getIdConsolida().equals(entidad.id);
                    }
                };
                //si se encuentra la muestra poner agregar datos de bio molecular a la fila
                Collection<ConsolidadoRecepcion> resExamen = FilterLists.filter(resumen, bySilais);
                if (resExamen.size()>0) {
                    for (ConsolidadoRecepcion datos: resExamen) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("total", String.valueOf(datos.getTotal()));
                        map.put("entidad", datos.getNombreConsolida() != null ? datos.getNombreConsolida() : "SIN SILAIS");
                        mapResponse.put(indice, map);
                        indice++;
                    }
                }else {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("total", "0");
                    map.put("entidad", entidad.getNombre());
                    mapResponse.put(indice, map);
                    indice++;
                }
            }
            jsonResponse = new Gson().toJson(mapResponse);
        }
        //escapar caracteres especiales, escape de los caracteres con valor numérico mayor a 127
        UnicodeEscaper escaper     = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    @RequestMapping(value = "getResumenRecepcionMxMunSILAIS", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String getResumenMuestrasMunSILAIS(@RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                       @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                       @RequestParam(value = "codSilais", required = false) Long codSilais) throws Exception {
        String jsonResponse = "";
        Laboratorio laboratorio = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        if (laboratorio!=null) {
            List<ConsolidadoRecepcion> resumen = reportesService.getResumenRecepcionMuestrasMunSILAIS(laboratorio.getCodigo(), DateUtil.StringToDate(fechaInicio, "dd/MM/yyyy"),
                    DateUtil.StringToDate(fechaFin + " 23:59:59", "dd/MM/yyyy HH:mm:ss"), codSilais);
            Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
            Integer indice = 0;
            for (ConsolidadoRecepcion obj : resumen) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("total", obj.getTotal() != null ? obj.getTotal().toString() : "");
                map.put("entidad", obj.getNombreConsolida() != null ? obj.getNombreConsolida() : "Sin Municipio");
                mapResponse.put(indice, map);
                indice++;
            }
            jsonResponse = new Gson().toJson(mapResponse);
        }
        //escapar caracteres especiales, escape de los caracteres con valor numérico mayor a 127
        UnicodeEscaper escaper     = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    @RequestMapping(value = "getResumenRecepcionMxSolicitud", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String getResumenMuestrasSolicitud(@RequestParam(value = "fechaInicio", required = false) String fechaInicio, @RequestParam(value = "fechaFin", required = false) String fechaFin) throws Exception {
        String jsonResponse = "";
        Laboratorio laboratorio = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
        if (laboratorio!=null) {
            List<Object[]> resumen = reportesService.getResumenRecepcionMuestrasSolicitud(laboratorio.getCodigo(), DateUtil.StringToDate(fechaInicio, "dd/MM/yyyy"), DateUtil.StringToDate(fechaFin+" 23:59:59", "dd/MM/yyyy HH:mm:ss"));
            Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
            Integer indice = 0;
            for (Object[] obj : resumen) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("total", obj[0] != null ? obj[0].toString() : "");
                map.put("entidad", obj[2] != null ? obj[2].toString() : "SIN DX");
                mapResponse.put(indice, map);
                indice++;
            }
            jsonResponse = new Gson().toJson(mapResponse);
        }
        //escapar caracteres especiales, escape de los caracteres con valor numérico mayor a 127
        UnicodeEscaper escaper     = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    /*******************************************************************/
    /************************ REPORTE POR RESULTADO DX ***********************/
    /*******************************************************************/

    @RequestMapping(value = "reportResultDx/init", method = RequestMethod.GET)
    public String initReportResultDx(Model model,HttpServletRequest request) throws Exception {
        logger.debug("Reporte por Resultado");

        usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
        List<EntidadesAdtvas> entidades = new ArrayList<EntidadesAdtvas>();
        //ABRIL2019
        List<Catalogo> areas = CallRestServices.getCatalogos(CatalogConstants.AreaRep, CatalogConstants.NIVELES_REP_DX.split(","));
        laboratorio = seguridadService.getLaboratorioUsuario(usuario.getUsername());
        List<Laboratorio> laboratorios = null;
        if (usuario.getNivelCentral()!=null && usuario.getNivelCentral()) {
            laboratorios = laboratoriosService.getLaboratoriosRegionales();
            //areas.add(catalogosService.getAreaRep("AREAREP|PAIS"));
            entidades = CallRestServices.getEntidadesAdtvas();
        }else{
            if (laboratorio!=null) {
                entidades = laboratoriosService.getEntidadesAdtvasAsignadasLab(laboratorio.getCodigo());
                laboratorios = new ArrayList<Laboratorio>();
                laboratorios.add(laboratorio);
            }
        }
        //ABRIL2019
        /*areas.add(catalogosService.getAreaRep("AREAREP|PAIS"));
        areas.add(catalogosService.getAreaRep("AREAREP|SILAIS"));
        areas.add(catalogosService.getAreaRep("AREAREP|MUNI"));
        areas.add(catalogosService.getAreaRep("AREAREP|UNI"));
*/
        List<Catalogo_Dx> catDx = associationSamplesRequestService.getDxs();
        List<Catalogo_Estudio> catEs = null;
        if (laboratorio!=null && laboratorio.getCodigo().equalsIgnoreCase("CNDR")) {
            catEs = associationSamplesRequestService.getStudies();
        }

        model.addAttribute("areas", areas);
        model.addAttribute("entidades", entidades);
        model.addAttribute("dxs", catDx);
        model.addAttribute("estudios", catEs);
        model.addAttribute("laboratorios", laboratorios);
        return "reportes/resultadoDx";
    }

    /**
     * Método para obtener data para Reporte por Resultado dx
     * @param filtro JSon con los datos de los filtros a aplicar en la búsqueda
     * @return Object
     * @throws Exception
     */
    @RequestMapping(value = "dataReportResultDx", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<Object[]> fetchReportResultDxJson(@RequestParam(value = "filtro", required = true) String filtro) throws Exception{
        logger.info("Obteniendo los datos para Reporte por Resultado ");
        FiltrosReporte filtroRep = jsonToFiltroReportes(filtro);

        if (filtroRep.getIdDx()!=null)
            return reportesService.getDataDxResultReport(filtroRep, null, 8);
        else
            return reportesService.getDataEstResultReport(filtroRep, null, 8);
    }

    /**
     * Método para obtener data para Reporte por Resultado dx
     * @param filtro JSon con los datos de los filtros a aplicar en la búsqueda
     * @return Object
     * @throws Exception
     */
    @RequestMapping(value = "dataReportResultDxMail", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String fetchReportResultDxCsv(@RequestParam(value = "filtro", required = true) String filtro,
                                  @RequestParam(value = "csv", required = true) String csv) throws Exception{
        logger.info("Obteniendo los datos para Reporte por Resultado ");

        try {
            FiltrosReporte filtroRep = jsonToFiltroReportes(filtro);
            //Destinatario
            String toEmail = "";
            Parametro parametro = parametrosService.getParametroByName("EMAIL_DEST_RESDX");
            if (parametro!=null)
                toEmail = parametro.getValor(); // can be any email id
            //asunto
            String subject = messageSource.getMessage("menu.reports", null, null) + "-" + messageSource.getMessage("menu.report.result.dx", null, null);

            //cuerpo
            String body = messageSource.getMessage("mail.body.resultDx", null, null);

            List<Catalogo> areas = CallRestServices.getCatalogos(CatalogConstants.AreaRep);
            String entidad = "";
            String desde = messageSource.getMessage("lbl.from", null, null) + DateUtil.DateToString(filtroRep.getFechaInicio(), "dd/MM/yyyy");
            String hasta = messageSource.getMessage("lbl.to", null, null) + DateUtil.DateToString(filtroRep.getFechaFin(), "dd/MM/yyyy");
            if (filtroRep.getCodArea().equalsIgnoreCase("AREAREP|PAIS")) {
                entidad = messageSource.getMessage("lbl.nic.rep", null, null);
            } else if (filtroRep.getCodArea().equalsIgnoreCase("AREAREP|SILAIS")) {
                EntidadesAdtvas entidadesAdtva = CallRestServices.getEntidadAdtva(filtroRep.getCodSilais());
                if (entidadesAdtva != null)
                    entidad = messageSource.getMessage("lbl.silais1", null, null) + " " + entidadesAdtva.getNombre();
            }
            if (filtroRep.getCodArea().equalsIgnoreCase("AREAREP|UNI")) {
                //ABRIL2019
                Unidades unidad = CallRestServices.getUnidadSalud(filtroRep.getCodUnidad());
                if (unidad != null)
                entidad = messageSource.getMessage("lbl.health.unit1", null, null) + " " + unidad.getNombre();
            }

            body = String.format(body, catalogosService.buscarValorCatalogo(areas, filtroRep.getCodArea()), entidad, desde, hasta);

            //adjunto
            csv = URLDecoder.decode(csv, "utf-8");
            Attachment attachment = new Attachment("reporteResDx.csv","application/octet-stream", csv);

            //enviar correo
            Session session = EmailUtil.openSession(getMailSessionData());
            EmailUtil.sendAttachmentEmail(session, toEmail, subject, body, attachment);

            return new Gson().toJson("OK");
        }catch (Exception ex){
            return new Gson().toJson(messageSource.getMessage("msg.error.sending.email",null,null)+" "+ ex.getMessage());
        }
    }

    /**
     * Abrir sessión en servidor de correo
     * @return SessionData
     */
    private SessionData getMailSessionData(){
        SessionData sessionData = new SessionData();
        Parametro parametro = parametrosService.getParametroByName("EMAIL_USER");
        if (parametro!=null)
            sessionData.setFromEmail(parametro.getValor());

        parametro = parametrosService.getParametroByName("EMAIL_USER_PASS");
        if (parametro!=null)
            sessionData.setPassword(parametro.getValor());

        parametro = parametrosService.getParametroByName("SMTP_SERVER");
        if (parametro!=null)
            sessionData.setSmtpHost(parametro.getValor());

        parametro = parametrosService.getParametroByName("SMTP_PORT");
        if (parametro!=null)
            sessionData.setSmtpPort(parametro.getValor());

        parametro = parametrosService.getParametroByName("SSL_PORT");
        if (parametro!=null)
            sessionData.setSslPort(parametro.getValor());

        return sessionData;
    }

    /**
     * Convertir lista de Objetos a formato CSV
     */
    private String createCSV(List<Object[]> datos, String columnaEntidad){
        StringBuilder sb = new StringBuilder();
        sb.append(columnaEntidad);
        sb.append(',');
        sb.append(messageSource.getMessage("lbl.total",null,null));
        sb.append(',');
        sb.append(messageSource.getMessage("lbl.positive",null,null));
        sb.append(',');
        sb.append(messageSource.getMessage("lbl.negative",null,null));
        sb.append(',');
        sb.append(messageSource.getMessage("lbl.without.result",null,null));
        sb.append(',');
        sb.append(messageSource.getMessage("lbl.sample.inadequate2",null,null));
        sb.append(',');
        sb.append(messageSource.getMessage("lbl.pos.percentage",null,null));
        sb.append('\n');

        for(Object[] dato : datos) {
            sb.append(dato[0]);
            sb.append(',');
            sb.append(dato[2]);
            sb.append(',');
            sb.append(dato[3]);
            sb.append(',');
            sb.append(dato[4]);
            sb.append(',');
            sb.append(dato[5]);
            sb.append(',');
            sb.append(dato[7]);
            sb.append(',');
            sb.append(dato[6]);
            sb.append('\n');
        }
        return sb.toString();
    }
    /**
     * Convierte un JSON con los filtros de búsqueda a objeto FiltrosReporte
     * @param strJson filtros
     * @return FiltrosReporte
     * @throws Exception
     */
    private FiltrosReporte jsonToFiltroReportes(String strJson) throws Exception {
        JsonObject jObjectFiltro = new Gson().fromJson(strJson, JsonObject.class);
        FiltrosReporte filtroRep = new FiltrosReporte();
        Date fechaInicio = null;
        Date fechaFin = null;
        Long codSilais = null;
        Long codUnidadSalud = null;
        String tipoNotificacion = null;
        Integer factor= 0;
        Long codDepartamento = null;
        Long codMunicipio = null;
        String codArea = null;
        boolean subunidad = false;
        boolean porSilais = true;//por defecto true
        String codZona = null;
        String idSolicitud = null;
        String codLabo = null;
        String consolidarPor = null;

        if (jObjectFiltro.get("codSilais") != null && !jObjectFiltro.get("codSilais").getAsString().isEmpty())
            codSilais = jObjectFiltro.get("codSilais").getAsLong();
        if (jObjectFiltro.get("codUnidadSalud") != null && !jObjectFiltro.get("codUnidadSalud").getAsString().isEmpty())
            codUnidadSalud = jObjectFiltro.get("codUnidadSalud").getAsLong();
        if (jObjectFiltro.get("tipoNotificacion") != null && !jObjectFiltro.get("tipoNotificacion").getAsString().isEmpty())
            tipoNotificacion = jObjectFiltro.get("tipoNotificacion").getAsString();
        if (jObjectFiltro.get("codFactor") != null && !jObjectFiltro.get("codFactor").getAsString().isEmpty())
            factor = jObjectFiltro.get("codFactor").getAsInt();
        if (jObjectFiltro.get("fechaInicio") != null && !jObjectFiltro.get("fechaInicio").getAsString().isEmpty())
            fechaInicio = DateUtil.StringToDate(jObjectFiltro.get("fechaInicio").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFin") != null && !jObjectFiltro.get("fechaFin").getAsString().isEmpty())
            fechaFin = DateUtil.StringToDate(jObjectFiltro.get("fechaFin").getAsString() + " 23:59:59");
        if (jObjectFiltro.get("codDepartamento") != null && !jObjectFiltro.get("codDepartamento").getAsString().isEmpty())
            codDepartamento = jObjectFiltro.get("codDepartamento").getAsLong();
        if (jObjectFiltro.get("codMunicipio") != null && !jObjectFiltro.get("codMunicipio").getAsString().isEmpty())
            codMunicipio = jObjectFiltro.get("codMunicipio").getAsLong();
        if (jObjectFiltro.get("codArea") != null && !jObjectFiltro.get("codArea").getAsString().isEmpty())
            codArea = jObjectFiltro.get("codArea").getAsString();
        if (jObjectFiltro.get("subunidades") != null && !jObjectFiltro.get("subunidades").getAsString().isEmpty())
            subunidad = jObjectFiltro.get("subunidades").getAsBoolean();
        if (jObjectFiltro.get("porSilais") != null && !jObjectFiltro.get("porSilais").getAsString().isEmpty())
            porSilais = jObjectFiltro.get("porSilais").getAsBoolean();
        if (jObjectFiltro.get("codZona") != null && !jObjectFiltro.get("codZona").getAsString().isEmpty())
            codZona = jObjectFiltro.get("codZona").getAsString();
        if (jObjectFiltro.get("idDx") != null && !jObjectFiltro.get("idDx").getAsString().isEmpty())
            idSolicitud = jObjectFiltro.get("idDx").getAsString();
        if (jObjectFiltro.get("codLabo") != null && !jObjectFiltro.get("codLabo").getAsString().isEmpty())
            codLabo = jObjectFiltro.get("codLabo").getAsString();
        if (jObjectFiltro.get("consolidarPor") != null && !jObjectFiltro.get("consolidarPor").getAsString().isEmpty())
            consolidarPor = jObjectFiltro.get("consolidarPor").getAsString();

        filtroRep.setSubunidades(subunidad);
        filtroRep.setCodSilais(codSilais);
        filtroRep.setCodUnidad(codUnidadSalud);
        filtroRep.setFechaInicio(fechaInicio);
        filtroRep.setFechaFin(fechaFin);
        filtroRep.setTipoNotificacion(tipoNotificacion);
        filtroRep.setFactor(factor);
        filtroRep.setCodDepartamento(codDepartamento);
        filtroRep.setCodMunicipio(codMunicipio);
        filtroRep.setCodArea(codArea);
        filtroRep.setAnioInicial(DateUtil.DateToString(fechaInicio, "yyyy"));
        filtroRep.setPorSilais(porSilais);
        filtroRep.setCodZona(codZona);
        if (idSolicitud!=null && idSolicitud.contains("R")){
            filtroRep.setIdDx(Integer.valueOf(idSolicitud.substring(0,idSolicitud.indexOf("-"))));
        } else if (idSolicitud!=null && idSolicitud.contains("E")){
            filtroRep.setIdEstudio(Integer.valueOf(idSolicitud.substring(0, idSolicitud.indexOf("-"))));
        }
        filtroRep.setCodLaboratio(codLabo);
        filtroRep.setNivelCentral(usuario.getNivelCentral());
        filtroRep.setConsolidarPor(consolidarPor);
        return filtroRep;
    }

}
