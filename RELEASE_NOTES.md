Release Notes — 2026-03-20

Kurz:
- Fehlende Test-Assets wiederhergestellt: assets/config/config1.json und assets/config/seedConfigTemplate.json (kopiert aus assets_test).
- Bugfix: JsonMapFacade.buildInstanceFromMap() — Map-basierte JsonObjects werden nun mit einer JsonMap-Klasse umwickelt, sodass buildInstance() eine nutzbare Instanz zurückliefert und NullPointer vermieden werden.
- Tests & Build: Lokale Ausführung: mvn package → BUILD SUCCESS. Alle Unit-Tests lokal erfolgreich.

Commit: e311d70 ("Restore test assets and fix JsonMapFacade to allow buildInstance from Map")

Hinweis: Änderungen wurden committet und gepusht ins aktuelle Remote-Branch.