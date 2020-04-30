package ni.gob.minsa.laboratorio.restServices;

import com.google.common.base.Predicate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import ni.gob.minsa.laboratorio.restServices.entidades.*;
import ni.gob.minsa.laboratorio.utilities.HL7.TestOrder;
import ni.gob.minsa.laboratorio.utilities.reportes.FilterLists;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Rest Adapter para ejecutar los endpoints de las API's
 * Created by Miguel Salinas on 22/05/2019.
 * V1.0
 */
public class CallRestServices {

    public static List<Catalogo> getCatalogos(String catalogoSup) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = servicios.catalogos(catalogoSup);
        Type founderListType = new TypeToken<ArrayList<Catalogo>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            return new Gson().fromJson(data, founderListType);
        }else return new ArrayList<Catalogo>();
    }

    public static Catalogo getCatalogo(String catalogo) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = servicios.catalogos(catalogo);
        Type founderListType = new TypeToken<ArrayList<Catalogo>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            return new Gson().fromJson(data, founderListType);
        }else return null;
    }

    public static List<EntidadesAdtvas> getEntidadesAdtvas() throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.entidadesAdtvas(0);
        Type founderListType = founderListType = new TypeToken<ArrayList<EntidadesAdtvas>>(){}.getType();
        List<EntidadesAdtvas> entidades = new ArrayList<EntidadesAdtvas>();
        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            entidades = new Gson().fromJson(data, founderListType);
            Collections.sort(entidades);
        }
        return entidades;
    }

    public static EntidadesAdtvas getEntidadAdtva(long entidadid) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.entidadesAdtvas(entidadid);
        Type founderListType = founderListType = new TypeToken<ArrayList<EntidadesAdtvas>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            List<EntidadesAdtvas> entidadesAdtvas = new Gson().fromJson(data, founderListType);
            if (entidadesAdtvas != null)
                return entidadesAdtvas.get(0);
            else return null;
        }else return null;
    }

    public static List<Departamento> getDepartamentos() throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.departamentos();
        Type founderListType = founderListType = new TypeToken<ArrayList<Departamento>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            return new Gson().fromJson(data, founderListType);
        }else return new ArrayList<Departamento>();
    }

    public static Municipio getMunicipio(long municipioid) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.municipio(municipioid);
        Type founderListType = founderListType = new TypeToken<ArrayList<Municipio>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            List<Municipio> municipios = new Gson().fromJson(data, founderListType);
            if (municipios != null)
                return municipios.get(0);
            else return null;
        }else return null;
    }

    public static List<Municipio> getMunicipiosDepartamento(long departamentoid) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.municipiosDepartamento(departamentoid);
        Type founderListType = founderListType = new TypeToken<ArrayList<Municipio>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            List<Municipio> municipios = new Gson().fromJson(data, founderListType);
            Collections.sort(municipios);
            return municipios;
        }else return new ArrayList<Municipio>();
    }

    public static List<Municipio> getMunicipiosEntidad(long entidadAdtvaId) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = servicios.unidadesEntidad(entidadAdtvaId);
        Type founderListType = new TypeToken<ArrayList<Unidades>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            List<Unidades> unidades = new Gson().fromJson(data, founderListType);
            List<Municipio> municipios = new ArrayList<Municipio>();
            if (unidades != null) {
                for (Unidades unidad : unidades) {
                    if (!municipios.contains(unidad.getMunicipio())) municipios.add(unidad.getMunicipio());
                }
            }
            Collections.sort(municipios);
            return municipios;
        }else return new ArrayList<Municipio>();
    }

    public static List<Unidades> getUnidadesByEntidadMunicipioTipo(long entidadAdtvaId, long municipioId, String[] tipoUnidadId) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.unidadesByEntidadMunicipioTipo(entidadAdtvaId, municipioId, 0);
        Type founderListType = founderListType = new TypeToken<ArrayList<Unidades>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            List<Unidades> unidadesResponse = new ArrayList<Unidades>();
            List<Unidades> unidades = new Gson().fromJson(data, founderListType);
            if (unidades != null) {
                for (final String tipo : tipoUnidadId) {
                    //Del subconjunto por dx y examen, se filtran todos los registros por mes y SILAIS
                    Predicate<Unidades> unidadesTipo = new Predicate<Unidades>() {
                        @Override
                        public boolean apply(Unidades unid) {
                            return unid.getTipoestablecimiento().getCodigo().equalsIgnoreCase(tipo);
                        }
                    };
                    //se aplica filtro por mes y SILAIS
                    Collection<Unidades> unidadesPorTipo = FilterLists.filter(unidades, unidadesTipo);
                    unidadesResponse.addAll(unidadesPorTipo);
                }
            }
            Collections.sort(unidadesResponse);
            return unidadesResponse;
        }else return new ArrayList<Unidades>();
    }

    public static List<Unidades> getUnidadesEntidad(long entidadAdtvaId) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = servicios.unidadesEntidad(entidadAdtvaId);
        Type founderListType = new TypeToken<ArrayList<Unidades>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            return new Gson().fromJson(data, founderListType);
        }else return new ArrayList<Unidades>();
    }

    public static Unidades getUnidadSalud(long unidadid) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.unidad(unidadid);
        Type founderListType = founderListType = new TypeToken<ArrayList<Unidades>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("data") != null) {
                data = jsonpObject.get("data").toString();
            }
            List<Unidades> unidadeses = new Gson().fromJson(data, founderListType);
            if (unidadeses != null)
                return unidadeses.get(0);
            else return null;
        }else return null;
    }

    /**
     * Método para buscar personas por nombre
     * @param identificada usar 0, para personas identificadas y 1 para No Identificadas y Desconocidas. NOsotros siempre lo enviamos NULL porque no sabemos si es identificada o no
     * @param nombrecompleto Se usa para indicar si el nombre ingresado en realidad es el nombre completo, 0 Nombre Incompleto y 1 Nombre Completo, preferiblemente su uso cuando la persona solo posee un nombre y un apellido.
     * @param primerapellido Primer Apellido de la persona
     * @param primernombre Primer Nombre de la persona
     * @param segundoapellido Segundo Apellido de la persona
     * @param segundonombre Segundo Nombre de la persona
     * @param pagina control de paginación, indica en que página va la búsqueda. Si se envia null inicia en la primera página
     * @param registros indica la cantidad de registros a recuperar por página
     * @return List<Persona>
     * @throws Exception
     */
    public static List<Persona> getPersonasByNombre(String identificada, String nombrecompleto, String primerapellido, String primernombre, String segundoapellido, String segundonombre, Integer pagina, Integer registros) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_PERSONA_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.personaNombres(identificada, nombrecompleto, primerapellido, primernombre, segundoapellido, segundonombre, pagina, registros);
        Type founderListType = founderListType = new TypeToken<ArrayList<Persona>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("datos") != null) {
                data = jsonpObject.get("datos").toString();
            }
            return new Gson().fromJson(data, founderListType);
        }else return null;
    }

    /**
     * Método para buscar persona por su número de identificación o Código Expediente Unico
     * @param numeroidentificacion Número de identificación
     * @return List<Persona>
     * @throws Exception
     */
    public static List<Persona> getPersonasByIdentificacion(String numeroidentificacion) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_PERSONA_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.personaIdentificacion(numeroidentificacion);
        Type founderListType = founderListType = new TypeToken<ArrayList<Persona>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("datos") != null) {
                data = jsonpObject.get("datos").toString();
            }
            return new Gson().fromJson(data, founderListType);
        }else return null;
    }

    /**
     * Busca Persona, haciendo uso del tipo Tipo de Registro (Identificada o Sin Identificar), y el ID
     * @param id Id de la persona
     * @param identificada 1 Identificada, 0 Sin Identificar
     * @return List<Persona>
     * @throws Exception
     */
    public static Persona getPersonasById(String id, String identificada) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MinsaServices.API_PERSONA_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MinsaServices servicios = retrofit.create(MinsaServices.class);

        Call<ResponseBody> call = call = servicios.personaId(id, identificada);
        Type founderListType = founderListType = new TypeToken<ArrayList<Persona>>(){}.getType();

        Response<ResponseBody> response = call.execute();
        if (response!=null && response.body()!=null) {
            String strResponse = response.body().string();
            JsonObject jsonpObject = new Gson().fromJson(strResponse, JsonObject.class);
            String data = null;
            if (jsonpObject.get("datos") != null) {
                data = jsonpObject.get("datos").toString();
            }
            List<Persona> personas = new Gson().fromJson(data, founderListType);
            if (personas != null)
                return personas.get(0);
            else return null;
        }else return null;
    }

    //HL7
    public static void crearSolicitud(TestOrder testOrder) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ResultadoHL7Services.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ResultadoHL7Services servicios = retrofit.create(ResultadoHL7Services.class);

        Call<ResponseBody> call = servicios.crearSolicitud(testOrder);
        Response<ResponseBody> response = call.execute();
        System.out.println("Respuesta es exitosa desde "+ResultadoHL7Services.API_URL);
        System.out.println(response.isSuccessful());
        if (response.body()!=null) System.out.println(response.body().string());
    }
}
