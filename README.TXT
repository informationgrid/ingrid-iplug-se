Run/Debug in Eclipse:
---------------------
a) Run without connection to ibus:
 Main class: org.apache.nutch.admin.AdministrationApplication
 Params:     /tmp/ingrid-iplug-se/nutchgui 8080
 VM-Params: -Xmx256m -Dnutch.gui.theme=portal-u
 
b) Connect to ibus and retrieve actual Partners/Providers:
 Main class: org.apache.nutch.admin.AdministrationApplication
 Params:     /tmp/ingrid-iplug-se/nutchgui 8080 --secure
 VM-Params:  -Xmx256m -Dnutch.gui.theme=portal-u -Djava.security.auth.login.config=portalu-nutch-gui/conf/nutchgui.auth
 (user/passwort: adminportal/adminportal)
 
TODO rwe:
---------
* In der Datei 'nutch' die Anwendnung wirder ohne Debug-Mode starten lassen 
* SNSQuery überarbeiten; die JUnit Tests laufen nicht

Deployment:
-----------
Alle Zugänge sind unter http://dev.wemove.com/confluence/pages/viewpage.action?pageId=360465 eingetragen.
* ssh tec@213.144.28.218
  cd temp
* ssh -p 882 ingrid@192.168.64.34
  cd /opt/ingrid/ingrid-iplug-se-dev/
  ...
  
  
