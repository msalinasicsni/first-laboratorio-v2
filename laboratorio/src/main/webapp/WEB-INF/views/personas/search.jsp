<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<!-- BEGIN HEAD -->
<head>
	<jsp:include page="../fragments/headTag.jsp" />
</head>
<!-- END HEAD -->
<!-- BEGIN BODY -->
<body class="">
	<!-- #HEADER -->
	<jsp:include page="../fragments/bodyHeader.jsp" />
	<!-- #NAVIGATION -->
	<jsp:include page="../fragments/bodyNavigation.jsp" />
    <spring:url value="/personas/update" var="sActionUrl"/>
    <spring:url value="/tomaMx/noticesrut" var="sActionNotiPacienteUrl"/>
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
				<li><a href="<spring:url value="/" htmlEscape="true "/>"><spring:message code="menu.home" /></a> <i class="fa fa-angle-right"></i> <a href="<spring:url value="/personas/search" htmlEscape="true "/>"><spring:message code="menu.persons" /></a></li>
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
						<i class="fa-fw fa fa-group"></i> 
							<spring:message code="persons" />
						<span> <i class="fa fa-angle-right"></i>  
							<spring:message code="person.search" />
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
                                <input id="smallBox_content" type="hidden" value="<spring:message code="smallBox.content.4s"/>"/>
                                <input id="msg_no_results_found" type="hidden" value="<spring:message code="msg.no.results.found"/>"/>
                                <input id="text_opt_select" type="hidden" value="<spring:message code="lbl.select"/>"/>
								<!-- widget edit box -->
								<div class="jarviswidget-editbox">
									<!-- This area used as dropdown edit box -->
									<input class="form-control" type="text">	
								</div>
								<!-- end widget edit box -->
								<!-- widget content -->
								<div class="widget-body no-padding">
                                    <input id="blockUI_message" type="hidden" value="<spring:message code="blockUI.message"/>"/>
									<form id="search-form" class="smart-form" autocomplete="off">
										<fieldset>
                                            <!-- START ROW -->
                                            <div class="row">
                                                <section class="col col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                                    <div class="inline-group">
                                                        <section class="col col-xs-6 col-sm-6 col-md-6 col-lg-6">
                                                            <label class="radio">
                                                                <input type="radio" name="rbTipoBusqueda" value="NOMBRE" id="NOMBRE" checked="checked">
                                                                <i></i><spring:message code="lbl.filter.by"/> <spring:message code="lbl.receipt.person.name"/></label>
                                                        </section>
                                                        <section class="col col-xs-6 col-sm-6 col-md-6 col-lg-6">
                                                            <label class="radio">
                                                                <input type="radio" name="rbTipoBusqueda" id="IDENT" value="IDENT">
                                                                <i></i><spring:message code="lbl.filter.by"/> <spring:message code="person.num.identificacion"/></label>
                                                        </section>
                                                    </div>
                                                </section>
                                            </div>
                                            <!-- END ROW -->
                                            <div class="row" id="filtroNombre">
                                                <section class="col col-sm-6 col-md-3 col-lg-3">
                                                    <label class="text-left txt-color-blue font-md">
                                                        <i class="fa fa-fw fa-asterisk txt-color-red font-sm"></i>
                                                        <spring:message code="person.name1"/>
                                                    </label>
                                                    <div class="">
                                                        <!--<span class="input-group-addon"><i class="fa fa-pencil fa-fw"></i></span>-->
                                                        <label class="input">
                                                            <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-sort-alpha-asc fa-fw"></i>
                                                            <input class="form-control" type="text" id="primerNombre" name="primerNombre" value="" placeholder=" <spring:message code="person.name1" />">
                                                            <b class="tooltip tooltip-bottom-right"> <i class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.nombre1"/>
                                                            </b>
                                                        </label>
                                                        <!--<span class="input-group-addon"><i class="fa fa-sort-alpha-asc fa-fw"></i></span>-->
                                                    </div>
                                                </section>
                                                <section class="col col-sm-6 col-md-3 col-lg-3">
                                                    <label class="text-left txt-color-blue font-md">
                                                        <spring:message code="person.name2"/>
                                                    </label>
                                                    <div class="">
                                                        <!--<span class="input-group-addon"><i class="fa fa-pencil fa-fw"></i></span>-->
                                                        <label class="input">
                                                            <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-sort-alpha-asc fa-fw"></i>
                                                            <input class="form-control" type="text" name="segundoNombre" id="segundoNombre" value="" placeholder=" <spring:message code="person.name2" />" />
                                                            <b class="tooltip tooltip-bottom-right"> <i
                                                                    class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.nombre2"/>
                                                            </b>
                                                            <!--<span class="input-group-addon"><i class="fa fa-sort-alpha-asc fa-fw"></i></span>-->
                                                        </label>
                                                    </div>
                                                </section>
                                                <section class="col col-sm-6 col-md-3 col-lg-3">
                                                    <label class="text-left txt-color-blue font-md">
                                                        <i class="fa fa-fw fa-asterisk txt-color-red font-sm"></i>
                                                        <spring:message code="person.lastname1"/>
                                                    </label>
                                                    <div class="">
                                                        <!--<span class="input-group-addon"><i class="fa fa-pencil fa-fw"></i></span>-->
                                                        <label class="input">
                                                            <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-sort-alpha-asc fa-fw"></i>
                                                            <input class="form-control" type="text" name="primerApellido" id="primerApellido" value="" placeholder=" <spring:message code="person.lastname1" />" />
                                                            <b class="tooltip tooltip-bottom-right"> <i
                                                                    class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.apellido1"/>
                                                            </b>
                                                        </label>
                                                        <!--<span class="input-group-addon"><i class="fa fa-sort-alpha-asc fa-fw"></i></span>-->
                                                    </div>
                                                </section>
                                                <section class="col col-sm-6 col-md-3 col-lg-3">
                                                    <label class="text-left txt-color-blue font-md">
                                                        <spring:message code="person.lastname2"/>
                                                    </label>
                                                    <div class="">
                                                        <label class="input">
                                                            <!--<span class="input-group-addon"><i class="fa fa-pencil fa-fw"></i></span>-->
                                                            <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-sort-alpha-asc fa-fw"></i>
                                                            <input class="form-control" type="text" name="segundoApellido" id="segundoApellido" value="" placeholder=" <spring:message code="person.lastname2" />"/>
                                                            <b class="tooltip tooltip-bottom-right"> <i
                                                                    class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.apellido2"/>
                                                            </b>
                                                            <!--<span class="input-group-addon"><i class="fa fa-sort-alpha-asc fa-fw"></i></span>-->
                                                        </label>
                                                    </div>
                                                </section>
                                            </div>
                                            <div class="row" id="filtroIdentificacion">
                                                <section class="col col-sm-6 col-md-3 col-lg-3">
                                                    <label class="text-left txt-color-blue font-md hidden-xs">
                                                        <i class="fa fa-fw fa-asterisk txt-color-red font-sm"></i>
                                                        <spring:message code="person.num.identificacion" />
                                                    </label>
                                                    <div class="">
                                                        <label class="input">
                                                            <i class="icon-prepend fa fa-pencil fa-fw"></i><i class="icon-append fa fa-sort-alpha-asc fa-fw"></i>
                                                            <input class="form-control" type="text" name="numIdentificacion" id="numIdentificacion" value="${persona.identNumero}"
                                                                   placeholder=" <spring:message code="person.num.identificacion" />"/>
                                                            <b class="tooltip tooltip-bottom-right"> <i
                                                                    class="fa fa-warning txt-color-pink"></i> <spring:message code="tooltip.numIdent"/>
                                                            </b>
                                                        </label>
                                                    </div>
                                                </section>
                                            </div>
										</fieldset>
										<footer>
                                            <button type="button" id="create-person" class="btn btn-info"><i class="fa fa-file"></i> <spring:message code="act.add" /></button>
											<button type="submit" id="search-person" class="btn btn-info"><i class="fa fa-search"></i> <spring:message code="act.search" /></button>
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
									<table id="persons_result" class="table table-striped table-bordered table-hover" width="100%">
										<thead>			                
											<tr>
												<th data-class="expand"><i class="fa fa-fw fa-key text-muted hidden-md hidden-sm hidden-xs"></i> <spring:message code="person.id"/></th>
												<th data-hide="phone"><i class="fa fa-fw fa-user text-muted hidden-md hidden-sm hidden-xs"></i> <spring:message code="person.name1"/></th>
												<th data-hide="phone,tablet"><i class="fa fa-fw fa-user text-muted hidden-md hidden-sm hidden-xs"></i> <spring:message code="person.name2"/></th>
												<th data-hide="phone"><i class="fa fa-fw fa-user text-muted hidden-md hidden-sm hidden-xs"></i> <spring:message code="person.lastname1"/></th>
												<th data-hide="phone,tablet"><i class="fa fa-fw fa-user text-muted hidden-md hidden-sm hidden-xs"></i> <spring:message code="person.lastname2"/></th>
												<th data-hide="phone"><i class="fa fa-fw fa-calendar txt-color-blue hidden-md hidden-sm hidden-xs"></i> <spring:message code="person.fecnac"/></th>
                                                <th data-hide="phone"><i class="fa fa-fw fa-calendar txt-color-blue hidden-md hidden-sm hidden-xs"></i> <spring:message code="lbl.age2"/></th>
												<th data-hide="phone,tablet"><i class="fa fa-fw fa-map-marker text-muted hidden-md hidden-sm hidden-xs"></i> <spring:message code="person.mun.res"/></th>
												<th></th>
                                                <th></th>
											</tr>
										</thead>
                                        <c:if test="${not empty persona}">
                                            <tbody>
                                            <tr>
                                                <td>${persona.identNumero}</td>
                                                <td>${persona.primerNombre}</td>
                                                <td>${persona.segundoNombre}</td>
                                                <td>${persona.primerApellido}</td>
                                                <td>${persona.segundoApellido}</td>
                                                <td>${persona.fechaNacimiento}</td>
                                                <td>${edad}</td>
                                                <td>${persona.muniResiNombre}</td>
                                                <td><a href="${sActionUrl}/${persona.personaId}" class="btn btn-success btn-xs"><i class="fa fa-edit"></i></a></td>
                                                <td><a href="${sActionNotiPacienteUrl}/${persona.personaId}" class="btn btn-primary btn-xs"><i class="fa fa-list"></i></a></td>
                                            </tr>
                                            </tbody>
                                        </c:if>
									</table>
                                    <form id="paginate-form" class="smart-form" autocomplete="off">
                                        <footer>
                                            <label class="text-left font-md" id="paginacionLbl" style="color: #3174c7">
                                            </label>
                                            <button type="button" title="<spring:message code="lbl.next"/>" id="next" class="btn btn-primary active"><i class="fa fa-fast-forward"></i></button>
                                            <button type="button" title="<spring:message code="lbl.prev"/>" id="prev" class="btn btn-primary active"><i class="fa fa-fast-backward"></i></button>
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
	<spring:url value="/resources/scripts/personas/person-search.js" var="personSearch" />
	<script src="${personSearch}"></script>
    <spring:url value="/resources/scripts/utilidades/calcularEdad.js" var="edadJS" />
    <script src="${edadJS}"></script>
	<!-- END PAGE LEVEL SCRIPTS -->
	<spring:url value="/personas/persons" var="sPersonUrl"/>
    <spring:url value="/personas/create" var="sCreatePersonUrl"/>
	<script type="text/javascript">
		$(document).ready(function() {
			pageSetUp();
			var parametros = {sPersonUrl: "${sPersonUrl}", sActionUrl: "${sActionUrl}", sActionNotiPacienteUrl : "${sActionNotiPacienteUrl}",
                sPersonaByIdentificacionUrl : "${personaByIdentificacion}",
                sPersonaByNombresUrl : "${personaByNombres}",
                sCreatePersonUrl: "${sCreatePersonUrl}", blockMess : $("#blockUI_message").val()};
			SearchPerson.init(parametros);
	    	$("li.mantenimiento").addClass("open");
	    	$("li.personas").addClass("active");
	    	if("top"!=localStorage.getItem("sm-setmenu")){
	    		$("li.personas").parents("ul").slideDown(200);
	    	}




		});
	</script>
	<!-- END JAVASCRIPTS -->
</body>
<!-- END BODY -->
</html>