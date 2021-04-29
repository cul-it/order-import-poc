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

public class GetLocationsTest extends ApiBaseTest {
	 

	public GetLocationsTest() {
		// TODO Auto-generated constructor stub
	}  
	
	@Test
	public void testGetLocations() {
	    init();
	    ApiService service = new ApiService(getTenant());
	    String endpoint =  getBaseOkapEndpoint() + "locations?limit=10000";
	    String result = new String();
		try {
			result = service.callApiGet(endpoint, getToken());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jsonObj = new JSONObject(result);
		 
		JSONArray jsonArray =  jsonObj.getJSONArray("locations");
		assertNotNull(jsonArray);
		assertTrue(jsonArray.length() > 0);
	     
	    for (int i=0; i < jsonArray.length(); i++)	{
	    	JSONObject obj = (JSONObject) jsonArray.get(i);
	    	boolean isActive = obj.getBoolean("isActive");
	    	//if (isActive) {
	    	//    System.out.println("name: "+ obj.getString("name"));
	    	//    System.out.println("code: "+ obj.getString("code"));
	    	//    System.out.println();
	    	//}
	    }
	    
	} 
	
}
