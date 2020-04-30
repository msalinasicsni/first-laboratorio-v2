
var resultReport = function () {

    var bloquearUI = function(mensaje){
        var loc = window.location;
        var pathName = loc.pathname.substring(0,loc.pathname.indexOf('/', 1)+1);
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
            }
        });
    };

    var desbloquearUI = function () {
        setTimeout($.unblockUI, 500);
    };

    return {
        //main function to initiate the module
        init: function (parametros) {
            var responsiveHelper_data_result = undefined;
            var breakpointDefinition = {
                tablet : 1024,
                phone : 480
            };
            var title = "";

            /* TABLETOOLS */
            var fecha = new Date();
            var fechaFormateada = (fecha.getDate()<10?'0'+fecha.getDate():fecha.getDate())
                +''+(fecha.getMonth()+1<10?'0'+(fecha.getMonth()+1):fecha.getMonth()+1)
                +''+fecha.getFullYear();

            var table1 = $('#tableRES').dataTable({

                // Tabletools options:
                "sDom": "<'dt-toolbar'<'col-xs-12 col-sm-6'f><'col-sm-6 col-xs-6 hidden-xs'>r>"+
                    "t"+
                    "<'dt-toolbar-footer'<'col-sm-6 col-xs-12 hidden-xs'i><'col-sm-6 col-xs-12'p>>",
                "aoColumns" : [ {sClass: "aw-left" },{sClass: "aw-right"},{sClass: "aw-right"},{sClass: "aw-right"},{sClass: "aw-right"},{sClass: "aw-right"},{sClass: "aw-right"},{sClass: "aw-right"}],
                "createdRow": function ( row, data, index ) {
                    if ( data[2] > 0 ) {
                        $('td', row).eq(2).addClass('highlight');
                    }
                    if ( data[7]  > 0 ) {
                        $('td', row).eq(7).addClass('highlight');
                    }
                },
                "autoWidth" : true,
                "pageLength": 20,
                "preDrawCallback" : function() {
                    // Initialize the responsive datatables helper once.
                    if (!responsiveHelper_data_result) {
                        responsiveHelper_data_result = new ResponsiveDatatablesHelper($('#tableRES'), breakpointDefinition);
                    }
                },
                "rowCallback" : function(nRow) {
                    responsiveHelper_data_result.createExpandIcon(nRow);
                },
                "drawCallback" : function(oSettings) {
                    responsiveHelper_data_result.respond();
                }
            });

            $(".export").on('click', function (event) {
                // CSV
                var fileHeader = $("#fileTitle").val()+'"\r\n"'+$("#from").val()+$("#initDate").val()+'  '+$("#to").val()+$("#endDate").val();
                var args = [$('#tableRES'), $("#fileName").val()+'.csv', fileHeader];
                exportTableToCSV.apply(this, args);

                // If CSV, don't do event.preventDefault() or return false
                // We actually need this to be a typical hyperlink
            });

            /* END TABLETOOLS */

            $('#result_form').validate({
                // Rules for form validation
                rules : {

                    codArea : {
                        required : true
                    },
                    codSilaisAtencion: {
                        required : true
                    },
                    codMunicipio: {
                        required : true
                    },
                    codUnidadAtencion: {
                        required : true
                    },
                    idDx:{
                        required:true
                    },
                    initDate:{
                        required:true
                    },
                    endDate:{
                        required:true
                    },
                    codigoLab: {
                        required:true
                    }
                },
                // Do not change code below
                errorPlacement : function(error, element) {
                    error.insertAfter(element.parent());
                },
                submitHandler: function (form) {
                    table1.fnClearTable();

                    //add here some ajax code to submit your form or just call form.submit() if you want to submit the form without ajax
                    getData();
                }
            });


            $('#codArea').change(
                function() {
                    if ($('#codArea option:selected').val() == "AREAREP|PAIS"){
                        $('#silais').hide();
                        $('#departamento').hide();
                        $('#municipio').hide();
                        $('#unidad').hide();
                        $('#dSubUnits').hide();
                        $('#dNivelPais').hide();
                        $('#zona').hide();
                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|SILAIS"){
                        $('#silais').show();
                        $('#departamento').hide();
                        $('#municipio').hide();
                        $('#unidad').hide();
                        $('#dSubUnits').hide();
                        $('#dNivelPais').hide();
                        $('#zona').hide();
                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|MUNI"){
                        $('#silais').show();
                        $('#departamento').hide();
                        $('#municipio').show();
                        $('#unidad').hide();
                        $('#dSubUnits').hide();
                        $('#dNivelPais').hide();
                        $('#zona').hide();
                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|UNI"){
                        $('#silais').show();
                        $('#departamento').hide();
                        $('#municipio').show();
                        $('#unidad').show();
                        //$('#dSubUnits').show();
                        $('#dNivelPais').hide();
                        $('#zona').hide();
                    }
                });

            $("#exportExcel").click(function(){
                var $validarForm = $("#result_form").valid();
                if (!$validarForm) {
                    $validator.focusInvalid();
                    return false;
                } else {
                    bloquearUI(parametros.blockMess);
                    var filtro = {};
                    //filtro['subunidades'] = $('#ckUS').is(':checked');
                    filtro['fechaInicio'] = $('#initDate').val();
                    filtro['fechaFin'] = $('#endDate').val();
                    filtro['codSilais'] = $('#codSilais').find('option:selected').val();
                    filtro['codUnidadSalud'] = $('#codUnidadAtencion').find('option:selected').val();
                    //filtro['codDepartamento'] = $('#codDepartamento').find('option:selected').val();
                    filtro['codMunicipio'] = $('#codMunicipio').find('option:selected').val();
                    filtro['codArea'] = $('#codArea').find('option:selected').val();
                    //filtro['tipoNotificacion'] = $('#codTipoNoti').find('option:selected').val();
                    filtro['porSilais'] = "true"; //$('input[name="rbNivelPais"]:checked', '#result_form').val();
                    //filtro['codZona'] = $('#codZona').find('option:selected').val();
                    filtro['idDx'] = $('#idDx').find('option:selected').val();
                    filtro['codLabo'] = $('#codigoLab').find('option:selected').val();
                    filtro['consolidarPor'] = $('input[name="rbFechaBusqueda"]:checked', '#result_form').val();
                    $(this).attr("href",parametros.sExcelResultDx+"?filtro="+JSON.stringify(filtro));
                    desbloquearUI();
                }
            });

            function getData() {
                var filtro = {};
                //filtro['subunidades'] = $('#ckUS').is(':checked');
                filtro['fechaInicio'] = $('#initDate').val();
                filtro['fechaFin'] = $('#endDate').val();
                filtro['codSilais'] = $('#codSilaisAtencion').find('option:selected').val();
                var valUni =$('#codUnidadAtencion').find('option:selected').val();
                var elemValUni = valUni.split(",");
                filtro['codUnidadSalud'] = elemValUni[1];//código
                //filtro['codDepartamento'] = $('#codDepartamento').find('option:selected').val();
                var valMuni =$('#codMunicipio').find('option:selected').val();
                var elemValMuni = valMuni.split(",");
                filtro['codMunicipio'] = elemValMuni[0];
                filtro['codArea'] = $('#codArea').find('option:selected').val();
                //filtro['tipoNotificacion'] = $('#codTipoNoti').find('option:selected').val();
                filtro['porSilais'] = "true"; //$('input[name="rbNivelPais"]:checked', '#result_form').val();
                //filtro['codZona'] = $('#codZona').find('option:selected').val();
                filtro['idDx'] = $('#idDx').find('option:selected').val();
                filtro['codLabo'] = $('#codigoLab').find('option:selected').val();
                filtro['consolidarPor'] = $('input[name="rbFechaBusqueda"]:checked', '#result_form').val();
                bloquearUI(parametros.blockMess);
                $.getJSON(parametros.sActionUrl, {
                    filtro: JSON.stringify(filtro),
                    ajax: 'true'
                }, function(data) {

                    var encontrado = false;
                    if ($('#codArea option:selected').val() == "AREAREP|PAIS") {
                        console.log(filtro['porSilais']);
                        if (filtro['porSilais'] == 'true') {
                            $('#firstTh').html($('#silaisT').val());
                        }else {
                            $('#firstTh').html($('#departaT').val());
                        }

                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|SILAIS"){
                        $('#firstTh').html( $('#municT').val() );

                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|DEPTO"){
                        title = title + '</br>' + $('#dep').val() + " " +$('#codDepartamento option:selected').text();
                        $('#firstTh').html( $('#municT').val() );

                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|MUNI"){
                        $('#firstTh').html( $('#usT').val() );

                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|UNI"){
                        $('#firstTh').html( $('#usT').val() );

                    }
                    else if ($('#codArea option:selected').val() == "AREAREP|ZE") {
                        $('#firstTh').html( $('#usT').val() );
                    }

                    for (var row in data) {
                        table1.fnAddData([data[row][0], data[row][2], data[row][3], data[row][4], data[row][5],data[row][7], data[row][8], data[row][6]]);
                        encontrado = true;

                    }


                    if(!encontrado){
                        showMessage(parametros.msgTitle, parametros.msgNoData, "#AF801C", "fa fa-warning", 3000);
                        //title='';
                        //$('#lineChart-title').html("<h5>"+title+"</h5>");

                    }
                    desbloquearUI();
                })
                    .fail(function(XMLHttpRequest, textStatus, errorThrown) {
                        alert(" status: " + textStatus + " er:" + errorThrown);
                        setTimeout($.unblockUI, 5);
                    });
            }


            function showMessage(title,content,color,icon,timeout){
                $.smallBox({
                    title: title,
                    content: content,
                    color: color,
                    iconSmall: icon,
                    timeout: timeout
                });
            }

            $("#sendMail").click(function () {
                sendMail();
            });

            function sendMail() {
                // CSV
                var fileHeader = $("#fileTitle").val()+'"\r\n"'+$("#from").val()+$("#initDate").val()+'  '+$("#to").val()+$("#endDate").val();
                var csv = exportTableToCSV($('#tableRES'), $("#fileName").val()+'.csv', fileHeader);

                var filtro = {};
                //filtro['subunidades'] = $('#ckUS').is(':checked');
                filtro['fechaInicio'] = $('#initDate').val();
                filtro['fechaFin'] = $('#endDate').val();
                filtro['codSilais'] = $('#codSilaisAtencion').find('option:selected').val();
                var valUni =$('#codUnidadAtencion').find('option:selected').val();
                var elemValUni = valUni.split(",");
                filtro['codUnidadSalud'] = elemValUni[1];
                //filtro['codDepartamento'] = $('#codDepartamento').find('option:selected').val();
                var valMuni =$('#codMunicipio').find('option:selected').val();
                var elemValMuni = valMuni.split(",");
                filtro['codMunicipio'] = elemValMuni[0];
                filtro['codArea'] = $('#codArea').find('option:selected').val();
                //filtro['tipoNotificacion'] = $('#codTipoNoti').find('option:selected').val();
                filtro['porSilais'] = $('input[name="rbNivelPais"]:checked', '#result_form').val();
                //filtro['codZona'] = $('#codZona').find('option:selected').val();
                filtro['idDx'] = $('#idDx').find('option:selected').val();
                filtro['codLabo'] = $('#codigoLab').find('option:selected').val();
                filtro['consolidarPor'] = $('input[name="rbFechaBusqueda"]:checked', '#result_form').val();
                bloquearUI(parametros.blockMess);
                $.getJSON(parametros.sMailUrl, {
                    filtro: JSON.stringify(filtro),
                    csv : encodeURIComponent(csv),
                    ajax: 'true'
                }, function(data) {
                    if (data==='OK'){
                        showMessage(parametros.msgTitle, $("#msg_email_ok").val(), "#739E73", "fa fa-success", 3000);
                    }else {
                        showMessage(parametros.msgTitle, data, "#AF801C", "fa fa-warning", 6000);
                    }
                    desbloquearUI();
                })
                    .fail(function(XMLHttpRequest, textStatus, errorThrown) {
                        alert(" status: " + textStatus + " er:" + errorThrown);
                        setTimeout($.unblockUI, 5);
                    });
            }
        }
    };

}();
