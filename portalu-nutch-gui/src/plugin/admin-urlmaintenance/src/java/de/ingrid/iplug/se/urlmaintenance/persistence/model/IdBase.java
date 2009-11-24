package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class IdBase {

  @Id
//  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long _id;

  public Long getId() {
    return _id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IdBase other = (IdBase) obj;
    if (_id == null) {
      if (other._id != null)
        return false;
    } else if (!_id.equals(other._id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "id: " + _id;
  }

}
