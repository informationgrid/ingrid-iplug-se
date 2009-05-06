package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "URL")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING, length = 20)
public class Url extends IdBase {

  private String _url;

  // @OneToMany
  // @JoinColumn(name = "parentUrl_fk")
  // private List<Url> _childUrls = new ArrayList<Url>();

  public String getUrl() {
    return _url;
  }

  public void setUrl(String url) {
    _url = url;
  }

  // public List<Url> getChildUrls() {
  // return _childUrls;
  // }
  //
  // public void setChildUrls(List<Url> childUrls) {
  // _childUrls = childUrls;
  // }

}
