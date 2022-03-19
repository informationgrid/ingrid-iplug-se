/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import static org.junit.Assert.assertEquals;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class UrlErrorPagableFilterTest {

	@Test
	public void test() throws ParseException {

		String json = "["
		        + "{\"url\":\"http://poseidon.bafg.de/servlet/is/2884/\",\"status\":18,\"msg\":\"robots_denied\"},"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html1\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html2\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html3\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html4\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html5\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html6\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html7\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html8\",\"status\":14,\"msg\":\"notfound\"}"
		        + "{\"url\":\"http://www.boklim.de/boklimPublic/index.html9\",\"status\":14,\"msg\":\"notfound\"}"
		        + "]";

		JSONParser parser = new JSONParser();

		UrlErrorPagableFilter pager = new UrlErrorPagableFilter(0, 10, "", null);
		parser.parse(json, pager);
		assertEquals(10, pager.getTotalResults());
		assertEquals(10, pager.getResult().size());
		JSONObject obj = (JSONObject) pager.getResult().get(0);
		assertEquals("http://poseidon.bafg.de/servlet/is/2884/", obj.get("url").toString());

		pager = new UrlErrorPagableFilter(0, 10, "boklim", null);
		parser.parse(json, pager);
		assertEquals(9, pager.getTotalResults());
		assertEquals(9, pager.getResult().size());
		obj = (JSONObject) pager.getResult().get(0);
		assertEquals("http://www.boklim.de/boklimPublic/index.html1", obj.get("url").toString());

		pager = new UrlErrorPagableFilter(1, 5, "boklim", null);
		parser.parse(json, pager);
		assertEquals(9, pager.getTotalResults());
		assertEquals(4, pager.getResult().size());
		obj = (JSONObject) pager.getResult().get(0);
		assertEquals("http://www.boklim.de/boklimPublic/index.html6", obj.get("url").toString());

		pager = new UrlErrorPagableFilter(0, 5, "poseidon", null);
		parser.parse(json, pager);
		assertEquals(1, pager.getTotalResults());
		assertEquals(1, pager.getResult().size());
		obj = (JSONObject) pager.getResult().get(0);
		assertEquals("http://poseidon.bafg.de/servlet/is/2884/", obj.get("url").toString());

		pager = new UrlErrorPagableFilter(1, 5, "poseidon", null);
		parser.parse(json, pager);
		assertEquals(1, pager.getTotalResults());
		assertEquals(0, pager.getResult().size());
		
		pager = new UrlErrorPagableFilter(0, 5, "", new String[] {"18"});
		parser.parse(json, pager);
		assertEquals(1, pager.getTotalResults());
		assertEquals(1, pager.getResult().size());
		obj = (JSONObject) pager.getResult().get(0);
		assertEquals("http://poseidon.bafg.de/servlet/is/2884/", obj.get("url").toString());

		pager = new UrlErrorPagableFilter(0, 10, "boklim", new String[] {"18", "14"});
		parser.parse(json, pager);
		assertEquals(9, pager.getTotalResults());
		assertEquals(9, pager.getResult().size());
		obj = (JSONObject) pager.getResult().get(0);
		assertEquals("http://www.boklim.de/boklimPublic/index.html1", obj.get("url").toString());
		
		pager = new UrlErrorPagableFilter(0, 10, "", new String[] {"18", "14"});
		parser.parse(json, pager);
		assertEquals(10, pager.getTotalResults());
		assertEquals(10, pager.getResult().size());
		obj = (JSONObject) pager.getResult().get(0);
		assertEquals("http://poseidon.bafg.de/servlet/is/2884/", obj.get("url").toString());
		
	}

}
