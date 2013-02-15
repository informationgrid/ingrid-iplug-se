Duplikat Analyse (15.02.2013)
-----------------------------

# export aller URLs aus der CrawlDB
bin/nutch-se.sh readdb nutch-instances/web/crawls/<MY_CRAWL>/crawldb -dump <ANALYSIS_FOLDER>/dump -format csv
# alle gefetchten URLs filtern
grep db_fetched part-00000 > fetched_urls.txt
# alle URLs nach der Signatur sortieren
sort -t\; -k10 fetched_urls > fetched_urls_sorted.txt
# alle Zeilen mit gleicher Signatur ausgeben (inkl.  der ersten)
cat fetched_urls_sorted.txt | awk '{if (x[$10]) { x_count[$10]++; print $0; if (x_count[$10] == 1) { print x[$10] } } x[$10] = $0}' FS=";" > result.txt


Run/Debug indexer:
---------------------
a) Run without connection to ibus (eclipse):
 Main class: de.ingrid.iplug.se.AdministrationApp
 Params:     --plugdescription=portalu-nutch-gui/conf/plugdescription-index.xml --descriptor=portalu-nutch-gui/conf/communication-index.xml --workingDirectory=/tmp/nutchGui_eclipse --port=8080
 VM-Params:  -Xmx256m -Dnutch.gui.theme=portal-u

b) Connect to ibus and retrieve actual Partners/Providers (eclipse):
 Main class: de.ingrid.iplug.se.AdministrationApp
 Params:     --plugdescription=portalu-nutch-gui/conf/plugdescription-index.xml --descriptor=portalu-nutch-gui/conf/communication-index.xml --workingDirectory=/tmp/nutchGui --secure --port=8080
 VM-Params:  -Xmx256m -Dnutch.gui.theme=portal-u -Djava.security.auth.login.config=portalu-nutch-gui/conf/nutchgui.auth
 (user/passwort: adminportal/adminportal)
 
c) Start from distribution installation:
 run ./start-index.sh 
 
Run/Debug searcher:
---------------------
a) in eclipse:
 Main class: de.ingrid.iplug.PlugServer
 Params:     --plugdescription portalu-search/conf/plugdescription-search.xml --descriptor portalu-search/conf/communication-search.xml
 VM-Params:  -Dlog4j.rootLogger=INFO,DRFA,stdout
 
b) run ./start-search.sh 

TODO rwe:
---------
* persistence.xml muss noch in der Installation ber�cksichtigt werden!
* Versionsnummer zum Iplug-SE hinzuf�gen (Manifest-Datei) 
* Management der Segmente, siehe Mail 'Management der Segmente' vom 04.01. und 05.01. von Max und Joachim (?)
* Report �ber Status der gefetchten URLs. (Hier muss ich noch mit Martin/Joachim kl�ren, ob auch Mailversand notwendig ist und ob Status in DB gespeichert werden soll)
* optionale Libs zum Parse von PDF hinzuf�gen/Testen, hier Marko fragen, ob notwendig (?)
* Verzeichnis core/ sollte gel�scht werden, Marko fragen, warum dies �berhaupt existiert


Deployment:
-----------
* Installations-Jar bauen durch: mvn assembly:assembly -Dmaven.test.skip
* Installatioin starten: java -jar ./ingrid-iplug-se-2.0.0-SNAPSHOT-installer.jar [text]
Alle Zug�nge sind unter http://dev.wemove.com/confluence/pages/viewpage.action?pageId=360465 eingetragen.
* ssh tec@213.144.28.218
  scp -P882 /home/tec/temp/nutch-gui-0.3-dev.tar.gz ingrid@192.168.64.34:/opt/ingrid/ingrid-iplug-se-dev
* ssh -p 882 ingrid@192.168.64.34
  cd /opt/ingrid/ingrid-iplug-se-dev/nutch-gui-0.3-dev/
  ...

Test:
-----
* Eine Abfrage �ber harris: http://harrison.its-technidata.de/ingrid-portal/portal/main-search.psml?action=doSearch&q=http+objects+iplugs%3A%22%2Ftorwald-group%3Aiplug-se-search%22&ds=1&grouping=domain
  (http objects iplugs:"/torwald-group:iplug-se-search")  
  

Maven2 Krams:
-------------
Build the installer: mvn assembly:assembly -Dmaven.test.skip
