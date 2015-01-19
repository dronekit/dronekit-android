// IDroneApi.aidl
package com.o3dr.services.android.lib.model;

import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.IMavlinkObserver;
import com.o3dr.services.android.lib.model.action.Action;

/**
* Interface used to access the drone properties.
*/
interface IDroneApi {

    /**
    * Retrieves the attribute whose type is specified by the parameter.
    * @param attributeType type of the attribute to retrieve. The list of supported
                        types is stored in {@link com.o3dr.services.android.lib.drone.attribute.AttributeType}.
    * @return Bundle object containing the requested attribute.
    */
    Bundle getAttribute(String attributeType);

    /**
    * Build and return complex mission item.
    * @param itemBundle bundle containing the complex mission item to update.
    */
    void buildComplexMissionItem(inout Bundle itemBundle);

    /*** Oneway method calls ***/

    /**
    * Register a listener to receive drone events.
    * @param observer the observer to register.
    */
    oneway void addAttributesObserver(IObserver observer);

    /**
    * Removes a drone events listener.
    * @param observer the observer to remove.
    */
    oneway void removeAttributesObserver(IObserver observer);

    /**
    * Register a listener to receive mavlink messages.
    * @param observer the observer to register.
    */
    oneway void addMavlinkObserver(IMavlinkObserver observer);

    /**
    * Removes a mavlink message listener.
    * @param observer the observer to remove.
    */
    oneway void removeMavlinkObserver(IMavlinkObserver observer);

    /**
    * Performs an action among the set exposed by the api.
    */
    oneway void performAction(in Action action);

}
