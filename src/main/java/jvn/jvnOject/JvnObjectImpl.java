package jvn.jvnOject;

import jvn.JvnException;
import jvn.Server.*;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private final int uid; //Unique ID given by coordinator


    Serializable object;
    LockState lockState;

    public JvnObjectImpl(Serializable object, int uid) throws JvnException {
        this.object = object;
        this.uid = uid;
        this.lockState = LockState.W;//this.jvnLockWrite(); // after creation, I have a write lock on the object
    }

    @Override
    public synchronized void jvnLockRead() throws JvnException {
        System.out.println("[JvnObject] jvnLockRead state -> " + this.lockState);
        JvnLocalServer server= JvnServerImpl.jvnGetServer();
        switch (this.lockState){
            case NL:
            case WC:
                this.object = server.jvnLockRead(this.uid);
                this.lockState = LockState.R;
                break;
            case RWC:
            case RC:
                this.lockState = LockState.R;
                break;
            default:
                throw new JvnException("failed to acquire lock read, bad state : " + this.lockState);

        }
    }

    @Override
    public synchronized void updateSharedObject(Serializable object) {
        this.object = object;
    }

    @Override
    public synchronized void jvnLockWrite() throws JvnException {
        System.out.println("[JvnObject] jvnLockWrite state -> " + this.lockState);
       JvnLocalServer server;

        switch (this.lockState){
            case RC:
            case NL:
                server = JvnServerImpl.jvnGetServer();
                assert server != null;
                this.object = server.jvnLockWrite(this.uid);
                this.lockState = LockState.W;
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
        System.out.println("[JvnObject] new state -> " + this.lockState);
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
        System.out.println("[JvnObject] jvnInvalidateReader " + this.lockState);
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
        System.out.println("[JvnObject] jvnInvalidateWriter " + this.lockState);
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
            default:
                throw new JvnException("Error when invalidate lock write, bad state : " + this.lockState);
        }
        return this.object;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        System.out.println("[JvnObject] jvnInvalidateWriterForReader state -> " + this.lockState);
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
                break;
            case WC:
                this.lockState = LockState.RC;
                break;
            default:
                throw new JvnException("Error when invalidate writer for reader, bad state : " + this.lockState);
        }
        return this.object;
    }
}
