/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se;

import java.util.List;

import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

public interface Index {

	/**
	 * 
	 * @param query
	 * @param startHit
	 * @param num
	 * @return
	 */
	public IngridHits search(IngridQuery query, int startHit, int num);
	
	
	/**
	 * 
	 * @param hit
     * @param query
	 * @param requestedFields 
	 * @return
	 */
	public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] requestedFields);
	
	
	/**
	 * TODO: Move to another interface!
	 * 
	 * @param url
	 * @return
	 */
	public boolean deleteUrl(String url);


	/**
	 * 
	 */
	public void close();

}
