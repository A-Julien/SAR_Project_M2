package jvn.jvnOject;

import java.io.Serializable;

/**
 * Lock for jvnObj
 */
public enum LockState implements Serializable {
    R,
    W,
    RC,
    WC,
    RWC,
    NL
}
