//ICommandListener.aidl
package org.droidplanner.services.android.lib.model;

/**
* Asynchronous notification of a command execution state.
*/
oneway interface ICommandListener {

    /**
    * Called when the command was executed successfully.
    */
    void onSuccess();

    /**
    * Called when the command execution failed.
    * @param executionError Defined by {@link org.droidplanner.services.android.lib.drone.attribute.error.CommandExecutionError}
    */
    void onError(int executionError);

    /**
    * Called when the command execution times out.
    */
    void onTimeout();

}