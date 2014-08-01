package edu.oregonstate.edu.TDD_Classifier;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Created by mcdse_000 on 7/21/2014.
 */
public class Event {

    private JSONObject operationEvent;

    public Event(String jsonString) {
        operationEvent = parseJSONString(jsonString);
    }

    /***
     * Returns the number of JSON properties (key/value pairs) in this Event instance. This
     * method utilizes the Interface JSON (net.sf.json) provided through implementation of
     * the JSONObject library (org.json.JSONObject).
     *
     * @return size     the number of JSON properties in the this Event object
     */
    public int size() {
        return operationEvent.size();
    }

    public String toString() {
        return operationEvent.toString();
    }

    protected static JSONObject parseJSONString(String jsonString) {
        JSONParser parser = new JSONParser();
        JSONObject jObj = null;

        try {
            jObj = (JSONObject) parser.parse(jsonString);
        }
        catch (ParseException pe) {
            System.err.format("JSON ParseException: %s%n", pe);
        }

        return jObj;
    }
}
