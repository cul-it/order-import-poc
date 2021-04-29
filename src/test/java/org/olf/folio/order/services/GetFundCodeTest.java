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

public class GetFundCodeTest extends ApiBaseTest { 
	

	public GetFundCodeTest() {
		// TODO Auto-generated constructor stub
	} 
	
	@Ignore
	public void nullTest() {
		//
	} 
	
	@Test
	public void testGetAllFundCode() {
		 
		
		String fundEndpoint = getBaseOkapEndpoint() + "finance/funds?limit=1";
		try {
			
			String fundResponse = getApiService().callApiGet(fundEndpoint, getToken());
			JSONObject fundsObject = new JSONObject(fundResponse);
			//System.out.println(fundsObject.toString(3));
			JSONArray fundsArray = fundsObject.getJSONArray("funds");
			assertNotNull(fundsArray);
			assertEquals(fundsArray.length(), 1);
			//for (int i=0; i < fundsArray.length(); i++) {
			//	JSONObject fundObj = (JSONObject) fundsArray.get(i);
			//	System.out.println("code: "+ fundObj.get("code"));
			//	System.out.println("name: "+ fundObj.get("name"));
			//	System.out.println("id: "+ fundObj.get("id"));
			//	System.out.println();
			//}
			String fundId = (String) fundsArray.getJSONObject(0).get("id");
			assertNotNull(fundId);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetFundCode() {
		 
		String fundCode = "1010";
		//String fundCode = "p2755";
		String fundEndpoint = getBaseOkapEndpoint() + "finance/funds?limit=3&offset=0&query=((code='" + fundCode + "'))";
		try { 
		   String fundResponse = getApiService().callApiGet(fundEndpoint, getToken());
		   JSONObject fundsObject = new JSONObject(fundResponse);
		   JSONArray fundsArray = fundsObject.getJSONArray("funds");
		   //System.out.println(fundsObject.toString(3));
		   String returnCode = (String) fundsArray.getJSONObject(0).get("code");
		   assertEquals(fundCode, returnCode);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	
}
