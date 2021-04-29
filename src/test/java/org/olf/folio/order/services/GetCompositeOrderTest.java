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

public class GetCompositeOrderTest extends ApiBaseTest { 
	

	public GetCompositeOrderTest() { 
	} 
	
	@Ignore
	public void nullTest() { 
		//
	} 
	
	@Test
	public void testGetOrders() { 
	     
	    String result = new String();
		try {
			result = getApiService().callApiGet(getBaseOkapEndpoint() + "orders/composite-orders?limit=2", getToken());
			JSONObject resultsObj = new JSONObject(result);
			 
			//System.out.println(resultsObj.toString(3));
			 
			JSONArray jsonArray = resultsObj.getJSONArray("purchaseOrders"); 
			assertNotNull(jsonArray);
			assertEquals(jsonArray.length(), 2);
			//System.out.println("len: "+ jsonArray.length());
			 
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	@Test
	public void testGetOrder() { 
	     
	    String result = new String();
	    String poNumber = "11087";
		try {
			result = getApiService().callApiGet(getBaseOkapEndpoint() + "orders/composite-orders?limit=30&offset=0&query=((poNumber=" + poNumber + "))", getToken());
			// result = service.callApiGet(getBaseOkapEndpoint() + "orders/composite-orders/"+ orderId, getToken());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	    JSONObject resultObj = new JSONObject(result);
	    assertNotNull(resultObj);
	    int totalRecords = (Integer) resultObj.get("totalRecords");
	    assertNotNull(totalRecords);
	    assertEquals(totalRecords, 1);
	    //System.out.println(resultObj.toString(3));
	}
	
	
}
