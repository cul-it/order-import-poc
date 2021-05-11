package org.olf.folio.order.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader; 
import org.marc4j.marc.Record;
 
/**
 * MarcUtilsBaseTest.
 * 
 * @author jaf30
 *
 */ 
public class MarcUtilsBaseTest {

    private String buildDir;

    protected String harrass;
    protected String casalini;
    protected String physical;
    protected String amazonFO;
    protected String coutts;
    protected String requestors;
    protected String singleharrass;
    protected String harrassowitz;

    MarcUtils marcUtils = new MarcUtils();

    public MarcUtilsBaseTest() {
        init();
        this.harrass = this.buildDir + "/marc-test-files/harrass.mrc";
        this.casalini = this.buildDir + "/marc-test-files/Casalini.1.mrc";
        this.physical = this.buildDir + "/marc-test-files/physical.mrc";
        this.amazonFO = this.buildDir + "/marc-test-files/AmazonFO.1.mrc";
        this.coutts = this.buildDir + "/marc-test-files/CouttsUKFO.1.mrc";
        this.requestors = this.buildDir + "/marc-test-files/requesters_5-records_2021-03-11.mrc";
        this.singleharrass = this.buildDir + "/marc-test-files/singleharrass.mrc";
        this.harrassowitz = this.buildDir + "/marc-test-files/harrasowitz_9-records_2021-03-10.mrc";
    }

    public List<Record> getRecords(String fname) throws Exception {
        List<Record> records = new ArrayList<Record>();
        FileInputStream in = new FileInputStream(fname);
        MarcReader reader = new MarcStreamReader(in);
        Record record = null;
        while (reader.hasNext()) {
            record = reader.next();
            records.add(record);
        }
        return records;
    }

    public void init() {
        CompositeConfiguration config = new CompositeConfiguration();
        PropertiesConfiguration props = new PropertiesConfiguration();

        String use_env = System.getenv("USE_SYSTEM_ENV");
        if (StringUtils.isNotEmpty(use_env) && StringUtils.equals(use_env, "true")) {
            config.setProperty("buildDir", System.getenv("buildDir"));

        } else {
            try {
                props.load(ClassLoader.getSystemResourceAsStream("application.properties"));
                this.buildDir = (String) props.getProperty("buildDir");
            } catch (ConfigurationException e) {
                throw new RuntimeException("Could not load application.properties file");
            }
            config.addConfiguration(props);
        }
    }
}
