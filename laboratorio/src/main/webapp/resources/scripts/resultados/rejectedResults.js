var RejectedResults = function () {
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
            var table1 = $('#rejected_result').dataTable({
                "sDom": "<'dt-toolbar'<'col-xs-12 col-sm-6'f><'col-sm-6 col-xs-12 hidden-xs'l>r>" +
                    "t" +
                    "<'dt-toolbar-footer'<'col-sm-6 col-xs-12 hidden-xs'i><'col-xs-12 col-sm-6'p>>",
                "autoWidth": true, //"T<'clear'>"+
                "columns": [
                    null, null, null, null, null, null, null,
                    null/*{ //PARA MOSTRAR TABLA DETALLE RESULTADO
                        "className": 'details-control',
                        "orderable": false,
                        "data": null,
                        "defaultContent": ''
                    }*/
                ],
                "preDrawCallback": function () {
                    // Initialize the responsive datatables helper once.
                    if (!responsiveHelper_dt_basic) {
                        responsiveHelper_dt_basic = new ResponsiveDatatablesHelper($('#rejected_result'), breakpointDefinition);
                    }
                },
                "rowCallback": function (nRow) {
                    responsiveHelper_dt_basic.createExpandIcon(nRow);
                },
                "drawCallback": function (oSettings) {
                    responsiveHelper_dt_basic.respond();
                }
            });

            /*PARA MOSTRAR TABLA DETALLE RESULTADO*/
            function format(d, indice) {
                // `d` is the original data object for the row
                var texto = d[indice]; //indice donde esta el input hidden
                var resultado = $(texto).val();
                var json = JSON.parse(resultado);
                var len = Object.keys(json).length;
                console.log(json);
                var childTable = '<table style="padding-left:20px;border-collapse: separate;border-spacing:  10px 3px;">' +
                    '<tr><td style="font-weight: bold">' + $('#text_response').val() + '</td><td style="font-weight: bold">' + $('#text_value').val() + '</td><td style="font-weight: bold">' + $('#text_date').val() + '</td></tr>';
                for (var i = 1; i <= len; i++) {
                    childTable = childTable +
                        '<tr></tr><tr><td>' + json[i].respuesta + '</td><td>' + json[i].valor + '</td><td>' + json[i].fechaResultado + '</td></tr>';
                }
                childTable = childTable + '</table>';
                return childTable;
            }

            $('#rejected_result tbody').on('click', 'td.details-control', function () {
                var tr = $(this).closest('tr');
                var row = table1.api().row(tr);
                if (row.child.isShown()) {
                    // This row is already open - close it
                    row.child.hide();
                    tr.removeClass('shown');
                }
                else {
                    // Open this row
                    row.child(format(row.data(), 7)).show();
                    tr.addClass('shown');
                }
            });
            /// FIN MOSTRAR TABLA DETALLE RESULTADO
            $("#all-results").click(function () {
                getResultadosRechazados(true);
            });

            <!-- formulario de b�squeda de resultados finales -->
            $('#searchResults-form').validate({
                // Rules for form validation
                rules: {
                    fecFinTomaMx: {required: function () {
                        return $('#fecInicioTomaMx').val().length > 0;
                    }},
                    fecInicioTomaMx: {required: function () {
                        return $('#fecFinTomaMx').val().length > 0;
                    }}
                },
                // Do not change code below
                errorPlacement: function (error, element) {
                    error.insertAfter(element.parent());
                },
                submitHandler: function (form) {
                    table1.fnClearTable();
                    //add here some ajax code to submit your form or just call form.submit() if you want to submit the form without ajax
                    getResultadosRechazados(false);
                }
            });

            function getResultadosRechazados(showAll) {
                var filtros = {};
                if (showAll) {
                    filtros['nombreApellido'] = '';
                    filtros['fechaInicioTomaMx'] = '';
                    filtros['fechaFinTomaMx'] = '';
                    filtros['codSilais'] = '';
                    filtros['codUnidadSalud'] = '';
                    filtros['codTipoMx'] = '';
                    filtros['esLab'] = $('#txtEsLaboratorio').val();
                    filtros['codTipoSolicitud'] = '';
                    filtros['nombreSolicitud'] = '';
                    filtros['conResultado'] = 'Si';
                } else {
                    filtros['nombreApellido'] = $('#txtfiltroNombre').val();
                    filtros['fechaInicioRechazo'] = $('#fecInicioTomaMx').val();
                    filtros['fechaFinRechazo'] = $('#fecFinTomaMx').val();
                    filtros['codSilais'] = $('#codSilais').find('option:selected').val();
                    filtros['codUnidadSalud'] = $('#codUnidadSalud').find('option:selected').val();
                    filtros['codTipoMx'] = $('#codTipoMx').find('option:selected').val();
                    filtros['esLab'] = $('#txtEsLaboratorio').val();
                    filtros['codigoUnicoMx'] = $('#txtCodUnicoMx').val();
                    filtros['codTipoSolicitud'] = $('#tipo').find('option:selected').val();
                    filtros['nombreSolicitud'] = $('#nombreSoli').val();
                    filtros['conResultado'] = 'Si';

                }
                bloquearUI(parametros.blockMess);
                $.getJSON(parametros.sSearchUrl, {
                    strFilter: JSON.stringify(filtros),
                    ajax: 'true'
                }, function (dataToLoad) {
                    table1.fnClearTable();
                    var len = Object.keys(dataToLoad).length;
                    if (len > 0) {
                        for (var i = 0; i < len; i++) {
                            table1.fnAddData(
                                [dataToLoad[i].nombreSolicitud, dataToLoad[i].fechaSolicitud, dataToLoad[i].fechaRechazo, dataToLoad[i].codigoUnicoMx,
                                    dataToLoad[i].tipoMx, dataToLoad[i].tipoNotificacion, dataToLoad[i].persona, dataToLoad[i].resultados]);
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

        }
    };

}();