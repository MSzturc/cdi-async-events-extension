package de.mszturc.cdi.asyncevents.data;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * Author: MSzturc
 * Date:   25.07.2014 
 */
@ApplicationScoped
public class TimeWatch {

    private Date start;
    private Date end;
    
    @PostConstruct
    public void reset(){
        start = new Date();
        end = new Date();
    }
    
    public void update(){
        end = new Date();
    }
    
    public long time(){
        long diffInMilliseconds = (end.getTime() - start.getTime());
        return diffInMilliseconds;
    }
}
