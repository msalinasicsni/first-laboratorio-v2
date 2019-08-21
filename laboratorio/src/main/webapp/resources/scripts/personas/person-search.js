var SearchPerson = function () {

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

    return {

        //main function to initiate the module
        init: function (parametros) {
            var page=0;
            var rowsPage=50;
            $("#prev").prop('disabled',true);
            $("#next").prop('disabled',true);
            var responsiveHelper_dt_basic = undefined;
            var breakpointDefinition = {
                tablet: 1024,
                phone: 480
            };
            var table1 = $('#persons_result').dataTable({
                "sDom": "<'dt-toolbar'<'col-xs-12 col-sm-6'f><'col-sm-6 col-xs-12 hidden-xs'l>r>" +
                    "t" +
                    "<'dt-toolbar-footer'<'col-sm-6 col-xs-12 hidden-xs'i><'col-xs-12 col-sm-6'p>>",
                "autoWidth": true,
                "preDrawCallback": function () {
                    // Initialize the responsive datatables helper once.
                    if (!responsiveHelper_dt_basic) {
                        responsiveHelper_dt_basic = new ResponsiveDatatablesHelper($('#persons_result'), breakpointDefinition);
                    }
                },
                "paging": false,
                "lengthChange": false,
                "info": false,
                "rowCallback": function (nRow) {
                    responsiveHelper_dt_basic.createExpandIcon(nRow);
                },
                "drawCallback": function (oSettings) {
                    responsiveHelper_dt_basic.respond();
                }
            });

            function desbloquearUI() {
                setTimeout($.unblockUI, 500);
            }

            $("input[name$='rbTipoBusqueda']").click(function () {
                var valor = $(this).val();
                if (valor == 'NOMBRE') {
                    $('#filtroNombre').show();
                    $('#filtroIdentificacion').hide();
                }else {
                    $('#filtroIdentificacion').show();
                    $('#filtroNombre').hide();
                }
            });

            $("#prev").on('click', function (event) {
                if (page>1) {
                    page = page - 1;
                    getPersons2(page);
                }
            });
            $("#next").on('click', function (event) {
                page = page+1;
                getPersons2(page);

            });

            $('#search-form').validate({
                // Rules for form validation
                rules: {
                    primerNombre: {
                        required: true,
                        minlength: 3
                    },
                    primerApellido: {
                        required: true,
                        minlength: 3
                    },
                    numIdentificacion: {
                        required: true,
                        minlength: 3
                    }
                },
                // Do not change code below
                errorPlacement: function (error, element) {
                    error.insertAfter(element.parent());
                },
                submitHandler: function (form) {
                    //add here some ajax code to submit your form or just call form.submit() if you want to submit the form without ajax
                    page=1;
                    //getPersons(page*rowsPage);
                    getPersons2(page);
                }
            });


            function getPersons2(pagina) {
                table1.fnClearTable();
                bloquearUI(parametros.blockMess);
                var tipoBusqueda = $('input[name="rbTipoBusqueda"]:checked', '#search-form').val();
                if (tipoBusqueda == 'NOMBRE') {
                    getPersonsByName(pagina);
                } else {
                    getPersonsByIdentification();
                }
            }

            function getPersonsByIdentification() {
                table1.fnClearTable();
                bloquearUI(parametros.blockMess);
                $.getJSON(parametros.sPersonaByIdentificacionUrl+$("#numIdentificacion").val(), {
                    ajax: 'true'
                }, function (data) {
                    if (data.datos != null) {
                        loadPersons(data.datos);
                        setPagination(data.paginacion);
                    }
                    if (data.error!=null){
                        $.smallBox({
                            title: data.error.messageUser,
                            content: $("#smallBox_content").val(),
                            color: "#C79121",
                            iconSmall: "fa fa-warning",
                            timeout: 4000
                        });
                        setPagination(data.paginacion);
                    }
                    desbloquearUI();
                }).fail(function (jqXHR) {
                    setPagination(null);
                    desbloquearUI();
                    validateLogin(jqXHR);
                });
            }

            function getPersonsByName(pagina) {
                table1.fnClearTable();
                bloquearUI(parametros.blockMess);
                $.ajax({
                    url: parametros.sPersonaByNombresUrl,
                    data: {
                        nombrecompleto : "0",
                        primerapellido:  $("#primerApellido").val(),
                        primernombre: $("#primerNombre").val(),
                        segundoapellido: $("#segundoApellido").val(),
                        segundonombre: $("#segundoNombre").val(),
                        pagina: pagina,
                        registros: rowsPage
                    },
                    error: function(xhr) {
                        setPagination(null);
                        desbloquearUI();
                        validateLogin(xhr);
                    }
                }).then(function(data) {
                    if (data.datos != null) {
                        loadPersons(data.datos);
                        setPagination(data.paginacion);
                    }
                    if (data.error!=null){
                        $.smallBox({
                            title: data.error.messageUser,
                            content: $("#smallBox_content").val(),
                            color: "#C79121",
                            iconSmall: "fa fa-warning",
                            timeout: 4000
                        });
                        setPagination(data.paginacion);
                    }
                    desbloquearUI();
                });
            }

            function setPagination(paginacion){
                if (paginacion!=null) {
                    var total = paginacion.paginaRegistros * paginacion.pagina;
                    if (total > paginacion.cantidadRegistros)
                        total = paginacion.cantidadRegistros;
                    $("#paginacionLbl").html("Página " + paginacion.pagina + " De " + paginacion.paginasCantidad + ", Mostrando " + (paginacion.paginaRegistros * (paginacion.pagina - 1) + 1) + " al " + total + " de <strong>" + paginacion.cantidadRegistros+"</strong>")
                    if (paginacion.pagina>1) {
                        $("#prev").prop('disabled',false);
                    }else{
                        $("#prev").prop('disabled',true);
                    }
                    if (paginacion.cantidadRegistros < rowsPage) {
                        $("#next").prop('disabled', true);
                    } else {
                        $("#next").prop('disabled', false);
                    }
                }else {
                    $("#paginacionLbl").html("");
                    $("#prev").prop('disabled',true);
                    $("#next").prop('disabled',true);
                }
            }

            function loadPersons(datos){
                if (datos != null) {
                    var len = datos.length;
                    for (var i = 0; i < len; i++) {
                        var nombreMuniRes = "";

                        if (datos[i].divisionPolitica.nacimiento.municipio != null) {
                            nombreMuniRes = datos[i].divisionPolitica.nacimiento.municipio.nombre;
                        }
                        var actionUrl = parametros.sActionUrl + '/' + datos[i].id + '/'+datos[i].identificada;
                        var actionNotiPacienteUrl = parametros.sActionNotiPacienteUrl + '/' + datos[i].id + '/'+datos[i].identificada;
                        var edad = getAge(datos[i].fechaNacimiento).split(",");
                        table1.fnAddData(
                            [
                                (datos[i].identificacion != null ? datos[i].identificacion.valor : ""),
                                datos[i].primerNombre,
                                (datos[i].segundoNombre != null ? datos[i].segundoNombre : ""),
                                datos[i].primerApellido,
                                (datos[i].segundoApellido != null ? datos[i].segundoApellido : ""),
                                datos[i].fechaNacimiento,
                                edad[0],
                                nombreMuniRes,
                                    '<a title="Ver" href=' + actionUrl + ' class="btn btn-success btn-xs"><i class="fa fa-mail-forward"></i></a>',
                                    '<a title="Eventos Previos" href=' + actionNotiPacienteUrl + ' class="btn btn-primary btn-xs"><i class="fa fa-list"></i></a>']);
                    }
                }
            }

            //datos por defecto
            $('#filtroIdentificacion').hide();

        }
    };

}();