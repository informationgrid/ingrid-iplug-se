Run/Debug indexer:
---------------------
a) Run without connection to ibus (eclipse):
 Main class: org.apache.nutch.admin.AdministrationApplication
 Params:     /tmp/ingrid-iplug-se/nutchgui 8080
 VM-Params: -Xmx256m -Dnutch.gui.theme=portal-u
or 
 Main class: de.ingrid.iplug.se.AdministrationApp
 Params:     --plugdescription=portalu-nutch-gui/conf/plugdescription-index.xml_blah --descriptor=portalu-nutch-gui/conf/communication-index.xml --workingDirectory=/tmp/nutchGui_eclipse --secure --port=8080
 VM-Params: -Xmx256m -Dnutch.gui.theme=portal-u

b) Connect to ibus and retrieve actual Partners/Providers (eclipse):
 Main class: org.apache.nutch.admin.AdministrationApplication
 Params:     /tmp/ingrid-iplug-se/nutchgui 8080 --secure
 VM-Params:  -Xmx256m -Dnutch.gui.theme=portal-u -Djava.security.auth.login.config=portalu-nutch-gui/conf/nutchgui.auth
 (user/passwort: adminportal/adminportal)
 
c) run ./start-indexer.sh 
 
Run/Debug searcher:
---------------------
a) in eclipse:
 Main class: de.ingrid.iplug.PlugServer
 Params:     --plugdescription portalu-search/conf/plugdescription-search.xml --descriptor portalu-search/conf/communication-search.xml
 VM-Params: 
 
b) run ./start-indexer.sh 

TODO rwe:
---------
* optionale Libs zum Parse von PDF hinzufügen
* SNSQuery überarbeiten; die JUnit Tests laufen nicht alle
* Auf myles ist der Port 50061 (plugdescription-search.xml) schon belegt.


Deployment:
-----------
* Installations-Jar bauen durch: mvn assembly:assembly -Dmaven.test.skip
* Installatioin starten: java -jar ./ingrid-iplug-se-2.0.0-SNAPSHOT-installer.jar [text]
Alle Zugänge sind unter http://dev.wemove.com/confluence/pages/viewpage.action?pageId=360465 eingetragen.
* ssh tec@213.144.28.218
  scp -P882 /home/tec/temp/nutch-gui-0.3-dev.tar.gz ingrid@192.168.64.34:/opt/ingrid/ingrid-iplug-se-dev
* ssh -p 882 ingrid@192.168.64.34
  cd /opt/ingrid/ingrid-iplug-se-dev/nutch-gui-0.3-dev/
  ...

Test:
-----
* Eine Abfrage über harris: http://harrison.its-technidata.de/ingrid-portal/portal/main-search.psml?action=doSearch&q=http+objects+iplugs%3A%22%2Ftorwald-group%3Aiplug-se-search%22&ds=1&grouping=domain
  (http objects iplugs:"/torwald-group:iplug-se-search")  
  

Maven2 Krams:
-------------
Build the installer: mvn assembly:assembly -Dmaven.test.skip

skip tests: -Dmaven.test.skip