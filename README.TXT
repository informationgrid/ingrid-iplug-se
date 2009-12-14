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
 
 
TODO rwe:
---------
* In der Datei 'nutch' die Anwendnung wirder ohne Debug-Mode starten lassen 
