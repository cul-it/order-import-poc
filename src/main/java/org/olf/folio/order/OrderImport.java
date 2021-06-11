package org.olf.folio.order;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream; 
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import javax.servlet.ServletContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.marc4j.MarcJsonWriter; 
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter; 
import org.marc4j.MarcWriter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.olf.folio.order.services.ApiService;
import org.olf.folio.order.util.LookupUtil;
import org.olf.folio.order.util.MarcUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class OrderImport {
	
	private static final Logger logger = Logger.getLogger(OrderImport.class);
	private ServletContext myContext;
	private HashMap<String, String> lookupTable;
	private HashMap<String, String> billingMap;
	private String tenant;
	
	private ApiService apiService;
	MarcUtils marcUtils = new MarcUtils();
	
	public  JSONArray  upload(String fileName) throws IOException, InterruptedException, Exception {
	    long start = 0L; // to be used for timing
        long end = 0L;  // to be used for timing
		logger.debug("...starting...");
		JSONArray responseMessages = new JSONArray();
		JSONArray errorMessages = new JSONArray();
		
		//COLLECT VALUES FROM THE CONFIGURATION FILE
		String baseOkapEndpoint = (String) getMyContext().getAttribute("baseOkapEndpoint");
		String apiUsername = (String) getMyContext().getAttribute("okapi_username");
		String apiPassword = (String) getMyContext().getAttribute("okapi_password");
		tenant = (String) getMyContext().getAttribute("tenant"); 
		// we might need this later to validate if resources are electronic...leave commented out
		//String permELocationName = (String) getMyContext().getAttribute("permELocation");
		String noteTypeName = (String) getMyContext().getAttribute("noteType");
		String materialTypeName = (String) getMyContext().getAttribute("materialType");
		String billTo = (String) getMyContext().getAttribute("billTo");
		
		JSONArray envErrors = validateEnvironment();
		if (envErrors != null) {
		    return envErrors;
		}
		
		//GET THE FOLIO TOKEN
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", apiUsername);
		jsonObject.put("password", apiPassword);
		jsonObject.put("tenant",tenant);
		
		this.apiService = new ApiService(tenant);
		String token = this.apiService.callApiAuth( baseOkapEndpoint + "authn/login",  jsonObject); 
		
		//GET THE UPLOADED FILE
		String filePath = (String) myContext.getAttribute("uploadFilePath");
		InputStream in = null;		
		//MAKE SURE A FILE WAS UPLOADED
		InputStream is = null;
		if (fileName != null) {
			in = new FileInputStream(filePath + fileName);			
		} else {
			JSONObject errorMessage = new JSONObject();
			errorMessage.put("error", "no input file provided");
			errorMessage.put("PONumber", "~error~");
			errorMessages.put(errorMessage);
			return errorMessages;
		}
		
		//READ THE MARC RECORD FROM THE FILE AND VALIDATE IT
		//VALIDATES THE FUND CODE and  VENDOR CODE DATA
		// We don't want to continue if any of the marc records do not contain valid data
		MarcReader reader = new MarcStreamReader(in);	   
	    
	   	JSONArray validateRequiredResult = validateRequiredValues(reader, token, baseOkapEndpoint);
	   	if (!validateRequiredResult.isEmpty()) return validateRequiredResult;
	   	
		//SAVE REFERENCE TABLE VALUES (JUST LOOKUP THEM UP ONCE)
	   	logger.debug("Get Lookup table");
		if (myContext.getAttribute(Constants.LOOKUP_TABLE) == null) {
			 LookupUtil lookupUtil = new LookupUtil();
			 lookupUtil.setBaseOkapEndpoint(baseOkapEndpoint);
			 lookupUtil.setApiService(apiService);
			 lookupUtil.load();
			 this.lookupTable = lookupUtil.getReferenceValues(token);
			 String billingEndpoint = baseOkapEndpoint+"configurations/entries?query=(configName==tenant.addresses)";
			 this.billingMap = lookupUtil.getBillingAddresses(billingEndpoint, token);
			 myContext.setAttribute(Constants.LOOKUP_TABLE, lookupTable);
			 myContext.setAttribute(Constants.BILLINGMAP, billingMap);
			 
			 
			 logger.debug("put lookup table in context");
		} else {
			 this.lookupTable = (HashMap<String, String>) myContext.getAttribute(Constants.LOOKUP_TABLE);
			 this.billingMap = (HashMap<String, String>) myContext.getAttribute(Constants.BILLINGMAP);
			 logger.debug("got lookup table from context");
		}
		String ISBNId = this.lookupTable.get("ISBN");
		String personalNameTypeId = this.lookupTable.get("Personal name");
		
		
		// READ THE MARC RECORD FROM THE FILE
		in = new FileInputStream(filePath + fileName);
		reader = new MarcStreamReader(in);
		
		// TODO: clean up... the instantiation of this byteArrayOutputStream is used below but seems to be 
		// handled differently...this one uses Unicode...others use UTF-8
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		MarcWriter w = new MarcStreamWriter(byteArrayOutputStream, "UTF-8");		
		AnselToUnicode conv = new AnselToUnicode();
		w.setConverter(conv);
		
		// GENERATE UUID for the PO
	   
	    UUID orderUUID = UUID.randomUUID();
	    String vendorCode = new String();
	    
	    //GET THE NEXT PO NUMBER 
		logger.trace("get next PO number");
		String poNumber = this.apiService.callApiGet(baseOkapEndpoint + "orders/po-number", token);		
		JSONObject poNumberObj = new JSONObject(poNumber);
		logger.debug("NEXT PO NUMBER: " + poNumberObj.get("poNumber")); 
        // does this have to be a UUID object?
		String billingUUID = this.billingMap.get(billTo);		
		
		// CREATING THE PURCHASE ORDER
		JSONObject order = new JSONObject();
		
		order.put("orderType", "One-Time");
		order.put("reEncumber", false);
		order.put("id", orderUUID.toString());
		order.put("approved", true);
		order.put("workflowStatus", "Open");
		order.put("billTo", billingUUID);
		order.put("poNumber", poNumberObj.get("poNumber"));
		
		JSONArray poLines = new JSONArray();
		
		// map of records with orderline uuid as key
		HashMap<String, Record> recordMap = new HashMap<String, Record>();
		
		// iterator over records in the marc file.
	     
		logger.debug("reading marc file");
		int numRec = 0; 
		
		while (reader.hasNext()) {
			try {
				Record record = reader.next();
				//logger.debug(record.toString());				
				 
				DataField twoFourFive = (DataField) record.getVariableField("245");
				DataField nineEighty = (DataField) record.getVariableField("980");
			    DataField nineFiveTwo = (DataField) record.getVariableField("952");
			    DataField nineEightyOne = (DataField) record.getVariableField("981");
			    DataField nineSixtyOne = (DataField) record.getVariableField("961");
			    
				String title = marcUtils.getTitle(twoFourFive);						 
				String fundCode = marcUtils.getFundCode(nineEighty);
				// vendor code instantiated outside of loop because it will be the same for all orderLines and added to response later
				vendorCode =  marcUtils.getVendorCode(nineEighty);
				    
				String quantity =  marcUtils.getQuantity(nineEighty);
				Integer quantityNo = 0; //INIT
			    if (quantity != null)  quantityNo = Integer.valueOf(quantity);
			    
				String price = marcUtils.getPrice(nineEightyOne);
				String vendorItemId = marcUtils.getVendorItemId(nineSixtyOne);
			    String locationName = marcUtils.getLocation(nineFiveTwo);
			    
			     
				// LOOK UP THE ORGANIZATION (vendor) again!
				// TODO: Refactor validateRequiredValues() to store org & fund IDs and avoid redundant HTTP requests
				//logger.debug("lookupVendor");
				try {
					// URL encode organization code to avoid cql parse error on forward slash
					String encodedOrgCode = URLEncoder.encode("\"" + vendorCode + "\"", StandardCharsets.UTF_8.name());
					logger.debug("encodedOrgCode: " + encodedOrgCode);

					String organizationEndpoint = baseOkapEndpoint
							+ "organizations-storage/organizations?query=(code=" + encodedOrgCode + ")";
					logger.debug("organizationEndpoint: " + organizationEndpoint);
					String orgLookupResponse = apiService.callApiGet(organizationEndpoint, token);
					JSONObject orgObject = new JSONObject(orgLookupResponse);
					String vendorId = (String) orgObject.getJSONArray("organizations").getJSONObject(0).get("id");
					order.put("vendor", vendorId);				
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage());
				}
				
				//LOOK UP THE FUND
				//logger.debug("lookup Fund");
				String fundEndpoint = baseOkapEndpoint + "finance/funds?limit=30&offset=0&query=((code='" + fundCode + "'))";
				String fundResponse = this.apiService.callApiGet(fundEndpoint, token);
				JSONObject fundsObject = new JSONObject(fundResponse);
				String fundId = (String) fundsObject.getJSONArray("funds").getJSONObject(0).get("id");				
				
				// CREATING THE PURCHASE ORDER				
				
				
				// POST ORDER LINE
				//FOLIO WILL CREATE THE INSTANCE, HOLDINGS, ITEM (IF PHYSICAL ITEM)
				JSONObject orderLine = new JSONObject();
				JSONObject cost = new JSONObject();
				JSONObject location = new JSONObject();
				JSONArray locations = new JSONArray(); 
				
				// all items are assumed to be physical
				JSONObject physical = new JSONObject();
				physical.put("createInventory", "Instance, Holding, Item");
				physical.put("materialType", lookupTable.get(materialTypeName));
				orderLine.put("physical", physical);
				orderLine.put("orderFormat", "Physical Resource");
				cost.put("listUnitPrice", price);
				cost.put("quantityPhysical", 1);
				location.put("quantityPhysical", quantityNo);
				location.put("locationId", lookupTable.get(locationName + "-location"));
				locations.put(location);
				 
				
				// as of IRIS release vendorDetail is slightly more complex
				if (StringUtils.isNotEmpty(vendorItemId)) {
                    JSONArray referenceNumbers = new JSONArray();
                    JSONObject vendorDetail = new JSONObject();
                    vendorDetail.put("instructions", ""); // required element, even if empty
                    vendorDetail.put("vendorAccount", ""); // required element, even if empty
                    JSONObject referenceNumber = new JSONObject();
                    referenceNumber.put("refNumber", vendorItemId);
                    referenceNumber.put("refNumberType", "Vendor internal number");
                    referenceNumbers.put(referenceNumber);
                    vendorDetail.put("referenceNumbers", referenceNumbers);
                    orderLine.put("vendorDetail", vendorDetail);
                }
				
				UUID orderLineUUID = UUID.randomUUID();
				// save the record
				recordMap.put(orderLineUUID.toString(), record);
				orderLine.put("id", orderLineUUID);
				orderLine.put("source", "User");
				cost.put("currency", "USD"); // TODO: get this from marc or use an env variable
				orderLine.put("cost", cost);
				orderLine.put("locations", locations);
				orderLine.put("titleOrPackage", title);
				orderLine.put("acquisitionMethod", "Purchase");
				
				// get the "internal note", which apparently will be used as a description 
				String internalNotes =  marcUtils.getInternalNotes(nineEighty);
				if (StringUtils.isNotEmpty(internalNotes)) {
				    orderLine.put("description", internalNotes);
				}
				
				// add a detailsObject if a receiving note or ISBN identifiers are found
                JSONObject detailsObject = new JSONObject();
                
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
                
                // get the "receiving note"
                String receivingNote =  marcUtils.getReceivingNote(nineEightyOne);
                if (StringUtils.isNotEmpty(receivingNote)) {
                    detailsObject.put("receivingNote", receivingNote);
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
				// TODO: check if match rush value to ;Rush:yes before adding to orderLine
                if (StringUtils.isNotEmpty(rush) && StringUtils.contains(rush.toLowerCase(), "rush:yes")) {
                    orderLine.put("rush", true);
                }

				// get selector
				String selector = marcUtils.getSelector(nineEighty);
				if (StringUtils.isNotEmpty(selector)) {
					orderLine.put("selector", selector);
				}
				
				// get requester
				String requester = marcUtils.getRequester(nineEightyOne);
				if (StringUtils.isNotEmpty(requester)) {
					orderLine.put("requester", requester);
				}
				
				
				// add fund distribution info
				JSONArray funds = new JSONArray();
				JSONObject fundDist = new JSONObject();
				fundDist.put("distributionType", "percentage");
				fundDist.put("value", 100);
				fundDist.put("fundId", fundId);
				funds.put(fundDist);
				orderLine.put("fundDistribution", funds);
				

				orderLine.put("purchaseOrderId", orderUUID.toString());
				poLines.put(orderLine);
				order.put("compositePoLines", poLines);				
			
			} catch(Exception e) {
				logger.error(e.toString());
				JSONObject errorMessage = new JSONObject();
				errorMessage.put("error",e.toString());
				errorMessage.put("PONumber", poNumber);
				errorMessages.put(errorMessage);
				return errorMessages;
			}
			numRec++;
		} 
		
		logger.info("Here is the PO, order number: "+ poNumberObj.get("poNumber"));
		logger.info(order.toString(3));
		
		//POST THE ORDER AND LINE:
		String orderResponse = apiService.callApiPostWithUtf8(baseOkapEndpoint + "orders/composite-orders", order, token);  
		 
		
		//GET THE UPDATED PURCHASE ORDER FROM THE API AND PULL OUT THE ID FOR THE INSTANCE FOLIO CREATED:
		logger.debug("getUpdatedPurchaseOrder");
		String updatedPurchaseOrder = apiService.callApiGet(baseOkapEndpoint + "orders/composite-orders/" +orderUUID.toString() ,token); 
		JSONObject updatedPurchaseOrderJson = new JSONObject(updatedPurchaseOrder);
		logger.debug("updated purchase order...");
		logger.debug(updatedPurchaseOrderJson.toString(3));
		
		 
        numRec = 0;         
        start = System.currentTimeMillis();
        Iterator<Object> poLineIterator = updatedPurchaseOrderJson.getJSONArray("compositePoLines").iterator();
        
        while (poLineIterator.hasNext()) { 
            JSONObject poLineObject = (JSONObject) poLineIterator.next();
			try {
			     
				JSONObject responseMessage = new JSONObject();
				responseMessage.put("poNumber", poNumberObj.get("poNumber"));
				responseMessage.put("poUUID", orderUUID.toString());
				UUID snapshotId = UUID.randomUUID();
				UUID recordTableId = UUID.randomUUID(); 
				
                String poLineUUID = poLineObject.getString("id");
                String poLineNumber = poLineObject.getString("poLineNumber");
                String instanceId = poLineObject.getString("instanceId");
                String title = poLineObject.getString("titleOrPackage");
                String requester = poLineObject.optString("requester");
                String internalNote = poLineObject.optString("description");
                JSONObject polDetails = poLineObject.optJSONObject("details");
                
                Record record = recordMap.get(poLineUUID);
                
				String receivingNote = null;
				if (polDetails != null) {
					receivingNote = polDetails.optString("receivingNote");
				}
				
				responseMessage.put("poLineUUID", poLineUUID);
				responseMessage.put("poLineNumber", poLineNumber);
				responseMessage.put("title", title);
				responseMessage.put("requester", requester);
				responseMessage.put("internalNote", internalNote);
				responseMessage.put("receivingNote", receivingNote);
				responseMessage.put("vendorCode", vendorCode);
				
				//GET THE INSTANCE RECORD FOLIO CREATED, SO WE CAN ADD BIB INFO TO IT:
				logger.debug("get InstanceResponse");
				String instanceResponse = apiService.callApiGet(baseOkapEndpoint + "inventory/instances/" + instanceId, token);
				JSONObject instanceAsJson = new JSONObject(instanceResponse);
				String hrid = instanceAsJson.getString("hrid");
				
				//PREPARING TO ADD THE MARC RECORD TO SOURCE RECORD STORAGE:
				//CONSTRUCTING THE 999 OF THE MARC RECORD for FOLIO: 
				DataField field = MarcFactory.newInstance().newDataField();
				field.setTag("999");
				field.setIndicator1('f');
				field.setIndicator2('f');
				Subfield one = MarcFactory.newInstance().newSubfield('i', instanceId);
				Subfield two = MarcFactory.newInstance().newSubfield('s', recordTableId.toString());
				field.addSubfield(one);
				field.addSubfield(two);
				record.addVariableField(field);
			    if (record.getControlNumberField() != null) {
			    	record.getControlNumberField().setData(hrid);
			    }  else {
			    	ControlField cf = MarcFactory.newInstance().newControlField("001");
			    	cf.setData(hrid);
			    	record.addVariableField(cf);
			    }
			    
				//TRANSFORM THE RECORD INTO JSON
				logger.trace("MARC RECORD: " + record.toString());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				MarcJsonWriter jsonWriter =  new MarcJsonWriter(baos);
				jsonWriter.setUnicodeNormalization(true);
				jsonWriter.write(record);
				jsonWriter.close();
				String jsonString = baos.toString();
				JSONObject mRecord = new JSONObject(jsonString);
				JSONObject content = new JSONObject();
				content.put("content", mRecord);
				logger.trace("MARC TO JSON: " + mRecord.toString(3));

				//GET THE RAW MARC READY TO POST TO THE API
				ByteArrayOutputStream rawBaos = new ByteArrayOutputStream();
				MarcWriter writer = new MarcStreamWriter(rawBaos);
				writer.write(record);
				// TODO: is this a bug?...nothing was previously written to byteArrayOutputStream
				JSONObject jsonWithRaw = new JSONObject();
				jsonWithRaw.put("id", instanceId);
				jsonWithRaw.put("content", byteArrayOutputStream);
				
				//CREATING JOB EXECUTION?
				//TODO: I'M NOT ENTIRELY SURE IF THIS IS NECESSARY?
				//WHAT THE CONSEQUENCES OF THIS ARE?
				//TO POST TO SOURCE RECORD STORAGE, A SNAPSHOT ID
				//SEEMS TO BE REQUIRECD
				JSONObject jobExecution = new JSONObject();
				jobExecution.put("jobExecutionId", snapshotId.toString());
				jobExecution.put("status", "PARSING_IN_PROGRESS");
				logger.debug("post snapShot to source-storage");
				String snapShotResponse = apiService.callApiPostWithUtf8(baseOkapEndpoint + "source-storage/snapshots",  jobExecution, token);
				
				//OBJECT FOR SOURCE RECORD STORAGE API CALL:
				JSONObject sourceRecordStorageObject = new JSONObject();
				sourceRecordStorageObject.put("recordType", "MARC");
				sourceRecordStorageObject.put("snapshotId", snapshotId.toString());
				sourceRecordStorageObject.put("matchedId", recordTableId.toString());
				
				//LINK THE INSTANCE TO SOURCE RECORD STORAGE
				JSONObject externalId = new JSONObject();
				externalId.put("instanceId", instanceId);
				sourceRecordStorageObject.put("externalIdsHolder", externalId);
				
				//RAW RECORD
				JSONObject rawRecordObject = new JSONObject();
				rawRecordObject.put("id", instanceId);
				rawRecordObject.put("content", jsonWithRaw.toString());
				
				//PARSED RECORD
				JSONObject parsedRecord = new JSONObject();
				parsedRecord.put("id", instanceId);
				parsedRecord.put("content", mRecord);
				sourceRecordStorageObject.put("rawRecord", rawRecordObject);
				sourceRecordStorageObject.put("parsedRecord", parsedRecord);
				sourceRecordStorageObject.put("id", instanceId);
				
				//CALL SOURCE RECORD STORAGE POST
				logger.debug("post sourceRecordStoractObject");
				String storageResponse = apiService.callApiPostWithUtf8(baseOkapEndpoint + "source-storage/records", sourceRecordStorageObject,token);
				
				// Add Identifiers to the instance
				JSONArray identifiers = marcUtils.buildIdentifiers(record, lookupTable);
				Iterator isbnIterator = identifiers.iterator();
                List<String> isbnList = new ArrayList();
                while (isbnIterator.hasNext()) {
                    JSONObject identifierObj = (JSONObject) isbnIterator.next();
                    String identifierType = identifierObj.getString("identifierTypeId");
                    if (identifierType.equals(ISBNId)) {
                        isbnList.add((String) identifierObj.get("value"));
                   }
                }
                if (isbnList.size() > 0) {
                    responseMessage.put("isbn", isbnList.get(0));    
                }
                
                // now get Contributors
				JSONArray contributors = marcUtils.buildContributors(record, lookupTable);
				
				instanceAsJson.put("title", title);
				instanceAsJson.put("source", "MARC");
				instanceAsJson.put("instanceTypeId", lookupTable.get("text"));
				if (identifiers.length() > 0) {
                    logger.trace("Adding identifiers...");
                    instanceAsJson.put("identifiers", identifiers);
                }
                if (contributors.length() > 0) {
                    logger.trace("Adding contributors ...");
                    instanceAsJson.put("contributors", contributors);
                }
				instanceAsJson.put("discoverySuppress", false);				
				
				//GET THE HOLDINGS RECORD FOLIO CREATED, SO WE CAN ADD URLs FROM THE 856 IN THE MARC RECORD
				String holdingResponse = apiService.callApiGet(baseOkapEndpoint + "holdings-storage/holdings?query=(instanceId==" + instanceId + ")", token);
				JSONObject holdingsAsJson = new JSONObject(holdingResponse);
				JSONObject holdingRecord = holdingsAsJson.getJSONArray("holdingsRecords").getJSONObject(0);
							
				//UPDATE THE INSTANCE RECORD
				logger.debug("Update Instance Record");
				//instanceAsJson.put("electronicAccess", eResources);
				instanceAsJson.put("natureOfContentTermIds", new JSONArray());
				instanceAsJson.put("precedingTitles", new JSONArray());
				instanceAsJson.put("succeedingTitles", new JSONArray());
				logger.debug(instanceAsJson.toString(3));
				String instanceUpdateResponse = apiService.callApiPut(baseOkapEndpoint + "inventory/instances/" + instanceId,  instanceAsJson, token);
				
				logger.debug("Update holdings record");
				String createHoldingsResponse = apiService.callApiPut(baseOkapEndpoint + "holdings-storage/holdings/" + holdingRecord.getString("id"), holdingRecord,token);
				
				responseMessage.put("instanceHrid", hrid);
				responseMessage.put("instanceUUID", instanceId);
				//responseMessage.put("location", locationName +" ("+ lookupTable.get(locationName + "-location") +")");				
				responseMessages.put(responseMessage);
				numRec++;				
				
			} catch(Exception e) {
				e.printStackTrace();
				logger.error(e.toString());
				JSONObject errorMessage = new JSONObject();
				errorMessage.put("error", e.toString());
				errorMessage.put("PONumber", poNumberObj.get("poNumber"));
				errorMessages.put(errorMessage);
				return errorMessages;
			}
			
		}

		logger.info("Number of records: "+ numRec);
		logger.info(responseMessages.toString(3));
		return responseMessages;

	}
	
	
	// this method validates required values and will return a JSONArray with error messages or an empty array if it passes
	
	public JSONArray validateRequiredValues(MarcReader reader,String token, String baseOkapEndpoint ) {
		
	    Record record = null;
	    JSONArray errorMessages = new JSONArray();
		while(reader.hasNext()) {
			try {
		    	record = reader.next();    					    
		    	//GET THE 980, 981, 245, and 952 fields FROM THE MARC RECORD
			    DataField nineEighty = (DataField) record.getVariableField("980");
			    DataField nineEightyOne = (DataField) record.getVariableField("981");
			    DataField twoFourFive = (DataField) record.getVariableField("245");
			    DataField nineFiveTwo = (DataField) record.getVariableField("952");
			    
			    if (twoFourFive == null) {
					JSONObject errorMessage = new JSONObject();
					errorMessage.put("error", "Record is missing the 245 field");
					errorMessage.put("PONumber", "~error~");
					errorMessage.put("title", "~error~");
					errorMessages.put(errorMessage);
					continue;
				}
			    
			    String title = marcUtils.getTitle(twoFourFive); 
			    
				if (nineEighty == null) {
					JSONObject errorMessage = new JSONObject();
					errorMessage.put("error", "Record is missing the 980 field");
					errorMessage.put("PONumber", "~error~");
					errorMessage.put("title", title);
					errorMessages.put(errorMessage);
					continue;
				}
				
								
				String fundCode = marcUtils.getFundCode(nineEighty); 
				String vendorCode =  marcUtils.getVendorCode(nineEighty);
			    
				String quantity =  marcUtils.getQuantity(nineEighty);
				Integer quantityNo = 0;
			    if (quantity != null)  quantityNo = Integer.valueOf(quantity);				
			    
				 
			    Map<String, String> requiredFields = new HashMap<String, String>(); 
			    requiredFields.put("Fund code", fundCode);
			    requiredFields.put("Vendor Code", vendorCode);
			    
			    // MAKE SURE EACH OF THE REQUIRED SUBFIELDS HAS DATA
		        for (Map.Entry<String,String> entry : requiredFields.entrySet())  {
		        	if (entry.getValue()==null) {
		        		JSONObject errorMessage = new JSONObject();
		        		errorMessage.put("title", title);
		        		//errorMessage.put("theOne", theOne);
					    errorMessage.put("error", entry.getKey() + " Missing");
						errorMessage.put("PONumber", "~error~");
						errorMessages.put(errorMessage);
		        	}
		        }
		        
		        // return errorMessages if we found any so far...
		        if (!errorMessages.isEmpty()) return errorMessages;
		        
		        
			    //VALIDATE THE ORGANIZATION,  AND FUND
			    //STOP THE PROCESS IF AN ERRORS WERE FOUND
			    JSONObject orgValidationResult = validateOrganization(vendorCode, title, token, baseOkapEndpoint);
			    if (orgValidationResult != null) {
			    	logger.error("organization invalid: "+ vendorCode);
			    	logger.error(record.toString());
			    	errorMessages.put(orgValidationResult);				    
			    }
			    				    
			    JSONObject fundValidationResult = validateFund(fundCode, title, token, baseOkapEndpoint);
			    if (fundValidationResult != null) {
			    	//logger.error("fundCode invalid: "+ fundCode + " (price: "+ price +")");
			    	logger.error(record.toString());
			    	errorMessages.put(fundValidationResult);
			    }
			}  catch(Exception e) {
				e.printStackTrace();
		    	logger.fatal(e.getMessage());
		    	JSONObject errorMessage = new JSONObject();
		    	errorMessage.put("error", e.getMessage());
		    	errorMessage.put("PONumber", "~error~");
		    	errorMessages.put(errorMessage);
		    	return errorMessages;
		    }
		}
		return errorMessages;
		
	}
   
	public ServletContext getMyContext() {
		return myContext;
	}


	public void setMyContext(ServletContext myContext) {
		this.myContext = myContext;
	}
	
	static String readFile(String path, Charset encoding)  throws IOException  {
		  byte[] encoded = Files.readAllBytes(Paths.get(path));
		  return new String(encoded, encoding);
	}
	

	//TODO 
	//THESE VALIDATION METHODS COULD
	//USE IMPROVEMENT
	public JSONObject validateFund(String fundCode, String title, String token, String baseOkapiEndpoint) throws IOException, InterruptedException, Exception {
		
		//GET CURRENT FISCAL YEAR
		String fiscalYearCode =  (String) getMyContext().getAttribute("fiscalYearCode");
		String fundEndpoint = baseOkapiEndpoint + "finance/funds?limit=30&offset=0&query=((code='" + fundCode + "'))";
		
		JSONObject errorMessage = new JSONObject();
		
		String fundResponse = apiService.callApiGet(fundEndpoint, token);
		JSONObject fundsObject = new JSONObject(fundResponse);
		//----------->VALIDATION #1: MAKE SURE THE FUND CODE EXISTS
		if (fundsObject.getJSONArray("funds").length() < 1) {
			errorMessage.put("error", "Fund code in file (" + fundCode + ") does not exist in FOLIO");
			errorMessage.put("PONumber", "~error~");
			return errorMessage;
		}
		String fundId = (String) fundsObject.getJSONArray("funds").getJSONObject(0).get("id");
		logger.debug("FUNDS: " + fundsObject.get("funds"));
		
		//----------->VALIDATION #2: MAKE SURE THE FUND CODE FOR THE CURRENT FISCAL HAS ENOUGH MONEY
		String fundBalanceQuery = baseOkapiEndpoint + "finance/budgets?query=(name=="  + fundCode + "-" + fiscalYearCode + ")";
		String fundBalanceResponse = apiService.callApiGet(fundBalanceQuery, token);
		JSONObject fundBalanceObject = new JSONObject(fundBalanceResponse);
		if (fundBalanceObject.getJSONArray("budgets").length() < 1) {
			errorMessage.put("error", "Fund code in file (" + fundCode + ") does not have a budget");
			errorMessage.put("title", title);
			errorMessage.put("PONumber", "~error~");
			return errorMessage;
		}
		return null;
	}
	
	
	
	/**
	 * @param orgCode
	 * @param title
	 * @param token
	 * @param baseOkapiEndpoint
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public JSONObject validateOrganization(String orgCode, String title,  String token, String baseOkapiEndpoint ) throws IOException, InterruptedException, Exception {
		JSONObject errorMessage = new JSONObject();

		try {
			// URL encode organization code to avoid cql parse error on forward slash
			String encodedOrgCode = URLEncoder.encode("\"" + orgCode + "\"", StandardCharsets.UTF_8.name());
			logger.debug("encodedOrgCode: " + encodedOrgCode);

			//LOOK UP THE ORGANIZATION
			String organizationEndpoint = baseOkapiEndpoint + "organizations-storage/organizations?query=(code=" + encodedOrgCode + ")";
			logger.debug("organizationEndpoint: " + organizationEndpoint);
			String orgLookupResponse = apiService.callApiGet(organizationEndpoint, token);
			JSONObject orgObject = new JSONObject(orgLookupResponse);
			//---------->VALIDATION: MAKE SURE THE ORGANIZATION CODE EXISTS
			if (orgObject.getJSONArray("organizations").length() < 1) {
				logger.error(orgObject.toString(3));
				errorMessage.put("error", "Organization code in file (" + orgCode + ") does not exist in FOLIO");
				errorMessage.put("title", title);
				errorMessage.put("PONumber", "~error~");
				return errorMessage;
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			errorMessage.put("error", "Unable to URL encoode organization code " + orgCode);
			errorMessage.put("PONumber", "~error~");
			return errorMessage;
		}
	}
	
	/**
	 * @return and array with environment variable missing errors or null if no errors
	 */
	public JSONArray validateEnvironment( ) {
	    JSONArray errors = new JSONArray();
	    if (StringUtils.isEmpty((String) getMyContext().getAttribute("baseOkapEndpoint"))) {
	       JSONObject errMsg = new JSONObject();
	       errMsg.put("error", "baseOkapEndpoint environment variable not found");
	       errors.put(errMsg);     
	    }
        if (StringUtils.isEmpty((String) getMyContext().getAttribute("okapi_username"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "api user environment variable not found");
            errors.put(errMsg);
        }
        if (StringUtils.isEmpty((String) getMyContext().getAttribute("okapi_username"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "api password environment variable not found");
            errors.put(errMsg);
        }
        if (StringUtils.isEmpty((String) getMyContext().getAttribute("tenant"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "api tenant environment variable not found");
            errors.put(errMsg);
        }
        if (StringUtils.isEmpty((String) getMyContext().getAttribute("fiscalYearCode"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "fiscalYearCode environment variable not found");
            errors.put(errMsg);
        }
        if (StringUtils.isEmpty((String) getMyContext().getAttribute("billTo"))) {
            JSONObject errMsg = new JSONObject();
            errMsg.put("error", "billTo environment variable not found");
            errors.put(errMsg);
        }
        if (errors.isEmpty()) {
	        return null;
        } else {
            return errors;
        }
	}

}
