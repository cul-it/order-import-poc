package org.olf.folio.order.services;
 
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore; 
import org.junit.jupiter.api.Test; 

public class GetFiscalYearTest extends ApiBaseTest { 
	

	public GetFiscalYearTest() {
		// TODO Auto-generated constructor stub
	} 
	
	@Ignore
	public void nullTest() {
		//
	} 
	
	@Test
	public void testGetAllFiscalYeer() {
		 
		String currentDateTime = "2021-03-25T00:00:00Z";
		String endpoint = getBaseOkapEndpoint() + "finance/fiscal-years?limit=1000";
		//System.out.println("endpoint: "+ endpoint);
		try {
			
			String fyResponse = getApiService().callApiGet(endpoint, getToken());
			JSONObject fyObject = new JSONObject(fyResponse);
			//System.out.println(fyObject.toString(3));
			 
			JSONArray jsonArray = fyObject.getJSONArray("fiscalYears");
			assertNotNull(jsonArray);
			assertTrue(jsonArray.length() > 0);
			String name = (String) jsonArray.getJSONObject(0).get("name");
			assertTrue(name.length() > 0);
			//for (int i=0; i < fyArray.length(); i++) {
			//	JSONObject fyObj = (JSONObject) fyArray.get(i); 
			//	
			//	System.out.println("name: "+ fyObj.get("name"));
			//	System.out.println("startDate: "+ fyObj.get("periodStart"));
			//	System.out.println("endDate: "+ fyObj.get("periodEnd"));
			//	System.out.println();
			//} 
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	
}
