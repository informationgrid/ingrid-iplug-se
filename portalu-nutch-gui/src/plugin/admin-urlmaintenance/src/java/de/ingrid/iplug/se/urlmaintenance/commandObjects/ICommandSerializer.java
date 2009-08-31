package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.IdBase;

public interface ICommandSerializer<T extends IdBase> {

  T write();

  void read(T t);
}
