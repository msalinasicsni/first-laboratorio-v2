<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sec"
           uri="http://www.springframework.org/security/tags"%>
<html>
<head>
    <jsp:include page="../../fragments/headTag.jsp" />
    <style>
        .modal .modal-dialog {
            width: 55%;
        }
        .styleButton {
            float: right;
            height: 31px;
            margin: 10px 0px 0px 5px;
            padding: 0px 22px;
            font: 300 15px/29px "Open Sans", Helvetica, Arial, sans-serif;
            cursor: pointer;
        }
        .alert{
            margin-bottom: 0px;
        }
    </style>
</head>
<body class="">
<jsp:include page="../../fragments/bodyHeader.jsp" />
<jsp:include page="../../fragments/bodyNavigation.jsp" />
<!-- Main bar -->
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
            <li><a href="<spring:url value="/" htmlEscape="true "/>"><spring:message code="menu.home" /></a> <i class="fa fa-angle-right"></i> <spring:message code="menu.catalogs" /> <i class="fa fa-angle-right"></i> <a href="<spring:url value="/administracion/examenes/list" htmlEscape="true "/>"><spring:message code="menu.test" /></a></li>
        </ol>
        <!-- end breadcrumb -->
        <jsp:include page="../../fragments/layoutOptions.jsp" />
    </div>
    <!-- END RIBBON -->
    <!-- Matter -->

    <div id="content">
        <!-- row -->
        <div class="row">
            <!-- col -->
            <div class="col-xs-12 col-sm-7 col-md-7 col-lg-4">
                <h1 class="page-title txt-color-blueDark">
                    <!-- PAGE HEADER -->
                    <i class="fa-fw fa fa-medkit"></i>
                    <spring:message code="lbl.tests" />
						<span> <i class="fa fa-angle-right"></i>
							<spring:message code="lbl.test.header" />
						</span>
                </h1>
            </div>
            <!-- end col -->
        </div>
        <!-- end row -->
        <section id="widget-grid" class="">

            <!-- Table -->

            <div class="row">
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
                                <p class="alert alert-info">
                                    <button type="button" id="btnAddTest" class="btn btn-primary">
                                        <i class="fa fa-plus"></i>
                                        <spring:message code="lbl.test2" />
                                    </button>
                                </p>
                                <input id="blockUI_message" type="hidden" value="<spring:message code="blockUI.message"/>"/>
                                <table class="table table-striped table-bordered table-hover" width="100%" id="test-list">
                                    <thead>
                                    <tr>
                                        <th><spring:message code="lbl.name" /></th>
                                        <th><spring:message code="lbl.test.price" /></th>
                                        <th><spring:message code="lbl.enabled" /></th>
                                        <th><spring:message code="lbl.area" /></th>
                                        <th style="width: 5%" align="center"><spring:message code="lbl.rules" /></th>
                                        <th style="width: 5%" align="center"><spring:message code="act.edit" /></th>
                                        <th style="width: 5%" align="center"><spring:message code="act.override" /></th>
                                    </tr>
                                    </thead>
                                </table>
                            </div>
                            </div>
                            <!-- end widget div -->
                        </div>
                        <!-- end widget -->
                </article>
                <!-- WIDGET END -->
            </div>

        </section>
        <!-- Matter ends -->
        <!-- Modal AREAS ROL ANALISTA-->
        <div class="modal fade" id="modalTest" aria-hidden="true" data-backdrop="static">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <div class="alert alert-info">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                &times;
                            </button>
                            <h4 class="modal-title">
                                <i class="fa-fw fa fa-medkit"></i>
                                <spring:message code="lbl.header.modal.test" />
                            </h4>
                        </div>
                    </div>
                    <div class="modal-body"> <!--  no-padding -->
                        <div class="row">
                            <div class="col col-sm-12 col-md-12 col-lg-12">
                                <form class="smart-form" autocomplete="off" id="add-test-form">
                                    <fieldset>
                                        <div class="row">
                                            <section class="col col-sm-6 col-md-9 col-lg-8">
                                                <label class="text-left txt-color-blue font-md">
                                                    <i class="fa fa-fw fa-asterisk txt-color-red font-sm"></i><spring:message code="lbl.name" />
                                                </label>
                                                <div class="">
                                                    <label class="input">
                                                        <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-user fa-fw"></i>
                                                        <input class="form-control" type="text" id="nombre"  name="nombre" value="" placeholder=" <spring:message code="lbl.name" />">
                                                        <b class="tooltip tooltip-bottom-right"> <i
                                                                class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.test.name.create"/>
                                                        </b>
                                                    </label>
                                                </div>
                                            </section>
                                            <section class="col col-sm-6 col-md-3 col-lg-4">
                                                <label class="text-left txt-color-blue font-md">
                                                    <spring:message code="lbl.test.price" />
                                                </label>
                                                <div class="">
                                                    <label class="input">
                                                        <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-sort-alpha-asc fa-fw"></i>
                                                        <input class="form-control" type="text" id="precio" name="precio" placeholder=" <spring:message code="lbl.test.price" />">
                                                        <b class="tooltip tooltip-bottom-right"> <i
                                                                class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.test.price"/>
                                                        </b>
                                                    </label>
                                                </div>
                                            </section>
                                            </div>
                                        <div class="row">
                                            <section class="col col-sm-6 col-md-9 col-lg-8">
                                                <label class="text-left txt-color-blue font-md">
                                                    <i class="fa fa-fw fa-asterisk txt-color-red font-sm"></i><spring:message code="lbl.area" />
                                                </label>
                                                <div class="input-group">
                                                    <span class="input-group-addon"><i class="fa fa-location-arrow fa-fw"></i></span>
                                                    <select id="area" name="area"
                                                            class="select2">
                                                        <option value=""><spring:message code="lbl.select" />...</option>
                                                        <c:forEach items="${areas}" var="area">
                                                            <option value="${area.idArea}">${area.nombre}</option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </section>
                                            <section class="col col-sm-6 col-md-3 col-lg-4">
                                                <label class="text-left txt-color-blue font-md"><spring:message code="lbl.enabled"/></label>
                                                <div class="row">
                                                    <div class="col col-4">
                                                        <label class="checkbox">
                                                            <input type="checkbox" name="checkbox-enable" id="checkbox-enable" checked>
                                                            <i></i></label>
                                                    </div>
                                                </div>
                                            </section>
                                        </div>
                                    </fieldset>
                                    <footer>
                                        <input id="msjSuccessful" type="hidden" value="<spring:message code="msg.test.added"/>"/>
                                        <input id="disappear" type="hidden" value="<spring:message code="msg.disappear"/>"/>
                                        <input id="msjOverride" type="hidden" value="<spring:message code="msg.confirm.override"/>"/>
                                        <input id="msjOverrideC" type="hidden" value="<spring:message code="msg.override.canceled"/>"/>
                                        <input id="msjConfirm" type="hidden" value="<spring:message code="msg.confirm.title"/>"/>
                                        <input id="msjSuccessfulOverride" type="hidden" value="<spring:message code="msg.test.disabled"/>"/>
                                        <input id="confirm_msg_opc_yes" type="hidden" value="<spring:message code="lbl.confirm.msg.opc.yes"/>"/>
                                        <input id="confirm_msg_opc_no" type="hidden" value="<spring:message code="lbl.confirm.msg.opc.no"/>"/>
                                        <input id="msjSuccessfulRule" type="hidden" value="<spring:message code="msg.rule.added"/>"/>
                                        <input id="msjOverrideRule" type="hidden" value="<spring:message code="msg.rule.disabled"/>"/>
                                        <input id="idExamen" type="hidden" value="" />

                                        <button type="submit" class="btn btn-success styleButton">
                                            <i class="fa fa-save"></i>  <spring:message code="act.save" />
                                        </button>
                                        <button type="button" class="btn btn-danger" data-dismiss="modal">
                                            <i class="fa fa-times"></i>  <spring:message code="act.end" />
                                        </button>
                                    </footer>
                                </form>
                            </div>
                        </div>
                    </div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div><!-- /.modal -->

        <!-- Modal Rules -->
        <div class="modal fade" id="modalRules" aria-hidden="true" data-backdrop="static">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <div class="alert alert-info">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                &times;
                            </button>
                            <h4 class="modal-title" id="headerRules">
                                <i class="fa-fw fa fa-list"></i>
                                <spring:message code="lbl.add.edit.rules" />
                            </h4>
                        </div>
                    </div>
                    <div class="modal-body">
                        <form id="rules-form" class="smart-form" autocomplete="off">
                            <div class="row">
                                <input id="idRegla" hidden="hidden" type="text" name="idRegla"/>
                                <input id="idExamenAsociado" hidden="hidden" type="text" name="idExamenAsociado"/>
                                <section class="col col-sm-12 col-md-8 col-lg-8">
                                    <label class="text-left txt-color-blue font-md">
                                        <spring:message code="lbl.rule"/>
                                    </label>
                                    <div class="">
                                        <label class="textarea">
                                            <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-sort-alpha-asc fa-fw"></i>
                                            <textarea class="form-control" rows="3" maxlength="250" name="descripcion" id="descripcion"
                                                      placeholder="<spring:message code="lbl.rule" />"></textarea>
                                            <b class="tooltip tooltip-bottom-right"> <i
                                                    class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.rule"/>
                                            </b>
                                        </label>
                                    </div>
                                </section>
                                <section style="padding-top: 10px" class="col col-sm-6 col-md-3 col-lg-3">
                                    <button type="submit" id="btnSave" class="btn btn-success styleButton"><i class="fa fa-save"></i></button>
                                </section>
                            </div>
                        </form>
                        <div class="widget-body no-padding">
                            <table id="rules-list" class="table table-striped table-bordered table-hover" width="100%">
                                <thead>
                                <tr>
                                    <th data-class="expand"><i class="fa fa-fw fa-file-text-o text-muted hidden-md hidden-sm hidden-xs"></i><spring:message code="lbl.rule"/></th>
                                    <th><spring:message code="act.edit"/></th>
                                    <th><spring:message code="lbl.override"/></th>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                </div>
                <!-- /.modal-content -->
            </div>
            <!-- /.modal-dialog -->
        </div>
        <!-- /.modal -->
    </div>
</div>

<!-- Content ends -->
<!-- Footer starts -->
<jsp:include page="../../fragments/footer.jsp" />
<jsp:include page="../../fragments/corePlugins.jsp" />
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
<!-- jQuery Selecte2 Input -->
<spring:url value="/resources/js/plugin/select2/select2.min.js" var="selectPlugin"/>
<script src="${selectPlugin}"></script>
<!-- JQUERY VALIDATE -->
<spring:url value="/resources/js/plugin/jquery-validate/jquery.validate.min.js" var="jqueryValidate" />
<script src="${jqueryValidate}"></script>
<spring:url value="/resources/js/plugin/jquery-validate/messages_{language}.js" var="jQValidationLoc">
    <spring:param name="language" value="${pageContext.request.locale.language}" /></spring:url>
<script src="${jQValidationLoc}"></script>
<!-- JQUERY BLOCK UI -->
<spring:url value="/resources/js/plugin/jquery-blockui/jquery.blockUI.js" var="jqueryBlockUi" />
<script src="${jqueryBlockUi}"></script>
<!-- END PAGE LEVEL PLUGINS -->
<!-- BEGIN PAGE LEVEL SCRIPTS -->
<spring:url value="/resources/scripts/administracion/catalogos/tests.js" var="testsJs" />
<script src="${testsJs}"></script>
<!-- END PAGE LEVEL SCRIPTS -->
<c:url var="saveUrl" value="/administracion/examenes/save"/>
<c:url var="examenesUrl" value="/administracion/examenes/getTests"/>
<c:url var="overrideUrl" value="/administracion/examenes/override"/>
<c:url var="examenUrl" value="/administracion/examenes/getTest"/>
<c:url var="rulesUrl" value="/administracion/examenes/obtenerReglas"/>
<c:url var="ruleUrl" value="/administracion/examenes/obtenerRegla"/>
<c:url var="saveRuleUrl" value="/administracion/examenes/guardarReglaExamen"/>
<c:url var="overrideRuleUrl" value="/administracion/examenes/deshabilitarRegla"/>
<c:set var="headerModalRules"><spring:message code="lbl.add.edit.rules" /></c:set>
<script type="text/javascript" charset="utf-8">
    $(document).ready(function() {
        pageSetUp();
        var parametros = {
            blockMess : $("#blockUI_message").val(),
            saveUrl : "${saveUrl}",
            examenesUrl : "${examenesUrl}",
            overrideUrl : "${overrideUrl}",
            examenUrl : "${examenUrl}",
            rulesUrl : "${rulesUrl}",
            ruleUrl : "${ruleUrl}",
            saveRuleUrl : "${saveRuleUrl}",
            overrideRuleUrl : "${overrideRuleUrl}",
            headerModalRules : "${headerModalRules}"
        };
        Tests.init(parametros);
        $("li.administracion").addClass("open");
        $("li.catalogos").addClass("active");
        $("li.examen").addClass("active");
        if("top"!=localStorage.getItem("sm-setmenu")){
            $("li.examen").parents("ul").slideDown(200);
        }
    });
</script>
</body>
</html>