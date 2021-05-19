package org.olf.folio.order.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail; 
import org.junit.Ignore; 
import org.junit.jupiter.api.Test; 

/**
 * @author jaf30
 *
 */
public class LookupBillingAddressesTest extends LookupUtilBaseTest {

    boolean debug = false;

    /**
     * Constructor.
     */
    public LookupBillingAddressesTest() {
    }

    @Test
    public void getBillingAddressesTest() {
        String endpoint = this.getBaseOkapEndpoint() + "configurations/entries?query=(configName==tenant.addresses)";
        String testKey = "LTS Acquisitions";
        try {
            Map<String, String> addresses = this.getUtil().getBillingAddresses(endpoint, this.getToken());
            if (debug) {
                Iterator iter2 = addresses.keySet().iterator();
                while (iter2.hasNext()) {
                    String name = (String) iter2.next();
                    String uuid = addresses.get(name);
                    System.out.println("name: " + name);
                    System.out.println("uuid: " + uuid);
                    System.out.println();
                }
            } else {
                assertNotNull(addresses);
                assertTrue(addresses.containsKey(testKey));
                assertNotNull(addresses.get(testKey));
            }
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // fileOut.close();
        }
    }

}
