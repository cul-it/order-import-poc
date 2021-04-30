package org.olf.folio.order.util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail; 
import org.junit.Ignore; 
import org.junit.jupiter.api.Test;

public class LookupUtilLocationsTest extends LookupUtilBaseTest {
	
	boolean debug = false;
    
	public LookupUtilLocationsTest() {
		// TODO Auto-generated constructor stub
	} 
	
	@Test
	public void getLocationsTest() {  
		this.getUtil().setEndPoint("locations", "1000");
Map<String,String> map = new HashMap<String, String>();
		
		try { 
			map = this.getUtil().getReferenceValues(this.getToken());
			
			if (debug) {
				System.out.println("mann-location: "+ map.get("mann-location"));
				System.out.println("olin-location: "+ map.get("olin-location"));
				System.out.println("rmc-location: "+ map.get("rmc-location"));
				System.out.println("vey-location: "+ map.get("vet-location"));
			} else {
				String testKey = "mann-location";
				assertNotNull(map);
				assertTrue(map.containsKey(testKey));
				assertNotNull(map.get(testKey));
			}
			
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			//fileOut.close();
		}
		
		
		
	}

}
