/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn.Server;

import jvn.App._Runnable;
import jvn.JvnCoord.JvnRemoteCoord;
import jvn.JvnException;
import jvn.RmiServices.ConfigManager;
import jvn.RmiServices.RmiConnection;
import jvn.jvnOject.JvnObject;
import jvn.jvnOject.JvnObjectImpl;

import java.rmi.NotBoundException;

import java.rmi.RemoteException;

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.HashMap;


public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;

    private Registry registry;
    private JvnRemoteCoord jvnCoord;

    private HashMap<Integer, JvnObject> interceptorList;

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnServerImpl() throws Exception {
        super();
        this.interceptorList = new HashMap<>();


        try {
            this.RmiConnect();
        } catch (RemoteException e) {
            System.out.println("Server Failed to start");
            System.exit(1);
        }
    }

    private void RmiConnect() throws RemoteException, NotBoundException, JvnException {
        this.registry = RmiConnection.RmiConnect(_Runnable.port);

        this.registry.rebind(ConfigManager.buildRmiAddr(JvnRemoteCoord.rmiName, _Runnable.address), this);

        this.jvnCoord =
                (JvnRemoteCoord) this.registry.lookup(ConfigManager.buildRmiAddr(JvnRemoteCoord.rmiName, _Runnable.address));
        if(this.jvnCoord == null) throw new JvnException("Can not find coordinator");
    }

    /**
     * Static method allowing an application to get a reference to
     * a JVN server instance
     *
     * @throws JvnException
     **/
    public static JvnServerImpl jvnGetServer() {
        if (js == null) {
            try {
                js = new JvnServerImpl();
            } catch (Exception e) {
                return null;
            }
        }
        return js;
    }

    /**
     * The JVN service is not used anymore
     *
     * @throws JvnException
     **/
    public void jvnTerminate()
            throws jvn.JvnException {
        // to be completed
    }

    /**
     * creation of a JVN object
     *
     * @param o : the JVN object state
     * @throws JvnException
     **/
    public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException, RemoteException {
        JvnObject jvnObject = new JvnObjectImpl(o,this.jvnCoord.jvnGetObjectId());
        this.interceptorList.put(jvnObject.jvnGetObjectId(), jvnObject);
        return jvnObject;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException, RemoteException {
        this.jvnCoord.jvnRegisterObject(jon,jo,jo.jvnGetObjectId(),this);
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
        JvnObject findOject = null;
        try {
            findOject = this.jvnCoord.jvnLookupObject(jon, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (findOject != null) this.interceptorList.put(findOject.jvnGetObjectId(), findOject);

        return findOject;
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockRead(int joi) throws JvnException {
        try {
            return jvnCoord.jvnLockRead(joi, this);
        } catch (RemoteException e) {
            throw new JvnException("Error when lock read : " + e.getMessage());
        }
    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockWrite(int joi) throws JvnException {
        try {
            return jvnCoord.jvnLockWrite(joi, this);
        } catch (RemoteException e) {
            throw new JvnException("Error when lock write : " + e.getMessage());
        }
    }


    /**
     * Invalidate the Read lock of the JVN object identified by id
     * called by the JvnCoord
     *
     * @param joi : the JVN object id
     * @return void
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        this.interceptorList.get(joi).jvnInvalidateReader();
    }

    ;

    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        return this.interceptorList.get(joi).jvnInvalidateWriter();
    }

    ;

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        return this.interceptorList.get(joi).jvnInvalidateWriterForReader();
    }
}

 
