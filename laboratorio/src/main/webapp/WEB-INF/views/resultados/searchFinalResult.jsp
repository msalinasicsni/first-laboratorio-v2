<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<!-- BEGIN HEAD -->
<head>
    <jsp:include page="../fragments/headTag.jsp" />
    <spring:url value="/resources/img/plus.png" var="plus"/>
    <spring:url value="/resources/img/minus.png" var="minus"/>
    <style>
        textarea {
            resize: none;
        }
        td.details-control {
            background: url("${plus}") no-repeat center center;
            cursor: pointer;
        }
        tr.shown td.details-control {
            background: url("${minus}") no-repeat center center;
        }
    </style>
</head>
<!-- END HEAD -->
<!-- BEGIN BODY -->
<body class="">
<!-- #HEADER -->
<jsp:include page="../fragments/bodyHeader.jsp" />
<!-- #NAVIGATION -->
<jsp:include page="../fragments/bodyNavigation.jsp" />
<!-- MAIN PANEL -->
<div id="main" data-role="main">
<!-- RIBBON -->
<div id="ribbon">
			<span class="ribbon-button-alignment">
				<span id="refresh" class="btn btn-ribbon" data-action="resetWidgets" data-placement="bottom" data-original-title="<i class='text-warning fa fa-warning'></i> <spring:message code="msg.reset" />" data-html="true">
					<i class="fa fa-refresh"></i>
				</span>
			</span>
    <!-- breadcrumb -->
    <ol class="breadcrumb">
        <li><a href="<spring:url value="/" htmlEscape="true "/>"><spring:message code="menu.home" /></a> <i class="fa fa-angle-right"></i> <a href="<spring:url value="/aprobacion/init" htmlEscape="true "/>"><spring:message code="menu.approval.results" /></a></li>
    </ol>
    <!-- end breadcrumb -->
    <jsp:include page="../fragments/layoutOptions.jsp" />
</div>
<!-- END RIBBON -->
<!-- MAIN CONTENT -->
<div id="content">
<!-- row -->
<div class="row">
    <!-- col -->
    <div class="col-xs-12 col-sm-7 col-md-7 col-lg-4">
        <h1 class="page-title txt-color-blueDark">
            <!-- PAGE HEADER -->
            <i class="fa-fw fa fa-check-circle"></i>
            <spring:message code="lbl.final.result" />
						<span> <i class="fa fa-angle-right"></i>
							<spring:message code="lbl.approve.result.subtitle" />
						</span>
        </h1>
    </div>
    <!-- end col -->
</div>
<!-- end row -->
<!-- widget grid -->
<section id="widget-grid" class="">
<!-- row -->
<div class="row">
<!-- NEW WIDGET START -->
<article class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
    <!-- Widget ID (each widget will need unique ID)-->
    <div class="jarviswidget jarviswidget-color-darken" id="wid-id-0">
        <header>
            <span class="widget-icon"> <i class="fa fa-search"></i> </span>
            <h2><spring:message code="lbl.parameters" /> </h2>
        </header>
        <!-- widget div-->
        <div>
            <!-- widget edit box -->
            <div class="jarviswidget-editbox">
                <!-- This area used as dropdown edit box -->
                <input class="form-control" type="text">
            </div>
            <!-- end widget edit box -->
            <!-- widget content -->
            <div class="widget-body no-padding">
                <input id="text_opt_select" type="hidden" value="<spring:message code="lbl.select"/>"/>
                <input id="smallBox_content" type="hidden" value="<spring:message code="smallBox.content.4s"/>"/>
                <input id="msg_no_results_found" type="hidden" value="<spring:message code="msg.no.results.found"/>"/>
                <input id="txtEsLaboratorio" type="hidden" value="false"/>
                <input id="text_value" type="hidden" value="<spring:message code="lbl.result.value"/>"/>
                <input id="text_date" type="hidden" value="<spring:message code="lbl.result.date"/>"/>
                <input id="text_response" type="hidden" value="<spring:message code="lbl.approve.response"/>"/>
                <input id="text_selected_all" type="hidden" value="<spring:message code="lbl.selected.all"/>"/>
                <input id="text_selected_none" type="hidden" value="<spring:message code="lbl.selected.none"/>"/>
                <input id="msg_approval_select_order" type="hidden" value="<spring:message code="msg.approve.select.order"/>"/>
                <input id="msg_approval_cancel" type="hidden" value="<spring:message code="msg.approve.cancel"/>"/>
                <input id="msg_approval_confirm_t" type="hidden" value="<spring:message code="msg.confirm.title"/>"/>
                <input id="msg_approval_confirm_c" type="hidden" value="<spring:message code="msg.approve.confirm.content"/>"/>
                <input id="confirm_msg_opc_yes" type="hidden" value="<spring:message code="lbl.confirm.msg.opc.yes"/>"/>
                <input id="confirm_msg_opc_no" type="hidden" value="<spring:message code="lbl.confirm.msg.opc.no"/>"/>
                <input id="msg_approval_succes" type="hidden" value="<spring:message code="msg.approve.successfully"/>"/>
                <form id="searchResults-form" class="smart-form" autocomplete="off">
                    <fieldset>
                        <div class="row">
                            <section class="col col-sm-6 col-md-4 col-lg-3">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.prc.start.date" />
                                </label>
                                <label class="input">
                                    <i class="icon-prepend fa fa-pencil"></i> <i class="icon-append fa fa-calendar"></i>
                                    <input type="text" name="fecInicioProc" id="fecInicioProc"
                                           placeholder="<spring:message code="lbl.date.format"/>"
                                           class="form-control from_date" data-date-end-date="+0d"/>
                                </label>
                            </section>
                            <section class="col col-sm-6 col-md-4 col-lg-3">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.prc.end.date" />
                                </label>
                                <label class="input">
                                    <i class="icon-prepend fa fa-pencil"></i> <i class="icon-append fa fa-calendar"></i>
                                    <input type="text" name="fecFinProc" id="fecFinProc"
                                           placeholder="<spring:message code="lbl.date.format"/>"
                                           class="form-control to_date" data-date-end-date="+0d"/>
                                </label>
                            </section>
                            <section class="col col-sm-6 col-md-4 col-lg-3">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.receipt.start.date" />
                                </label>
                                <label class="input">
                                    <i class="icon-prepend fa fa-pencil"></i> <i class="icon-append fa fa-calendar"></i>
                                    <input type="text" name="fecInicioRecep" id="fecInicioRecep"
                                           placeholder="<spring:message code="lbl.date.format"/>"
                                           class="form-control from_date" data-date-end-date="+0d"/>
                                </label>
                            </section>
                            <section class="col col-sm-6 col-md-4 col-lg-3">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.receipt.end.date" />
                                </label>
                                <label class="input">
                                    <i class="icon-prepend fa fa-pencil"></i> <i class="icon-append fa fa-calendar"></i>
                                    <input type="text" name="fecFinRecep" id="fecFinRecep"
                                           placeholder="<spring:message code="lbl.date.format"/>"
                                           class="form-control to_date" data-date-end-date="+0d"/>
                                </label>
                            </section>

                        </div>
                        <div class="row">
                            <section class="col col-sm-12 col-md-5 col-lg-5">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.silais" /> </label>
                                <div class="input-group">
                                    <span class="input-group-addon"><i class="fa fa-location-arrow fa-fw"></i></span>
                                    <select id="codSilais" name="codSilais"
                                            class="select2">
                                        <option value=""><spring:message code="lbl.select" />...</option>
                                        <c:forEach items="${entidades}" var="entidad">
                                            <option value="${entidad.id}">${entidad.nombre}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </section>
                            <section class="col col-sm-12 col-md-7 col-lg-7">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.health.unit" /> </label>
                                <div class="input-group">
                                    <span class="input-group-addon"><i class="fa fa-location-arrow fa-fw"></i></span>
                                    <select id="codUnidadSalud" name="codUnidadSalud"
                                            class="select2">
                                        <option value=""><spring:message code="lbl.select" />...</option>
                                    </select>
                                </div>
                            </section>
                        </div>
                        <div class="row">
                            <section class="col col-sm-6 col-md-4 col-lg-5">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.sample.type" /> </label>
                                <div class="input-group">
                                    <span class="input-group-addon"><i class="fa fa-location-arrow fa-fw"></i></span>
                                    <select id="codTipoMx" name="codTipoMx"
                                            class="select2">
                                        <option value=""><spring:message code="lbl.select" />...</option>
                                        <c:forEach items="${tipoMuestra}" var="tipoMuestra">
                                            <option value="${tipoMuestra.idTipoMx}">${tipoMuestra.nombre}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </section>

                            <section class="col col-sm-12 col-md-7 col-lg-3">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.request.type" /> </label>
                                <div class="input-group">
                                    <span class="input-group-addon"><i class="fa fa-location-arrow fa-fw"></i></span>
                                    <select id="tipo" name="tipo"
                                            class="select2">
                                        <option value=""><spring:message code="lbl.select" />...</option>
                                        <option value="Estudio"><spring:message code="lbl.study" /></option>
                                        <option value="Rutina"><spring:message code="lbl.routine" /></option>
                                    </select>
                                </div>
                            </section>

                            <section class="col col-sm-12 col-md-12 col-lg-4">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.request.name" />
                                </label>
                                <label class="input"><i class="icon-prepend fa fa-pencil"></i> <i class="icon-append fa fa-sort-alpha-asc"></i>
                                    <input type="text" id="nombreSoli" name="nombreSoli" placeholder="<spring:message code="lbl.request.name"/>">
                                    <b class="tooltip tooltip-bottom-right"><i class="fa fa-warning txt-color-pink"></i><spring:message code="tooltip.send.request.name"/></b>
                                </label>
                            </section>
                        </div>
                        <div class="row">
                            <section class="col col-sm-12 col-md-6 col-lg-3">
                                <label class="text-left txt-color-blue font-md">
                                    <spring:message code="lbl.unique.code.mx" /> </label>
                                <label class="input"><i class="icon-prepend fa fa-pencil"></i> <i class="icon-append fa fa-sort-alpha-asc"></i>
                                    <input type="text" id="txtCodUnicoMx" name="txtCodUnicoMx" placeholder="<spring:message code="lbl.unique.code.mx"/>">
                                    <b class="tooltip tooltip-bottom-right"><i class="fa fa-warning txt-color-pink"></i><spring:message code="tooltip.unique.code.mx"/></b>
                                </label>
                            </section>
                        </div>
                    </fieldset>
                    <footer>
                        <button type="button" id="all-results" class="btn btn-info"><i class="fa fa-search"></i> <spring:message code="act.show.all" /></button>
                        <button type="submit" id="search-orders" class="btn btn-info"><i class="fa fa-search"></i> <spring:message code="act.search" /></button>
                    </footer>
                </form>
            </div>
            <!-- end widget content -->
        </div>
        <!-- end widget div -->
    </div>
    <!-- end widget -->
</article>
<!-- WIDGET END -->
<!-- NEW WIDGET START -->
<article class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
    <!-- Widget ID (each widget will need unique ID)-->
    <div class="jarviswidget jarviswidget-color-darken" id="wid-id-1">
        <header>
            <span class="widget-icon"> <i class="fa fa-reorder"></i> </span>
            <h2><spring:message code="lbl.results" /> </h2>
        </header>
        <!-- widget div-->
        <div>
            <!-- widget edit box -->
            <div class="jarviswidget-editbox">
                <!-- This area used as dropdown edit box -->
                <input class="form-control" type="text">
            </div>
            <!-- end widget edit box -->
            <!-- widget content -->
            <div class="widget-body no-padding">
                <table id="orders_result" class="table table-striped table-bordered table-hover" width="100%">
                    <thead>
                    <tr>
                        <th data-class="expand"><i class="fa fa-fw fa-barcode text-muted hidden-md hidden-sm hidden-xs"></i><spring:message code="lbl.lab.code.mx"/></th>
                        <th data-hide="phone"><i class="fa fa-fw fa-list text-muted hidden-md hidden-sm hidden-xs"></i><spring:message code="lbl.sample.type"/></th>
                        <th data-hide="phone"><i class="fa fa-fw fa-list text-muted hidden-md hidden-sm hidden-xs"></i><spring:message code="lbl.sampling.datetime"/></th>
                        <th data-hide="phone"><i class="fa fa-fw fa-calendar text-muted hidden-md hidden-sm hidden-xs"></i><spring:message code="lbl.receipt.symptoms.start.date"/></th>
                        <th data-hide="phone"><spring:message code="lbl.silais"/></th>
                        <th data-hide="phone"><spring:message code="lbl.health.unit"/></th>
                        <th data-class="expand"><i class="fa fa-fw fa-user text-muted hidden-md hidden-sm hidden-xs"></i><spring:message code="lbl.receipt.person.applicant.name"/></th>
                        <th><spring:message code="lbl.solic.short"/></th>
                        <th><spring:message code="lbl.final.result"/></th>
                        <th></th>
                    </tr>
                    </thead>
                </table>
                <form id="sendOrders-form" class="smart-form" autocomplete="off">
                    <footer>
                        <button type="button" id="approve_selected" class="btn btn-success btn-lg pull-right header-btn"><i class="fa fa-send"></i> <spring:message code="act.approve.selected" /></button>
                    </footer>
                </form>
            </div>
            <!-- end widget content -->
        </div>
        <!-- end widget div -->
    </div>
    <!-- end widget -->
</article>
<!-- WIDGET END -->
</div>
<!-- end row -->
<!-- row -->
<div class="row">
    <!-- a blank row to get started -->
    <div class="col-sm-12">
        <!-- your contents here -->
    </div>
</div>
<!-- end row -->
</section>
<!-- end widget grid -->
</div>
<!-- END MAIN CONTENT -->
</div>
<!-- END MAIN PANEL -->
<!-- BEGIN FOOTER -->
<jsp:include page="../fragments/footer.jsp" />
<!-- END FOOTER -->
<!-- BEGIN JAVASCRIPTS(Load javascripts at bottom, this will reduce page load time) -->
<jsp:include page="../fragments/corePlugins.jsp" />
<!-- BEGIN PAGE LEVEL PLUGINS -->
<spring:url value="/resources/js/plugin/datatables/jquery.dataTables.min.js" var="dataTables" />
<script src="${dataTables}"></script>
<spring:url value="/resources/js/plugin/datatables/dataTables.colVis.min.js" var="dataTablesColVis" />
<script src="${dataTablesColVis}"></script>
<spring:url value="/resources/js/plugin/datatables/dataTables.tableTools.min.js" var="dataTablesTableTools" />
<script src="${dataTablesTableTools}"></script>
<spring:url value="/resources/js/plugin/datatables/dataTables.bootstrap.min.js" var="dataTablesBootstrap" />
<script src="${dataTablesBootstrap}"></script>
<spring:url value="/resources/js/plugin/datatable-responsive/datatables.responsive.min.js" var="dataTablesResponsive" />
<script src="${dataTablesResponsive}"></script>
<spring:url value="/resources/js/plugin/datatables/swf/copy_csv_xls_pdf.swf" var="tabletools" />
<!-- jQuery Selecte2 Input -->
<spring:url value="/resources/js/plugin/select2/select2.min.js" var="selectPlugin"/>
<script src="${selectPlugin}"></script>
<!-- bootstrap datepicker -->
<spring:url value="/resources/js/plugin/bootstrap-datepicker/bootstrap-datepicker.js" var="datepickerPlugin" />
<script src="${datepickerPlugin}"></script>
<spring:url value="/resources/js/plugin/bootstrap-datepicker/locales/bootstrap-datepicker.{languagedt}.js" var="datePickerLoc">
    <spring:param name="languagedt" value="${pageContext.request.locale.language}" /></spring:url>
<script src="${datePickerLoc}"></script>
<!-- JQUERY VALIDATE -->
<spring:url value="/resources/js/plugin/jquery-validate/jquery.validate.min.js" var="jqueryValidate" />
<script src="${jqueryValidate}"></script>
<spring:url value="/resources/js/plugin/jquery-validate/messages_{language}.js" var="jQValidationLoc">
    <spring:param name="language" value="${pageContext.request.locale.language}" /></spring:url>
<script src="${jQValidationLoc}"></script>
<!-- JQUERY BLOCK UI -->
<spring:url value="/resources/js/plugin/jquery-blockui/jquery.blockUI.js" var="jqueryBlockUi" />
<script src="${jqueryBlockUi}"></script>
<!-- JQUERY INPUT MASK -->
<spring:url value="/resources/js/plugin/jquery-inputmask/jquery.inputmask.bundle.min.js" var="jqueryInputMask" />
<script src="${jqueryInputMask}"></script>
<!-- END PAGE LEVEL PLUGINS -->
<!-- BEGIN PAGE LEVEL SCRIPTS -->
<spring:url value="/resources/scripts/resultados/searchFinalResult.js" var="incomeResult" />
<script src="${incomeResult}"></script>
<spring:url value="/resources/scripts/utilidades/handleDatePickers.js" var="handleDatePickers" />
<script src="${handleDatePickers}"></script>
<spring:url value="/resources/scripts/utilidades/calcularEdad.js" var="calculateAge" />
<script src="${calculateAge}"></script>
<spring:url value="/resources/scripts/utilidades/handleInputMask.js" var="handleInputMask" />
<script src="${handleInputMask}"></script>
<spring:url value="/resources/scripts/utilidades/seleccionUnidadLab.js" var="selectUnidad" />
<script src="${selectUnidad}"></script>
<!-- END PAGE LEVEL SCRIPTS -->
<spring:url value="/personas/search" var="sPersonUrl"/>
<c:set var="blockMess"><spring:message code="blockUI.message" /></c:set>
<c:url var="sSearchUrl" value="/aprobacion/search"/>

<c:url var="unidadesURL" value="/api/v1/unidadesPrimariasHospSilais"/>
<c:url var="sApprovalUrl" value="/aprobacion/create/"/>
<c:url var="sApprovalMassiveUrl" value="/aprobacion/approveMassiveResult"/>
<script type="text/javascript">
    $(document).ready(function() {
        pageSetUp();
        var parametros = {sPersonUrl: "${sPersonUrl}",
            sSearchUrl : "${sSearchUrl}",
            sUnidadesUrl : "${unidadesURL}",
            blockMess: "${blockMess}",
            sActionUrl : "${sApprovalUrl}",
            sEsIngreso : 'false',
            sTableToolsPath : "${tabletools}",
            sApprovalMassiveUrl : "${sApprovalMassiveUrl}"
        };
        SearchFinalResult.init(parametros);
        SeleccionUnidadLab.init(parametros);
        handleDatePickers("${pageContext.request.locale.language}");
        handleInputMasks();
        $("li.resultado").addClass("open");
        $("li.approveResult").addClass("active");
        if("top"!=localStorage.getItem("sm-setmenu")){
            $("li.approveResult").parents("ul").slideDown(200);
        }
    });
</script>
<!-- END JAVASCRIPTS -->
</body>
<!-- END BODY -->
</html>