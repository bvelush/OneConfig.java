package com.oneconfig.core.stores;

import com.oneconfig.core.IInit;

// store implementation must have no constructors (or empty default parameterless constructor)
// initialization of the class must be performed through 'init' call
public interface IStore extends IInit {
    String getName();

    StoreResult resolvePath(String path); // return value probably should be something like Sensor/single val class
}
