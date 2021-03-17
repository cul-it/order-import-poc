package org.olf.folio.order.util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.olf.folio.order.services.ApiService;

public class LookupUtilTest {
	
	CompositeConfiguration config = new CompositeConfiguration();
	PropertiesConfiguration props = new PropertiesConfiguration();
    private LookupUtil util;
    private String token;
    private String tenant;
    
	public LookupUtilTest() {
		// TODO Auto-generated constructor stub
	}
	
	@Before
	public void init() {
		
		try {
		    props.load(ClassLoader.getSystemResourceAsStream("application.properties"));
		} catch (ConfigurationException e) {
		    throw new RuntimeException(e);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", props.getString("okapi_username"));
		jsonObject.put("password", props.getString("okapi_password"));
		
		this.tenant = props.getString("tenant");
		System.out.println("tenant: "+ this.tenant);
		jsonObject.put("tenant",  this.tenant);
		
		ApiService apiService = new ApiService(this.tenant);
		  
		try {
			this.token =  apiService.callApiAuth( props.getString("baseOkapEndpoint") + "authn/login",  jsonObject); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.util = new LookupUtil();
		this.util.setBaseOkapEndpoint(props.getString("baseOkapEndpoint"));
		this.util.setApiService(apiService);
	}
	
	@Test
	public void getReferenceValuesTest() { 
		PrintStream originalOut = System.out;
		PrintStream fileOut = null;
		this.util.load();
		Map<String,String> map = new HashMap<String, String>();
		try {
			//fileOut = new PrintStream("/cul/src/order-import-poc/identifiers.txt");
			//System.setOut(fileOut);
			map = this.util.getReferenceValues(this.token);
			Iterator iter = map.keySet().iterator();
			
			while (iter.hasNext()) {
				String key = (String) iter.next();
				System.out.println("name: "+ key);
				System.out.println("id: "+ map.get(key));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//fileOut.close();
		}
		//System.setOut(originalOut);
		System.out.println("========================");
		System.out.println("Mann-location: "+ map.get("Mann-location"));
		System.out.println("Book: "+ map.get("Book"));
		System.out.println("General Note: "+ map.get("General note"));
		System.out.println("text: "+ map.get("text"));
		System.out.println("Electronic: "+ map.get("Electronic"));
		System.out.println("Personal name: "+ map.get("Personal name"));
		System.out.println("ISBN: "+ map.get("ISBN"));
		System.out.println("Invalid ISBN: "+ map.get("Invalid ISBN"));
		System.out.println("ISSN: "+ map.get("ISSN"));
		System.out.println("Linking ISSN: "+ map.get("Linking ISSN"));
		System.out.println("Invalid ISSN: "+ map.get("Invalid ISSN"));
		
		
		
		
		
		
	}

}
