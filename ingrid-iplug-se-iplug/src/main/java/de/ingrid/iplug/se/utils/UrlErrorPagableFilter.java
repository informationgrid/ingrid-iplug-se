/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.utils;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

/**
 * Allows to retrieve a filtered, pagable resultset from an rather large JSON
 * file. Implements the {@link ContentHandler} interface. See
 * https://code.google.com/p/json-simple/wiki/DecodingExamples#Example_5_-_Stoppable_SAX-like_content_handler
 * 
 * @author jm
 *
 */
public class UrlErrorPagableFilter implements ContentHandler {

	int page = 0;
	int pageSize = 0;

	private int hitCounter = 0;

	JSONArray result = new JSONArray();

	JSONObject tmpObj = null;
	String tmpKey = null;
	Object tmpVal = null;
	String urlPattern = null;
	String[] statusFilter = null;

	/**
	 * Constructor.
	 * 
	 * @param page
	 *            Page of the result, starting with 0.
	 * @param pageSize
	 *            Number of results per page.
	 * @param urlPattern
	 * @param statusFilter
	 */
	public UrlErrorPagableFilter(int page, int pageSize, String urlPattern, String[] statusFilter) {
		this.page = page;
		this.pageSize = pageSize;
		this.urlPattern = urlPattern;
		this.statusFilter = statusFilter;
	}

	public JSONArray getResult() {
		return result;
	}

	public int getTotalResults() {
		return hitCounter;
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		return false;
	}

	@Override
	public void endJSON() throws ParseException, IOException {
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		boolean urlHit = false;
		boolean statusHit = false;
		if (tmpObj.containsKey("url") && tmpObj.containsKey("status")) {
			String url = tmpObj.get("url").toString();
			if (urlPattern.length() == 0 || url.contains(urlPattern)) {
				urlHit = true;
			}
			String status = tmpObj.get("status").toString();
			if (statusFilter == null || statusFilter.length == 0) {
				statusHit = true;
			} else {
				for (String s : statusFilter) {
					if (status.equals(s)) {
						statusHit = true;
						break;
					}
				}
			}

			if (urlHit && statusHit) {
				if (hitCounter >= page * pageSize && result.size() <= pageSize) {
					result.add(tmpObj);
				}
				hitCounter++;
			}
		}
		return true;
	}

	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		tmpObj.put(tmpKey, tmpVal);
		return true;
	}

	@Override
	public boolean primitive(Object value) throws ParseException, IOException {
		tmpVal = value;
		return true;
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		return true;
	}

	@Override
	public void startJSON() throws ParseException, IOException {
	}

	@Override
	public boolean startObject() throws ParseException, IOException {
		tmpObj = new JSONObject();
		return true;
	}

	@Override
	public boolean startObjectEntry(String key) throws ParseException, IOException {
		tmpKey = key;
		return true;
	}

}
