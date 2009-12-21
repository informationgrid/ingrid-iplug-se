Run/Debug indexer:
---------------------
a) Run without connection to ibus (eclipse):
 Main class: org.apache.nutch.admin.AdministrationApplication
 Params:     /tmp/ingrid-iplug-se/nutchgui 8080
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
* In der Datei 'nutch' die Anwendnung wieder ohne Debug-Mode starten lassen 
* SNSQuery überarbeiten; die JUnit Tests laufen nicht alle
* Auf myles ist der Port 50061 (plugdescription-search.xml) schon belegt.

Fragen:
-------
* SearchRunnable.run() ruft das _searchBean.serach() immer mit einem Parameter _sortField="date" auf. Ein paar Aufrufe 
  später wird manchmal ein org.apache.lucene.search.Searcher.search() mit einer leeren query aber mit einem sortField="date"
  aufgerufen, was zu einer exception führt. Kann man sich denn nicht Aufrufe mit leerer Query generell sparen?
  Callstack:  
  java.lang.RuntimeException: field "date" does not appear to be indexed
	at org.apache.lucene.search.ExtendedFieldCacheImpl$5.createValue(ExtendedFieldCacheImpl.java:173)
	at org.apache.lucene.search.FieldCacheImpl$Cache.get(FieldCacheImpl.java:71)
	at org.apache.lucene.search.ExtendedFieldCacheImpl.getAuto(ExtendedFieldCacheImpl.java:127)
	at org.apache.lucene.search.FieldSortedHitQueue.comparatorAuto(FieldSortedHitQueue.java:487)
	at org.apache.lucene.search.FieldSortedHitQueue$1.createValue(FieldSortedHitQueue.java:184)
	at org.apache.lucene.search.FieldCacheImpl$Cache.get(FieldCacheImpl.java:71)
	at org.apache.lucene.search.FieldSortedHitQueue.getCachedComparator(FieldSortedHitQueue.java:167)
	at org.apache.lucene.search.FieldSortedHitQueue.<init>(FieldSortedHitQueue.java:55)
	at org.apache.lucene.search.TopFieldDocCollector.<init>(TopFieldDocCollector.java:43)
	at org.apache.lucene.search.IndexSearcher.search(IndexSearcher.java:121)
	at org.apache.lucene.search.Searcher.search(Searcher.java:86)
	at org.apache.nutch.searcher.LuceneQueryOptimizer.optimize(LuceneQueryOptimizer.java:281)
	at org.apache.nutch.searcher.IndexSearcher.search(IndexSearcher.java:99)
	at org.apache.nutch.searcher.LuceneSearchBean.search(LuceneSearchBean.java:84)
	at org.apache.nutch.searcher.NutchBean.search(NutchBean.java:172)
	at org.apache.nutch.admin.searcher.SearchRunnable.run(SearchRunnable.java:60)
	at org.apache.nutch.admin.searcher.ThreadPool$PooledThread.run(ThreadPool.java:36)
	
	Ursprung ist NutchSearcher:120:
	if (IngridQuery.DATE_RANKED.equalsIgnoreCase(query.getRankingType())) {
      hits = _searcher.search(nutchQuery, start + length, null, "date", true); <--- autsch, wenn nutchQuery leer ist!
    } else {
      hits = _searcher.search(nutchQuery, start + length, "site", null, false);
    }
	 

Deployment:
-----------
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
  
