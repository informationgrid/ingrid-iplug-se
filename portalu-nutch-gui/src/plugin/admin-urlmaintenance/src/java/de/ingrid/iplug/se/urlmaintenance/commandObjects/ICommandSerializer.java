package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.IdBase;

public interface ICommandSerializer<T extends IdBase> {

  /**
   * Provides a new entity object that gets the information this ui command
   * represents. When this command contains a valid id, the method tries to read
   * the relevant entity from database an overwrites its information.
   * 
   * @return A entity that can be persists.
   */
  T write();

  /**
   * Reads information from entity entity into this command object.
   * 
   * @param entity
   *          The entity this command represent in ui.
   */
  void read(T entity);
}
