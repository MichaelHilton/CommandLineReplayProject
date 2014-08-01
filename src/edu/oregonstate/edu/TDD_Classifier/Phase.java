package edu.oregonstate.edu.TDD_Classifier;

import org.json.simple.JSONObject;

/**
 * Created by mcdse_000 on 7/21/2014.
 * Updated version from TDDSessionsLibrary
 */
public class Phase {

    protected String type;
    protected int start;
    protected int end;
    protected JSONObject jsonPhase;

    public Phase(String phaseType, int phaseStart, int phaseEnd) {
        type = phaseType;
        start = phaseStart;
        end = phaseEnd;
    }

    public Phase(String jsonString) {

        try {
            JSONObject jsonPhase = Event.parseJSONString(jsonString);
            type = jsonPhase.get("CycleType").toString();
            start = Integer.parseInt(jsonPhase.get("CycleStart").toString());
            end = Integer.parseInt(jsonPhase.get("CycleEnd").toString());
        }
        catch (ClassCastException ce) {
            System.err.format("Malformed JSON: %s%n", ce);
        }
        catch (NullPointerException ne) {
            type = null;
            start = -1;
            end = -1;
        }
    }

    public String toString() {
        return String.format("{\"CycleType\":\"%s\",\"CycleStart\":\"%d\",\"CycleEnd\":\"%d\"}", type, start, end);
    }

}
