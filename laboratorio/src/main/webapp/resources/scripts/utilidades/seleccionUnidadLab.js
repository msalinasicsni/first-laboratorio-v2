var SeleccionUnidadLab = function () {
	
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
            $('#codSilais').change(function () {
                console.log('en SeleccionUnidadLab');
                bloquearUI(parametros.blockMess);
                if ($(this).val().length > 0) {
                    $.getJSON(parametros.sUnidadesUrl, {
                        codSilais: $(this).val(),
                        ajax: 'true'
                    }, function (data) {
                        var html = null;
                        var len = data.length;
                        html += '<option value="">' + $("#text_opt_select").val() + '...</option>';
                        for (var i = 0; i < len; i++) {
                            html += '<option value="' + data[i].codigo + '">'
                                + data[i].nombre
                                + '</option>';
                            // html += '</option>';
                        }
                        $('#codUnidadSalud').html(html);
                        desbloquearUI();
                    }).fail(function (jqXHR) {
                        setTimeout($.unblockUI, 10);
                        validateLogin(jqXHR);
                    });
                } else {
                    var html = '<option value="">' + $("#text_opt_select").val() + '...</option>';
                    $('#codUnidadSalud').html(html);
                    desbloquearUI();
                }
                $('#codUnidadSalud').val('').change();
            });

            $('#codSilaisAtencion').change(
                function() {
                    bloquearUI(parametros.blockMess);
                    var area = $('#codArea').find('option:selected').val();
                    if ($(this).val().length > 0 && (area == null || area === "AREAREP|MUNI" || area === "AREAREP|UNI")) {
                        var valSilais =$('#codSilaisAtencion').val();
                        var elemValSilais = valSilais.split(",");
                        $.getJSON(parametros.sMunicipiosUrl, {
                            idSilais: elemValSilais[0],
                            ajax: 'true'
                        }, function (data) {
                            $("#codMunicipio").select2('data', null);
                            $("#codUnidadAtencion").select2('data', null);
                            $("#codMunicipio").empty();
                            $("#codUnidadAtencion").empty();
                            var html = '<option value="">' + $("#text_opt_select").val() + '...</option>';
                            var len = data.length;
                            for (var i = 0; i < len; i++) {
                                html += '<option value="' + data[i].id+","+data[i].codigo + '">'
                                    + data[i].nombre + '</option>';
                            }
                            html += '</option>';
                            $('#codMunicipio').html(html);
                            desbloquearUI();
                        }).fail(function (jqXHR) {
                            setTimeout($.unblockUI, 10);
                            validateLogin(jqXHR);
                        });
                    } else {
                        var html = '<option value="">' + $("#text_opt_select").val() + '...</option>';
                        $('#codMunicipio').html(html);
                        $('#codUnidadAtencion').html(html);
                        desbloquearUI();
                    }
                });

            $('#codMunicipio').change(
                function() {
                    bloquearUI(parametros.blockMess);
                    var area = $('#codArea').find('option:selected').val();
                    if ($(this).val().length > 0 && (area == null || area === "AREAREP|UNI")) {
                        var valSilais =$('#codSilaisAtencion').val();
                        var elemValSilais = valSilais.split(",");

                        var valMuni =$('#codMunicipio').val();
                        var elemValMuni = valMuni.split(",");

                        $.getJSON(parametros.sUnidadesUrl, {
                            codMunicipio: elemValMuni[0],
                            codSilais: elemValSilais[0],
                            ajax: 'true'
                        }, function (data) {
                            $("#codUnidadAtencion").select2('data', null);
                            $("#codUnidadAtencion").empty();
                            var html = '<option value="">' + $("#text_opt_select").val() + '...</option>';
                            var len = data.length;
                            for (var i = 0; i < len; i++) {
                                html += '<option value="'+ data[i].id+"," + data[i].codigo + '">'
                                    + data[i].nombre + '</option>';
                            }
                            html += '</option>';
                            $('#codUnidadAtencion').html(html);
                            desbloquearUI();
                        });
                    } else {
                        var html = '<option value="">' + $("#text_opt_select").val() + '...</option>';
                        $('#codUnidadAtencion').html(html);
                        desbloquearUI();
                    }
                });
        }
	};
	
}();