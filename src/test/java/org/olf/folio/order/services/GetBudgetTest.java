package org.olf.folio.order.services;
 
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileOutputStream;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore; 
import org.junit.jupiter.api.Test;

public class GetBudgetTest extends ApiBaseTest { 
	
    boolean debug = false;
    PrintStream stdout = System.out;
    
	public GetBudgetTest() {
		// TODO Auto-generated constructor stub
	} 
	
	@Ignore
	public void nullTest() {
		//
	} 
	
	@Test
	public void testGetAllBudget() { 
		
		String budgetEndpoint = getBaseOkapEndpoint() + "finance/budgets?limit=1000";
		//System.out.println("endpoint: "+ budgetEndpoint);
		try {
			
			String budgetResponse = getApiService().callApiGet(budgetEndpoint, getToken());
			JSONObject budgetsObject = new JSONObject(budgetResponse);
			JSONArray budgetsArray = budgetsObject.getJSONArray("budgets"); 
			if (debug) {
			    System.setOut(new PrintStream(new FileOutputStream("/cul/src/order-import-poc/output.json")));
			    //System.out.println(budgetsObject.toString(3));
			    for (int i = 0; i < budgetsArray.length(); i++) {
                    JSONObject fundObj = (JSONObject) budgetsArray.get(i);
                     
                    System.out.println("name: " + fundObj.get("name"));
                    //System.out.println("id: " + fundObj.get("id"));
                    System.out.println("available: " + fundObj.get("available"));
                    System.out.println();
                }
			    System.setOut(stdout);
			} 
			
			assertNotNull(budgetsArray);
			assertTrue(budgetsArray.length() > 0);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetBudget() { 
		String fundCode = "6945";
		String badFundCode = "p2651";
		String fiscalYearCode = "FY2023";
		String budgetEndpoint = getBaseOkapEndpoint() + "finance/budgets?query=(name=="  + fundCode + "-" + fiscalYearCode + ")";
		
		try { 
		    String budgetResponse = getApiService().callApiGet(budgetEndpoint, getToken());
		    JSONObject budgetsObject = new JSONObject(budgetResponse);
		    if (debug) {
		        System.out.println("endpoint: "+ budgetEndpoint);
		        System.out.println("fundcode: "+ fundCode);
		        System.out.println(budgetsObject.toString(3));
            } else  {
		        assertNotNull(budgetsObject);
		        int totalRecords = (Integer) budgetsObject.get("totalRecords");
		        assertNotNull(totalRecords);
		        assertEquals(totalRecords, 1);
		        //System.out.println(budgetsObject.toString(3));
            }
		} catch (Exception e) {
		    fail(e.getMessage());
		}
		// now try with bad fundcode
		budgetEndpoint = getBaseOkapEndpoint() + "finance/budgets?query=(name=="  + badFundCode + "-" + fiscalYearCode + ")";
		try { 
		    String budgetResponse = getApiService().callApiGet(budgetEndpoint, getToken());
		    JSONObject budgetsObject = new JSONObject(budgetResponse);
		    if (debug) {
		        System.out.println("endpoint: "+ budgetEndpoint);
		        System.out.println("bad fundcode: "+ badFundCode);
                System.out.println(budgetsObject.toString(3));
            } else  {
                assertNotNull(budgetsObject);
                int totalRecords = (Integer) budgetsObject.get("totalRecords");
                assertNotNull(totalRecords);
                assertEquals(totalRecords, 1);
                //System.out.println(budgetsObject.toString(3));
            } 
		} catch (Exception e) {
			fail(e.getMessage());
		}
	} 
	
}
