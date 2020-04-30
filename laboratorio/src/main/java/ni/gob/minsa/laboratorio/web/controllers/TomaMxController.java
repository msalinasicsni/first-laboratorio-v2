package ni.gob.minsa.laboratorio.web.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ni.gob.minsa.laboratorio.domain.irag.DaIrag;
import ni.gob.minsa.laboratorio.domain.muestra.*;
import ni.gob.minsa.laboratorio.domain.notificacion.DaNotificacion;
import ni.gob.minsa.laboratorio.domain.parametros.Parametro;
import ni.gob.minsa.laboratorio.domain.persona.PersonaTmp;
import ni.gob.minsa.laboratorio.domain.portal.Usuarios;
import ni.gob.minsa.laboratorio.domain.vigilanciaSindFebril.DaSindFebril;
import ni.gob.minsa.laboratorio.restServices.CallRestServices;
import ni.gob.minsa.laboratorio.restServices.MinsaServices;
import ni.gob.minsa.laboratorio.restServices.constantes.CatalogConstants;
import ni.gob.minsa.laboratorio.restServices.entidades.*;
import ni.gob.minsa.laboratorio.service.*;
import ni.gob.minsa.laboratorio.utilities.DateUtil;
import ni.gob.minsa.laboratorio.utilities.StringUtil;
import ni.gob.minsa.laboratorio.utilities.enumeration.HealthUnitType;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by souyen-ics on 11-05-14.
 */
@Controller
@RequestMapping("tomaMx")
public class TomaMxController {

    private static final Logger logger = LoggerFactory.getLogger(TomaMxController.class);

    @Autowired
    @Qualifier(value = "seguridadService")
    private SeguridadService seguridadService;

    @Resource(name="tomaMxService")
    private TomaMxService tomaMxService;

    @Resource(name="usuarioService")
    public UsuarioService usuarioService;

    @Resource(name = "catalogosService")
    public CatalogoService catalogoService;

    @Resource(name = "daNotificacionService")
    public DaNotificacionService daNotificacionService;

    @Resource(name = "sindFebrilService")
    public SindFebrilService sindFebrilService;

    @Resource(name = "daIragService")
    public DaIragService daIragService;

    @Resource(name = "personaService")
    public PersonaService personaService;

    @Resource(name = "parametrosService")
    private ParametrosService parametrosService;

    @Resource(name = "datosSolicitudService")
    private DatosSolicitudService datosSolicitudService;

    @Resource(name = "solicitanteService")
    private SolicitanteService solicitanteService;

    @Resource(name = "recepcionMxService")
    private RecepcionMxService recepcionMxService;

    @Autowired
    MessageSource messageSource;

    List<TipoMx_TipoNotificacion> catTipoMx;



    @RequestMapping(value = "search", method = RequestMethod.GET)
    public ModelAndView initSearchForm() throws ParseException {
        logger.debug("Crear/Buscar Toma de Mx");
        ModelAndView mav = new ModelAndView();
        mav.addObject("personaByIdentificacion", MinsaServices.SEVICIO_PERSONAS_IDENTIFICACION);
        mav.addObject("personaByNombres", MinsaServices.SEVICIO_PERSONAS_NONBRES);
        mav.setViewName("tomaMx/search");
        return mav;
    }

    @RequestMapping("noticesrut/{idPerson}/{identificada}")
    public ModelAndView showNoticesRutPerson(@PathVariable("idPerson") long idPerson, @PathVariable("identificada") boolean identificada) throws Exception {
        ModelAndView mav = new ModelAndView();
        List<DaNotificacion> results = daNotificacionService.getNoticesByPerson(idPerson);
        mav.addObject("notificaciones", results);
        mav.addObject("personaId",idPerson);
        mav.addObject("identificada",identificada);
        mav.setViewName("tomaMx/results");
        return  mav;
    }

    /**
     * Handler for create tomaMx.
     *
     * @param idNotificacion the ID of the person to create noti
     * @return a ModelMap with the model attributes for the respective view
     */
    @RequestMapping("createInicial/{idNotificacion}")
    public ModelAndView createTomaMxInicial(@PathVariable("idNotificacion") String idNotificacion) throws Exception {
        ModelAndView mav = new ModelAndView();
        //registros anteriores de toma Mx
        DaTomaMx tomaMx = new DaTomaMx();
        DaNotificacion noti;
        //si es numero significa que es un id de persona, no de notificaci�n por tanto hay que crear una notificaci�n para esa persona
        noti = daNotificacionService.getNotifById(idNotificacion);
        if (noti != null) {
            //catTipoMx = tomaMxService.getTipoMxByTipoNoti("TPNOTI|CAESP");
            catTipoMx = tomaMxService.getTipoMxByTipoNoti("TPNOTI|PCNT");
            List<EntidadesAdtvas> entidadesAdtvases =  CallRestServices.getEntidadesAdtvas();
            List<Municipio> municipios = null;
            if (noti.getCodSilaisAtencion()!=null){
                municipios = CallRestServices.getMunicipiosEntidad(noti.getIdSilaisAtencion());//ABRIL2019
            }
            List<Unidades> unidades = null;
            if (noti.getCodUnidadAtencion()!=null && noti.getCodSilaisAtencion()!=null){
                unidades = CallRestServices.getUnidadesByEntidadMunicipioTipo(noti.getIdSilaisAtencion(),
                        noti.getIdMuniUnidadAtencion(), HealthUnitType.UnidadesPrimHosp.getDiscriminator().split(",")); //ABRIL2019
            }
            //ABRIL2019
            List<Catalogo> tiposNotificacion = CallRestServices.getCatalogos(CatalogConstants.TipoNotificacion);
            List<Catalogo> tiposNotificacionValidas = new ArrayList<Catalogo>();
            for(Catalogo catalogo : tiposNotificacion){
                if (catalogo.getCodigo().equalsIgnoreCase("TPNOTI|PCNT")
                        || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|SINFEB")
                        || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|IRAG")
                        || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|VIH")
                        || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|TB"))
                    tiposNotificacionValidas.add(catalogo);
            }
            List<Catalogo> catResp =CallRestServices.getCatalogos(CatalogConstants.Respuesta);//ABRIL2019
            Laboratorio labUser = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());

            mav.addObject("noti", noti);
            mav.addObject("tomaMx", tomaMx);
            mav.addObject("catTipoMx", catTipoMx);
            mav.addObject("entidades",entidadesAdtvases);
            mav.addObject("municipios",municipios);
            mav.addObject("unidades",unidades);
            mav.addObject("notificaciones",tiposNotificacionValidas);
            mav.addObject("catResp", catResp);
            mav.addObject("esNuevaNoti",true);
            mav.addObject("mostrarPopUpMx",(labUser!=null?labUser.getPopUpCodigoMx():false));
            //mav.addAllObjects(mapModel);
            mav.setViewName("tomaMx/enterForm");
        } else {
            mav.setViewName("404");
        }

        return mav;
    }

    /**
     * Handler for create tomaMx.
     *
     * @param idNotificacion the ID of the notification
     * @return a ModelMap with the model attributes for the respective view
     */
    @RequestMapping("create/{idNotificacion}")
    public ModelAndView createTomaMx(@PathVariable("idNotificacion") String idNotificacion) throws Exception {
        ModelAndView mav = new ModelAndView();
        //registros anteriores de toma Mx
        DaTomaMx tomaMx = new DaTomaMx();
        DaNotificacion noti;
        //si es numero significa que es un id de persona, no de notificaci�n por tanto hay que crear una notificaci�n para esa persona
        noti = daNotificacionService.getNotifById(idNotificacion);
        if (noti != null) {
            catTipoMx = tomaMxService.getTipoMxByTipoNoti(noti.getCodTipoNotificacion());//ABRIL2019
            List<EntidadesAdtvas> entidadesAdtvases =  CallRestServices.getEntidadesAdtvas();
            List<Municipio> municipios = null;
            if (noti.getCodSilaisAtencion()!=null){
                municipios = CallRestServices.getMunicipiosEntidad(noti.getIdSilaisAtencion());//ABRIL2019
            }
            List<Unidades> unidades = null;
            if (noti.getCodUnidadAtencion()!=null && noti.getCodSilaisAtencion()!=null){
                unidades = CallRestServices.getUnidadesByEntidadMunicipioTipo(noti.getIdSilaisAtencion(),
                        noti.getIdMuniUnidadAtencion(), HealthUnitType.UnidadesPrimHosp.getDiscriminator().split(",")); //ABRIL2019
            }
            //ABRIL2019
            List<Catalogo> tiposNotificacion = CallRestServices.getCatalogos(CatalogConstants.TipoNotificacion);
            List<Catalogo> tiposNotificacionValidas = new ArrayList<Catalogo>();
            //No permitir cambio de tipo de notificación si no es PACIENT
            if (noti.getCodTipoNotificacion().equalsIgnoreCase("TPNOTI|PCNT")){
                for(Catalogo catalogo : tiposNotificacion){
                    if (catalogo.getCodigo().equalsIgnoreCase("TPNOTI|PCNT")
                            || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|SINFEB")
                            || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|IRAG")
                            || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|VIH")
                            || catalogo.getCodigo().equalsIgnoreCase("TPNOTI|TB"))
                        tiposNotificacionValidas.add(catalogo);
                }
            }else {
                for(Catalogo catalogo : tiposNotificacion){
                    if (catalogo.getCodigo().equalsIgnoreCase(noti.getCodTipoNotificacion()))
                        tiposNotificacionValidas.add(catalogo);
                }
            }

            List<Catalogo> catResp =CallRestServices.getCatalogos(CatalogConstants.Respuesta);//ABRIL2019
            Laboratorio labUser = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());

            mav.addObject("noti", noti);
            mav.addObject("tomaMx", tomaMx);
            mav.addObject("catTipoMx", catTipoMx);
            mav.addObject("entidades",entidadesAdtvases);
            mav.addObject("municipios",municipios);
            mav.addObject("unidades",unidades);
            mav.addObject("notificaciones",tiposNotificacionValidas);
            mav.addObject("catResp", catResp);
            mav.addObject("esNuevaNoti",false);
            mav.addObject("mostrarPopUpMx",labUser.getPopUpCodigoMx());
            //mav.addAllObjects(mapModel);
            mav.setViewName("tomaMx/enterForm");
        } else {
            mav.setViewName("404");
        }

        return mav;
    }

    @RequestMapping("override/{idNotificacion}")
    public String overrideNotificacion(@PathVariable("idNotificacion") String idNotificacion) throws Exception {
        ModelAndView mav = new ModelAndView();
        //registros anteriores de toma Mx
        DaTomaMx tomaMx = new DaTomaMx();
        DaNotificacion noti;
        //si es numero significa que es un id de persona, no de notificaci�n por tanto hay que crear una notificaci�n para esa persona
        noti = daNotificacionService.getNotifById(idNotificacion);
        if (noti != null) {
            noti.setPasivo(true);
            noti.setFechaAnulacion(new Timestamp(new Date().getTime()));
            daNotificacionService.updateNotificacion(noti);
            tomaMxService.anularTomasMxByIdNotificacion(noti.getIdNotificacion(),noti.getFechaAnulacion());
            if (noti.getPersona()!=null)
                return "redirect:/tomaMx/noticesrut/"+noti.getPersona().getPersonaId();
            else
                return "redirect:/tomaMx/notices/applicant/"+noti.getSolicitante().getIdSolicitante();
        } else {
            return "redirect:/404";
        }
    }

    /**
     * Retorna una lista de dx
     * @return Un arreglo JSON de dx
     */
    @RequestMapping(value = "dxByMx", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<Dx_TipoMx_TipoNoti> getDxBySample(@RequestParam(value = "codMx", required = true) String codMx, @RequestParam(value = "tipoNoti", required = true) String tipoNoti) throws Exception {
        logger.info("Obteniendo los diagn�sticos segun muestra y tipo de Notificacion en JSON");
        //nombre usuario null, para que no valide autoridad
        return tomaMxService.getDx(codMx, tipoNoti,null, null);
    }

    private boolean saveDxRequest(String idTomaMx, String dx, String strRespuestas, Integer cantRespuestas) throws Exception {
        try {
            DaSolicitudDx soli = new DaSolicitudDx();
            String[] arrayDx = dx.split(",");
            Laboratorio laboratorio = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
            for (String anArrayDx : arrayDx) {
                soli.setCodDx(tomaMxService.getDxById(anArrayDx));
                soli.setFechaHSolicitud(new Timestamp(new Date().getTime()));
                Parametro pUsuarioRegistro = parametrosService.getParametroByName("USU_REGISTRO_NOTI_CAESP");
                if (pUsuarioRegistro != null) {
                    long idUsuario = Long.valueOf(pUsuarioRegistro.getValor());
                    soli.setUsarioRegistro(usuarioService.getUsuarioById((int) idUsuario));
                }
                soli.setIdTomaMx(tomaMxService.getTomaMxById(idTomaMx));
                soli.setAprobada(false);
                soli.setLabProcesa(laboratorio);
                soli.setControlCalidad(false);
                soli.setInicial(true);//es lo que viene en la ficha
                tomaMxService.addSolicitudDx(soli);

                JsonObject jObjectRespuestas = new Gson().fromJson(strRespuestas, JsonObject.class);
                for (int i = 0; i < cantRespuestas; i++) {
                    String respuesta = jObjectRespuestas.get(String.valueOf(i)).toString();
                    JsonObject jsRespuestaObject = new Gson().fromJson(respuesta, JsonObject.class);

                    Integer idRespuesta = jsRespuestaObject.get("idRespuesta").getAsInt();
                    Integer idConcepto = jsRespuestaObject.get("idConcepto").getAsInt();

                    DatoSolicitud conceptoTmp = datosSolicitudService.getDatoRecepcionSolicitudById(idRespuesta);
                    //si la respuesta pertenece al dx de la solicitud, se registra
                    if (conceptoTmp.getDiagnostico().getIdDiagnostico().equals(soli.getCodDx().getIdDiagnostico())) {
                        String valor = jsRespuestaObject.get("valor").getAsString();
                        if (valor != null) {
                            DatoSolicitudDetalle datoSolicitudDetalle = new DatoSolicitudDetalle();
                            datoSolicitudDetalle.setFechahRegistro(new Timestamp(new Date().getTime()));
                            datoSolicitudDetalle.setValor(valor.isEmpty() ? " " : valor);
                            datoSolicitudDetalle.setDatoSolicitud(conceptoTmp);
                            datoSolicitudDetalle.setSolicitudDx(soli);
                            datoSolicitudDetalle.setUsuarioRegistro(seguridadService.getUsuario(seguridadService.obtenerNombreUsuario()));
                            datosSolicitudService.saveOrUpdateDetalleDatoRecepcion(datoSolicitudDetalle);
                        }
                    }
                }
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    private Timestamp StringToTimestamp(String fechah) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = sdf.parse(fechah);
        return new Timestamp(date.getTime());
    }

    private ResponseEntity<String> createJsonResponse(Object o) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        Gson gson = new Gson();
        String json = gson.toJson(o);
        return new ResponseEntity<String>(json, headers, HttpStatus.CREATED);
    }

    /**
     * M�todo para generar un string alfanum�rico de 8 caracteres, que se usar� como c�digo �nico de muestra
     * @return String codigoUnicoMx
     */
    private String generarCodigoUnicoMx(){
        DaTomaMx validaC;
        //Se genera el c�digo
        String codigoUnicoMx = StringUtil.getCadenaAlfanumAleatoria(8);
        //Se consulta BD para ver si existe toma de Mx que tenga mismo c�digo
        validaC = tomaMxService.getTomaMxByCodUnicoMx(codigoUnicoMx);
        //si existe, de manera recursiva se solicita un nuevo c�digo
        if (validaC!=null){
            codigoUnicoMx = generarCodigoUnicoMx();
        }
        //si no existe se retorna el �ltimo c�digo generado
        return codigoUnicoMx;
    }

    /***************************************************************************/
    /******************** TOMA MUESTRAS PACIENTES Y OTRAS MX********************************/
    /***************************************************************************/

    @RequestMapping(value = "validateTomaMx", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String validateTomaMx(@RequestParam(value = "idNotificacion", required = true) String idNotificacion,
                          @RequestParam(value = "fechaHTomaMx", required = true) String fechaToma,
                          @RequestParam(value = "dxs", required = true) String dxs) throws Exception {
        logger.info("Realizando validacion de Toma de Mx.");
        String respuesta = "OK";
        if (existeTomaMx(idNotificacion, fechaToma, dxs)){
            respuesta = messageSource.getMessage("msg.existe.toma", null, null);
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("respuesta", respuesta);
        String jsonResponse = new Gson().toJson(map);
        return jsonResponse;
    }

    private boolean existeTomaMx(String idNotificacion, String fechaToma, String dxs) throws Exception{
        int totalEncontrados = 0;
        boolean respuesta = false;
        String[] dxArray = dxs.split(",");
        Date fecha1 = DateUtil.StringToDate(fechaToma, "dd/MM/yyyy");
        List<DaTomaMx> muestras = tomaMxService.getTomaMxActivaByIdNoti(idNotificacion);
        for(DaTomaMx muestra : muestras){
            List<DaSolicitudDx> solicitudDxList = tomaMxService.getSoliDxByIdMxFechaToma(muestra.getIdTomaMx(), fecha1);
            for(String dx : dxArray) {
                for (DaSolicitudDx solicitudDx : solicitudDxList) {
                    if (solicitudDx.getCodDx().getIdDiagnostico().equals(Integer.valueOf(dx))) {
                        totalEncontrados++;
                        break;
                    }
                }
            }
            if (totalEncontrados == dxArray.length && totalEncontrados == solicitudDxList.size()) {
                respuesta = true;
                break;
            }
            totalEncontrados = 0;
        }
        return respuesta;
    }

    @RequestMapping(value = "saveToma", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
    protected void saveTomaMxPacienteOtras(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json = "";
        String resultado = "";
        String fechaHTomaMx="";
        String idNotificacion="";
        String codTipoMx="";
        Integer canTubos=null;
        String volumen=null;
        String horaRefrigeracion="";
        String dx="";
        String strRespuestas="";
        Integer cantRespuestas=0;
        Long idSilais=null;
        Long idMunicipio=null;
        Long idUnidadSalud=null;
        String codTipoNoti="";
        String horaTomaMx="";
        String fechaInicioSintomas="";
        String codigoGenerado = "";
        String urgente = "";
        String embarazada = "";
        Integer semanasEmbarazo=null;
        String areaEntrega = "";
        String codExpediente = "";
        try {
            logger.debug("Guardando datos de Toma de Muestra");
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF8"));
            json = br.readLine();
            //Recuperando Json enviado desde el cliente
            JsonObject jsonpObject = new Gson().fromJson(json, JsonObject.class);
            idNotificacion = jsonpObject.get("idNotificacion").getAsString();
            fechaHTomaMx = jsonpObject.get("fechaHTomaMx").getAsString();
            codTipoMx = jsonpObject.get("codTipoMx").getAsString();
            if (jsonpObject.get("strRespuestas")!=null)
                strRespuestas = jsonpObject.get("strRespuestas").toString();
            if (jsonpObject.get("cantRespuestas")!=null && !jsonpObject.get("cantRespuestas").getAsString().isEmpty())
                cantRespuestas = jsonpObject.get("cantRespuestas").getAsInt();
            if (jsonpObject.get("canTubos")!=null && !jsonpObject.get("canTubos").getAsString().isEmpty())
                canTubos = jsonpObject.get("canTubos").getAsInt();

            if (jsonpObject.get("volumen")!=null && !jsonpObject.get("volumen").getAsString().isEmpty())
                volumen = jsonpObject.get("volumen").getAsString();

            if (jsonpObject.get("dx")!=null && !jsonpObject.get("dx").getAsString().isEmpty())
                dx = jsonpObject.get("dx").getAsString();

            if (jsonpObject.get("idSilais")!=null && !jsonpObject.get("idSilais").getAsString().isEmpty())
                idSilais = jsonpObject.get("idSilais").getAsLong();

            if (jsonpObject.get("idMunicipio")!=null && !jsonpObject.get("idMunicipio").getAsString().isEmpty())
                idMunicipio = jsonpObject.get("idMunicipio").getAsLong();

            if (jsonpObject.get("idUnidadSalud")!=null && !jsonpObject.get("idUnidadSalud").getAsString().isEmpty())
                idUnidadSalud = jsonpObject.get("idUnidadSalud").getAsLong();

            if (jsonpObject.get("codTipoNoti")!=null && !jsonpObject.get("codTipoNoti").getAsString().isEmpty())
                codTipoNoti = jsonpObject.get("codTipoNoti").getAsString();

            if (jsonpObject.get("horaTomaMx")!=null && !jsonpObject.get("horaTomaMx").getAsString().isEmpty())
                horaTomaMx = jsonpObject.get("horaTomaMx").getAsString();

            if (jsonpObject.get("fechaInicioSintomas")!=null && !jsonpObject.get("fechaInicioSintomas").getAsString().isEmpty())
                fechaInicioSintomas = jsonpObject.get("fechaInicioSintomas").getAsString();

            horaRefrigeracion = jsonpObject.get("horaRefrigeracion").getAsString();

            if (jsonpObject.get("urgente")!=null && !jsonpObject.get("urgente").getAsString().isEmpty())
                urgente = jsonpObject.get("urgente").getAsString();
            if (jsonpObject.get("embarazada")!=null && !jsonpObject.get("embarazada").getAsString().isEmpty())
                embarazada = jsonpObject.get("embarazada").getAsString();
            if (jsonpObject.get("semanasEmbarazo")!=null && !jsonpObject.get("semanasEmbarazo").getAsString().isEmpty())
                semanasEmbarazo = jsonpObject.get("semanasEmbarazo").getAsInt();
            if (jsonpObject.get("codExpediente")!=null && !jsonpObject.get("codExpediente").getAsString().isEmpty())
                codExpediente = jsonpObject.get("codExpediente").getAsString();

            Parametro pUsuarioRegistro = parametrosService.getParametroByName("USU_REGISTRO_NOTI_CAESP");
            Usuarios usuarioRegistro = new Usuarios();
            if(pUsuarioRegistro!=null) {
                long idUsuario = Long.valueOf(pUsuarioRegistro.getValor());
                usuarioRegistro = usuarioService.getUsuarioById((int)idUsuario);
            }

            DaEnvioMx envioOrden = new DaEnvioMx();

            envioOrden.setUsarioRegistro(usuarioRegistro);
            envioOrden.setFechaHoraEnvio(new Timestamp(new Date().getTime()));
            envioOrden.setNombreTransporta("");
            envioOrden.setTemperaturaTermo(null);
            envioOrden.setLaboratorioDestino(seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario()));
            try {
                tomaMxService.addEnvioOrden(envioOrden);
            }catch (Exception ex){
                resultado = messageSource.getMessage("msg.sending.error.add",null,null);
                resultado=resultado+". \n "+ex.getMessage();
                ex.printStackTrace();
                throw new Exception(ex);
            }

            DaTomaMx tomaMx = new DaTomaMx();
            Laboratorio labUsuario = seguridadService.getLaboratorioUsuario(seguridadService.obtenerNombreUsuario());
            DaNotificacion notificacion = daNotificacionService.getNotifById(idNotificacion);
            //ABRIL2019 Consultar servicio o obtener todos los datos del Silais
            if (idSilais!=null){
                notificacion.setIdSilaisAtencion(idSilais);
                tomaMx.setIdSilaisAtencion(notificacion.getIdSilaisAtencion());

                EntidadesAdtvas silais = CallRestServices.getEntidadAdtva(idSilais);
                if (silais!=null){
                    notificacion.setCodSilaisAtencion(Long.valueOf(silais.getCodigo()));
                    notificacion.setNombreSilaisAtencion(silais.getNombre());

                    tomaMx.setCodSilaisAtencion(notificacion.getCodSilaisAtencion());
                    tomaMx.setNombreSilaisAtencion(notificacion.getNombreSilaisAtencion());
                }
            }

            //ABRIL2019 Consultar servicio o obtener todos los datos de la unidad
            if (idUnidadSalud!=null){
                notificacion.setIdUnidadAtencion(idUnidadSalud);
                tomaMx.setIdUnidadAtencion(notificacion.getIdUnidadAtencion());

                Unidades unidad = CallRestServices.getUnidadSalud(idUnidadSalud);
                if (unidad!=null){
                    notificacion.setCodUnidadAtencion(Long.valueOf(unidad.getCodigo()));
                    notificacion.setNombreUnidadAtencion(unidad.getNombre());
                    tomaMx.setCodUnidadAtencion(notificacion.getCodUnidadAtencion());
                    tomaMx.setNombreUnidadAtencion(notificacion.getNombreUnidadAtencion());
                    if (unidad.getMunicipio()!=null){
                        notificacion.setIdMuniUnidadAtencion(unidad.getMunicipio().getId());
                        notificacion.setCodMuniUnidadAtencion(Long.valueOf(unidad.getMunicipio().getCodigo()));
                        notificacion.setNombreMuniUnidadAtencion(unidad.getMunicipio().getNombre());

                        tomaMx.setIdMuniUnidadAtencion(unidad.getMunicipio().getId());
                        tomaMx.setCodMuniUnidadAtencion(Long.valueOf(unidad.getMunicipio().getCodigo()));
                        tomaMx.setNombreMuniUnidadAtencion(unidad.getMunicipio().getNombre());
                    }

                    if (unidad.getTipoestablecimiento()!=null){
                        notificacion.setTipoUnidad(unidad.getTipoestablecimiento().getId());
                        tomaMx.setTipoUnidad(unidad.getTipoestablecimiento().getId());
                    }
                }
            }
            if (notificacion.getIdMuniUnidadAtencion() == null && idMunicipio!=null){
                Municipio municipio = CallRestServices.getMunicipio(idMunicipio);
                if (municipio!=null) {
                    notificacion.setIdMuniUnidadAtencion(municipio.getId());
                    notificacion.setCodMuniUnidadAtencion(Long.valueOf(municipio.getCodigo()));
                    notificacion.setNombreMuniUnidadAtencion(municipio.getNombre());

                    tomaMx.setIdMuniUnidadAtencion(municipio.getId());
                    tomaMx.setCodMuniUnidadAtencion(Long.valueOf(municipio.getCodigo()));
                    tomaMx.setNombreMuniUnidadAtencion(municipio.getNombre());
                }
            }
            if (!codTipoNoti.isEmpty()){
                notificacion.setCodTipoNotificacion(codTipoNoti);//ABRIL2019
            }
            if (!fechaInicioSintomas.isEmpty()){
                notificacion.setFechaInicioSintomas(DateUtil.StringToDate(fechaInicioSintomas,"dd/MM/yyyy"));
            }
            if (!urgente.isEmpty()) {
                notificacion.setUrgente(urgente);//ABRIL2019
            }
            if (!embarazada.isEmpty()) {
                notificacion.setEmbarazada(embarazada);//ABRIL2019
            }
            if (semanasEmbarazo!=null) {
                notificacion.setSemanasEmbarazo(semanasEmbarazo);
            }
            if (!codExpediente.isEmpty()) {
                notificacion.setCodExpediente(codExpediente);
            }
            tomaMx.setIdNotificacion(notificacion);
            if(fechaHTomaMx != null){
                tomaMx.setFechaHTomaMx(StringToTimestamp(fechaHTomaMx));
            }

            tomaMx.setCodTipoMx(tomaMxService.getTipoMxById(codTipoMx));
            tomaMx.setCanTubos(canTubos);
            tomaMx.setHoraTomaMx(horaTomaMx);

            if(volumen != null && !volumen.equals("")){
                tomaMx.setVolumen(Float.valueOf(volumen));
            }

            tomaMx.setHoraRefrigeracion(horaRefrigeracion);
            tomaMx.setMxSeparada(false);
            tomaMx.setFechaRegistro(new Timestamp(new Date().getTime()));

            tomaMx.setUsuario(usuarioRegistro);
            tomaMx.setEstadoMx("ESTDMX|RCP"); //quedan listas para enviar a procesar en el area que le corresponde//ABRIL2019
            String codigo = generarCodigoUnicoMx();
            tomaMx.setCodigoUnicoMx(codigo);
            //todas deben tener codigo lab, porque son rutinas
            tomaMx.setCodigoLab(recepcionMxService.obtenerCodigoLab(labUsuario.getCodigo(),1));
            codigoGenerado = tomaMx.getCodigoLab();
            tomaMx.setEnvio(envioOrden);
            try {
                if (existeTomaMx(idNotificacion, fechaHTomaMx, dx)) {
                    throw new Exception(messageSource.getMessage("msg.existe.toma", null, null));
                } else {
                    tomaMxService.addTomaMx(tomaMx);
                }
            }catch (Exception ex){
                tomaMxService.deleteEnvioOrden(envioOrden);
                resultado=resultado+". \n "+ex.getMessage();
                ex.printStackTrace();
                throw ex;
            }

            try {
                //daNotificacionService.updateNotificacion(notificacion);
                crearFicha(notificacion);
            }catch (Throwable ex){
                tomaMxService.deleteTomaMx(tomaMx);
                tomaMxService.deleteEnvioOrden(envioOrden);
                resultado = messageSource.getMessage("msg.error.update.noti",null,null);
                resultado=resultado+". \n "+ex.getMessage();
                ex.printStackTrace();
                throw ex;
            }
            //se procede a registrar los diagn�sticos o rutinas solicitados (incluyendo los datos que se pidan para cada uno)
            if (saveDxRequest(tomaMx.getIdTomaMx(), dx, strRespuestas, cantRespuestas)) {
                try {
                    List<DaSolicitudDx> solicitudDxList = tomaMxService.getSoliDxPrioridadByTomaAndLab(tomaMx.getIdTomaMx(), labUsuario.getCodigo());
                    List<DaSolicitudEstudio> solicitudEstudioList = new ArrayList<DaSolicitudEstudio>();
                    //area que procesa la solicitud con mayor prioridad
                    if (solicitudDxList.size() > 0)
                        areaEntrega = solicitudDxList.get(0).getCodDx().getArea().getNombre();
                    else {
                        solicitudEstudioList = tomaMxService.getSolicitudesEstudioByIdTomaMx(tomaMx.getIdTomaMx());
                        if (solicitudEstudioList.size() > 0)
                            areaEntrega = solicitudEstudioList.get(0).getTipoEstudio().getArea().getNombre();
                    }
                    //Como la muestra queda en estado recepcionada, entonces es necesario registrar la recepci�n de la misma
                    RecepcionMx recepcionMx = new RecepcionMx();
                    recepcionMx.setUsuarioRecepcion(seguridadService.getUsuario(seguridadService.obtenerNombreUsuario()));
                    recepcionMx.setLabRecepcion(labUsuario);
                    recepcionMx.setFechaHoraRecepcion(new Timestamp(new Date().getTime()));
                    recepcionMx.setTipoMxCk(true);
                    recepcionMx.setCantidadTubosCk(true);
                    recepcionMx.setTipoRecepcionMx("TPRECPMX|VRT");//ABRIL2019
                    recepcionMx.setTomaMx(tomaMx);

                    recepcionMxService.addRecepcionMx(recepcionMx);
                } catch (Exception ex) { //rollback completo
                    ex.printStackTrace();
                    datosSolicitudService.deleteDetallesDatosRecepcionByTomaMx(tomaMx.getIdTomaMx());
                    tomaMxService.deleteSolicitudesDxByTomaMx(tomaMx.getIdTomaMx());
                    tomaMxService.deleteTomaMx(tomaMx);
                    resultado=resultado+". \n "+ex.getMessage();
                }
            }else{ //rollback completo
                datosSolicitudService.deleteDetallesDatosRecepcionByTomaMx(tomaMx.getIdTomaMx());
                tomaMxService.deleteSolicitudesDxByTomaMx(tomaMx.getIdTomaMx());
                tomaMxService.deleteTomaMx(tomaMx);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            ex.printStackTrace();
            resultado =  messageSource.getMessage("msg.add.receipt.error",null,null);
            resultado=resultado+". \n "+ex.getMessage();

        } finally {
            UnicodeEscaper escaper     = UnicodeEscaper.above(127);
            Map<String, String> map = new HashMap<String, String>();
            map.put("idNotificacion",idNotificacion);
            map.put("canTubos",String.valueOf(canTubos));
            map.put("volumen",volumen);
            map.put("mensaje",resultado);
            map.put("fechaHTomaMx", fechaHTomaMx);
            map.put("codTipoMx",codTipoMx);
            map.put("horaRefrigeracion", horaRefrigeracion);
            map.put("strRespuestas",strRespuestas);
            map.put("cantRespuestas",cantRespuestas.toString());
            map.put("codSilais","");
            map.put("codUnidadSalud","");
            map.put("codTipoNoti",codTipoNoti);
            map.put("horaTomaMx",horaTomaMx);
            map.put("fechaInicioSintomas",fechaInicioSintomas);
            map.put("codigoLab", codigoGenerado);
            map.put("areaPrc",escaper.translate(areaEntrega));
            map.put("codExpediente", codExpediente);
            String jsonResponse = new Gson().toJson(map);
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        }
    }

    private void crearFicha(DaNotificacion notificacion) throws Exception{
        switch (notificacion.getCodTipoNotificacion()){//ABRIL2019
            case "TPNOTI|SINFEB": {
                DaSindFebril sindFebril = sindFebrilService.getDaSindFebril(notificacion.getIdNotificacion());
                if (sindFebril==null) {
                    sindFebril = new DaSindFebril();
                    sindFebril.setFechaFicha(notificacion.getFechaRegistro());
                }
                sindFebril.setIdNotificacion(notificacion);
                if (notificacion.getSemanasEmbarazo()!=null) {
                    sindFebril.setMesesEmbarazo(notificacion.getSemanasEmbarazo());
                }else {
                    sindFebril.setMesesEmbarazo(0);
                }
                if (notificacion.getEmbarazada()!=null){
                    sindFebril.setEmbarazo(notificacion.getEmbarazada());
                }
                if (notificacion.getCodExpediente()!=null){
                    sindFebril.setCodExpediente(notificacion.getCodExpediente());
                }
                sindFebrilService.saveSindFebril(sindFebril);
                break;
            }
            case "TPNOTI|IRAG": {
                DaIrag irag = daIragService.getFormById(notificacion.getIdNotificacion());
                if (irag==null) {
                    irag = new DaIrag();
                    irag.setFechaRegistro(notificacion.getFechaRegistro());
                    irag.setUsuario(notificacion.getUsuarioRegistro());
                }
                irag.setIdNotificacion(notificacion);
                if (notificacion.getEmbarazada()!=null){
                    if (irag.getCondiciones()!=null) {
                        if (!irag.getCondiciones().contains("CONDPRE|EMB"))
                            irag.setCondiciones(irag.getCondiciones() + ",CONDPRE|EMB");
                    }
                    else irag.setCondiciones("CONDPRE|EMB");
                }
                if (notificacion.getSemanasEmbarazo()!=null) {
                    irag.setSemanasEmbarazo(notificacion.getSemanasEmbarazo());
                }
                if (notificacion.getCodExpediente()!=null){
                    irag.setCodExpediente(notificacion.getCodExpediente());
                }
                daIragService.saveOrUpdateIrag(irag);
                break;
            }
            default:
                DaNotificacion noti = daNotificacionService.getNotifById(notificacion.getIdNotificacion());
                if (noti!=null) {
                    daNotificacionService.updateNotificacion(notificacion);
                }else{
                    daNotificacionService.addNotification(notificacion);
                }
                break;
        }
    }

    @RequestMapping(value = "getDatosSolicitudDetalleBySolicitud", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<DatoSolicitudDetalle> getDatosSolicitudDetalleBySolicitud(@RequestParam(value = "idSolicitud", required = true) String idSolicitud) throws Exception {
        logger.info("Se obtienen los detalles de resultados activos para la solicitud");
        List<DatoSolicitudDetalle> resultados = datosSolicitudService.getDatosSolicitudDetalleBySolicitud(idSolicitud);
        return resultados;
    }

    @RequestMapping(value = "createnoti", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
    protected void crearNotificacion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json = "";
        String resultado = "";
        String idNotificacion = "";
        String idPersona=null;
        String idSolicitante = "";
        boolean identificada = true;
        try {
            logger.debug("Guardando datos de la notificacion");
            DaNotificacion noti = new DaNotificacion();
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF8"));
            json = br.readLine();
            //Recuperando Json enviado desde el cliente
            JsonObject jsonpObject = new Gson().fromJson(json, JsonObject.class);
            if (jsonpObject.get("idPersona")!=null && !jsonpObject.get("idPersona").getAsString().isEmpty()) {
                idPersona = jsonpObject.get("idPersona").getAsString();
                identificada = jsonpObject.get("identificada").getAsBoolean();
                //ABRIL2019
                Persona persona = CallRestServices.getPersonasById(idPersona, (identificada?"1":"0"));
                if (persona !=null) {
                    PersonaTmp personaTmp = personaService.parsePersonaMinsaToDatosPersona(persona);
                    personaTmp.setUsuarioRegistro(seguridadService.obtenerNombreUsuario());
                    personaService.saveOrUpdateDatosPersona(personaTmp);
                    noti.setPersona(personaTmp);
                    noti.setCodTipoNotificacion("TPNOTI|PCNT");//ABRIL2019
                    noti.setIdMunicipioResidencia(personaTmp.getIdMunicipioResidencia());
                    noti.setNombreMunicipioResidencia(personaTmp.getNombreMunicipioResidencia());
                    noti.setDireccionResidencia(personaTmp.getDireccionResidencia());
                }else {
                    throw new Exception("No se pudo obtener los datos de la persona");
                }
            }
            if (jsonpObject.get("idSolicitante")!=null && !jsonpObject.get("idSolicitante").getAsString().isEmpty()){
                idSolicitante = jsonpObject.get("idSolicitante").getAsString();
                noti.setSolicitante(solicitanteService.getSolicitanteById(idSolicitante));
                noti.setCodTipoNotificacion("TPNOTI|OMX");//ABRIL2019
            }

            noti.setFechaRegistro(new Timestamp(new Date().getTime()));
            Parametro pUsuarioRegistro = parametrosService.getParametroByName("USU_REGISTRO_NOTI_CAESP");
            if(pUsuarioRegistro!=null) {
                long idUsuario = Long.valueOf(pUsuarioRegistro.getValor());
                noti.setUsuarioRegistro(usuarioService.getUsuarioById((int)idUsuario));
            }
            noti.setCompleta(false);
            //noti.setCodTipoNotificacion(catalogoService.getTipoNotificacion("TPNOTI|CAESP"));

            daNotificacionService.addNotification(noti);
            idNotificacion = noti.getIdNotificacion();
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            ex.printStackTrace();
            resultado =  messageSource.getMessage("msg.add.notification.error",null,null);
            resultado=resultado+". \n "+ex.getMessage();

        }finally {
            Map<String, String> map = new HashMap<String, String>();
            map.put("idNotificacion",idNotificacion);
            map.put("mensaje",resultado);
            map.put("idPersona",String.valueOf(idPersona));
            map.put("idSolicitante",idSolicitante);
            String jsonResponse = new Gson().toJson(map);
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        }
    }

    @RequestMapping(value = "tomaMxByIdNoti", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<DaTomaMx> getTestBySample(@RequestParam(value = "idNotificacion", required = true) String idNotificacion) throws Exception {
        logger.info("Realizando b�squeda de Toma de Mx.");

        return tomaMxService.getTomaMxByIdNoti(idNotificacion);

    }

    @RequestMapping(value = "getTipoMxByTipoNoti", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<TipoMx_TipoNotificacion> getTipoMxByTipoNoti(@RequestParam(value = "codigo", required = true) String codigo) throws Exception {
        logger.info("Realizando b�squeda de tipos de muestras seg�n el tipo de notificaci�n");

        return tomaMxService.getTipoMxByTipoNoti(codigo);

    }

    @RequestMapping(value = "updatenoti", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
    protected void modificarNotificacion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json = "";
        String resultado = "";
        String idNotificacion = "";
        Long idSilais=null;
        Long idUnidadSalud=null;
        String codTipoNoti="";
        String fechaInicioSintomas="";
        try {
            logger.debug("actualizando datos de la notificacion");
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF8"));
            json = br.readLine();
            //Recuperando Json enviado desde el cliente
            JsonObject jsonpObject = new Gson().fromJson(json, JsonObject.class);
            idNotificacion = jsonpObject.get("idNotificacion").getAsString();
            DaNotificacion notificacion = daNotificacionService.getNotifById(idNotificacion);
            if (notificacion!=null) {
                if (jsonpObject.get("codSilais") != null && !jsonpObject.get("codSilais").getAsString().isEmpty())
                    idSilais = jsonpObject.get("codSilais").getAsLong();

                if (jsonpObject.get("codUnidadSalud") != null && !jsonpObject.get("codUnidadSalud").getAsString().isEmpty())
                    idUnidadSalud = jsonpObject.get("codUnidadSalud").getAsLong();

                if (jsonpObject.get("codTipoNoti") != null && !jsonpObject.get("codTipoNoti").getAsString().isEmpty())
                    codTipoNoti = jsonpObject.get("codTipoNoti").getAsString();

                if (jsonpObject.get("fechaInicioSintomas") != null && !jsonpObject.get("fechaInicioSintomas").getAsString().isEmpty())
                    fechaInicioSintomas = jsonpObject.get("fechaInicioSintomas").getAsString();

                if (!codTipoNoti.isEmpty()) {
                    notificacion.setCodTipoNotificacion(codTipoNoti);//ABRIL2019
                }
                if (!fechaInicioSintomas.isEmpty()) {
                    notificacion.setFechaInicioSintomas(DateUtil.StringToDate(fechaInicioSintomas, "dd/MM/yyyy"));
                }

                //ABRIL2019 Consultar servicio o obtener todos los datos del Silais
                if (idSilais!=null){
                    notificacion.setIdSilaisAtencion(idSilais);

                    EntidadesAdtvas silais = CallRestServices.getEntidadAdtva(idSilais);
                    if (silais!=null){
                        notificacion.setCodSilaisAtencion(Long.valueOf(silais.getCodigo()));
                        notificacion.setNombreSilaisAtencion(silais.getNombre());
                    }
                }

                //ABRIL2019 Consultar servicio o obtener todos los datos de la unidad
                if (idUnidadSalud!=null){
                    notificacion.setIdUnidadAtencion(idUnidadSalud);

                    Unidades unidad = CallRestServices.getUnidadSalud(idUnidadSalud);
                    if (unidad!=null){
                        notificacion.setCodUnidadAtencion(Long.valueOf(unidad.getCodigo()));
                        notificacion.setNombreUnidadAtencion(unidad.getNombre());
                        if (unidad.getMunicipio()!=null){
                            notificacion.setIdMuniUnidadAtencion(unidad.getMunicipio().getId());
                            notificacion.setCodMuniUnidadAtencion(Long.valueOf(unidad.getMunicipio().getCodigo()));
                            notificacion.setNombreMuniUnidadAtencion(unidad.getMunicipio().getNombre());
                        }

                        if (unidad.getTipoestablecimiento()!=null){
                            notificacion.setTipoUnidad(unidad.getTipoestablecimiento().getId());
                        }
                    }
                }
                //en este caso sólo sería actualizar
                crearFicha(notificacion);
            }else{
                resultado =  messageSource.getMessage("msg.notification.notfound",null,null);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            ex.printStackTrace();
            resultado =  messageSource.getMessage("msg.update.notification.error",null,null);
            resultado=resultado+". \n "+ex.getMessage();

        }finally {
            Map<String, String> map = new HashMap<String, String>();
            map.put("idNotificacion",idNotificacion);
            map.put("mensaje",resultado);
            map.put("codSilais",String.valueOf(idSilais));
            map.put("codUnidadSalud",String.valueOf(idUnidadSalud));
            map.put("codTipoNoti", codTipoNoti);
            map.put("fechaInicioSintomas",fechaInicioSintomas);
            String jsonResponse = new Gson().toJson(map);
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        }
    }
    /*******************************************************************************/
    /***************************** OTRAS MUESTRAS **********************************/
    /*******************************************************************************/

    @RequestMapping(value = "searchOMx", method = RequestMethod.GET)
    public String initSearchOtrasForm() throws ParseException {
        logger.debug("Crear/Buscar Toma de Mx");
        return "tomaMx/searchOtherSamples";
    }

    @RequestMapping("notices/applicant/{idSolicitante}")
    public ModelAndView showNoticesApplicant (@PathVariable("idSolicitante") String idSolicitante) throws Exception {
        ModelAndView mav = new ModelAndView();
        List<DaNotificacion> results = daNotificacionService.getNoticesByApplicant(idSolicitante, "TPNOTI|OMX");
        mav.addObject("notificaciones", results);
        mav.addObject("idSolicitante",idSolicitante);
        mav.setViewName("tomaMx/resultsOtherSamples");
        return  mav;
    }

    /**
     * Handler for create tomaMx.
     *
     * @param idNotificacion the ID of the notification
     * @return a ModelMap with the model attributes for the respective view
     */
    @RequestMapping("createOMx/{idNotificacion}")
    public ModelAndView createOtherSample(@PathVariable("idNotificacion") String idNotificacion) throws Exception {
        ModelAndView mav = new ModelAndView();
        //registros anteriores de toma Mx
        DaTomaMx tomaMx = new DaTomaMx();
        DaNotificacion noti;
        //si es numero significa que es un id de persona, no de notificaci�n por tanto hay que crear una notificaci�n para esa persona
        noti = daNotificacionService.getNotifById(idNotificacion);
        if (noti != null) {
            //catTipoMx = tomaMxService.getTipoMxByTipoNoti("TPNOTI|CAESP");
            catTipoMx = tomaMxService.getTipoMxByTipoNoti("TPNOTI|OMX");
            List<Catalogo> tiposNotificacion = new ArrayList<Catalogo>();

            tiposNotificacion.add(CallRestServices.getCatalogo("TPNOTI|OMX"));
            mav.addObject("notificaciones",tiposNotificacion);
            mav.addObject("noti", noti);
            mav.addObject("tomaMx", tomaMx);
            mav.addObject("catTipoMx", catTipoMx);
            //mav.addAllObjects(mapModel);
            mav.setViewName("tomaMx/enterFormOtherSamples");
        } else {
            mav.setViewName("404");
        }

        return mav;
    }
}
