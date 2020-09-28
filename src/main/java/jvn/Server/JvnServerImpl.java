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

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;


public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;

    private Registry registry;
    private JvnRemoteCoord jvnCoord;

    private Map<Integer, JvnObject> interceptorList;

    private Integer uid;

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

        this.jvnCoord =
                (JvnRemoteCoord) this.registry.lookup(ConfigManager.buildRmiAddr(JvnRemoteCoord.rmiName, _Runnable.address));
        this.uid = this.jvnCoord.jvnGetServerUid();
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
    public JvnObject jvnCreateObject(Serializable object) throws jvn.JvnException, RemoteException {
        JvnObject jvnObject = new JvnObjectImpl(object,this.jvnCoord.jvnGetObjectUid());
        this.interceptorList.put(jvnObject.getUid(), jvnObject);
        return jvnObject;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     **/
    public void jvnRegisterObject(String jvnObjectName, JvnObject jvnObject) throws jvn.JvnException, RemoteException {
        this.jvnCoord.jvnRegisterObject(jvnObjectName, jvnObject, this);
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
        JvnObject findObject = null;
        try {
            findObject = this.jvnCoord.jvnLookupObject(jon, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (findObject != null) this.interceptorList.put(findObject.getUid(), findObject);

        return findObject;
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

    @Override
    public Integer getUid() {
        return this.uid;
    }
}

 
