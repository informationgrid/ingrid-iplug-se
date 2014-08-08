package de.ingrid.iplug.se;

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
	 * @return
	 */
	public IngridHitDetail getDetail(IngridHit hit);
	
	
	/**
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
