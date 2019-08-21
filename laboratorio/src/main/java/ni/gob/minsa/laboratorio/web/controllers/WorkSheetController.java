package ni.gob.minsa.laboratorio.web.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ni.gob.minsa.laboratorio.domain.examen.Area;
import ni.gob.minsa.laboratorio.domain.muestra.*;
import ni.gob.minsa.laboratorio.domain.muestra.traslado.TrasladoMx;
import ni.gob.minsa.laboratorio.restServices.CallRestServices;
import ni.gob.minsa.laboratorio.restServices.entidades.EntidadesAdtvas;
import ni.gob.minsa.laboratorio.service.*;
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
import java.util.*;
import java.util.List;

/**
 * Created by FIRSTICT on 4/21/2015.
 * V1.0
 */
@Controller
@RequestMapping("workSheet")
public class WorkSheetController {

    private static final Logger logger = LoggerFactory.getLogger(SendMxReceiptController.class);
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
    @Qualifier(value = "areaService")
    private AreaService areaService;

    @Autowired
    @Qualifier(value = "hojaTrabajoService")
    private HojaTrabajoService hojaTrabajoService;

    @Autowired
    @Qualifier(value = "trasladosService")
    private TrasladosService trasladosService;

    @Autowired
    MessageSource messageSource;

    @RequestMapping(value = "init", method = RequestMethod.GET)
    public ModelAndView initSearchForm(HttpServletRequest request) throws Exception {
        logger.debug("buscar ordenes para recepcion");

        ModelAndView mav = new ModelAndView();
        List<EntidadesAdtvas> entidadesAdtvases =  CallRestServices.getEntidadesAdtvas();
        List<TipoMx> tipoMxList = catalogosService.getTipoMuestra();
        List<Area> areaList = areaService.getAreas();
        mav.addObject("entidades",entidadesAdtvases);
        mav.addObject("tipoMuestra", tipoMxList);
        mav.addObject("area",areaList);
        mav.setViewName("reportes/searchWorkSheet");

        return mav;
    }

    @RequestMapping(value = "printWorkSheets", method = RequestMethod.GET)
    public @ResponseBody
    String getPDFHoja(@RequestParam(value = "hojas", required = true) String hojas) throws IOException, COSVisitorException, ParseException {
        String res = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PDDocument doc = new PDDocument();


        List<DaSolicitudDx> solicitudDxList;
        List<DaSolicitudEstudio> solicitudEstudioList;

        String fechaImpresion = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(new Date());
        List<DaTomaMx> tomasHoja = new ArrayList<DaTomaMx>();
        Laboratorio labProcesa = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());

        String[] hojasArray = hojas.split(",");
        for (String numHoja : hojasArray) {
            List<String[]> filasSolicitudes = new ArrayList<String[]>();
            //Prepare the document.
            PDPage page = GeneralUtils.addNewPage(doc);
            page.setRotation(90);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
            HojaTrabajo hojaTrabajo = hojaTrabajoService.getHojaTrabajo(Integer.valueOf(numHoja),labProcesa.getCodigo());
            tomasHoja = hojaTrabajoService.getTomaMxByHojaTrabajo(Integer.valueOf(numHoja), hojaTrabajo.getLaboratorio().getCodigo());
            //dibujar encabezado pag y pie de pagina
            GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

            String pageNumber= String.valueOf(doc.getNumberOfPages());
            GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

            float y = drawInfoLab(stream,page, labProcesa);

            float xCenter = GeneralUtils.centerTextPositionXHorizontal(page, PDType1Font.HELVETICA_BOLD, 12, messageSource.getMessage("lbl.work.sheet", null, null));
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.work.sheet", null, null), y, xCenter, stream, 12, PDType1Font.HELVETICA_BOLD);
            y-=20;
            //draw worksheet info
            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.sheet.number", null, null) + ": ", y, 40, stream, 12, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(String.valueOf(hojaTrabajo.getNumero()), y, 130, stream, 12, PDType1Font.HELVETICA);

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.sheet.date", null, null) + ": ", y, 550, stream, 12, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(DateUtil.DateToString(hojaTrabajo.getFechaRegistro(), "dd/MM/yyyy hh:mm:ss a"), y, 650, stream, 12, PDType1Font.HELVETICA);
            //float y = 590;
            y-=10;
            for (DaTomaMx tomaMx_hoja : tomasHoja) {

                //cod_mx, solicitud,lab_destino,techa toma mx, fec_inicio_sintomas
                Laboratorio labUser = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
                solicitudDxList = tomaMxService.getSoliDxPrioridadByTomaAndLab(tomaMx_hoja.getIdTomaMx(), labUser.getCodigo());
                solicitudEstudioList = tomaMxService.getSolicitudesEstudioByIdTomaMx(tomaMx_hoja.getIdTomaMx());
                String[] content = null;
                String fis = "";
                String controlCalidad = messageSource.getMessage("lbl.no", null, null);
                TrasladoMx trasladoMxActivo = trasladosService.getTrasladoCCMx(tomaMx_hoja.getIdTomaMx());
                if (trasladoMxActivo != null) {
                    if (trasladoMxActivo.isControlCalidad()) {
                        controlCalidad = messageSource.getMessage("lbl.yes", null, null);
                    }
                }
                String solicitudes = "";
                String areaEntrega = "";
                int prioridad = 100;
                for (DaSolicitudDx solicitudDx : solicitudDxList) {
                    solicitudes += (solicitudes.isEmpty() ? "" : ",") + solicitudDx.getCodDx().getNombre();
                    if (prioridad >= solicitudDx.getCodDx().getPrioridad()) {
                        areaEntrega += areaEntrega.isEmpty() ? solicitudDx.getCodDx().getArea().getNombre() : "";
                        prioridad = solicitudDx.getCodDx().getPrioridad();
                    }

                }
                for (DaSolicitudEstudio solicitudEstudio : solicitudEstudioList) {
                    solicitudes += (solicitudes.isEmpty() ? "" : ",") + solicitudEstudio.getTipoEstudio().getNombre();
                    areaEntrega += areaEntrega.isEmpty() ? solicitudEstudio.getTipoEstudio().getArea().getNombre() : "";
                }
                if (tomaMx_hoja.getIdNotificacion().getFechaInicioSintomas() != null) {
                    fis = DateUtil.DateToString(tomaMx_hoja.getIdNotificacion().getFechaInicioSintomas(), "dd/MM/yyyy");
                }
                String nombrePersona = tomaMx_hoja.getIdNotificacion().getPersona().getPrimerNombre();
                if (tomaMx_hoja.getIdNotificacion().getPersona().getSegundoNombre() != null)
                    nombrePersona = nombrePersona + " " + tomaMx_hoja.getIdNotificacion().getPersona().getSegundoNombre();
                nombrePersona = nombrePersona + " " + tomaMx_hoja.getIdNotificacion().getPersona().getPrimerApellido();
                if (tomaMx_hoja.getIdNotificacion().getPersona().getSegundoApellido() != null)
                    nombrePersona = nombrePersona + " " + tomaMx_hoja.getIdNotificacion().getPersona().getSegundoApellido();

                content = new String[7];
                content[0] = tomaMx_hoja.getCodigoLab() != null ? tomaMx_hoja.getCodigoLab() : tomaMx_hoja.getCodigoUnicoMx();
                content[1] = nombrePersona;
                content[2] = solicitudes;
                content[3] = areaEntrega;
                content[4] = DateUtil.DateToString(tomaMx_hoja.getFechaHTomaMx(), "dd/MM/yyyy") +
                        (tomaMx_hoja.getHoraTomaMx() != null ? " " + tomaMx_hoja.getHoraTomaMx() : "");
                content[5] = fis;
                content[6] = controlCalidad;

                filasSolicitudes.add(content);
            }
            //Initialize table
            float margin = 40;
            float tableWidth = 750;
            float bottomMargin = 45;
            BaseTable table = new BaseTable(y, y, bottomMargin, tableWidth, margin, doc, page, true, true);

            //Create Fact header row
            Row factHeaderrow = table.createRow(15f);
            Cell cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.lab.code.mx", null, null));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            cell.setFillColor(Color.LIGHT_GRAY);

            cell = factHeaderrow.createCell((22), messageSource.getMessage("lbl.person", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell((18), messageSource.getMessage("lbl.requests", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell((15), messageSource.getMessage("lbl.solic.area.prc", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell((16), messageSource.getMessage("lbl.sampling.datetime", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell((12), messageSource.getMessage("lbl.receipt.symptoms.start.date", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);

            cell = factHeaderrow.createCell((4), messageSource.getMessage("lbl.cc", null, null));
            cell.setFillColor(Color.lightGray);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(10);
            //Create row
            Row row;
            //Add multiple rows with random facts about Belgium

            for (String[] fact : filasSolicitudes) {

                if (y < 260){
                    table.draw();
                    stream.close();
                    page = GeneralUtils.addNewPage(doc);
                    page.setRotation(90);
                    stream = new PDPageContentStream(doc, page);
                    stream.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
                    y = 490;
                    //dibujar encabezado pag y pie de pagina
                    GeneralUtils.drawHeaderAndFooter(stream, doc, 500, 840, 90, 840, 70);

                    pageNumber = String.valueOf(doc.getNumberOfPages());
                    GeneralUtils.drawTEXT(pageNumber, 15, 800, stream, 10, PDType1Font.HELVETICA_BOLD);

                    table = new BaseTable(y, y, bottomMargin, tableWidth, margin, doc, page, true, true);
                    factHeaderrow = table.createRow(15f);
                    cell = factHeaderrow.createCell(13, messageSource.getMessage("lbl.lab.code.mx", null, null));
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    cell.setFillColor(Color.LIGHT_GRAY);

                    cell = factHeaderrow.createCell((22), messageSource.getMessage("lbl.person", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell((18), messageSource.getMessage("lbl.requests", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell((15), messageSource.getMessage("lbl.solic.area.prc", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell((16), messageSource.getMessage("lbl.sampling.datetime", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell((12), messageSource.getMessage("lbl.receipt.symptoms.start.date", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);

                    cell = factHeaderrow.createCell((4), messageSource.getMessage("lbl.cc", null, null));
                    cell.setFillColor(Color.lightGray);
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFontSize(10);
                    y -= 15;
                }

                row = table.createRow(15f);
                y -= 15;
                for (int i = 0; i < fact.length; i++) {
                    if (i==0) {
                        cell = row.createCell(13, fact[i]);
                    }else if (i==1) {
                        cell = row.createCell(22, fact[i]);
                    }else if (i==2) {
                        cell = row.createCell(18, fact[i]);
                    }else if (i==3) {
                        cell = row.createCell(15, fact[i]);
                    }else if (i==5) {
                        cell = row.createCell(12, fact[i]);
                    }else if (i==6) {
                        cell = row.createCell(4, fact[i]);
                    }else {
                        cell = row.createCell(16, fact[i]);
                    }
                    cell.setFont(PDType1Font.HELVETICA);
                    cell.setFontSize(10);
                }
            }
            table.draw();

            GeneralUtils.drawTEXT(messageSource.getMessage("lbl.print.datetime", null, null) + " ", 95, 600, stream, 12, PDType1Font.HELVETICA_BOLD);
            GeneralUtils.drawTEXT(fechaImpresion, 95, 700, stream, 10, PDType1Font.HELVETICA);


            stream.close();
        }

        doc.save(output);
        doc.close();
        // generate the file
        res = Base64.encodeBase64String(output.toByteArray());

        return res;
    }


    /**
     * M�todo para realizar la b�squeda de Mx para recepcionar en Mx Vigilancia general
     * @param filtro JSon con los datos de los filtros a aplicar en la b�squeda(Nombre Apellido, Rango Fec Toma Mx, Tipo Mx, SILAIS, unidad salud)
     * @return String con las Mx encontradas
     * @throws Exception
     */
    @RequestMapping(value = "search", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String fetchOrdersJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception{
        logger.info("Obteniendo las mx seg�n filtros en JSON");
        JsonObject jObjectFiltro = new Gson().fromJson(filtro, JsonObject.class);
        Integer hoja = null;
        Date fechaInicioHoja = null;
        Date fechaFinHoja = null;
        if (jObjectFiltro.get("hoja") != null && !jObjectFiltro.get("hoja").getAsString().isEmpty())
            hoja = jObjectFiltro.get("hoja").getAsInt();
        if (jObjectFiltro.get("fechaInicioHoja") != null && !jObjectFiltro.get("fechaInicioHoja").getAsString().isEmpty())
            fechaInicioHoja = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioHoja").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFinHoja") != null && !jObjectFiltro.get("fechaFinHoja").getAsString().isEmpty())
            fechaFinHoja = DateUtil.StringToDate(jObjectFiltro.get("fechaFinHoja").getAsString()+" 23:59:59");
        List<HojaTrabajo> hojaTrabajoList = hojaTrabajoService.getTomaMxByFiltro(hoja,fechaInicioHoja,fechaFinHoja,seguridadService.obtenerNombreUsuario());
        return hojasTrabajoToJson(hojaTrabajoList);
    }

    /**
     * M�todo que convierte una lista de tomaMx a un string con estructura Json
     * @param hojaTrabajoList lista con las hojas de trabajo a convertir
     * @return String
     */
    private String hojasTrabajoToJson(List<HojaTrabajo> hojaTrabajoList){
        String jsonResponse;
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice=0;
        List<DaTomaMx> tomaMxList = null;
        for(HojaTrabajo hojaTrabajo:hojaTrabajoList){
            Map<String, String> map = new HashMap<String, String>();
            map.put("numero", String.valueOf(hojaTrabajo.getNumero()));
            map.put("fecha",DateUtil.DateToString(hojaTrabajo.getFechaRegistro(), "dd/MM/yyyy hh:mm:ss a"));
            tomaMxList = hojaTrabajoService.getTomaMxByHojaTrabajo(hojaTrabajo.getNumero(),hojaTrabajo.getLaboratorio().getCodigo());
            map.put("cantidad",String.valueOf(tomaMxList.size()));
            Map<Integer, Object> mapMxList = new HashMap<Integer, Object>();
            Map<String, String> mapMx = new HashMap<String, String>();
            int subIndice=0;
            for (DaTomaMx tomaMx : tomaMxList){
                boolean esEstudio = tomaMxService.getSolicitudesEstudioByIdTomaMx( tomaMx.getIdTomaMx()).size() > 0;
                mapMx.put("codigoUnicoMx", esEstudio?tomaMx.getCodigoUnicoMx():tomaMx.getCodigoLab());
                mapMx.put("fechaTomaMx",DateUtil.DateToString(tomaMx.getFechaHTomaMx(), "dd/MM/yyyy")+
                        (tomaMx.getHoraTomaMx()!=null?" "+tomaMx.getHoraTomaMx():""));
                if (tomaMx.getIdNotificacion().getCodSilaisAtencion()!=null) {
                    mapMx.put("codSilais", tomaMx.getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                }else {
                    mapMx.put("codSilais","");
                }
                if (tomaMx.getIdNotificacion().getCodUnidadAtencion()!=null) {
                    mapMx.put("codUnidadSalud", tomaMx.getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                }else{
                    mapMx.put("codUnidadSalud","");
                }
                mapMx.put("tipoMuestra", tomaMx.getCodTipoMx().getNombre());
                //Si hay persona
                if (tomaMx.getIdNotificacion().getPersona()!=null){
                    /// se obtiene el nombre de la persona asociada a la ficha
                    String nombreCompleto = "";
                    nombreCompleto = tomaMx.getIdNotificacion().getPersona().getPrimerNombre();
                    if (tomaMx.getIdNotificacion().getPersona().getSegundoNombre()!=null)
                        nombreCompleto = nombreCompleto +" "+ tomaMx.getIdNotificacion().getPersona().getSegundoNombre();
                    nombreCompleto = nombreCompleto+" "+ tomaMx.getIdNotificacion().getPersona().getPrimerApellido();
                    if (tomaMx.getIdNotificacion().getPersona().getSegundoApellido()!=null)
                        nombreCompleto = nombreCompleto +" "+ tomaMx.getIdNotificacion().getPersona().getSegundoApellido();
                    mapMx.put("persona",nombreCompleto);
                }else{
                    mapMx.put("persona"," ");
                }
                subIndice++;
                mapMxList.put(subIndice,mapMx);
                mapMx = new HashMap<String, String>();
            }
            map.put("muestras", new Gson().toJson(mapMxList));
            mapResponse.put(indice, map);
            indice ++;
        }
        jsonResponse = new Gson().toJson(mapResponse);
        //escapar caracteres especiales, escape de los caracteres con valor num�rico mayor a 127
        UnicodeEscaper escaper     = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    private float drawInfoLab(PDPageContentStream stream, PDPage page, Laboratorio labProcesa) throws IOException {
        float xCenter;

        float inY = 490;
        float m = 20;

        xCenter = GeneralUtils.centerTextPositionXHorizontal(page, PDType1Font.HELVETICA_BOLD, 14, messageSource.getMessage("lbl.minsa", null, null));
        GeneralUtils.drawTEXT(messageSource.getMessage("lbl.minsa", null, null), inY, xCenter, stream, 14, PDType1Font.HELVETICA_BOLD);
        inY -= m;

        if(labProcesa != null){

            if(labProcesa.getDescripcion()!= null){
                xCenter = GeneralUtils.centerTextPositionXHorizontal(page, PDType1Font.HELVETICA_BOLD, 14, labProcesa.getDescripcion());
                GeneralUtils.drawTEXT(labProcesa.getDescripcion(), inY, xCenter, stream, 14, PDType1Font.HELVETICA_BOLD);
                inY -= m;
            }

            if(labProcesa.getDireccion() != null){
                xCenter = GeneralUtils.centerTextPositionXHorizontal(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getDireccion());
                GeneralUtils.drawTEXT(labProcesa.getDireccion(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                inY -= m;
            }

            if(labProcesa.getTelefono() != null){

                if(labProcesa.getTelefax() != null){
                    xCenter = GeneralUtils.centerTextPositionXHorizontal(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getTelefono() + " - " + labProcesa.getTelefax());
                    GeneralUtils.drawTEXT(labProcesa.getTelefono() + " " + labProcesa.getTelefax(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                }else{
                    xCenter = GeneralUtils.centerTextPositionXHorizontal(page, PDType1Font.HELVETICA_BOLD, 11, labProcesa.getTelefono());
                    GeneralUtils.drawTEXT(labProcesa.getTelefono(), inY, xCenter, stream, 11, PDType1Font.HELVETICA_BOLD);
                }
                inY -= m;
            }
        }
        return inY;
    }


}
