package org.olf.folio.order.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.marc4j.MarcJsonWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MarcUtils {
	
	private final String TITLE = "a";  // 245$a
	private final String TITLE2 = "b"; // 245$b
	private final String TITLE3 = "c"; // 245$c
	
	private final String PRICE = "i"; // 981$i
	private final String CURRENCY = "k"; // 981$k
	private final String RUSH = "q"; // 981$q
	
	private final String QUANTITY = "g"; // 980$g
	private final String FUNDCODE = "h"; // 980$h
	private final String VENDOR = "v"; // 980$v
	private final String VENDORID = "i"; // 961$i...formerly 980$c
	
	//private final String NOTE = "n"; // 980$n
	private final String INTERNAL_NOTE = "z"; // 980$z
	private final String RECEIVING_NOTE = "n"; // 981$n
	 
	private final String PERSONNAME = "s"; // 980$s
	private final String ELECTRONIC = "z"; // 980$z
	private final String SELECTOR = "y"; // 980$y Selector	
	
	private final String LOCATION = "b"; // 952$b
	private final String REQUESTER = "r"; // 981$r
	
	private final String PUBLISHER = "b"; // 264$b
    private final String PUBLISHER_LOCATION = "a"; // 264$a
    private final String PUBLICATION_DATE = "c"; // 264$c
    
    private final String BARCODE = "p"; // 976$p

	public MarcUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public String getTitle(DataField twoFourFive) {
		String title = new String();
		if (twoFourFive != null) {
		    title = twoFourFive.getSubfieldsAsString(TITLE);
		    String titleTwo = twoFourFive.getSubfieldsAsString(TITLE2);
	        String titleThree = twoFourFive.getSubfieldsAsString(TITLE3);
	        if (titleTwo != null) title += " " + titleTwo;
	        if (titleThree != null) title += " " + titleThree;
		}
	    return title;
	}
	
	/**
	 * getPrice from marc, defalt to 0.00.
	 * @param nineEightyOne
	 * @return
	 */
	public String getPrice(DataField nineEightyOne) {
        String price = new String();
        if (nineEightyOne != null) {
            price = nineEightyOne.getSubfieldsAsString(PRICE);
            if (price == null) {
                price = "0.00";
            } else  {
                return normalizePrice(price);
            }
        } else {
            price = "0.00";   
        }
        return price;
    }
	
	public String getQuantity(DataField nineEighty ) {
		String quantity = new String();
		if (nineEighty != null) {
	        quantity =  nineEighty.getSubfieldsAsString(QUANTITY);
		}
	    return quantity;
	}
	
	public String getFundCode(DataField nineEighty ) {
		String fundCode = new String();
		if (nineEighty != null) {
			fundCode = nineEighty.getSubfieldsAsString(FUNDCODE);	       
		}
	    return fundCode;
	}
	
	public String getVendorCode(DataField nineEighty ) {
		String vendorCode = new String();
		if (nineEighty != null) {
		    vendorCode = nineEighty.getSubfieldsAsString(VENDOR);
		}
		return vendorCode;
	}
	
	public String getVendorItemId(DataField nineSixtyOne ) {
        String vendorItemId= new String();
        if (nineSixtyOne != null) {
            vendorItemId = nineSixtyOne.getSubfieldsAsString(VENDORID);
        } else {
            return null;
        }
        return vendorItemId;
    }
	
	public String getLocation(DataField nineFiveTwo ) {
		String location = new String();
	    if (nineFiveTwo != null) {
	       location = nineFiveTwo.getSubfieldsAsString(LOCATION); 
	    } else {
	        location = "olin";
	    }
	    return location;
	}
	
	public String getRequester(DataField nineEightyOne ) {
		String requester = new String();
	    if (nineEightyOne != null) {
	    	requester = nineEightyOne.getSubfieldsAsString(REQUESTER);
	    } 
	    return requester;
	}
	
	public String getCurrency(DataField nineEightyOne ) {
		String currency = new String();
	    if (nineEightyOne != null) {
	    	currency = nineEightyOne.getSubfieldsAsString(CURRENCY);
	    } 
	    return currency;
	}
	
	public String getRush(DataField nineEightyOne ) {
		String rush = new String();
	    if (nineEightyOne != null) {
	    	rush = nineEightyOne.getSubfieldsAsString(RUSH);
	    } 
	    return rush;
	}
	
	public String getInternalNotes(DataField nineEighty ) {
		String notes = new String();
		if (nineEighty != null) {
		    notes =  nineEighty.getSubfieldsAsString(INTERNAL_NOTE);
		}
		return notes;
	}
	
	public String getReceivingNote(DataField nineEightyOne ) {
		String notes = new String();
		if (nineEightyOne != null) {
		    notes =  nineEightyOne.getSubfieldsAsString(RECEIVING_NOTE);
		}
		return notes;
	}
	
	public String getSelector(DataField nineEighty ) {
		String email = new String();
		if (nineEighty != null) {
		    email = nineEighty.getSubfieldsAsString(SELECTOR);
		}
		return email;
	}
	
	public String getPersonName(DataField nineEighty ) {
		String personName = new String();
		if (nineEighty != null) {
		    personName = nineEighty.getSubfieldsAsString(PERSONNAME);
		}
		return personName;
	}
	
	public String getElectronicIndicator(DataField nineEighty ) {
		String electronicIndicator = new String();
		if (nineEighty != null) {
			electronicIndicator = nineEighty.getSubfieldsAsString(ELECTRONIC);
		}
		return electronicIndicator;
	}
	
	public String getPublisher(Record record) {
        String publisher = new String();
        List<DataField> datafields = new ArrayList<DataField>();
        List<DataField> fields = record.getDataFields();        
        
        for (DataField df: fields) {
            char id2 = df.getIndicator2();
            if (df.getTag().equals("264") && id2 == '1' ) {
                datafields.add(df);
            }
        }
        if (datafields.size() > 0 ) {
            DataField twoSixtyFour = datafields.get(0);
            List<Subfield> subfields = twoSixtyFour.getSubfields(PUBLISHER);
            
            for (Subfield sf: subfields) {
                publisher += sf.getData() + " ";
            }
        } else {
            return null;
        }
        return StringUtils.removeEnd(publisher.trim(), ",").trim();
    }
    
    public String getPublicationDate(DataField twoSixtyFour ) {
        String publicationDate = new String();
        if (twoSixtyFour != null) {
            publicationDate = twoSixtyFour.getSubfieldsAsString(PUBLICATION_DATE);
            
        } else {
            return null;
        }
        return matchYear(publicationDate);
    }
	
    public String getBarcode(DataField df ) {
        // expecting a 976 field
        String barcode = new String();
        if (df != null) {
            barcode = df.getSubfieldsAsString(BARCODE);
        } else {
            return null;
        }
        return barcode.trim();
    }
	
	public JSONArray getLinks(Record record) {
		JSONArray eResources = new JSONArray();
		List<VariableField> urls = record.getVariableFields("856"); 
		Iterator<VariableField> iterator = urls.iterator();
		String linkText = new String("Avaialble to Snapshot Users");
		while (iterator.hasNext()) {
			DataField dataField = (DataField) iterator.next();
			if (dataField != null && dataField.getSubfield('u') != null) {
				String url = dataField.getSubfield('u').getData();
				if (dataField.getSubfield('z') != null) {
					linkText = dataField.getSubfield('z').getData();
				}
				JSONObject eResource = new JSONObject();
				eResource.put("uri", dataField.getSubfield('u').getData());
				//TODO - DO WE WANT TO CHANGE THE LINK TEXT?
				eResource.put("linkText", linkText);
				//I 'THINK' THESE RELATIONSHIP TYPES ARE HARDCODED INTO FOLIO
				//CANT BE LOOKED UP WITH AN API?
				//https://github.com/folio-org/mod-inventory-storage/blob/master/reference-data/electronic-access-relationships/resource.json
				eResource.put("relationshipId", "f5d0068e-6272-458e-8a81-b85e7b9a14aa");
				eResources.put(eResource);
			}
		}
		return eResources;
		
		
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
   
    public List<String> getSeriesFields(Record record) {
        List<DataField> fields = record.getDataFields();
        Iterator<DataField> fieldsIterator = fields.iterator();
        List<String> seriesFields = new ArrayList<String>(); 
        
        while (fieldsIterator.hasNext()) {
            
            DataField field = (DataField) fieldsIterator.next();
            if (StringUtils.equals(field.getTag(), "490") || StringUtils.equals(field.getTag(), "830") ) {
                seriesFields.add(field.toString());
            }
        }
        return seriesFields;
    }
	
	public String normalizePrice(String priceStr) {
	    try {
	       double f = Double.parseDouble(priceStr);
	       return String.format("%.2f", new BigDecimal(f));
	    } catch (NumberFormatException e) {
	        return "0.00";
	    }
	}
	
	public String matchYear(String pubDate) {
        String year = new String();
        try {
            Pattern pattern = Pattern.compile("(\\d{4})");
            Matcher matcher = pattern.matcher(pubDate);

            if (matcher.find()) {
               year = matcher.group(1);
            }
            return year;
        } catch (Exception e) {
            return "";
        }
    }
	

    public String recordToMarcJson(Record record) throws IOException {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        final MarcJsonWriter writer = new MarcJsonWriter(out);
        trimBarcode(record);
        writer.write(record);
        writer.close();
        return out.toString();
      }
    }

    private void trimBarcode(Record record) {
      DataField nineSevenSix = (DataField) record.getVariableField("976");
      String barcode = getBarcode(nineSevenSix);

      // Trim spaces from barcode (976$p)
      if (barcode != null) {
        // Remove existing subfield
        Character barcodeCode = BARCODE.charAt(0);
        Subfield originalBarcode = nineSevenSix.getSubfield(barcodeCode);
        nineSevenSix.removeSubfield(originalBarcode);

        // Add new subfield with trimmed barcode
        MarcFactory factory = MarcFactory.newInstance();
        nineSevenSix.addSubfield(factory.newSubfield(barcodeCode, barcode));
      }
    }
}
