<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /> - Zeitplanung</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/jquery.ptTimeSelect.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/blue.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery.easytabs.min.js"></script>
<script type="text/javascript" src="../js/jquery.ptTimeSelect.min.js"></script>
<script type="text/javascript" src="../js/icheck.min.js"></script>

<script type="text/javascript">
	$(document).ready(
		function() {
			$('#schedulingTabs').easytabs({
				animate : false
			});

			var currentTab = "${selectedTab}";
			if (currentTab) $('#schedulingTabs').easytabs('select', currentTab);
			
			$('.time').ptTimeSelect({
				containerWidth: '28em',
				hoursLabel:     'Stunde',
		        minutesLabel:   'Minute',
		        setButtonLabel: 'Übernehmen'
			});
			
			$('#weekDays input, #monthDays input').each(function(){
			    var self = $(this),
					label = self.next(),
				    label_text = label.text();

			    label.remove();
			    self.iCheck({
			      checkboxClass: 'icheckbox_line-blue',
			      insert: '<div class="icheck_line-icon"></div>' + label_text
			    });
			});
		}
	);

	function selectTab(id) {
		$('#tabsInstance').show();
	}
	
</script>

</head>
<body>
	
    <c:set var="activeTab" value="3" scope="request"/>
    <c:import url="includes/body.jsp"></c:import>
    
</body>
</html>

