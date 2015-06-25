package org.droidplanner.services.android.drone;

import android.os.Handler;
import android.os.RemoteException;

import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.core.drone.CommandTracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * Handles tracking and dispatching of the command listener events.
 * Created by Fredia Huya-Kouadio on 6/24/15.
 */
public class DroneCommandTracker implements CommandTracker {

    private static final long COMMAND_TIMEOUT_PERIOD = 1000l; //1 seconds

    private final Handler handler;

    private final ConcurrentHashMap<CallbackKey, AckCallback> callbacksStore = new ConcurrentHashMap<>();

    DroneCommandTracker(Handler handler){
        this.handler = handler;
    }

    @Override
    public void onCommandSubmitted(msg_command_long command, ICommandListener listener) {
        if(command == null || listener == null)
            return;

        final CallbackKey key = new CallbackKey(command.command);
        final AckCallback callback = new AckCallback(listener, key, callbacksStore);
        callbacksStore.put(key, callback);

        handler.postDelayed(callback, COMMAND_TIMEOUT_PERIOD);
    }

    @Override
    public void onCommandAcknowledged(msg_command_ack ack) {
        final CallbackKey key = new CallbackKey(ack.command);
        final AckCallback callback = callbacksStore.remove(key);
        if(callback != null){
            handler.removeCallbacks(callback);
            callback.setAckResult(ack.result);
            handler.post(callback);
        }
    }

    private static class CallbackKey {
        private final int commandId;

        CallbackKey(int commandId){
            this.commandId = commandId;
        }

        @Override
        public boolean equals(Object o){
            if(o == this)
                return true;

            if(!(o instanceof CallbackKey))
                return false;

            CallbackKey that = (CallbackKey) o;
            return this.commandId == that.commandId;
        }

        @Override
        public int hashCode(){
            return this.commandId;
        }
    }

    private static class AckCallback implements Runnable {

        private static final int COMMAND_TIMED_OUT = -1;
        private static final int COMMAND_SUCCEED = 0;

        private int ackResult = COMMAND_TIMED_OUT;

        private final ICommandListener listener;
        private final CallbackKey key;
        private final Map<CallbackKey, AckCallback> store;

        AckCallback(ICommandListener listener, CallbackKey key, Map<CallbackKey, AckCallback> store){
            this.listener = listener;
            this.key = key;
            this.store = store;
        }

        void setAckResult(int result){
            this.ackResult = result;
        }

        @Override
        public void run() {
            if(listener == null)
                return;

            store.remove(key);

            try {
                switch (ackResult) {
                    case COMMAND_TIMED_OUT:
                        listener.onTimeout();
                        break;

                    case COMMAND_SUCCEED:
                        listener.onSuccess();
                        break;

                    default:
                        listener.onError(ackResult);
                        break;
                }
            }catch(RemoteException e){
                Timber.e(e, e.getMessage());
            }
        }
    }
}
