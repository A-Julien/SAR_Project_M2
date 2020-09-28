package jvn.jvnOject;

import jvn.JvnException;
import jvn.Server.*;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private final int uid; //Unique ID given by coordinator

    JvnLocalServer server;

    Serializable object;
    LockState lockState;

    public JvnObjectImpl(Serializable object, int uid) throws JvnException {
        this.server = JvnServerImpl.jvnGetServer();

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
        switch (this.lockState){
            case NL:
                this.object = this.server.jvnLockWrite(this.uid);
                this.lockState = LockState.W;
                break;
            case RC:
                this.object = this.server.jvnLockWrite(this.uid);
                this.lockState = LockState.RWC;
                break;
            case WC:
                this.lockState = LockState.W;
                break;
            default:
                throw new JvnException("failed to acquire lock write, bad state : " + this.lockState);
        }

    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        switch (this.lockState){
            case R:
                this.lockState = LockState.RC;
                break;
            case W:
                this.lockState = LockState.WC;
                break;
        }
        this.notify(); // notify that dev free lock to unlock invalidate routine
    }

    @Override
    public int getUid() throws JvnException {
        return this.uid;
    }

    @Override
    public Serializable getSharedObject() throws JvnException {
        if(this.object != null) return this.object;
        return null;
    }

    @Override
    public LockState getCurrentLockState() throws JvnException {
        return this.lockState;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        switch (this.lockState){
            case RC:
                this.lockState = LockState.NL;
                break;
            case R:
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new JvnException("Error when invalidate lock read : " + e.getMessage());
                }
                this.lockState = LockState.NL;
                break;
            default:
                throw new JvnException("Error when invalidate lock read, bad state : " + this.lockState);
        }
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        switch (this.lockState){
            case WC:
                this.lockState = LockState.NL;
                break;
            case W:
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new JvnException("Error when invalidate lock write : " + e.getMessage());
                }
                this.lockState = LockState.NL;
                break;
            case RWC:
                this.jvnInvalidateWriterForReader();
                this.jvnInvalidateReader();
                break;
            default:
                throw new JvnException("Error when invalidate lock read, bad state : " + this.lockState);
        }
        return object;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        switch (this.lockState){
            case W:
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new JvnException("Error when invalidate writer for reader : " + e.getMessage());
                }
                this.lockState = LockState.RC;
                break;
            case RWC:
                this.lockState = LockState.R;
            default:
                throw new JvnException("Error when invalidate writer for reader, bad state : " + this.lockState);
        }
        return object;
    }
}
