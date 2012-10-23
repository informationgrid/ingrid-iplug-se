Hinweis zu den Patch-Files:
---------------------------

Die Patches funktionieren leider nicht gegen�ber der contrib-Version, so wie es urspr�nglich gedacht war.
Das Problem bei der Erstellung war, dass eclipse automatisch die Imports in sehr vielen java-Dateien ge�ndert hat,
nachdem das nutch-Release aus dem contrib-Ordner entpackt wurde. Dadurch ergaben sich sehr viele �nderungen
in den Dateien, die jedoch keine wirklich �nderung in der Funktionalit�t des Codes bedeuteten.
 
Die im Ordner 'nutch_relevant' sowie die in diesem Ordner flach liegenden patch-Dateien geben jedoch einen
�berblick, was sich funktionell geben�ber dem nutch-gui Release ge�ndert hat. Diese patch-Dateien funktionieren
aber nicht mehr.

Mit dem target 'create_full_nutch_patch' kann allerdings eine Patch-Datei erzeugt werden, die wirklich alle �n-
derungen gegen�ber dem OS nutch-gui Release enth�lt. (Dazu wird das nutch-gui-Zip entpackt und mit dem aktuellen 
Inhalt aus dem 101tec..-Order verglichen.)

Hinweis:
Bevor also ein neues Release der OS nutch-gui integriert wird, sollte einmal das target 
'create_full_nutch_patch' in der build.xml aufgerufen werden, damit fest steht, was sich in dem 
ingrid-iplug-se/101tec.. Ordner wirklich gegen�ber der OS version ge�ndert hat.
