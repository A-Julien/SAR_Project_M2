package jvn.jvnOject;

import java.io.Serializable;

public enum LockState implements Serializable {
    R,
    W,
    RC,
    WC,
    RWC,
    NL
}
