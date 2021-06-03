package org.olf.folio.order.util;

 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.marc4j.marc.DataField;
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
	
	/**
	 * Get an array of productIds that contain ISBN values.
	 * @param record
	 * @param isbnType
	 * @return
	 */
	public JSONArray getISBN(Record record, String isbnType) {
        JSONArray productIds = new JSONArray();
        DataField df = (DataField) record.getVariableField("020");
        if (df != null) {
            List<Subfield> subfields =  df.getSubfields();
            for (Subfield subfield: subfields) {
                if (subfield.getCode() == 'a') {
                    JSONObject productId = new JSONObject();
                    String fullValue = subfield.getData();
                    if (df.getSubfield('c') != null) {
                        fullValue += " "  + df.getSubfieldsAsString("c");
                    }
                    if (df.getSubfield('q') != null) {
                        fullValue += " " + df.getSubfieldsAsString("q");
                    }
                    String isbn = StringUtils.substringBefore(fullValue, " ");
                    String qualifier = StringUtils.substringAfter(fullValue, " ");
                    productId.put("productId", isbn);
                    productId.put("productIdType", isbnType);
                    if (StringUtils.isNotEmpty(qualifier)) {
                        productId.put("qualifier", qualifier);
                    }
                    productIds.put(productId);
               
                } 
            }
            
        }
        return productIds;
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
	
	public String normalizePrice(String priceStr) {
	    try {
	       double f = Double.parseDouble(priceStr);
	       return String.format("%.2f", new BigDecimal(f));
	    } catch (NumberFormatException e) {
	        return "0.00";
	    }
	}
	

}
