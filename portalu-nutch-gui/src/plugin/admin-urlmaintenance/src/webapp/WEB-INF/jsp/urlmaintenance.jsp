<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="includes/include.jsp" %>
<html>
<head>
<title>Admin URL Pflege - Welcome</title>
	<link rel="stylesheet" type="text/css" href="<%=rootPath%>/theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=rootPath%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="<%=rootPath%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=rootPath%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=rootPath%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=rootPath%>/theme/${theme}/css/style.css" />
	<script type="text/javascript" src="<%=rootPath%>/theme/${theme}/js/jquery-1.3.2.min.js"></script>
	<script type="text/javascript">
		var map = ${jsonMap};
	
	    $(document).ready(function() {
	        $("#partner").change(function() {
	        	var select = $("#provider");
	        	select.find("option[value]").remove();
	            var partner = $(this).val();
	            // at first call it's normally empty / not selected
	            if (map[partner]) {
	                for(var i = 0; i < map[partner].length; i++) {
	                    select.append("<option value='" + map[partner][i].id + "'>" + map[partner][i].name + "</option>");
	                }
	            }
	        }).trigger('change');
	        <%/*$("#provider").val("${plugDescription.organisationAbbr}");*/%>
	    });
	</script> 
</head>
<body class="yui-skin-sam">
	<div id="doc2" class="yui-t4">
		<div id="hd">
			<%@ include file="includes/header.jsp" %>
		</div>
		
		<%@ include file="includes/menu.jsp" %>

		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3>URL Pflege</h3>
					
					<c:if test="${!empty error}">
		                <div class="error">
		                    <b>Fehler:</b>
					        <c:choose>
					            <c:when test="${error == 'session'}">
	                               Ihre Sitzung ist abgelaufen.<br />
	                               Hierdurch sind ungespeicherte Daten eventuell verloren gegangen.<br />
	                               Bitte versuchen Sie es erneut.</p>
	                            </c:when>
						        <c:when test="${error == 'lost'}">
                                   Es fehlen notwendige Informationen.<br />
                                   Hierdurch sind ungespeicherte Daten eventuell verloren gegangen.<br />
                                   Bitte versuchen Sie es erneut.</p>
                                </c:when>
						    </c:choose>
                        </div>
					</c:if>
					
					<form:form action="./index.html" commandName="partnerProviderCommand" method="post">
						<fieldset>
						    <legend>Partner und Anbieter ausw&auml;hlen</legend>
						    
						    <row>
						        <label><form:label path="partner" >Partner: </form:label></label>
						        <field>
						           	<form:select path="partner" items="${partners}" itemLabel="name"/>
						        </field>
						        <desc></desc>
						    </row>
						    
						    <row>
						        <label><form:label path="provider" >Anbieter: </form:label></label>
						        <field>
						           <form:select path="provider" items="${providers}" itemLabel="name"/>
						        </field>
						        <desc></desc>
						    </row>
						    
						     <row>
						        <label>&nbsp;</label>
						        <field>
						            <input type="submit" value="Weiter"/>
						        </field>
						    </row>
						    
						</fieldset>
					</form:form>
				</div>
			</div>
			<div class="yui-b">
				
			</div>
		</div>
		<div id="ft">
			<%@ include file="includes/footer.jsp" %>
		</div>
	</div>
</body>
</html>