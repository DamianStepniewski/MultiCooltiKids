package pl.snikk.multicooltikids;

import java.util.ArrayList;

public class Venue {
    public ArrayList<Event> events;
    public String type;

    public Venue(ArrayList<Event> events, String type) {
        this.type = type;
        this.events = events;
    }
}
