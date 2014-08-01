package edu.oregonstate.edu.TDD_Classifier;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by mcdse_000 on 7/21/2014.
 */
public class Cycle {

    //TODO: Phase objects indicate event start/stop values, but do not require that those events exist in this Cycle object

    private List<Event> events;
    private List<Phase> phases;

    public Cycle () {
        events = new ArrayList<>();
        phases = new ArrayList<>();
    }

    public Cycle(List<Event> allEvents, List<Phase> allPhases) {
        events = new ArrayList<>();
        phases = new ArrayList<>();

        if (!allEvents.isEmpty()) {
            events.addAll(allEvents);
        }
        if (!allPhases.isEmpty()) {
            phases.addAll(allPhases);
        }
    }

    public int start() {
        if (phases.isEmpty()) {
            return 0;
        }
        return phases.get(0).start;
    }

    public int end() {
        if (phases.isEmpty()) {
            return 0;
        }
        return phases.get(phases.size()-1).end;
    }

    public int eventSize() {
        return events.size();
    }

    public int phaseSize() {
        return phases.size();
    }

    public boolean addEvent(Event event) {
        return events.add(event);
    }

    public boolean addEvent(String jsonString) {
        return events.add(new Event(jsonString));
    }

    public boolean addPhase(Phase phase) {
        return phases.add(phase);
    }

    public boolean addPhase(String jsonString) {
        return phases.add(new Phase(jsonString));
    }

    public Event getEvent(int index) {
        return events.get(index);
    }

    public Phase getPhase(int index) {
        return phases.get(index);
    }

    protected static List<Event> parseEventList(List<String> eventFileContent) {
        List<Event> eventsList = new ArrayList<>();

        for (String contentLine : eventFileContent) {
            eventsList.add(new Event(contentLine));
        }

        return eventsList;
    }

    protected static List<Phase> parsePhaseList(List<String> phaseFileContent) {
        List<Phase> phasesList = new ArrayList<>();

        for (String contentLine : phaseFileContent) {
            phasesList.add(new Phase(contentLine));
        }

        return phasesList;
    }
}
