package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("EXCLUDE")
public class ExcludeUrl extends WebUrl {

}
