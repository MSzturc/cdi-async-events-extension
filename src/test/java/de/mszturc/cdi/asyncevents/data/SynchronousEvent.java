package de.mszturc.cdi.asyncevents.data;

/**
 * Author: MSzturc
 * Date:   25.07.2014 
 */
public class SynchronousEvent {

    private String name;

    public SynchronousEvent() {
    }

    public SynchronousEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
