package org.olf.folio.order.services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.apache.commons.lang.StringUtils; 

public class GetOrganizationsTest extends ApiBaseTest { 
	
    boolean debug = false;

	public GetOrganizationsTest() {
		// TODO Auto-generated constructor stub
	} 
	
	@Test
	public void nullTest() {
		//
	} 
	
	@Test
	public void testGetAllOrganizations() {
		 
		String organizationEndpoint = getBaseOkapEndpoint() + "organizations-storage/organizations";
		try {
			String orgLookupResponse = getApiService().callApiGet(organizationEndpoint,  getToken());
			JSONObject orgObject = new JSONObject(orgLookupResponse);
			JSONArray jsonArray =  orgObject.getJSONArray("organizations");
			if (debug) {
			    System.out.println(orgObject.toString(3)); 
			} else {
			    assertNotNull(jsonArray);
			    assertTrue(jsonArray.length() > 0);
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetOrganization() {
		String orgCode = "AFRICARES";

		try {
			String encodedOrgCode = URLEncoder.encode("\"" + orgCode + "\"", StandardCharsets.UTF_8.name());

			String organizationEndpoint = getBaseOkapEndpoint() + "organizations-storage/organizations?query=(code=" + encodedOrgCode + ")";
			try {
				String orgLookupResponse = getApiService().callApiGet(organizationEndpoint,  getToken());
			    JSONObject orgObject = new JSONObject(orgLookupResponse);
			    JSONArray jsonArray =  orgObject.getJSONArray("organizations");
			    if (debug) {
	                System.out.println(orgObject.toString(3)); 
	            } else {
				    assertNotNull(jsonArray);
				    String vendorId = (String) jsonArray.getJSONObject(0).get("id");
				    assertTrue(StringUtils.length(vendorId) > 0); 
				    UUID uuid = UUID.fromString(vendorId);
				    //System.out.println("vendorId: "+ vendorId);
	            }
			} catch (IllegalArgumentException e) {
				fail("vendorID was not a valid UUID");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				fail(e.getMessage());
			}
		} catch (UnsupportedEncodingException e) {;
			fail("Unable to URL encoode organization code " + orgCode);
		}
	}
	
	
}
