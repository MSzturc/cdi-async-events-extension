package de.mszturc.cdi.asyncevents.data;

/**
 * Author: MSzturc
 * Date:   25.07.2014 
 */
public class AsynchronousEvent {

    private String name;

    public AsynchronousEvent() {
    }

    public AsynchronousEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
