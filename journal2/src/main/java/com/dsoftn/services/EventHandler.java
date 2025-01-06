package com.dsoftn.services;

import javafx.event.Event;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.utils.UError;


public class EventHandler {

    public class EventListener {
        private ICustomEventListener classObject;
        private Set<Event> events;
    
        public EventListener(ICustomEventListener classObject) {
            this.classObject = classObject;
            this.events = new HashSet<>();
        }
    
        public ICustomEventListener getClassObject() {
            return classObject;
        }
    
        public void addEvents(Event... events) {
            this.events.addAll(List.of(events));
        }

        public boolean hasEvent(Event event) {
            return events.contains(event);
        }
    }
    

    // Variables
    private Map<String, EventListener> eventMap = new LinkedHashMap<>();

    // Methods

    public void register (ICustomEventListener classObject, Event... events) {
        String name = String.valueOf(System.identityHashCode(classObject));

        if (eventMap.containsKey(name)) {
            UError.exception("EventHandler.register: Name already exists.", "EventListener with name '" + name + "' already exists.");
            return;
        }

        // Make new entry
        EventListener listener = new EventListener(classObject);
        listener.addEvents(events);
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
        Set<ICustomEventListener> notifiedStages = new HashSet<>();
        for (EventListener listener : eventMap.values()) {
            if (listener.hasEvent(event) && !notifiedStages.contains(listener.getClassObject())) {
                listener.getClassObject().onCustomEvent(event);
                notifiedStages.add(listener.getClassObject());
                isHandled = true;
            }
        }

        if (!isHandled) {
            UError.info("EventHandler.fireEvent: Event not handled.", "Event '" + event + "' was not handled by any registered listener.");
        }
    }
  

}
