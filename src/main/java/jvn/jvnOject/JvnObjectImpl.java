package jvn.jvnOject;

import jvn.JvnException;
import jvn.JvnLocalServer;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private final int uid; //Unique ID given by coordinator

    JvnLocalServer server;

    Serializable object;
    LockState lockState;

    public JvnObjectImpl(Serializable object, int uid, JvnLocalServer server) throws JvnException {
        this.server = server;

        this.object = object;
        this.uid = uid;
        this.jvnLockWrite(); // after creation, I have a write lock on the object
    }

    @Override
    public void jvnLockRead() throws JvnException {
        this.object = this.server.jvnLockRead(this.uid);
        this.lockState = LockState.R;
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        this.object = this.server.jvnLockWrite(this.uid);
        this.lockState = LockState.W;
    }

    @Override
    public void jvnUnLock() throws JvnException {
        switch (this.lockState){
            case R:
                this.lockState = LockState.RC;
                break;
            case W:
                this.lockState = LockState.WC;
                break;
        }
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return this.uid;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        if(this.object != null) return this.object;
        return null;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {
        switch (this.lockState){
            case RC:
                this.lockState = LockState.NL;
                break;
            case R:
                //TODO synchro here
                this.lockState = LockState.NL;
                break;
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        switch (this.lockState){
            case WC:
                this.lockState = LockState.NL;
                break;
            case W:
                //TODO synchro here
                this.lockState = LockState.NL;
                break;
            case RWC:
                this.jvnInvalidateWriterForReader();
                this.jvnInvalidateReader();
                break;
        }
        return object;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        switch (this.lockState){
            case W:
                //TODO synchro here
                this.lockState = LockState.RC;
                break;
            case RWC:
                this.lockState = LockState.R;
        }
        return object;
    }
}
