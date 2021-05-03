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

public class GetBudgetTest extends ApiBaseTest { 
	

	public GetBudgetTest() {
		// TODO Auto-generated constructor stub
	} 
	
	@Ignore
	public void nullTest() {
		//
	} 
	
	@Test
	public void testGetAllBudget() { 
		
		String budgetEndpoint = getBaseOkapEndpoint() + "finance/budgets?limit=1";
		//System.out.println("endpoint: "+ budgetEndpoint);
		try {
			
			String budgetResponse = getApiService().callApiGet(budgetEndpoint, getToken());
			JSONObject budgetsObject = new JSONObject(budgetResponse);
			//System.out.println(budgetsObject.toString(3));
			 
			JSONArray budgetsArray = budgetsObject.getJSONArray("budgets");
			assertNotNull(budgetsArray);
			assertTrue(budgetsArray.length() > 0);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetBudget() { 
		String fundCode = "p6793";
		String badFundCode = "bad";
		String fiscalYearCode = "FY2021";
		String budgetEndpoint = getBaseOkapEndpoint() + "finance/budgets?query=(name=="  + fundCode + "-" + fiscalYearCode + ")";
		//System.out.println("endpoint: "+ budgetEndpoint);
		try { 
		    String budgetResponse = getApiService().callApiGet(budgetEndpoint, getToken());
		    JSONObject budgetsObject = new JSONObject(budgetResponse);
		    assertNotNull(budgetsObject);
		    int totalRecords = (Integer) budgetsObject.get("totalRecords");
		    assertNotNull(totalRecords);
		    assertEquals(totalRecords, 1);
		    //System.out.println(budgetsObject.toString(3));	 
		} catch (Exception e) {
		    fail(e.getMessage());
		}
		// now try with bad fundcode
		budgetEndpoint = getBaseOkapEndpoint() + "finance/budgets?query=(name=="  + badFundCode + "-" + fiscalYearCode + ")";
		try { 
		    String budgetResponse = getApiService().callApiGet(budgetEndpoint, getToken());
		    JSONObject budgetsObject = new JSONObject(budgetResponse);
		    assertNotNull(budgetsObject);
		    int totalRecords = (Integer) budgetsObject.get("totalRecords");
		    assertNotNull(totalRecords);
		    assertEquals(totalRecords, 0); 
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	
}
