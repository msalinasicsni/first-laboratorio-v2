package ni.gob.minsa.laboratorio.web.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ni.gob.minsa.laboratorio.domain.muestra.*;
import ni.gob.minsa.laboratorio.domain.concepto.Catalogo_Lista;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultado;
import ni.gob.minsa.laboratorio.domain.resultados.DetalleResultadoFinal;
import ni.gob.minsa.laboratorio.domain.resultados.RespuestaSolicitud;
import ni.gob.minsa.laboratorio.domain.seguridadlocal.User;
import ni.gob.minsa.laboratorio.restServices.CallRestServices;
import ni.gob.minsa.laboratorio.restServices.constantes.CatalogConstants;
import ni.gob.minsa.laboratorio.restServices.entidades.Catalogo;
import ni.gob.minsa.laboratorio.restServices.entidades.EntidadesAdtvas;
import ni.gob.minsa.laboratorio.service.*;
import ni.gob.minsa.laboratorio.utilities.ConstantsSecurity;
import ni.gob.minsa.laboratorio.utilities.DateUtil;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
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
 * Created by souyen-ics.
 */
@Controller
@RequestMapping("resultadoFinal")
public class ResultadoFinalController {

    private static final Logger logger = LoggerFactory.getLogger(ResultadoFinalController.class);

    @Resource(name = "seguridadService")
    private SeguridadService seguridadService;

    @Resource(name = "catalogosService")
    private CatalogoService catalogoService;

    @Resource(name = "resultadoFinalService")
    private ResultadoFinalService resultadoFinalService;

    @Resource(name= "tomaMxService")
    private TomaMxService tomaMxService;

    @Resource(name= "respuestasSolicitudService")
    private RespuestasSolicitudService respuestasSolicitudService;

    @Resource(name= "ordenExamenMxService")
    private OrdenExamenMxService ordenExamenMxService;
    
    @Autowired
    MessageSource messageSource;

    @RequestMapping(value = "init", method = RequestMethod.GET)
    public ModelAndView initForm(HttpServletRequest request) throws Exception {
        logger.debug("Inicio de busqueda de dx para ingreso de resultado final");
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
            List<TipoMx> tipoMxList = catalogoService.getTipoMuestra();
            mav.addObject("entidades",entidadesAdtvases);
            mav.addObject("tipoMuestra", tipoMxList);

            mav.setViewName("resultados/searchResults");
        }else
            mav.setViewName(urlValidacion);

        return mav;
    }


    @RequestMapping(value = "search", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String fetchDxJson(@RequestParam(value = "strFilter", required = true) String filtro) throws Exception{
        logger.info("Obteniendo los diagn�sticos con examenes realizados");
        FiltroMx filt= jsonToFiltroDx(filtro);
        List<DaSolicitudDx> dxList = null;
        List<DaSolicitudEstudio> estudioList = null;
        Integer records = 0;

        if(filt.getCodTipoSolicitud()!= null){
            if(filt.getCodTipoSolicitud().equals("Rutina")){
                dxList = resultadoFinalService.getDxByFiltro(filt);
            }else{
                estudioList = resultadoFinalService.getEstudioByFiltro(filt);
                  }
        }else{
            dxList = resultadoFinalService.getDxByFiltro(filt);
            estudioList = resultadoFinalService.getEstudioByFiltro(filt);
        }

        return DxToJson(dxList, estudioList);
    }


    private String DxToJson(List<DaSolicitudDx> dxList, List<DaSolicitudEstudio> estudioList){
        String jsonResponse="";
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice=0;

        if(dxList != null){
            for(DaSolicitudDx diagnostico : dxList){
                Map<String, String> map = new HashMap<String, String>();
                map.put("codigoUnicoMx", diagnostico.getIdTomaMx().getCodigoLab());
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
                }else {
                    map.put("codUnidadSalud","");
                }
                map.put("tipoMuestra", diagnostico.getIdTomaMx().getCodTipoMx().getNombre());
                //Si hay fecha de inicio de sintomas se muestra
                Date fechaInicioSintomas = diagnostico.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas();
                if (fechaInicioSintomas!=null)
                    map.put("fechaInicioSintomas",DateUtil.DateToString(fechaInicioSintomas,"dd/MM/yyyy"));
                else
                    map.put("fechaInicioSintomas"," ");

                //Si hay persona
                /// se obtiene el nombre de la persona asociada a la ficha
                String nombreCompleto = " ";
                if (diagnostico.getIdTomaMx().getIdNotificacion().getPersona()!=null){
                    nombreCompleto = diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                    if (diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre()!=null)
                        nombreCompleto = nombreCompleto +" "+ diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                    nombreCompleto = nombreCompleto+" "+ diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                    if (diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido()!=null)
                        nombreCompleto = nombreCompleto +" "+ diagnostico.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();

                } else if (diagnostico.getIdTomaMx().getIdNotificacion().getSolicitante() != null) {
                    nombreCompleto = diagnostico.getIdTomaMx().getIdNotificacion().getSolicitante().getNombre();
                } else if (diagnostico.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                    nombreCompleto = diagnostico.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH();
                }
                map.put("persona",nombreCompleto);
                map.put("diagnostico", diagnostico.getCodDx().getNombre());
                map.put("idSolicitud", diagnostico.getIdSolicitudDx());

                List<DetalleResultadoFinal> resFinal = resultadoFinalService.getDetResActivosBySolicitud(diagnostico.getIdSolicitudDx());
                if (!resFinal.isEmpty()) {
                    map.put("resultadoS", messageSource.getMessage("lbl.yes", null, null));
                } else {
                    map.put("resultadoS", messageSource.getMessage("lbl.no", null, null));
                }
                map.put("detResultado",parseResultDetails(resFinal));
                List<OrdenExamen> ordenExamenList = ordenExamenMxService.getOrdenesExamenNoAnuladasSinResulByIdSolicitud(diagnostico.getIdSolicitudDx());
                map.put("pendientes", parsePendingTests(ordenExamenList));
                mapResponse.put(indice, map);
                indice ++;
            }
        }

      if(estudioList != null){
          for(DaSolicitudEstudio estudio : estudioList){
              boolean agregar = true;
              //para estudios cohorte dengue y clinico dengue, s�lo tomar en cuenta la muestra inicial
              if (estudio.getTipoEstudio().getIdEstudio() == 1 || estudio.getTipoEstudio().getIdEstudio() == 2){
                  String codigoUnicoMx = estudio.getIdTomaMx().getCodigoUnicoMx();
                  String inicial = codigoUnicoMx.substring(codigoUnicoMx.lastIndexOf('.') + 1, codigoUnicoMx.length());
                  agregar = inicial.equals("1");
              }
              if (agregar) {
                  Map<String, String> map = new HashMap<String, String>();
                  map.put("codigoUnicoMx", estudio.getIdTomaMx().getCodigoUnicoMx());
                  map.put("idTomaMx", estudio.getIdTomaMx().getIdTomaMx());
                  map.put("fechaTomaMx", DateUtil.DateToString(estudio.getIdTomaMx().getFechaHTomaMx(), "dd/MM/yyyy")+
                          (estudio.getIdTomaMx().getHoraTomaMx()!=null?" "+estudio.getIdTomaMx().getHoraTomaMx():""));
                  map.put("codSilais", estudio.getIdTomaMx().getIdNotificacion().getNombreSilaisAtencion());//ABRIL2019
                  map.put("codUnidadSalud", estudio.getIdTomaMx().getIdNotificacion().getNombreUnidadAtencion());//ABRIL2019
                  map.put("tipoMuestra", estudio.getIdTomaMx().getCodTipoMx().getNombre());
                  //Si hay fecha de inicio de sintomas se muestra
                  Date fechaInicioSintomas = estudio.getIdTomaMx().getIdNotificacion().getFechaInicioSintomas();
                  if (fechaInicioSintomas != null)
                      map.put("fechaInicioSintomas", DateUtil.DateToString(fechaInicioSintomas, "dd/MM/yyyy"));
                  else
                      map.put("fechaInicioSintomas", " ");

                  //Si hay persona
                  /// se obtiene el nombre de la persona asociada a la ficha
                  String nombreCompleto = " ";
                  if (estudio.getIdTomaMx().getIdNotificacion().getPersona() != null) {
                      nombreCompleto = estudio.getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                      if (estudio.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                          nombreCompleto = nombreCompleto + " " + estudio.getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                      nombreCompleto = nombreCompleto + " " + estudio.getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                      if (estudio.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                          nombreCompleto = nombreCompleto + " " + estudio.getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                  }
                  else if (estudio.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                	  nombreCompleto = estudio.getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH();
                  }
                  map.put("persona", nombreCompleto);

                  map.put("diagnostico", estudio.getTipoEstudio().getNombre());
                  map.put("idSolicitud", estudio.getIdSolicitudEstudio());

                  List<DetalleResultadoFinal> resFinal = resultadoFinalService.getDetResActivosBySolicitud(estudio.getIdSolicitudEstudio());
                  if (!resFinal.isEmpty()) {
                      map.put("resultadoS", messageSource.getMessage("lbl.yes", null, null));
                  } else {
                      map.put("resultadoS", messageSource.getMessage("lbl.no", null, null));
                  }
                  map.put("detResultado",parseResultDetails(resFinal));

                  List<OrdenExamen> ordenExamenList = ordenExamenMxService.getOrdenesExamenNoAnuladasSinResulByIdSolicitud(estudio.getIdSolicitudEstudio());
                  map.put("pendientes", parsePendingTests(ordenExamenList));

                  mapResponse.put(indice, map);
                  indice++;
              }
          }
      }


        jsonResponse = new Gson().toJson(mapResponse);
        UnicodeEscaper escaper     = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);
    }

    private String parsePendingTests(List<OrdenExamen> ordenExamenList){
        String examenesPen = "";
        for(OrdenExamen examen : ordenExamenList){
            examenesPen+=(examenesPen.isEmpty()?examen.getCodExamen().getNombre():", "+examen.getCodExamen().getNombre());
        }
        return examenesPen;
    }
    private String parseResultDetails(List<DetalleResultadoFinal> resultList){
        String resultados="";
        for(DetalleResultadoFinal res: resultList){
            if (res.getRespuesta()!=null) {
                resultados+=(resultados.isEmpty()?res.getRespuesta().getNombre():", "+res.getRespuesta().getNombre());
                if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) { //ABRIL2019
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

    private FiltroMx jsonToFiltroDx(String strJson) throws Exception {
        JsonObject jObjectFiltro = new Gson().fromJson(strJson, JsonObject.class);
        FiltroMx filtroDx = new FiltroMx();
        String nombreApellido = null;
        Date fechaInicioRecepcion = null;
        Date fechaFinRecepcion = null;
        String codSilais = null;
        String codUnidadSalud = null;
        String codTipoMx = null;
        String codigoUnicoMx = null;
        String codTipoSolicitud = null;
        String nombreSolicitud = null;
        String resultado = null;
        Date fechaInicioProc = null;
        Date fechaFinProc = null;

        if (jObjectFiltro.get("nombreApellido") != null && !jObjectFiltro.get("nombreApellido").getAsString().isEmpty())
            nombreApellido = jObjectFiltro.get("nombreApellido").getAsString();
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
        if (jObjectFiltro.get("codigoUnicoMx") != null && !jObjectFiltro.get("codigoUnicoMx").getAsString().isEmpty())
            codigoUnicoMx = jObjectFiltro.get("codigoUnicoMx").getAsString();
        if (jObjectFiltro.get("codTipoSolicitud") != null && !jObjectFiltro.get("codTipoSolicitud").getAsString().isEmpty())
            codTipoSolicitud = jObjectFiltro.get("codTipoSolicitud").getAsString();
        if (jObjectFiltro.get("nombreSolicitud") != null && !jObjectFiltro.get("nombreSolicitud").getAsString().isEmpty())
            nombreSolicitud = jObjectFiltro.get("nombreSolicitud").getAsString();
        if (jObjectFiltro.get("resultado") != null && !jObjectFiltro.get("resultado").getAsString().isEmpty())
           resultado = jObjectFiltro.get("resultado").getAsString();
        if (jObjectFiltro.get("fechaInicioProc") != null && !jObjectFiltro.get("fechaInicioProc").getAsString().isEmpty())
            fechaInicioProc = DateUtil.StringToDate(jObjectFiltro.get("fechaInicioProc").getAsString() + " 00:00:00");
        if (jObjectFiltro.get("fechaFinProc") != null && !jObjectFiltro.get("fechaFinProc").getAsString().isEmpty())
            fechaFinProc = DateUtil.StringToDate(jObjectFiltro.get("fechaFinProc").getAsString() + " 23:59:59");

        String nombreUsuario = seguridadService.obtenerNombreUsuario();
        filtroDx.setCodSilais(codSilais);
        filtroDx.setCodUnidadSalud(codUnidadSalud);
        filtroDx.setFechaInicioRecepLab(fechaInicioRecepcion);
        filtroDx.setFechaFinRecepLab(fechaFinRecepcion);
        filtroDx.setNombreApellido(nombreApellido);
        filtroDx.setCodTipoMx(codTipoMx);
        filtroDx.setCodEstado("ESTDMX|RCLAB"); // recepcionadas en lab
        filtroDx.setIncluirMxInadecuada(false);
        filtroDx.setCodigoUnicoMx(codigoUnicoMx);
        filtroDx.setCodTipoSolicitud(codTipoSolicitud);
        filtroDx.setNombreSolicitud(nombreSolicitud);
        filtroDx.setResultado(resultado);
        filtroDx.setNombreUsuario(nombreUsuario);
        filtroDx.setNivelLaboratorio(seguridadService.esDirector(nombreUsuario)?3:seguridadService.esJefeDepartamento(nombreUsuario)?2:1);
        filtroDx.setIncluirTraslados(true);
        filtroDx.setFechaInicioProcesamiento(fechaInicioProc);
        filtroDx.setFechaFinProcesamiento(fechaFinProc);
        return filtroDx;
    }

    @RequestMapping(value = "create/{idSolicitud}", method = RequestMethod.GET)
    public ModelAndView initCreationForm(@PathVariable("idSolicitud") String idSolicitud, HttpServletRequest request) throws Exception {
        logger.debug("Iniciando el ingreso de resultado final");
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
            if(idSolicitud != null){
              DaSolicitudDx rutina =  tomaMxService.getSolicitudDxByIdSolicitud(idSolicitud);
              DaSolicitudEstudio estudio = tomaMxService.getEstudioByIdSolicitud(idSolicitud);
                mav.addObject("rutina", rutina);
                mav.addObject("estudio", estudio);
                mav.setViewName("resultados/enterFinalResult");
            }

        }else
            mav.setViewName(urlValidacion);

        return mav;
    }

    @RequestMapping(value = "searchExams", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String fetchExamsJson(@RequestParam(value = "idSolicitudD", required = false) String idSolicitudD, @RequestParam(value = "idSolicitudE", required = false) String idSolicitudE) throws Exception{
        logger.info("Obteniendo los examenes realizados");
        List<OrdenExamen> ordenExa;

            if(!idSolicitudE.equals("")){
                ordenExa=  resultadoFinalService.getOrdenExaBySolicitudEstudio(idSolicitudE);
            }else{
                ordenExa=  resultadoFinalService.getOrdenExaBySolicitudDx(idSolicitudD);
            }

        return ExamsToJson(ordenExa);
    }

    private String ExamsToJson(List<OrdenExamen> examsList) throws Exception {
        String jsonResponse = "";
        Map<Integer, Object> mapResponse = new HashMap<Integer, Object>();
        Integer indice = 0;
        List<Catalogo> tiposNotificacion = CallRestServices.getCatalogos(CatalogConstants.TipoNotificacion);//ABRIL2019
            for (OrdenExamen examen : examsList) {
                if (!examen.isAnulado()) {
                    Map<String, String> map = new HashMap<String, String>();
                    if (examen.getSolicitudDx() != null) {
                        map.put("idSolicitud", examen.getSolicitudDx().getIdSolicitudDx());
                        map.put("fechaSolicitud", DateUtil.DateToString(examen.getSolicitudDx().getFechaHSolicitud(), "dd/MM/yyyy hh:mm:ss a"));
                        map.put("nombreSolicitud", examen.getSolicitudDx().getCodDx().getNombre());
                        map.put("codigoUnicoMx", examen.getSolicitudDx().getIdTomaMx().getCodigoLab());
                        map.put("tipoMx", examen.getSolicitudDx().getIdTomaMx().getCodTipoMx().getNombre());
                        map.put("tipoNotificacion", catalogoService.buscarValorCatalogo( tiposNotificacion, examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getCodTipoNotificacion()));//ABRIL2019
                        map.put("idOrdenExamen", examen.getIdOrdenExamen());
                        map.put("NombreExamen", examen.getCodExamen().getNombre());

                        //Si hay persona
                        if (examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona() != null) {
                            /// se obtiene el nombre de la persona asociada a la ficha
                            String nombreCompleto = "";
                            nombreCompleto = examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                nombreCompleto = nombreCompleto + " " + examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                            nombreCompleto = nombreCompleto + " " + examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            if (examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                nombreCompleto = nombreCompleto + " " + examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                            map.put("persona", nombreCompleto);
                        } else if (examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getSolicitante() != null) {
                            map.put("persona", examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getSolicitante().getNombre());
                        } else if (examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                            map.put("persona", examen.getSolicitudDx().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                        } else {
                            map.put("persona", " ");
                        }

                    } else {
                        //examenes para estudio
                        map.put("idSolicitud", examen.getSolicitudEstudio().getIdSolicitudEstudio());
                        map.put("fechaSolicitud", DateUtil.DateToString(examen.getSolicitudEstudio().getFechaHSolicitud(), "dd/MM/yyyy hh:mm:ss a"));
                        map.put("nombreSolicitud", examen.getSolicitudEstudio().getTipoEstudio().getNombre());
                        map.put("codigoUnicoMx", examen.getSolicitudEstudio().getIdTomaMx().getCodigoUnicoMx());
                        map.put("tipoMx", examen.getSolicitudEstudio().getIdTomaMx().getCodTipoMx().getNombre());
                        map.put("tipoNotificacion", catalogoService.buscarValorCatalogo( tiposNotificacion, examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getCodTipoNotificacion()));//ABRIL2019
                        map.put("idOrdenExamen", examen.getIdOrdenExamen());
                        map.put("NombreExamen", examen.getCodExamen().getNombre());

                        //Si hay persona
                        if (examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona() != null) {
                            /// se obtiene el nombre de la persona asociada a la ficha
                            String nombreCompleto = "";
                            nombreCompleto = examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getPrimerNombre();
                            if (examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre() != null)
                                nombreCompleto = nombreCompleto + " " + examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoNombre();
                            nombreCompleto = nombreCompleto + " " + examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getPrimerApellido();
                            if (examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido() != null)
                                nombreCompleto = nombreCompleto + " " + examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getPersona().getSegundoApellido();
                            map.put("persona", nombreCompleto);
                        } else if (examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH() != null) {
                        	map.put("persona", examen.getSolicitudEstudio().getIdTomaMx().getIdNotificacion().getCodigoPacienteVIH());
                        }
                        else {
                            map.put("persona", " ");
                        }

                    }
                    //detalle resultado examen
                    List<DetalleResultado> resultList = resultadoFinalService.getResultDetailExaByIdOrden(examen.getIdOrdenExamen());
                    Map<Integer, Object> mapResList = new HashMap<Integer, Object>();
                    Map<String, String> mapRes = new HashMap<String, String>();
                    int subIndice = 0;
                    for (DetalleResultado res : resultList) {

                        if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LIST")) {//ABRIL2019
                            Catalogo_Lista cat_lista = resultadoFinalService.getCatalogoLista(res.getValor());
                            mapRes.put("valor", cat_lista.getValor());
                        } else if (res.getRespuesta().getConcepto().getTipo().equals("TPDATO|LOG")) {//ABRIL2019
                            if (res.getValor().equals("false")) {
                                mapRes.put("valor", messageSource.getMessage("lbl.no", null, null));
                            } else {
                                mapRes.put("valor", messageSource.getMessage("lbl.yes", null, null));
                            }
                        } else {
                            mapRes.put("valor", res.getValor());
                        }
                        mapRes.put("respuesta", res.getRespuesta().getNombre());
                        mapRes.put("fechaResultado", DateUtil.DateToString(res.getFechahProcesa(), "dd/MM/yyyy hh:mm:ss a"));
                        subIndice++;
                        mapResList.put(subIndice, mapRes);
                        mapRes = new HashMap<String, String>();
                    }

                    map.put("resultado", new Gson().toJson(mapResList));
                    map.put("laboratorio", examen.getLabProcesa().getNombre());
                    map.put("procesado", (mapResList.size() > 0 ? messageSource.getMessage("lbl.yes", null, null) : messageSource.getMessage("lbl.no", null, null)));

                    mapResponse.put(indice, map);
                    indice++;

                }
            }
        jsonResponse = new Gson().toJson(mapResponse);
        UnicodeEscaper escaper = UnicodeEscaper.above(127);
        return escaper.translate(jsonResponse);

    }

    @RequestMapping(value = "getCatalogosListaConcepto", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<Catalogo_Lista> getCatalogoListaConcepto(@RequestParam(value = "idDx", required = false) String idDx, @RequestParam(value = "idEstudio", required = false) String idEstudio) throws Exception {
        logger.info("Obteniendo los valores para los conceptos tipo lista asociados a las respuesta del estudio o dx");

        List<Catalogo_Lista> catLista = null;
        if (idEstudio.isEmpty()) {
            catLista = respuestasSolicitudService.getCatalogoListaConceptoByIdDx(Integer.valueOf(idDx));
        } else {
            catLista = respuestasSolicitudService.getCatalogoListaConceptoByIdEstudio(Integer.valueOf(idEstudio));
        }

        return catLista;
    }

    @RequestMapping(value = "getDetResFinalBySolicitud", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<DetalleResultadoFinal> getDetResFinalBySolicitud(@RequestParam(value = "idSolicitud", required = true) String idSolicitud) throws Exception {
        logger.info("Se obtienen los detalles de resultados activos para la solicitud");
        List<DetalleResultadoFinal> resultados = resultadoFinalService.getDetResActivosBySolicitud(idSolicitud);
        return resultados;
    }

    @RequestMapping(value = "saveFinalResult", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    protected void saveFinalResult(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json;
        String resultado = "";
        String strRespuestas="";
        String idSolicitud="";
        Integer cantRespuestas=0;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF8"));
            json = br.readLine();
            //Recuperando Json enviado desde el cliente
            JsonObject jsonpObject = new Gson().fromJson(json, JsonObject.class);
            strRespuestas = jsonpObject.get("strRespuestas").toString();
            idSolicitud = jsonpObject.get("idSolicitud").getAsString();
            cantRespuestas = jsonpObject.get("cantRespuestas").getAsInt();
            DaSolicitudDx solicitud = tomaMxService.getSolicitudDxByIdSolicitud(idSolicitud);
            DaSolicitudEstudio estudio = tomaMxService.getEstudioByIdSolicitud(idSolicitud);
            User usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
            //se obtiene datos de los conceptos a registrar

            JsonObject jObjectRespuestas = new Gson().fromJson(strRespuestas, JsonObject.class);
            for(int i = 0; i< cantRespuestas;i++) {
                String respuesta = jObjectRespuestas.get(String.valueOf(i)).toString();
                JsonObject jsRespuestaObject = new Gson().fromJson(respuesta, JsonObject.class);

                Integer idRespuesta = jsRespuestaObject.get("idRespuesta").getAsInt();
                Integer idConcepto = jsRespuestaObject.get("idConcepto").getAsInt();

                RespuestaSolicitud conceptoTmp =  respuestasSolicitudService.getRespuestaDxById(idRespuesta);
                String valor = jsRespuestaObject.get("valor").getAsString();
                DetalleResultadoFinal detResFinal = new DetalleResultadoFinal();
                detResFinal.setFechahRegistro(new Timestamp(new Date().getTime()));
                detResFinal.setValor(valor);
                detResFinal.setRespuesta(conceptoTmp);
                detResFinal.setSolicitudDx(solicitud);
                detResFinal.setSolicitudEstudio(estudio);
                detResFinal.setUsuarioRegistro(usuario);
                //validar respuesta solicitud
                DetalleResultadoFinal resFinalRegistrado = resultadoFinalService.getDetResBySolicitudAndRespuesta(idSolicitud,idRespuesta);
                if (resFinalRegistrado!=null){
                    detResFinal.setIdDetalle(resFinalRegistrado.getIdDetalle());
                    resultadoFinalService.updateDetResFinal(detResFinal);
                }else {
                    //validar respuesta examen como respuesta solicitud
                    resFinalRegistrado = resultadoFinalService.getDetResBySolicitudAndRespuestaExa(idSolicitud,idRespuesta);
                    if (resFinalRegistrado!=null){
                        detResFinal.setIdDetalle(resFinalRegistrado.getIdDetalle());
                        resultadoFinalService.updateDetResFinal(detResFinal);
                    }else {
                        //validar si hay respuesta examen con el concetpo a registrar
                        if (resultadoFinalService.getDetResBySolicitudAndConceptoRespuestaExa(idSolicitud,idConcepto).size()<=0){
                            if (detResFinal.getValor() != null && !detResFinal.getValor().isEmpty()) {
                                resultadoFinalService.saveDetResFinal(detResFinal);
                            }
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
            map.put("idSolicitud",idSolicitud);
            map.put("strRespuestas",strRespuestas);
            map.put("mensaje",resultado);
            map.put("cantRespuestas",cantRespuestas.toString());
            String jsonResponse = new Gson().toJson(map);
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        }
    }


    @RequestMapping(value = "overrideResult", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    protected void overrideResult(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json;
        String resultado = "";
        String idSolicitud="";
        String causaAnulacion = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF8"));
            json = br.readLine();
            //Recuperando Json enviado desde el cliente
            JsonObject jsonpObject = new Gson().fromJson(json, JsonObject.class);
            idSolicitud = jsonpObject.get("idSolicitud").getAsString();
            causaAnulacion = jsonpObject.get("causaAnulacion").getAsString();
            DaSolicitudDx soli = tomaMxService.getSolicitudDxByIdSolicitud(idSolicitud);
            DaSolicitudEstudio estudio = tomaMxService.getEstudioByIdSolicitud(idSolicitud);
            User usuario = seguridadService.getUsuario(seguridadService.obtenerNombreUsuario());
            //se obtiene datos de los resultados a registrar
            List<DetalleResultadoFinal> detResFinalActivos =  resultadoFinalService.getDetResActivosBySolicitud(idSolicitud);
            for(DetalleResultadoFinal detResFinal : detResFinalActivos) {
                detResFinal.setFechahAnulacion(new Timestamp(new Date().getTime()));
                detResFinal.setSolicitudDx(soli);
                detResFinal.setSolicitudEstudio(estudio);
                detResFinal.setUsuarioAnulacion(usuario);
                detResFinal.setRazonAnulacion(causaAnulacion);
                detResFinal.setPasivo(true);
                resultadoFinalService.updateDetResFinal(detResFinal);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            ex.printStackTrace();
            resultado =  messageSource.getMessage("msg.result.error.canceled",null,null);
            resultado=resultado+". \n "+ex.getMessage();

        }finally {
            Map<String, String> map = new HashMap<String, String>();
            map.put("idSolicitud",idSolicitud);
            map.put("causaAnulacion",causaAnulacion);
            map.put("mensaje",resultado);
            String jsonResponse = new Gson().toJson(map);
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        }
    }

}
