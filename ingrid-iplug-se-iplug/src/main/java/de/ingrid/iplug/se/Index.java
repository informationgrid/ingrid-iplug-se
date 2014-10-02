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
