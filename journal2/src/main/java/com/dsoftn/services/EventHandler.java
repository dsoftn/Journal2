package com.dsoftn.services;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.utils.UError;


public class EventHandler {

    public class EventListener {
        private WeakReference<ICustomEventListener> classObject;
        private Set<EventType<?>> events;
    
        public EventListener(ICustomEventListener classObject) {
            this.classObject = new WeakReference<>(classObject);
            this.events = new HashSet<>();
        }
    
        public ICustomEventListener getClassObject() {
            return classObject.get();
        }
    
        public void addEvents(EventType<?>... events) {
            this.events.addAll(List.of(events));
        }

        public boolean hasEvent(EventType<?> event) {
            return events.contains(event);
        }
    }
    

    // Variables
    private Map<String, EventListener> eventMap = new ConcurrentHashMap<>();

    // Methods

    public void register (ICustomEventListener classObject, EventType<?>... eventTypes) {
        String name = String.valueOf(System.identityHashCode(classObject));

        if (eventMap.containsKey(name)) {
            UError.exception("EventHandler.register: Name already exists.", "EventListener with name '" + name + "' already exists.");
            return;
        }

        // Make new entry
        EventListener listener = new EventListener(classObject);
        listener.addEvents(eventTypes);
        eventMap.put(name, listener);
    }

    public void unregister (ICustomEventListener classObject) {
        String name = String.valueOf(System.identityHashCode(classObject));

        if (!eventMap.containsKey(name)) {
            UError.exception("EventHandler.unregister: Name not found.",
                    "EventListener with name '" + name + "' does not exist.");
            return;
        }

        eventMap.remove(name);
    }

    public void fireEvent (Event event) {
        boolean isHandled = false;
        Set<ICustomEventListener> notifiedClasses = new HashSet<>();
        
        Iterator<Map.Entry<String, EventListener>> iterator = eventMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, EventListener> entry = iterator.next();
            EventListener listener = entry.getValue();
            
            if (listener.getClassObject() == null) {
                iterator.remove();
                continue;
            }
            
            if (listener.hasEvent(event.getEventType()) && !notifiedClasses.contains(listener.getClassObject())) {
                listener.getClassObject().onCustomEvent(event);
                notifiedClasses.add(listener.getClassObject());
                isHandled = true;
            }
        }

        if (!isHandled) {
            UError.info("EventHandler.fireEvent: Event not handled.", "Event '" + event + "' was not handled by any registered listener.");
        }
    }
}
