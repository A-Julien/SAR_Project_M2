package jvn.jvnOject;

import jvn.JvnException;
import jvn.Proxy.JvnProxy;
import jvn.Server.JvnLocalServer;
import jvn.Server.JvnServerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class JvnObjectImpl implements JvnObject {
    private static final Logger logger = LogManager.getLogger(JvnProxy.class);


    private final int uid; //Unique ID given by coordinator


    Serializable object;
    LockState lockState;

    /**
     * Constructor
     * @param object
     * @param uid
     * @throws JvnException
     */
    public JvnObjectImpl(Serializable object, int uid) throws JvnException {
        this.object = object;
        this.uid = uid;
        this.lockState = LockState.W;//this.jvnLockWrite(); // after creation, I have a write lock on the object
    }

    /**
     * Apply a lock read.
     * @throws JvnException
     * @throws RemoteException
     */
    @Override
    public synchronized void jvnLockRead() throws JvnException, RemoteException, NotBoundException {
        logger.info("jvnLockRead state -> " + this.lockState);
        JvnLocalServer server= JvnServerImpl.jvnGetServer();
        switch (this.lockState){
            case NL:
                assert server != null;
                this.object = server.jvnLockRead(this.uid);
                this.lockState = LockState.R;
                break;
            case WC:
                //this.object = server.jvnLockRead(this.uid);
                //this.lockState = LockState.R;
                this.lockState = LockState.RWC;
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

    /**
     * Apply a lock write.
     * @throws JvnException
     * @throws RemoteException
     */
    @Override
    public synchronized void jvnLockWrite() throws JvnException, RemoteException, NotBoundException {
        logger.info("jvnLockWrite state -> " + this.lockState);
       JvnLocalServer server;

        switch (this.lockState){
            case R:
            case RC:
            case NL:
                server = JvnServerImpl.jvnGetServer();
                assert server != null;
                this.object = server.jvnLockWrite(this.uid);
                this.lockState = LockState.W;
                break;
            case RWC:
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
        logger.info("new state -> " + this.lockState);
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

    /**
     * Get the shared object associated to this JvnObject
     *
     * @throws JvnException
     **/
    @Override
    public void setCurrentLockState(LockState lockState) throws jvn.JvnException{
        this.lockState = lockState;
    }

    /**
     * Invalidate a lock read.
     * @throws JvnException
     */
    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        logger.info("jvnInvalidateReader " + this.lockState);
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

    /**
     * Invalidate a lock write and return the written object.
     * @return
     * @throws JvnException
     */
    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        logger.info("jvnInvalidateWriter " + this.lockState);
        switch (this.lockState){
            case WC:
            case NL:
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

    /**
     * Invalidate a write lock for a read lock and return the written object.
     * @return
     * @throws JvnException
     */
    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        logger.info("jvnInvalidateWriterForReader state -> " + this.lockState);
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
