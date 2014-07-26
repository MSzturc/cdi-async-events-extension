package de.mszturc.cdi.asyncevents;

import de.mszturc.cdi.asyncevents.data.AsynchronousEvent;
import de.mszturc.cdi.asyncevents.data.SynchronousEvent;
import de.mszturc.cdi.asyncevents.data.TimeWatch;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Author: MSzturc
 * Date:   24.07.2014 
 */
@RunWith(Arquillian.class)
public class AsyncEventExtensionTest {

    @Inject
    Event<SynchronousEvent> sync;

    @Inject
    Event<AsynchronousEvent> async;

    @Inject
    TimeWatch watch;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Async.class).addClass(AsyncEventExtension.class)
                .addAsManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension")
                .addClass(SynchronousEvent.class).addClass(AsynchronousEvent.class).addClass(TimeWatch.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testSynchronousEventHandling() throws Exception {

        assertNotNull(watch);
        assertNotNull(sync);

        watch.reset();
        sync.fire(new SynchronousEvent("Event 1"));
        sync.fire(new SynchronousEvent("Event 2"));

        assertNotNull(watch.time());
        
        assertTrue(watch.time() + " > 2000", watch.time() > 2000);
        assertTrue(watch.time() + " < 2500", watch.time() < 2500);
    }

    @Test
    public void testAsynchronousEventHandling() throws Exception {

        assertNotNull(watch);
        assertNotNull(sync);

        watch.reset();
        async.fire(new AsynchronousEvent("Event 1"));
        async.fire(new AsynchronousEvent("Event 2"));
        assertNotNull(watch.time());

        assertEquals(0, watch.time());
        Thread.sleep(1200);
        
        assertTrue(watch.time() + " > 1000", watch.time() > 1000);
        assertTrue(watch.time() + " < 1500", watch.time() < 1500);
    }

    public void oneSecondSynchronousConsumer(@Observes SynchronousEvent event) throws InterruptedException {
        Thread.sleep(1000);
        watch.update();
    }

    public void oneSecondAsynchronousConsumer(@Observes @Async AsynchronousEvent event) throws InterruptedException {
        Thread.sleep(1000);
        watch.update();
    }
}
