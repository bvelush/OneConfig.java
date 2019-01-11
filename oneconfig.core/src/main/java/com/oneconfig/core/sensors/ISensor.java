package com.oneconfig.core.sensors;

import com.oneconfig.core.IInit;

public interface ISensor extends IInit {
    String getName();

    String evaluate();
}
