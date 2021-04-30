package org.olf.folio.order.util;

import java.io.IOException;
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

public class LookupUtilClassificationTest extends LookupUtilBaseTest {
	
	boolean debug = false;
    
	public LookupUtilClassificationTest() {
		//System.out.println("endpoint: "+ this.getBaseOkapEndpoint());
		//System.out.println("token: "+ this.getToken());
	} 
	
	@Test
	public void getClassificationTest() {
		this.getUtil().setEndPoint("classification-types", "1000");
		
		Map<String,String> map = new HashMap<String, String>();
		try { 
			map = this.getUtil().getReferenceValues(this.getToken());
			if (debug) {
				Iterator iter = map.keySet().iterator();
				
				while (iter.hasNext()) {
					String key = (String) iter.next();
					System.out.println("name: "+ key);
					System.out.println("id: "+ map.get(key));
				}
			} else {
				String testKey = "LC";
				assertNotNull(map);
				assertTrue(map.containsKey(testKey));
				assertNotNull(map.get(testKey));
			}
			
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			//fileOut.close();
		} 
		
	}

}