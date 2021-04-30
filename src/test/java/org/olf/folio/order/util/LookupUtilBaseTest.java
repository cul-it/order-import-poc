package org.olf.folio.order.util;

import java.io.IOException; 

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject; 
import org.olf.folio.order.services.ApiService;

public class LookupUtilBaseTest {
	
	
    private LookupUtil util;
    private String apiUsername;
	private String apiPassword;
	private String tenant;
	private String baseOkapEndpoint;
    private String token; 
    private ApiService apiService;
    
    	
	public LookupUtil getUtil() {
		return util;
	}

	public void setUtil(LookupUtil util) {
		this.util = util;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	
	public String getApiUsername() {
		return apiUsername;
	}

	public void setApiUsername(String apiUsername) {
		this.apiUsername = apiUsername;
	}

	public String getApiPassword() {
		return apiPassword;
	}

	public void setApiPassword(String apiPassword) {
		this.apiPassword = apiPassword;
	}

	public String getBaseOkapEndpoint() {
		return baseOkapEndpoint;
	}

	public void setBaseOkapEndpoint(String baseOkapEndpoint) {
		this.baseOkapEndpoint = baseOkapEndpoint;
	}

	public ApiService getApiService() {
		return apiService;
	}

	public void setApiService(ApiService apiService) {
		this.apiService = apiService;
	}

	public LookupUtilBaseTest() {
		init();
		this.util = new LookupUtil();
		this.util.setBaseOkapEndpoint(this.baseOkapEndpoint);
		this.util.setApiService(getApiService());
		
	} 
	
    public void init() {
    	CompositeConfiguration config = new CompositeConfiguration();
    	PropertiesConfiguration props = new PropertiesConfiguration();
    	
		String use_env = System.getenv("USE_SYSTEM_ENV");
		if (StringUtils.isNotEmpty(use_env) && StringUtils.equals(use_env, "true")) {
			config.setProperty("baseOkapEndpoint",  System.getenv("baseOkapEndpoint"));
			config.setProperty("okapi_username", System.getenv("okapi_username"));
			config.setProperty("okapi_password", System.getenv("okapi_password"));
			config.setProperty("tenant", System.getenv("tenant"));
	 
			config.setProperty("permELocation", System.getenv("permELocation"));
			config.setProperty("permLocation", System.getenv("permLocation"));
			config.setProperty("fiscalYearCode", System.getenv("fiscalYearCode"));
			config.setProperty("loanType", System.getenv("loanType"));
			config.setProperty("textForElectronicResources", System.getenv("textForElectronicResources"));
			config.setProperty("noteType", System.getenv("noteType"));
			config.setProperty("materialType", System.getenv("materialType"));
		 	
		} else { 
		   try {
			   props.load(ClassLoader.getSystemResourceAsStream("application.properties"));
			   this.apiUsername = (String) props.getProperty("okapi_username");
			   this.apiPassword = (String) props.getProperty("okapi_password");
			   this.tenant = (String) props.getProperty("tenant");
			   this.baseOkapEndpoint = (String) props.getProperty("baseOkapEndpoint");
		   } catch (ConfigurationException e) { 
		      throw new RuntimeException("Could not load application.properties file");
		   }
		   config.addConfiguration(props);
		}
 
        this.setApiService(new ApiService(this.tenant));
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", this.apiUsername);
		jsonObject.put("password", this.apiPassword);
		jsonObject.put("tenant",tenant);
		try {
			String endpoint = this.baseOkapEndpoint + "authn/login";
			//System.out.println(endpoint);
			this.token = getApiService().callApiAuth( endpoint,  jsonObject);
			//System.out.println("token: "+ this.token);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	} 
}
