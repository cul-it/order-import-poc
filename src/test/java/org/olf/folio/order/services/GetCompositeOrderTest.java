package org.olf.folio.order.services;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GetCompositeOrderTest extends ApiBaseTest { 
	
    boolean debug = false;
    String harrassUUID = "5cf94601-26cf-17af-be30-34742fff402f";
    String amazonUUID = "3660cf19-837f-13ce-a300-642a2fff402f";

	public GetCompositeOrderTest() { 
	} 
	
	@Ignore
	public void nullTest() { 
		//
	} 
	
	@Test
	public void testGetOrders() { 
	    String userId = "8720f87c-1918-4658-b081-04f165689627"; 
	    String result = new String();
	    //String endpoint = getBaseOkapEndpoint() + "orders/composite-orders?limit=3&query=((createdByUserId=" + userId + "))";
	    String endpoint = getBaseOkapEndpoint() + "orders/composite-orders?limit=10";
		try {
			result = getApiService().callApiGet(endpoint, getToken());
			JSONObject resultsObj = new JSONObject(result);
			if (debug) {
			    System.out.println(resultsObj.toString(3));
			} else {
			    JSONArray jsonArray = resultsObj.getJSONArray("purchaseOrders"); 
			    assertNotNull(jsonArray);
			    assertTrue(jsonArray.length() > 0);
			    //System.out.println("len: "+ jsonArray.length());
			}
			 
		} catch (Exception e) {
			fail(e.getMessage());
		} 
	}
	
	@Disabled
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
