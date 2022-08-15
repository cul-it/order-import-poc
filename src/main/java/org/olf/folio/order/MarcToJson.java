package org.olf.folio.order;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.olf.folio.order.services.ApiService;
import org.olf.folio.order.util.LookupUtil;
import org.olf.folio.order.util.MarcUtils; 
 
/**
 * MarcToJson.
 * @author jaf30
 *
 */
public class MarcToJson {
    
    private static final Logger logger = Logger.getLogger(MarcToJson.class);
    
    private final int INDENT = 3;
    private final int TEN = 10;
    private final int FUNDVAL = 100;
    private final String DEFAULTPONUM = "12345";
    
    private HashMap<String, String> lookupTable;
    private HashMap<String, String> billingMap;
    private String tenant;
    private String token;
    private ApiService apiService;
    
    private String marcFileName;
    private boolean debug;
    private boolean rushPO = false;

    private String endpoint;
    
    MarcUtils marcUtils = new MarcUtils();
    LookupUtil lookupUtil;
    
    CompositeConfiguration config = new CompositeConfiguration();
    PropertiesConfiguration props = new PropertiesConfiguration(); 
    
    public String getMarcFileName() {
        return marcFileName;
    }

    public void setMarcFileName(String marcFileName) {
        this.marcFileName = marcFileName;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public CompositeConfiguration getConfig() {
        return config;
    }

    public void setConfig(CompositeConfiguration config) {
        this.config = config;
    }
    
    public PropertiesConfiguration getProps() {
        return props;
    }

    public void setProps(PropertiesConfiguration props) {
        this.props = props;
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

    public HashMap<String, String> getLookupTable() {
        return lookupTable;
    }

    public void setLookupTable(HashMap<String, String> lookupTable) {
        this.lookupTable = lookupTable;
    }

    public HashMap<String, String> getBillingMap() {
        return billingMap;
    }

    public void setBillingMap(HashMap<String, String> billingMap) {
        this.billingMap = billingMap;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    public MarcUtils getMarcUtils() {
        return marcUtils;
    }

    public void setMarcUtils(MarcUtils marcUtils) {
        this.marcUtils = marcUtils;
    }

    public LookupUtil getLookupUtil() {
        return lookupUtil;
    }

    public void setLookupUtil(LookupUtil lookupUtil) {
        this.lookupUtil = lookupUtil;
    }
    
    public MarcToJson() {
        // 
    }
    
    /**
     * Main.
     * @param args - arguments
     */
    public static void main(String[] args) {
        MarcToJson app = new MarcToJson();
        app.getArgs(args);
        try {
            app.init();
            JSONArray responseMessages  = app.run();
            System.out.println(responseMessages.toString(3));
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * GetArgs.
     * @param args - arguments
     */
    public  void getArgs(String[] args) {
        String appName = this.getClass().getSimpleName();
        Options options = new Options();
        options.addOption(new Option("f", "file", true, "marc file (REQUIRED)")); 
        options.addOption(new Option("D", "debug", false, "turn on debug output"));
         
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args); 
            if (cmd.hasOption("file")) {
                this.setMarcFileName(cmd.getOptionValue("file"));
            } else { 
                formatter.printHelp(appName, options);
                System.exit(0);
            }
     
            if (cmd.hasOption("D") || cmd.hasOption("debug")) {
                this.setDebug(true);
            }
            
            
        } catch (ParseException e) {
            formatter.printHelp(appName, options);
            System.exit(0);
        }
    }

    /**
     * @throws Exception - an exception
     */
    public void init() throws Exception { 
        // this.setConfig(AppConfig.getConfig());
        PropertiesConfiguration props = new PropertiesConfiguration();
        try {
            props.load(ClassLoader.getSystemResourceAsStream("application.properties"));

        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        
        getConfig().addConfiguration(props);
        JSONArray envErrors = validateEnvironment();
        if (envErrors != null) {
            System.out.println(envErrors.toString(3));
            System.exit(1);
        }
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", getConfig().getProperty("okapi_username"));
        jsonObject.put("password", getConfig().getProperty("okapi_password"));
        
        setTenant((String) config.getProperty("tenant"));
        jsonObject.put("tenant",  getTenant());
        
        this.setApiService(new ApiService(getTenant()));
        this.setEndpoint((String) config.getProperty("baseOkapEndpoint"));
          
        try {
            this.token =  getApiService().callApiAuth(getEndpoint() + "authn/login",  jsonObject); 
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
        this.lookupUtil = new LookupUtil();
        this.lookupUtil.setBaseOkapEndpoint(getEndpoint());
        this.lookupUtil.setApiService(getApiService());
        this.lookupUtil.load();
        this.setLookupTable(this.lookupUtil.getReferenceValues(this.getToken()));
        String billingEndpoint = this.getEndpoint()+"configurations/entries?query=(configName==tenant.addresses)";
        this.setBillingMap(this.lookupUtil.getBillingAddresses(billingEndpoint, this.token));
         
    } 
    
    
    /**
     * @return
     * @throws Exception
     */
    public JSONArray run() throws Exception {          
 
        String materialTypeName = "Book";
        String ISBNId = this.lookupTable.get("ISBN");
        String personalNameTypeId = this.lookupTable.get("Personal name");        
        
        JSONArray responseMessages = new JSONArray();        
        JSONArray errorMessages = new JSONArray();
        // GENERATE UUID for the PO 
       
        //GET THE NEXT PO NUMBER
        // harcode the ponumber just so we have one  
        JSONObject poNumberObj = new JSONObject();
        poNumberObj.put("poNumber", DEFAULTPONUM);
        logger.debug("NEXT PO NUMBER: " + poNumberObj.get("poNumber"));
        
        //responseMessages.put(poNumberObj);
        
        // CREATING THE PURCHASE ORDER
        JSONObject order = new JSONObject();
        UUID orderUUID = UUID.randomUUID();        
        Map<Integer, UUID> orderLineMap = new HashMap<Integer, UUID>();
        
        String billTo = (String) getConfig().getProperty("billToDefault");
        String billingUUID = this.getBillingMap().get(billTo);
        
        order.put("orderType", "One-Time");
        order.put("reEncumber", false);
        order.put("id", orderUUID.toString());
        order.put("approved", true);
        order.put("workflowStatus", "Open");
        order.put("billTo", billingUUID);
        order.put("poNumber", poNumberObj.get("poNumber"));
        
        JSONArray poLines = new JSONArray();
        
        // iterator over records in the marc file.
        String filePath =  this.getMarcFileName();
        InputStream in = new FileInputStream(filePath);        
        MarcReader reader = new MarcStreamReader(in);
        
        Record record = null;
        int numRec = 0;
        
        while (reader.hasNext()) {
            try {
                record = reader.next();
                //if (isDebug()) {
                //    System.out.println(record.toString());
                //}
                JSONObject responseMessage = new JSONObject();
                 
                DataField twoFourFive = (DataField) record.getVariableField("245");
                DataField nineEighty = (DataField) record.getVariableField("980");
                DataField nineEightyOne = (DataField) record.getVariableField("981");
                DataField nineSixtyOne = (DataField) record.getVariableField("961");
                DataField twoSixtyFour = (DataField) record.getVariableField("264");
                
                String title = marcUtils.getTitle(twoFourFive);
                if (isDebug()) {
                    System.out.println("Title: "+ title);
                }
                
                final String fundCode = marcUtils.getFundCode(nineEighty);
                final String vendorCode =  marcUtils.getVendorCode(nineEighty);
                    
                String quantity =  marcUtils.getQuantity(nineEighty);
                Integer quantityNo = 0; //INIT
                if (quantity != null)  { 
                    quantityNo = Integer.valueOf(quantity);
                }
                
                String price = marcUtils.getPrice(nineEightyOne);                
                final String vendorItemId = marcUtils.getVendorItemId(nineSixtyOne);
                
                //String personName = marcUtils.getPersonName(nineEighty);
                
                DataField nineFiveTwo = (DataField) record.getVariableField("952");
                String locationName = marcUtils.getLocation(nineFiveTwo);
                //responseMessage.put("location", locationName + " (" + lookupTable.get(locationName + "-location") + ")");
                             
                 
                //LOOK UP VENDOR
                if (isDebug()) {
                    System.out.println("Lookup vendor: "+ vendorCode);
                }
                 
                try {
                    // URL encode organization code to avoid cql parse error on forward slash
                    String encodedOrgCode = URLEncoder.encode("\"" + vendorCode + "\"", StandardCharsets.UTF_8.name());
                    logger.debug("encodedOrgCode: " + encodedOrgCode);

                    String organizationEndpoint = this.getEndpoint()
                            + "organizations-storage/organizations?query=(code==" + encodedOrgCode + ")";
                    logger.debug("organizationEndpoint: " + organizationEndpoint);
                    String orgLookupResponse = apiService.callApiGet(organizationEndpoint, token);
                    JSONObject orgObject = new JSONObject(orgLookupResponse);
                    String vendorId = (String) orgObject.getJSONArray("organizations").getJSONObject(0).get("id");
                    order.put("vendor", vendorId);                
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage());
                }

                //LOOK UP THE FUND
                if (isDebug()) {
                    System.out.println("Lookup fund: "+ fundCode);
                }
                final String fundEndpoint = this.getEndpoint() + "finance/funds?limit=30&offset=0&query=((code='" + fundCode + "'))";
                final String fundResponse = this.apiService.callApiGet(fundEndpoint, token);
                
                
                                
                 
                // CREATING THE PURCHASE ORDER 
                // POST ORDER LINE
                //FOLIO WILL CREATE THE INSTANCE, HOLDINGS, ITEM (IF PHYSICAL ITEM)
                JSONObject orderLine = new JSONObject();
                JSONObject cost = new JSONObject();
                JSONObject location = new JSONObject();
                JSONArray locations = new JSONArray(); 
  
                JSONObject physical = new JSONObject();
                physical.put("createInventory", "Instance, Holding, Item");
                physical.put("materialType", lookupTable.get(materialTypeName));
                orderLine.put("physical", physical);
                orderLine.put("orderFormat", "Physical Resource");
                cost.put("listUnitPrice", price);
                cost.put("quantityPhysical", quantityNo);
                location.put("quantityPhysical", quantityNo);
                location.put("locationId", lookupTable.get(locationName + "-location"));
                locations.put(location);
                 
                //VENDOR REFERENCE NUMBER IF INCLUDED IN THE MARC RECORD:                
                if (StringUtils.isNotEmpty(vendorItemId)) {                    
                    JSONArray referenceNumbers = new JSONArray();
                    JSONObject vendorDetail = new JSONObject();
                    JSONObject referenceNumber = new JSONObject();
                    referenceNumber.put("refNumber", vendorItemId);
                    referenceNumber.put("refNumberType", "Vendor internal number");
                    referenceNumbers.put(referenceNumber);
                    vendorDetail.put("referenceNumbers", referenceNumbers);
                    orderLine.put("vendorDetail", vendorDetail);
                }
                
                UUID orderLineUUID = UUID.randomUUID();
                orderLine.put("id", orderLineUUID);
                //responseMessage.put("id", orderLineUUID.toString());
                orderLineMap.put(numRec, orderLineUUID); 
                
                orderLine.put("source", "User");
                cost.put("currency", "USD");
                orderLine.put("cost", cost);
                orderLine.put("locations", locations);
                orderLine.put("titleOrPackage", title);
                orderLine.put("acquisitionMethod", "df26d81b-9d63-4ff8-bf41-49bf75cfa70e");
                
                // get the "internal note", which apparently will be used as a description 
                String internalNotes =  marcUtils.getInternalNotes(nineEighty);
                if (StringUtils.isNotEmpty(internalNotes)) {
                    orderLine.put("description", internalNotes);
                }
                
                // add a detailsObject if a receiving note or ISBN identifiers are found
                JSONObject detailsObject = new JSONObject();
                // get the "receiving note"
                String receivingNote =  marcUtils.getReceivingNote(nineEightyOne);
                if (StringUtils.isNotEmpty(receivingNote)) {
                    detailsObject.put("receivingNote", receivingNote);
                }
                
                // get ISBN values in a productIds array and add to detailsObject if not empty
                JSONArray productIds = new JSONArray();
                JSONArray identifiers = marcUtils.buildIdentifiers(record, lookupTable);
                Iterator identIter = identifiers.iterator();
                while (identIter.hasNext()) {
                    JSONObject identifierObj = (JSONObject) identIter.next();
                    String identifierType = identifierObj.getString("identifierTypeId");
                    String oldVal = identifierObj.getString("value");
                    JSONObject productId = new JSONObject();
                    String newVal = StringUtils.substringBefore(oldVal, " ");
                    String qualifier = StringUtils.substringAfter(oldVal, " ");
                    productId.put("productId", newVal);
                    productId.put("productIdType", identifierType);
                    if (StringUtils.isNotEmpty(qualifier)) {
                        productId.put("qualifier", qualifier);
                    }
                    productIds.put(productId);
                    
                }
                if (productIds.length() > 0) {
                    logger.debug(productIds.toString(3));
                    detailsObject.put("productIds", productIds);
                }                  
                if (! detailsObject.isEmpty()) {
                    orderLine.put("details", detailsObject);   
                }
                
                // add contributors
                JSONArray contribArray = new JSONArray();
                 
                JSONArray contributors = marcUtils.buildContributors(record, lookupTable);
                Iterator contribIter = contributors.iterator();
                while (contribIter.hasNext()) {
                    JSONObject contribObj = (JSONObject) contribIter.next();
                    JSONObject contribCopyObj = new JSONObject();
                    contribCopyObj.put("contributorNameTypeId", personalNameTypeId);
                    contribCopyObj.put("contributor", contribObj.get("name"));
                    contribArray.put(contribCopyObj);
                }
                if (contribArray.length() > 0) {
                    orderLine.put("contributors", contribArray);
                    logger.debug(contribArray.toString(3));
                }
                
                // get rush value
                String rush = marcUtils.getRush(nineEightyOne);
                // TODO: check if match rush value to Rush:yes before adding to orderLine
                if (StringUtils.isNotEmpty(rush) && StringUtils.contains(rush.toLowerCase(), "rush:yes")) {
                    orderLine.put("rush", true);
                }
                
                // get selector
                String selector = marcUtils.getSelector(nineEighty);
                if (StringUtils.isNotEmpty(selector)) {
                    orderLine.put("selector", selector);
                }
                
                 
                
                // add publisher and publicationDate
                String publisher = marcUtils.getPublisher(record);
                if (StringUtils.isNotEmpty(publisher)) {
                    orderLine.put("publisher", publisher);
                }
                
                if (twoSixtyFour != null) {
                    String pubYear = marcUtils.getPublicationDate(twoSixtyFour);
                    if (StringUtils.isNotEmpty(pubYear)) {                        
                        orderLine.put("publicationDate", pubYear);
                    }
                }
                
                JSONObject fundsObject = new JSONObject(fundResponse);
                String fundId = (String) fundsObject.getJSONArray("funds").getJSONObject(0).get("id");
                JSONObject fundDist = new JSONObject();
                fundDist.put("distributionType", "percentage");
                fundDist.put("value", FUNDVAL);
                fundDist.put("fundId", fundId);
                JSONArray funds = new JSONArray();
                funds.put(fundDist);
                orderLine.put("fundDistribution", funds);

                // get requester
                String requester = marcUtils.getRequester(nineEightyOne);
                if (StringUtils.isNotEmpty(requester)) {
                    orderLine.put("requester", requester);
                    rushPO = true;
                }

                // if rushPO is ever set, prefix the poNumber with "RUSH"
                if (rushPO) {
                    order.put("poNumberPrefix", "RUSH");
                    order.put("poNumber", "RUSH"+ poNumberObj.get("poNumber"));
                } else {
                    order.put("poNumber", poNumberObj.get("poNumber"));
                }
                orderLine.put("purchaseOrderId", orderUUID.toString());
                poLines.put(orderLine);
                order.put("compositePoLines", poLines);
                responseMessages.put(order);
                // responseMessages.put(responseMessage);
            } catch (Exception e) {
                logger.error(e.toString());
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("error", e.toString());
                errorMessage.put("PONumber", poNumberObj.get("poNumber"));
                errorMessages.put(errorMessage);
                return errorMessages;                
            }
            
            numRec++;
         
        } 
        
        return responseMessages;
    }
     
    
    /**
     * @param df - datafield
     * @param name - name of datafield
     */
    public void validateDataField(DataField df, String name) {
        if (df == null) {
            logger.error("Required Datafield " + name + " is null");
            System.exit(1);
        }
    }
    
    /**
     * @return and array with environment variable missing errors or null if no errors
     */
    public JSONArray validateEnvironment( ) {
        JSONArray errors = new JSONArray();
        if (StringUtils.isEmpty((String) this.getConfig().getProperty("baseOkapEndpoint"))) {
           JSONObject errMsg = new JSONObject();
           errMsg.put("error", "baseOkapEndpoint environment variable not found");
           errors.put(errMsg);     
        }
        if (StringUtils.isEmpty((String) this.getConfig().getProperty("okapi_username"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "api user environment variable not found");
            errors.put(errMsg);
        }
        if (StringUtils.isEmpty((String) this.getConfig().getProperty("okapi_username"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "api password environment variable not found");
            errors.put(errMsg);
        }
        if (StringUtils.isEmpty((String) this.getConfig().getProperty("tenant"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "api tenant environment variable not found");
            errors.put(errMsg); 
        } 
        if (StringUtils.isEmpty((String) this.getConfig().getProperty("billToDefault"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "billToDefault environment variable not found");
            errors.put(errMsg);
        }
        if (errors.isEmpty()) {
            return null;
        } else {
            return errors;
        }
    }
    
    
}
