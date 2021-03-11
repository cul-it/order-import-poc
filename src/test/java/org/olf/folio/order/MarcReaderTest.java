package org.olf.folio.order;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.junit.jupiter.api.Disabled; 

 
public class MarcReaderTest {
    
	 
	@Test
	public void test1() throws FileNotFoundException {
        String[] fileArray = {
		   //"/cul/src/order-import-poc/support/building_bridges.mrc",
		   "/cul/src/order-import-poc/support/gobi-sample-v2.mrc",
		   //"/cul/src/order-import-poc/support/physical.mrc",
		   "/cul/src/order-import-poc/support/requesters_5-records_2021-03-11.mrc",
		   "/cul/src/order-import-poc/support/harrassowitz_9-records_2021-03-10.mrc",
        };
        
		List<String> files = Arrays.asList(fileArray);
		for (String filePath: files) {
			System.out.println(filePath);
			FileInputStream in = new FileInputStream(filePath);	 
			MarcReader reader = new MarcStreamReader(in);
			Record record = null;
			int numRec = 0;
			while (reader.hasNext()) {
				record = reader.next();
				//System.out.println(record.toString());
				numRec++;
				DataField twoFourFive = (DataField) record.getVariableField("245");
			    String title = twoFourFive.getSubfieldsAsString("a");
			    DataField nineEighty = (DataField) record.getVariableField("980");
			    
			    String notes =  new String();
			    String fundCode = new String();
			    String vendorCode =  new String();
			    String price = new String();
			    String quantity =  new String();
			     
			    String electronicIndicator = new String();
			    String vendorItemId = new String();
			    String email = new String();
			    String personName = new String();
			    
			    if (nineEighty != null) {
			    	System.out.println(nineEighty.toString());
			    	notes =  nineEighty.getSubfieldsAsString("n"); 
				    fundCode = nineEighty.getSubfieldsAsString("b");
				    vendorCode =  nineEighty.getSubfieldsAsString("v");
				    quantity =  nineEighty.getSubfieldsAsString("q");
				    price = nineEighty.getSubfieldsAsString("m");
				    electronicIndicator = nineEighty.getSubfieldsAsString("z");
				    vendorItemId = nineEighty.getSubfieldsAsString("c");
				    email = nineEighty.getSubfieldsAsString("y");
				    personName = nineEighty.getSubfieldsAsString("s");
			    }
			    DataField nineFiveTwo = (DataField) record.getVariableField("952");
			    String loc = new String();
			    if (nineFiveTwo != null) {
			       loc = nineFiveTwo.getSubfieldsAsString("b");
			    }
			    
			    DataField nineEightyOne = (DataField) record.getVariableField("981");
			    String requestor = new String();
			    if (nineFiveTwo != null) {
			       requestor = nineEightyOne.getSubfieldsAsString("r");
			    }
			    
			    System.out.println("title: "+ title);
			    System.out.println("quantity: "+ quantity);
			    System.out.println("price: "+ price);
			    System.out.println("vendorCode: "+ vendorCode);
			    System.out.println("fundCode: "+ fundCode);
			    System.out.println("electronicIndicator: "+ electronicIndicator);
			    System.out.println("requestor: "+ requestor);
			    System.out.println("loc: "+ loc);
			    System.out.println("email: "+ email);
			    System.out.println("personName: "+ personName);
			    System.out.println();
				
			}
			System.out.println("Number of records found: "+ numRec);
		}
	}
    

}
