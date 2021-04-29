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

public class GetPONumberTest extends ApiBaseTest { 
	

	public GetPONumberTest() { 
	} 
	
	@Ignore
	public void nullTest() { 
		//
	} 
	
	@Test
	public void testGetPONumber() { 
	    
	    String poNumber = "";
		try {
			poNumber = getApiService().callApiGet(getBaseOkapEndpoint() + "orders/po-number", getToken());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    JSONObject poNumberObj = new JSONObject(poNumber);
	    assertNotNull(poNumberObj);
	    String poNum = (String) poNumberObj.get("poNumber");
	    assertNotNull(poNum);
	    //System.out.println("NEXT PO NUMBER: " + poNumberObj.get("poNumber"));
	}
	
	
}
