/**
 * Created by FIRSTICT on 7/26/2017.
 */
var EditarMxLab = function () {
    var bloquearUI = function (mensaje) {
        var loc = window.location;
        var pathName = loc.pathname.substring(0, loc.pathname.indexOf('/', 1) + 1);
        var mess = '<img src=' + pathName + 'resources/img/ajax-loading.gif>' + mensaje;
        $.blockUI({ message: mess,
            css: {
                border: 'none',
                padding: '15px',
                backgroundColor: '#000',
                '-webkit-border-radius': '10px',
                '-moz-border-radius': '10px',
                opacity: .5,
                color: '#fff'
            },
            baseZ: 1051 // para que se muestre bien en los modales
        });
    };

    var desbloquearUI = function () {
        setTimeout($.unblockUI, 500);
    };
    return {
        //main function to initiate the module
        init: function (parametros) {

            var responsiveHelper_dt_basic = undefined;
            var breakpointDefinition = {
                tablet: 1024,
                phone: 480
            };
            var text_selected_all = $("#text_selected_all").val();
            var text_selected_none = $("#text_selected_none").val();
            var table1 = $('#orders_result').dataTable({
                "sDom": "<'dt-toolbar'<'col-xs-12 col-sm-6'f><'col-sm-6 col-xs-12 hidden-xs'l>r>" +
                    "T" +
                    "t" +
                    "<'dt-toolbar-footer'<'col-sm-6 col-xs-12 hidden-xs'i><'col-xs-12 col-sm-6'p>>",
                "autoWidth": true,
                "columns": [
                    null, null, null, null, null, null, null, null, null, null, null, null,
                    {
                        "className": 'override',
                        "orderable": false
                    }
                ],
                "preDrawCallback": function () {
                    // Initialize the responsive datatables helper once.
                    if (!responsiveHelper_dt_basic) {
                        responsiveHelper_dt_basic = new ResponsiveDatatablesHelper($('#orders_result'), breakpointDefinition);
                    }
                },
                "rowCallback": function (nRow) {
                    responsiveHelper_dt_basic.createExpandIcon(nRow);
                },
                "drawCallback": function (oSettings) {
                    responsiveHelper_dt_basic.respond();
                },
                "oTableTools": {
                    "sSwfPath": parametros.sTableToolsPath,
                    "sRowSelect": "multi",
                    "aButtons": [
                        {"sExtends": "select_all", "sButtonText": text_selected_all},
                        {"sExtends": "select_none", "sButtonText": text_selected_none}
                    ]
                },
                fnDrawCallback: function () {
                    $('.override')
                        .off("click", overrideHandler)
                        .on("click", overrideHandler);
                }
            });

            <!-- formulario de b�squeda de ordenes -->
            $('#searchOrders-form').validate({
                // Rules for form validation
                rules: {
                    fecFinRecepcion: {required: function () {
                        return $('#fecInicioRecepcion').val().length > 0;
                    }},
                    fecInicioRecepcion: {required: function () {
                        return $('#fecFinRecepcion').val().length > 0;
                    }}
                },
                // Do not change code below
                errorPlacement: function (error, element) {
                    error.insertAfter(element.parent());
                },
                submitHandler: function (form) {
                    table1.fnClearTable();
                    //add here some ajax code to submit your form or just call form.submit() if you want to submit the form without ajax
                    getMxs(false)
                }
            });

            <!-- formulario para anular examen -->
            $('#override-mx-form').validate({
                // Rules for form validation
                rules: {
                    causaAnulacion: {required: true}
                },
                // Do not change code below
                errorPlacement: function (error, element) {
                    error.insertAfter(element.parent());
                },
                submitHandler: function (form) {
                    anularMuestra($("#codigoMx").val());
                }
            });

            function overrideHandler() {
                var id = $(this.innerHTML).data('id');
                if (id != null) {
                    $("#codigoMx").val(id);
                    $("#lblCodigoMx").text(id);
                    showModalOverride();
                }
            }

            function getMxs(showAll) {
                var mxFiltros = {};
                if (showAll) {
                    mxFiltros['nombreApellido'] = '';
                    mxFiltros['fechaInicioRecep'] = '';
                    mxFiltros['fechaInicioRecep'] = '';
                    mxFiltros['codSilais'] = '';
                    mxFiltros['codUnidadSalud'] = '';
                    mxFiltros['codTipoMx'] = '';
                    mxFiltros['codTipoSolicitud'] = '';
                    mxFiltros['nombreSolicitud'] = '';
                } else {
                    mxFiltros['nombreApellido'] = $('#txtfiltroNombre').val();
                    mxFiltros['fechaInicioRecep'] = $('#fecInicioRecepcion').val();
                    mxFiltros['fechaFinRecepcion'] = $('#fecFinRecepcion').val();
                    mxFiltros['codSilais'] = $('#codSilais').find('option:selected').val();
                    mxFiltros['codUnidadSalud'] = $('#codUnidadSalud').find('option:selected').val();
                    mxFiltros['codTipoMx'] = $('#codTipoMx').find('option:selected').val();
                    mxFiltros['codigoUnicoMx'] = $('#txtCodUnicoMx').val();
                    mxFiltros['codTipoSolicitud'] = $('#tipo').find('option:selected').val();
                    mxFiltros['nombreSolicitud'] = $('#nombreSoli').val();
                }
                bloquearUI(parametros.blockMess);
                $.getJSON(parametros.sOrdersUrl, {
                    strFilter: JSON.stringify(mxFiltros),
                    ajax: 'true'
                }, function (dataToLoad) {
                    table1.fnClearTable();
                    var len = Object.keys(dataToLoad).length;
                    if (len > 0) {
                        for (var i = 0; i < len; i++) {
                            var actionUrl = parametros.sActionUrl;
                            var idLoad = dataToLoad[i].idTomaMx;
                            var btnOverride = '<button title="Anular" type="button" class="btn btn-danger btn-xs" data-id="' + dataToLoad[i].codigoUnicoMx + '" > <i class="fa fa-times"></i></button>';
                            if ($('#nivelCentral').val()=='false'){
                                btnOverride = '<button title="Anular" type="button" disabled class="btn btn-danger btn-xs"> <i class="fa fa-times"></i></button>';
                            }
                            actionUrl = actionUrl + idLoad;
                            table1.fnAddData(
                                [dataToLoad[i].codigoUnicoMx + " <input type='hidden' value='" + idLoad + "'/>", dataToLoad[i].fechaTomaMx, dataToLoad[i].fechaInicioSintomas, dataToLoad[i].dias, dataToLoad[i].codSilais, dataToLoad[i].persona, dataToLoad[i].traslado, dataToLoad[i].origen,dataToLoad[i].embarazada, dataToLoad[i].urgente, dataToLoad[i].solicitudes,
                                        '<a target="_blank" title="Ver Detalle" href=' + actionUrl + ' class="btn btn-primary btn-xs"><i class="fa fa-mail-forward"></i></a>',
                                        btnOverride]);
                        }
                    } else {
                        $.smallBox({
                            title: $("#msg_no_results_found").val(),
                            content: $("#smallBox_content").val(),
                            color: "#C79121",
                            iconSmall: "fa fa-warning",
                            timeout: 4000
                        });
                    }
                    desbloquearUI();
                })
                    .fail(function (jqXHR) {
                        setTimeout($.unblockUI, 10);
                        validateLogin(jqXHR);
                    });
            }

            function showModalOverride() {
                $("#causaAnulacion").val('');
                $("#modalOverride").modal({
                    show: true
                });
            }

            function hideModalOverride() {
                $('#modalOverride').modal('hide');
            }

            function anularMuestra(codigoMx) {
                var anulacionObj = {};
                anulacionObj['codigoMx'] = codigoMx;
                anulacionObj['causaAnulacion'] = $("#causaAnulacion").val();
                anulacionObj['mensaje'] = '';
                bloquearUI(parametros.blockMess);
                $.ajax(
                    {
                        url: parametros.sOverrideUrl,
                        type: 'POST',
                        dataType: 'json',
                        data: JSON.stringify(anulacionObj),
                        contentType: 'application/json',
                        mimeType: 'application/json',
                        success: function (data) {
                            if (data.mensaje.length > 0) {
                                $.smallBox({
                                    title: data.mensaje,
                                    content: $("#smallBox_content").val(),
                                    color: "#C46A69",
                                    iconSmall: "fa fa-warning",
                                    timeout: 4000
                                });
                            } else {
                                table1.fnClearTable();
                                hideModalOverride();
                                var msg = $("#msg_override_success").val();
                                $.smallBox({
                                    title: msg,
                                    content: $("#smallBox_content").val(),
                                    color: "#739E73",
                                    iconSmall: "fa fa-success",
                                    timeout: 4000
                                });
                            }
                            desbloquearUI();
                        },
                        error: function (jqXHR) {
                            desbloquearUI();
                            validateLogin(jqXHR);
                        }
                    });
            }

        }
    };

}();
