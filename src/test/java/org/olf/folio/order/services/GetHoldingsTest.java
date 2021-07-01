package org.olf.folio.order.services;
 
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GetHoldingsTest extends ApiBaseTest {  
	
	@Ignore
	public void nullTest() {
		//
	} 
	
	 
	 
	
	@Test
	public void testGetHoldings() {
		String instanceId = "f7062999-d4bf-4764-b859-7334e818fbea"; 
		String holdingsEndpoint = getBaseOkapEndpoint() + "holdings-storage/holdings?query=(instanceId==" + instanceId + ")";
		try { 
		    String holdingsResponse = getApiService().callApiGet(holdingsEndpoint, getToken());
		    JSONObject holdingsObject = new JSONObject(holdingsResponse);
		    //System.out.println(holdingsObject.toString(3));
		    JSONObject holdingsAsJson = new JSONObject(holdingsResponse);
		    
		     
		    JSONArray holdingsArray = holdingsAsJson.getJSONArray("holdingsRecords");
		    System.out.println("holdingsArray size: "+ holdingsArray.length());
		    
		    Iterator  holdingsIter = holdingsArray.iterator();
		    while (holdingsIter.hasNext() ) {
		        JSONObject holdingsRecord = (JSONObject) holdingsIter.next();
		        String holdingsId = holdingsRecord.getString("id");
	            System.out.println("holdingsId: "+ holdingsId);
	             
	            String queryString =  "holdingsRecordId==" +holdingsId+ " not barcode=\"\"";
	            String encodedQS = URLEncoder.encode(queryString, StandardCharsets.UTF_8.name());
	            String itemsEndpoint = getBaseOkapEndpoint() + "inventory/items?query=(" + encodedQS + ")"; 
	            System.out.println("itemsEndpoint: "+ itemsEndpoint);
	            String itemsResponse = getApiService().callApiGet(itemsEndpoint, getToken());
	            //System.out.println("itemsObject");
	            JSONObject itemsObject = new JSONObject(itemsResponse);
	            //System.out.println(itemsObject.toString(3));
	            
	            JSONArray itemsArray = itemsObject.getJSONArray("items");
	            System.out.println("number of items: "+itemsArray.length());
                Iterator itemsIter = itemsArray.iterator();
                while (itemsIter.hasNext()) {
                    JSONObject itemRecord = (JSONObject) itemsIter.next();
                     
                    String itemId = itemRecord.getString("id");
                    System.out.println("item record: "+ itemId);
                    System.out.println(itemRecord.toString(3));
                     
                }
	            
		       
		    }
		     
		     
		     
		     
		} catch (Exception e) {
		    e.printStackTrace();
		    fail(e.getMessage());
		} 
	}
	
	
}
