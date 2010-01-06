Run/Debug indexer:
---------------------
a) Run without connection to ibus (eclipse):
 Main class: de.ingrid.iplug.se.AdministrationApp
 Params:     --plugdescription=portalu-nutch-gui/conf/plugdescription-index.xml_blah --descriptor=portalu-nutch-gui/conf/communication-index.xml --workingDirectory=/tmp/nutchGui_eclipse --port=8080
 VM-Params:  -Xmx256m -Dnutch.gui.theme=portal-u

b) Connect to ibus and retrieve actual Partners/Providers (eclipse):
 Main class: de.ingrid.iplug.se.AdministrationApp
 Params:     --plugdescription=portalu-nutch-gui/conf/plugdescription-index.xml_blah --descriptor=portalu-nutch-gui/conf/communication-index.xml --workingDirectory=/tmp/nutchGui_eclipse --secure --port=8080
 VM-Params:  -Xmx256m -Dnutch.gui.theme=portal-u -Djava.security.auth.login.config=portalu-nutch-gui/conf/nutchgui.auth
 (user/passwort: adminportal/adminportal)
 
c) Start from distribution installation:
 run ./start-indexer.sh 
 
Run/Debug searcher:
---------------------
a) in eclipse:
 Main class: de.ingrid.iplug.PlugServer
 Params:     --plugdescription portalu-search/conf/plugdescription-search.xml --descriptor portalu-search/conf/communication-search.xml
 VM-Params: 
 
b) run ./start-indexer.sh 

TODO rwe:
---------
* Installation des Searchers erstellen und ggf. Konfigurationstool anpassen, sofern Max das nicht komplett macht (Max hat was vorbereitet: admin-searcher.sh-Skript)
* Versionsnummer zum Iplug-SE hinzufügen (neue Maven-Konfiguration im Projekt dazu anpassen) 
* Management der Segmente, siehe Mail 'Management der Segmente' vom 04.01. und 05.01. von Max und Joachim (?)
* Report über Status der gefetchten URLs. (Hier muss ich noch mit Martin/Joachim klären, ob auch Mailversand notwendig ist und ob Status in DB gespeichert werden soll)
* Bug: Mängelliste aus altem iPlug-SE prüfen, ob Fehler in neuer Version gelöst wurden (siehe Dokument 'nutch-dev-kommentare..' in http://88.198.11.89/jira/browse/INGRID-1777)
* Bug: Sprache kann bei Katalog-Urls nicht mehr eingestellt werden, siehe http://88.198.11.89/jira/browse/INGRID-1793
* Erweiterung: Neues Indexfeld 'Location' über SNS-Service, siehe http://88.198.11.89/jira/browse/INGRID21-56
* optionale Libs zum Parse von PDF hinzufügen/Testen, hier Marko fragen, ob notwendig (?)
* SNSQuery überarbeiten; die JUnit Tests laufen nicht alle (?)


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
