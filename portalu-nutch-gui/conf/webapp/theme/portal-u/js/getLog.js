
function getLog(lineCount) {
	var http_request = false;
	var mode = document.getElementById('mode').value;
	if(mode == 'start'){
		var url = 'log.html?file=hadoop.log&lines=' +lineCount +'&ts=' +new Date().getTime();
	    if (window.XMLHttpRequest) { // Mozilla, Safari,...
	        http_request = new XMLHttpRequest();
	        if (http_request.overrideMimeType) {
	            http_request.overrideMimeType('text/html');
	        }
	    } else if (window.ActiveXObject) { // IE
	        try {
	            http_request = new ActiveXObject("Msxml2.XMLHTTP");
	        } catch (e) {
	            try {
	                http_request = new ActiveXObject("Microsoft.XMLHTTP");
	            } catch (e) {}
	        }
	    }
	
	    if (!http_request) {
	        return false;
	    }
	    
	    http_request.onreadystatechange = function(){
		    if (http_request.readyState == 4 && http_request.status == 200) {
	        	var logBox = document.getElementById('logFileContainer');
	        	logBox.innerHTML = '<pre>' +http_request.responseText +'</pre>';
	        	logBox.scrollTop = logBox.scrollHeight;
        		setTimeout(getLog, 500, lineCount);
		    }
	    };
	    
	    http_request.open('GET', url, true);
	    http_request.send(null);
	}
	
}