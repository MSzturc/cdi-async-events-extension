package de.mszturc.cdi.asyncevents;

import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * Author: MSzturc
 * Date:   24.07.2014 
 */
public class AsyncEventExtension implements Extension {

    private final List<ObserverMethod<?>> asyncObservers = new ArrayList<>();
    private final ForkJoinPool pool = new ForkJoinPool();

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event, final BeanManager beanManager) {
        final AnnotatedType<X> type = event.getAnnotatedType();
        for (AnnotatedMethod<?> method : type.getMethods()) {
            for (final AnnotatedParameter<?> param : method.getParameters()) {
                if (param.isAnnotationPresent(Observes.class) && param.isAnnotationPresent(Async.class)) {
                    asyncObservers.add(ObserverMethodHolder.create(this.pool, beanManager, type, method, param));
                }
            }
        }
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        for (ObserverMethod<?> om : this.asyncObservers) {
            event.addObserverMethod(om);
        }
    }

    public void beforeShutdown(@Observes BeforeShutdown event) {
        this.pool.shutdown();
    }

    private static class ObserverMethodHolder<T> implements ObserverMethod<T> {

        private ForkJoinPool pool;
        private BeanManager beanManager;
        private AnnotatedType<?> type;
        private AnnotatedMethod<?> method;
        private AnnotatedParameter<?> param;
        private Set<Annotation> qualifiers;

        private ObserverMethodHolder() {
        }

        public static <T> ObserverMethod<T> create(ForkJoinPool pool, BeanManager bm, AnnotatedType<?> type, AnnotatedMethod<?> method, AnnotatedParameter<?> param) {
            ObserverMethodHolder<T> objectMethodHolder = new ObserverMethodHolder<>();
            objectMethodHolder.pool = pool;
            objectMethodHolder.beanManager = bm;
            objectMethodHolder.type = type;
            objectMethodHolder.method = method;
            objectMethodHolder.param = param;
            objectMethodHolder.qualifiers = new HashSet<>();
            
            for (Annotation annotation : param.getAnnotations()) {
                if (bm.isQualifier(annotation.getClass()) && !annotation.annotationType().equals(Async.class)) {
                    objectMethodHolder.qualifiers.add(annotation);
                }
            }
            
            return objectMethodHolder;
        }

        @Override
        public Class<?> getBeanClass() {
            return this.type.getJavaClass();
        }

        @Override
        public Type getObservedType() {
            return this.param.getBaseType();
        }

        @Override
        public Set<Annotation> getObservedQualifiers() {
            return this.qualifiers;
        }

        @Override
        public Reception getReception() {
            return Reception.ALWAYS;
        }

        @Override
        public TransactionPhase getTransactionPhase() {
            return TransactionPhase.IN_PROGRESS;
        }

        @Override
        public void notify(T event) {
            if (this.method.isStatic()) {
                this.notify(null, method.getJavaMember(), event);
            } else {
                for (Bean<?> bean : this.beanManager.getBeans(this.type.getBaseType())) {
                    CreationalContext<?> ctx = this.beanManager.createCreationalContext(bean);
                    Object target = this.beanManager.getReference(bean, this.type.getBaseType(), ctx);
                    this.notify(target, method.getJavaMember(), event);
                }
            }
        }

        private void notify(final Object target, final Method method, final T event) {
            this.pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(target, event);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                }
            });
        }
    }
}
