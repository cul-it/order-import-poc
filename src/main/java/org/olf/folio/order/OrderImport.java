package org.olf.folio.order;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream; 
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	private HashMap<String,String> lookupTable;
	private String tenant;
	
	private ApiService apiService;
	MarcUtils marcUtils = new MarcUtils();
	
	public  JSONArray  upload(String fileName) throws IOException, InterruptedException, Exception {

		logger.debug("...starting...");
		JSONArray responseMessages = new JSONArray();
		JSONArray errorMessages = new JSONArray();
		
		//COLLECT VALUES FROM THE CONFIGURATION FILE
		String baseOkapEndpoint = (String) getMyContext().getAttribute("baseOkapEndpoint");
		String apiUsername = (String) getMyContext().getAttribute("okapi_username");
		String apiPassword = (String) getMyContext().getAttribute("okapi_password");
		tenant = (String) getMyContext().getAttribute("tenant"); 
		
		String permELocationName = (String) getMyContext().getAttribute("permELocation");
		String noteTypeName = (String) getMyContext().getAttribute("noteType");
		String materialTypeName = (String) getMyContext().getAttribute("materialType");
		
		//GET THE FOLIO TOKEN
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", apiUsername);
		jsonObject.put("password", apiPassword);
		jsonObject.put("tenant",tenant);
		
		this.apiService = new ApiService(tenant);
		String token = this.apiService.callApiAuth( baseOkapEndpoint + "authn/login",  jsonObject);	
		
		//TODO: REMOVE
		logger.debug("TOKEN: " + token);  
		
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
			 myContext.setAttribute(Constants.LOOKUP_TABLE, lookupTable);
			 logger.debug("put lookup table in context");
		} else {
			 this.lookupTable = (HashMap<String, String>) myContext.getAttribute(Constants.LOOKUP_TABLE);
			 logger.debug("got lookup table from context");
		}		 
		
		// READ THE MARC RECORD FROM THE FILE
		in = new FileInputStream(filePath + fileName);
		reader = new MarcStreamReader(in);
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		MarcWriter w = new MarcStreamWriter(byteArrayOutputStream,"UTF-8");
		
		AnselToUnicode conv = new AnselToUnicode();
		w.setConverter(conv);
		
		// GENERATE UUID for the PO
	   
	    UUID orderUUID = UUID.randomUUID();	    
	    Map<Integer, UUID> orderLineMap = new HashMap<Integer, UUID>(); 
	    
	    //GET THE NEXT PO NUMBER
		logger.trace("get next PO number");
		String poNumber = this.apiService.callApiGet(baseOkapEndpoint + "orders/po-number", token);		
		JSONObject poNumberObj = new JSONObject(poNumber);
		logger.debug("NEXT PO NUMBER: " + poNumberObj.get("poNumber"));
		
		
		
		// CREATING THE PURCHASE ORDER
		JSONObject order = new JSONObject();
		
		order.put("orderType", "One-Time");
		order.put("reEncumber", false);
		order.put("id", orderUUID.toString());
		order.put("approved", true);
		order.put("workflowStatus","Open");
		order.put("poNumber", poNumberObj.get("poNumber"));
		
		JSONArray poLines = new JSONArray();
		
		// iterator over records in the marc file.
	    Record record = null;
		logger.debug("reading marc file");
		int numRec = 0; 
		
		while (reader.hasNext()) {
			try {
				record = reader.next();
				//System.out.println(record.toString());				
				 
				DataField twoFourFive = (DataField) record.getVariableField("245");
				DataField nineEighty = (DataField) record.getVariableField("980");
			    DataField nineFiveTwo = (DataField) record.getVariableField("952");
			    DataField nineEightyOne = (DataField) record.getVariableField("981");
			    
				String title = marcUtils.getTitle(twoFourFive); 
						 
				String fundCode = marcUtils.getFundCode(nineEighty);
				String vendorCode =  marcUtils.getVendorCode(nineEighty);
				    
				String quantity =  marcUtils.getQuantity(nineEighty);
				Integer quantityNo = 0; //INIT
			    if (quantity != null)  quantityNo = Integer.valueOf(quantity);
			    
				String price = marcUtils.getPrice(nineEighty, nineEightyOne); 
				String vendorItemId = marcUtils.getVendorItemId(nineEighty);
				String selector = marcUtils.getSelector(nineEighty);
				String personName = marcUtils.getPersonName(nineEighty);
			    
			    String locationName = marcUtils.getLocation(nineFiveTwo);
			    String requester = marcUtils.getRequester(nineEightyOne);
			    String rush = marcUtils.getRush(nineEightyOne);
			     
			    //LOOK UP VENDOR
			    //System.out.println("lookupVendor");
				String organizationEndpoint = baseOkapEndpoint + "/organizations-storage/organizations?limit=30&offset=0&query=((code='" + vendorCode + "'))";
				String orgLookupResponse = this.apiService.callApiGet(organizationEndpoint,  token);
				JSONObject orgObject = new JSONObject(orgLookupResponse);
				String vendorId = (String) orgObject.getJSONArray("organizations").getJSONObject(0).get("id");
				
				//LOOK UP THE FUND
				//System.out.println("lookup Fund");
				String fundEndpoint = baseOkapEndpoint + "finance/funds?limit=30&offset=0&query=((code='" + fundCode + "'))";
				String fundResponse = this.apiService.callApiGet(fundEndpoint, token);
				JSONObject fundsObject = new JSONObject(fundResponse);
				String fundId = (String) fundsObject.getJSONArray("funds").getJSONObject(0).get("id");				
				
				// CREATING THE PURCHASE ORDER				
				
				order.put("vendor", vendorId);				
				
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
				 
				
				//VENDOR REFERENCE NUMBER IF INCLUDED IN THE MARC RECORD:
				if (vendorItemId != null) {
					JSONObject vendorDetail = new JSONObject();
					vendorDetail.put("instructions", "");
					vendorDetail.put("refNumber", vendorItemId);
					vendorDetail.put("refNumberType", "Internal vendor number");
					vendorDetail.put("vendorAccount", "");
					orderLine.put("vendorDetail", vendorDetail);
				}				
				UUID orderLineUUID = UUID.randomUUID();
				orderLine.put("id", orderLineUUID);
				orderLineMap.put(numRec, orderLineUUID); 
				
				orderLine.put("source", "User");
				cost.put("currency", "USD");
				orderLine.put("cost", cost);
				orderLine.put("locations", locations);
				orderLine.put("titleOrPackage",title);
				orderLine.put("acquisitionMethod", "Purchase");
				
				if (StringUtils.isNotEmpty(rush) && StringUtils.contains(rush.toLowerCase(), "rush")) {
					orderLine.put("rush", true);
				}
				
				if (StringUtils.isNotEmpty(selector)) {
					orderLine.put("selector", selector);
				}
				
				if (StringUtils.isNotEmpty(requester)) {
					orderLine.put("requester", requester);
				}
				
				JSONArray funds = new JSONArray();
				JSONObject fundDist = new JSONObject();
				fundDist.put("distributionType", "percentage");
				fundDist.put("value", 100);
				fundDist.put("fundId", fundId);
				funds.put(fundDist);
				orderLine.put("fundDistribution", funds);
				String numRecString = new String();
				if (numRec < 10 ) {
					numRecString = "0"+ String.valueOf(numRec);
				} else {
					numRecString = String.valueOf(numRec);
				} 
					
				String poLineNumber = "a" + poNumberObj.get("poNumber") + "-"+ numRecString;
				orderLine.put("poLineNumber", poLineNumber);
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
		
		logger.debug("Here is the PO order number: "+ poNumberObj.get("poNumber"));
		logger.debug(order.toString(3));
		
		//POST THE ORDER AND LINE:
		String orderResponse = apiService.callApiPostWithUtf8(baseOkapEndpoint + "orders/composite-orders", order, token); 
		JSONObject approvedOrder = new JSONObject(orderResponse);
		 
		// get approved order
		
		//GET THE UPDATED PURCHASE ORDER FROM THE API AND PULL OUT THE ID FOR THE INSTANCE FOLIO CREATED:
		logger.debug("getUpdatedPurchaseOrder");
		String updatedPurchaseOrder = apiService.callApiGet(baseOkapEndpoint + "orders/composite-orders/" +orderUUID.toString() ,token); 
		JSONObject updatedPurchaseOrderJson = new JSONObject(updatedPurchaseOrder);
		logger.debug("updated purchase order...");
		logger.debug(updatedPurchaseOrderJson.toString(3));
		
		//JSONObject poMessage = new JSONObject();
		//poMessage.put("poNum", poNumberObj.get("poNumber"));
		//responseMessages.put(poMessage);
		
		// read through again.
		FileInputStream in2 = new FileInputStream(filePath + fileName);
		MarcReader reader2 = new MarcStreamReader(in2);
		numRec = 0; 
		
		while (reader2.hasNext()) {
			try {
				 
				record = reader2.next();				
				JSONObject responseMessage = new JSONObject();
				responseMessage.put("poNumber", poNumberObj.get("poNumber"));				 
				
				DataField twoFourFive = (DataField) record.getVariableField("245");
				DataField nineEighty = (DataField) record.getVariableField("980");
				DataField nineEightyOne = (DataField) record.getVariableField("981");
			    DataField nineFiveTwo = (DataField) record.getVariableField("952"); 
			    
				String title = marcUtils.getTitle(twoFourFive);
				responseMessage.put("title", title);
				
				String notes =  marcUtils.getInternalNotes(nineEightyOne);
				String locationName = marcUtils.getLocation(nineFiveTwo);
				
				//INSERT THE NOTE IF THERE IS A NOTE IN THE MARC RECORD
				if (notes != null && !notes.equalsIgnoreCase("")) {
					logger.debug("NOTE TYPE NAME: " + noteTypeName);
					 
					JSONObject noteAsJson = new JSONObject();
					JSONArray links = new JSONArray();
					JSONObject link = new JSONObject();
					link.put("type", "poLine");
					link.put("id", orderLineMap.get(numRec));
					links.put(link);
					noteAsJson.put("links", links);
					noteAsJson.put("typeId", lookupTable.get(noteTypeName));
					noteAsJson.put("domain", "orders");
					noteAsJson.put("content", notes);
					noteAsJson.put("title", notes);
					logger.debug("post noteAsJson");
					String noteResponse = apiService.callApiPostWithUtf8(baseOkapEndpoint + "/notes", noteAsJson, token); 
					logger.debug(noteResponse);
				}
				
				UUID snapshotId = UUID.randomUUID();
				UUID recordTableId = UUID.randomUUID();					
				
				String instanceId = updatedPurchaseOrderJson.getJSONArray("compositePoLines").getJSONObject(numRec).getString("instanceId");
				
				responseMessage.put("id", orderLineMap.get(numRec));
				
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
				JSONObject jsonWithRaw = new JSONObject();
				jsonWithRaw.put("id", instanceId);
				jsonWithRaw.put("content",byteArrayOutputStream);
				
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
				sourceRecordStorageObject.put("matchedId", instanceId.toString());
				
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
				
				//ADD IDENTIFIERS AND CONTRIBUTORS TO THE INSTANCE
				//*AND* CHANGE THE SOURCE TO 'MARC'
				//SO THE OPTION TO VIEW THE MARC RECORD SHOWS UP 
				//IN INVENTORY!
				JSONArray identifiers = buildIdentifiers(record, lookupTable);
				JSONArray contributors = buildContributors(record, lookupTable);
				
				instanceAsJson.put("title", title);
				instanceAsJson.put("source", "MARC");
				instanceAsJson.put("instanceTypeId", lookupTable.get("text"));
				instanceAsJson.put("identifiers", identifiers);
				instanceAsJson.put("contributors", contributors);
				instanceAsJson.put("discoverySuppress", false);
				
				
				//GET THE HOLDINGS RECORD FOLIO CREATED, SO WE CAN ADD URLs FROM THE 856 IN THE MARC RECORD
				String holdingResponse = apiService.callApiGet(baseOkapEndpoint + "holdings-storage/holdings?query=(instanceId==" + instanceId + ")", token);
				JSONObject holdingsAsJson = new JSONObject(holdingResponse);
				JSONObject holdingRecord = holdingsAsJson.getJSONArray("holdingsRecords").getJSONObject(0);
				
				// TODO: Do we need to include 856 fields now?
				//JSONArray eResources = new JSONArray();
				//String linkText = (String) getMyContext().getAttribute("textForElectronicResources");
				
				// TODO: clean this up...
				//logger.debug("Add 856 fields");
				//List urls =  record.getVariableFields("856");
				//Iterator<DataField> iterator = urls.iterator();
				//while (iterator.hasNext()) {
				//	DataField dataField = (DataField) iterator.next();
				//	if (dataField != null && dataField.getSubfield('u') != null) {
				//		String url = dataField.getSubfield('u').getData();
				//		if (dataField.getSubfield('z') != null) {
				//			linkText = dataField.getSubfield('z').getData();
				//		}
				//		JSONObject eResource = new JSONObject();
				//		eResource.put("uri", dataField.getSubfield('u').getData());
				//		//TODO - DO WE WANT TO CHANGE THE LINK TEXT?
				//		eResource.put("linkText", linkText);
						//I 'THINK' THESE RELATIONSHIP TYPES ARE HARDCODED INTO FOLIO
						//CANT BE LOOKED UP WITH AN API?
						//https://github.com/folio-org/mod-inventory-storage/blob/master/reference-data/electronic-access-relationships/resource.json
				//		eResource.put("relationshipId", "f5d0068e-6272-458e-8a81-b85e7b9a14aa");
				//		eResources.put(eResource);
				//	}
				//}
				
				//UPDATE THE INSTANCE RECORD
				logger.debug("Update Instance Record");
				//instanceAsJson.put("electronicAccess", eResources);
				instanceAsJson.put("natureOfContentTermIds", new JSONArray());
				instanceAsJson.put("precedingTitles", new JSONArray());
				instanceAsJson.put("succeedingTitles", new JSONArray());
				String instanceUpdateResponse = apiService.callApiPut(baseOkapEndpoint + "inventory/instances/" + instanceId,  instanceAsJson, token);
				
				//UPDATE THE HOLDINGS RECORD
				//holdingRecord.put("electronicAccess", eResources);
		
				logger.debug("Update holdings record");
				String createHoldingsResponse = apiService.callApiPut(baseOkapEndpoint + "holdings-storage/holdings/" + holdingRecord.getString("id"), holdingRecord,token);
				
				responseMessage.put("theOne", hrid);
				responseMessage.put("location", locationName +" ("+ lookupTable.get(locationName + "-location") +")");				
				responseMessages.put(responseMessage);
				numRec++;
				
				
			} catch(Exception e) {
				e.printStackTrace();
				logger.error(e.toString());
				JSONObject errorMessage = new JSONObject();
				errorMessage.put("error",e.toString());
				errorMessage.put("PONumber", poNumberObj.get("poNumber"));
				errorMessages.put(errorMessage);
				return errorMessages;
			}
		}

		logger.debug("Number of records: "+ numRec);
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
				
				//if (nineFiveTwo == null) {
				//	JSONObject errorMessage = new JSONObject();
				//	errorMessage.put("error", "Record is missing the 952 field");
				//	errorMessage.put("PONumber", "~error~");
				//	errorMessage.put("title", title);
				//	errorMessages.put(errorMessage);
				//	continue;
				//}
				
				String fundCode = marcUtils.getFundCode(nineEighty); 
				String vendorCode =  marcUtils.getVendorCode(nineEighty);
				String price = marcUtils.getPrice(nineEighty, nineEightyOne);
			    
				String quantity =  marcUtils.getQuantity(nineEighty);
				Integer quantityNo = 0;
			    if (quantity != null)  quantityNo = Integer.valueOf(quantity);				
			    
				 
			    Map<String, String> requiredFields = new HashMap<String, String>(); 
			    requiredFields.put("Fund code", fundCode);
			    requiredFields.put("Vendor Code", vendorCode);
			    requiredFields.put("Price" , price); 
			    
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
			    				    
			    JSONObject fundValidationResult = validateFund(fundCode, title, token, baseOkapEndpoint, price);
			    if (fundValidationResult != null) {
			    	logger.error("fundCode invalid: "+ fundCode + " (price: "+ price +")");
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
	
	
	//TODO - FIX THESE METHODS THAT GATHER DETAILS FROM THE MARC RECORD.
	//THEY WERE HURRILY CODED
	//JUST WANTED TO GET SOME DATA IN THE INSTANCE
	//FROM THE MARC RECORD FOR THIS POC
	public JSONArray buildContributors(Record record, HashMap<String,String> lookupTable) {
		JSONArray contributors = new JSONArray();
		String[] subfields = {"a","b","c","d","f","g","j","k","l","n","p","t","u"};
		
		List<DataField> fields = record.getDataFields();
		Iterator<DataField> fieldsIterator = fields.iterator();
		while (fieldsIterator.hasNext()) {
			DataField field = (DataField) fieldsIterator.next();
			if (field.getTag().equalsIgnoreCase("100") || field.getTag().equalsIgnoreCase("700")) {
				contributors.put(makeContributor(field,lookupTable, "Personal name", subfields));
			}
		}
		return contributors;
	}
	
	public JSONObject makeContributor( DataField field, HashMap<String,String> lookupTable, String name_type_id, String[] subfieldArray) {
		List<String> list = Arrays.asList(subfieldArray);
		JSONObject contributor = new JSONObject();
		contributor.put("name", "");
		contributor.put("contributorNameTypeId", lookupTable.get(name_type_id));
		List<Subfield> subfields =  field.getSubfields();
		Iterator<Subfield> subfieldIterator = subfields.iterator();
		String contributorName = "";
		while (subfieldIterator.hasNext()) {
			Subfield subfield = (Subfield) subfieldIterator.next();
			String subfieldAsString = String.valueOf(subfield.getCode());  
			if (subfield.getCode() == '4') {
				if (lookupTable.get(subfield.getData()) != null) {
					contributor.put("contributorTypeId", lookupTable.get(subfield.getData()));
				}
				else {
					contributor.put("contributorTypeId", lookupTable.get("bkp"));
				}
			}
			else if (subfield.getCode() == 'e') {
				contributor.put("contributorTypeText", subfield.getData());
			}
			else if (list.contains(subfieldAsString)) {
				if (!contributorName.isEmpty()) {
					contributorName += ", " + subfield.getData();
				}
				else {
					contributorName +=  subfield.getData();
				}
			}
			
		}
		contributor.put("name", contributorName);
		return contributor;
	}
	
   
   public JSONArray buildIdentifiers(Record record, HashMap<String, String> lookupTable) {
		JSONArray identifiers = new JSONArray();
		
		List<DataField> fields = record.getDataFields();
		Iterator<DataField> fieldsIterator = fields.iterator();
		while (fieldsIterator.hasNext()) {
			DataField field = (DataField) fieldsIterator.next(); 
			List<Subfield> subfields =  field.getSubfields();
			Iterator<Subfield> subfieldIterator = subfields.iterator();
			while (subfieldIterator.hasNext()) {
				Subfield subfield = (Subfield) subfieldIterator.next();
				if (field.getTag().equalsIgnoreCase("020")) {
					if (subfield.getCode() == 'a') {
						JSONObject identifier = new JSONObject();
						String fullValue = subfield.getData();
						if (field.getSubfield('c') != null) fullValue += " "  + field.getSubfieldsAsString("c");
						if (field.getSubfield('q') != null) fullValue += " " + field.getSubfieldsAsString("q");
						identifier.put("value",fullValue);
						
						identifier.put("identifierTypeId", lookupTable.get("ISBN"));
						identifiers.put(identifier);
					}
					if (subfield.getCode() == 'z') {
						JSONObject identifier = new JSONObject();
						String fullValue = subfield.getData();
						if (field.getSubfield('c') != null) fullValue += " " + field.getSubfieldsAsString("c");
						if (field.getSubfield('q') != null) fullValue += " " + field.getSubfieldsAsString("q");
						identifier.put("value", fullValue);
						identifier.put("identifierTypeId", lookupTable.get("Invalid ISBN"));
						identifiers.put(identifier);
					}
				}
				if (field.getTag().equalsIgnoreCase("022")) {
					if (subfield.getCode() == 'a') {
						JSONObject identifier = new JSONObject();
						String fullValue = subfield.getData();
						if (field.getSubfield('c') != null) fullValue += " " + field.getSubfieldsAsString("c");
						if (field.getSubfield('q') != null) fullValue += " " + field.getSubfieldsAsString("q");
						identifier.put("value",fullValue);
						
						identifier.put("identifierTypeId", lookupTable.get("ISSN"));
						identifiers.put(identifier);
					} else if (subfield.getCode() == 'l') {
						JSONObject identifier = new JSONObject();
						String fullValue = subfield.getData();
						if (field.getSubfield('c') != null) fullValue += " " + field.getSubfieldsAsString("c");
						if (field.getSubfield('q') != null) fullValue += " " + field.getSubfieldsAsString("q");
						identifier.put("value", fullValue);
						identifier.put("identifierTypeId", lookupTable.get("Linking ISSN"));
						identifiers.put(identifier);
					} else {
						JSONObject identifier = new JSONObject();
						String fullValue = "";
						if (field.getSubfield('z') != null) fullValue += field.getSubfieldsAsString("z");
						if (field.getSubfield('y') != null) fullValue += " " +  field.getSubfieldsAsString("y");
						if (field.getSubfield('m') != null) fullValue += " " + field.getSubfieldsAsString("m");
						if (fullValue != "") {
							identifier.put("value", fullValue);
							identifier.put("identifierTypeId", lookupTable.get("Invalid ISSN"));
							identifiers.put(identifier);
						}
					}
				}
				
				
			}
			
		}
		return identifiers;
		
		
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
	public JSONObject validateFund(String fundCode, String title, String token, String baseOkapiEndpoint, String price ) throws IOException, InterruptedException, Exception {
		
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
	
	
	
	public JSONObject validateOrganization(String orgCode, String title,  String token, String baseOkapiEndpoint ) throws IOException, InterruptedException, Exception {
		JSONObject errorMessage = new JSONObject();
	    //LOOK UP THE ORGANIZATION
	    String organizationEndpoint = baseOkapiEndpoint + "organizations-storage/organizations?limit=30&offset=0&query=((code='" + orgCode + "'))";
	    String orgLookupResponse = apiService.callApiGet(organizationEndpoint,  token);
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
	}
	

}
