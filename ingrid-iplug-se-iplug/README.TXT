InGrid iPlug-SE
===============

Bei Problemen:

Die Suche bricht mit einer ClassCastException ab
------------------------------------------------
Hier kann es sich um einen defekten Index handeln. Bei der Score-Manipulation wird das boost-Feld
für die Berechnung des Scores mit verwendet. Dieser muss numerisch sein! Es kam vor, dass bei einem
defekten Index, dieses Feld keinen numerischen Inhalt mehr enthielt. Vermutlich liegt dies an einem
inkonsistenten Zustand einer Status-Datei.

Lösung:
-------
- löschen des Verzeichnis "_state" im Index-Verzeichnis "iplugse"
- Neustarten des iPlugs
- Crawl neu starten für Indexgenerierung