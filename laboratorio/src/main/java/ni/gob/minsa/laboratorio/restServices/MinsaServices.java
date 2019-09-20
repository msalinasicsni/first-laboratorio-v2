package ni.gob.minsa.laboratorio.restServices;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Define los endpoints del API del MINSA
 * Created by Miguel Salinas on 22/04/2019.
 * V1.0
 */
public interface MinsaServices {

    public static final String API_URL = "http://desa.minsa.gob.ni:8084"; //"http://prueba.minsa.gob.ni:8084/";
    public static final String API_PERSONA_URL = "http://desa.minsa.gob.ni:8084";
    public static final String SEVICIO_PERSONAS_IDENTIFICACION = API_URL+"/wspersonas/v2/hospitalario/personas/identificacion/";
    public static final String SEVICIO_PERSONAS_NONBRES = API_URL+"/wspersonas/v2/hospitalario/personas/nombre/";

    @GET("wscatalogos/ws/catalogos/catsuperior/{codigosup}")
    Call<ResponseBody> catalogos(
            @Path("codigosup") String codigosup
    );

    @GET("wscatalogosminsa/v2/catalogos/gestion-divpolitica/departamento")
    Call<ResponseBody> departamentos();

    @GET("wscatalogosminsa/v2/catalogos/gestion-divpolitica/municipio/departamento/{departamentoid}")
    Call<ResponseBody> municipiosDepartamento(
            @Path("departamentoid") long departamentoid
    );

    @GET("wscatalogosminsa/v2/catalogos/gestion-divpolitica/municipio/{municipioid}")
    Call<ResponseBody> municipio(
            @Path("municipioid") long municipioid
    );

    @GET("wscatalogosminsa/v2/catalogos/red-servicios/entidadesadmin/{entidadid}")
    Call<ResponseBody> entidadesAdtvas(
            @Path("entidadid") long entidadid
    );

    //@GET("wscatalogosminsa/v2/catalogos/red-servicios/unidades/redservicio/{unidadid}")
    @GET("wscatalogosminsa/v2/catalogos/red-servicios/unidadessalud/{unidadid}")
    Call<ResponseBody> unidad(
            @Path("unidadid") long unidadid
    );

    @GET("wscatalogosminsa/v2/catalogos/red-servicios/redservicios/unidades/{entidadid}")
    Call<ResponseBody> unidadesEntidad(
            @Path("entidadid") long entidadAdtvaId
    );

    @GET("wscatalogosminsa/v2/catalogos/red-servicios/unidades/tipo/{entidadid}/{municipioid}/{tipounidadid}")
    Call<ResponseBody> unidadesByEntidadMunicipioTipo(
            @Path("entidadid") long entidadAdtvaId,
            @Path("municipioid") long municipioId,
            @Path("tipounidadid") long tipoUnidadId
    );

    @GET("wspersonas/v2/hospitalario/personas/nombre/")
    Call<ResponseBody> personaNombres(
            @Query("nombrecompleto") String nombrecompleto,
            @Query("primerapellido") String primerapellido,
            @Query("primernombre") String primernombre,
            @Query("segundoapellido") String segundoapellido,
            @Query("segundonombre") String segundonombre,
            @Query("identificada") String identificada,
            @Query("pagina") Integer pagina,
            @Query("registros") Integer registros

    );

    @GET("wspersonas/v2/hospitalario/personas/identificacion/{numeroidentificacion}")
    Call<ResponseBody> personaIdentificacion(
            @Path("numeroidentificacion") String numeroidentificacion
    );

    @GET("wspersonas/v2/hospitalario/personas/{id}/{identificada}")
    Call<ResponseBody> personaId(
            @Path("id") String id,
            @Path("identificada") String identificada
    );
}
