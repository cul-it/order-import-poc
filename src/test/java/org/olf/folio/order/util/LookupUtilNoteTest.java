package org.olf.folio.order.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LookupUtilNoteTest extends LookupUtilBaseTest {
	
	boolean debug = false; 
    
	public LookupUtilNoteTest() {
		// TODO Auto-generated constructor stub
	} 
	
	@Test
	public void getNoteTest() { 
 
		this.getUtil().setEndPoint("note-types", "1000");
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
				String testKey = "General note";
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
