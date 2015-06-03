package org.droidplanner.core.drone;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DroneEvents extends DroneVariable {

    private static final long EVENT_DISPATCHING_DELAY = 33l; //milliseconds

    private final AtomicBoolean isDispatcherRunning = new AtomicBoolean(false);

    private final Runnable eventDispatcher = new Runnable() {

        @Override
        public void run() {
            handler.removeCallbacks(this);

            final DroneEventsType event = eventQueue.poll();
            if (event == null) {
                isDispatcherRunning.set(false);
                return;
            }

            for (OnDroneListener listener : droneListeners) {
                listener.onDroneEvent(event, myDrone);
            }

            handler.removeCallbacks(this);
            handler.postDelayed(this, EVENT_DISPATCHING_DELAY);

            isDispatcherRunning.set(true);
        }
    };

    private final DroneInterfaces.Handler handler;

    public DroneEvents(Drone myDrone, DroneInterfaces.Handler handler) {
        super(myDrone);
        this.handler = handler;
    }

    private final ConcurrentLinkedQueue<OnDroneListener> droneListeners = new ConcurrentLinkedQueue<OnDroneListener>();
    private final ConcurrentLinkedQueue<DroneEventsType> eventQueue = new ConcurrentLinkedQueue<>();

    public void addDroneListener(OnDroneListener listener) {
        if (listener != null & !droneListeners.contains(listener))
            droneListeners.add(listener);
    }

    public void removeDroneListener(OnDroneListener listener) {
        if (listener != null && droneListeners.contains(listener))
            droneListeners.remove(listener);
    }

    public void notifyDroneEvent(DroneEventsType event) {
        if (event == null || droneListeners.isEmpty() || eventQueue.contains(event))
            return;

        eventQueue.add(event);
        if (isDispatcherRunning.compareAndSet(false, true))
            handler.postDelayed(eventDispatcher, EVENT_DISPATCHING_DELAY);
    }
}
