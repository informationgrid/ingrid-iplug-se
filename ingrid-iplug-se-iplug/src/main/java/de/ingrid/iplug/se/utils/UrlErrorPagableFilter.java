package de.ingrid.iplug.se.utils;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

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
				urlHit=true;
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
				if (hitCounter >= (page - 1) * pageSize && result.size() <= pageSize) {
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