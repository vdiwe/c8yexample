package c8y.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class S3 {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(S3.class);
    public void  convertJSON() throws JsonMappingException, JsonProcessingException, IllegalArgumentException{
        JsonMapper mapper = new JsonMapper();
         String test = "{\"GivenName\" : \"Mortimer\",\"SurName\" : \"Smith\"}";
         LOG.info("raw: {}",test);
         LOG.info("transformed: {}",mapper.readTree(test));

    }
    public void test() throws ParseException{
        DateTimeZone gmtTime = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT"));
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy hh:mm:ss aa");

	    String date = "Mar 04 2024 05:51:12 PM";
        DateTime timeZoneStartDate = new DateTime(simpleDateFormat.parse(date));
	    LOG.info("datetime: {}",timeZoneStartDate.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CEST"))));
    }

    
}
