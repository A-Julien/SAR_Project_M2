/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn.JvnCoord;

import jvn.App._Runnable;
import jvn.JvnException;
import jvn.RmiServices.ConfigManager;
import jvn.RmiServices.RmiConnection;
import jvn.Server.JvnRemoteServer;
import jvn.jvnOject.JvnObject;
import jvn.jvnOject.LockState;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord, _Runnable{

    private static final long serialVersionUID = 1L;
    private static int interceptorUid = 0;

    /** RMI parameter **/
    private Registry registry;
    private String adresse;
    private int port;

    /** Server parameter **/
    private int serverUid = 0;


    /**
     * Server to {List of JvnObjectName}
     */
    Map<JvnRemoteServer, List<String>> serverToJvnObjectName;

    /**
     * JvnObjectName to {JvnRemoteServer to jvnLockSate}
     */
    Map<String, Map<JvnRemoteServer, LockState>> jvnObjectNameToLock;

    /**
     * Can e found ut
     */
    Map<String, JvnObject> jvnObjectNameToJvnObject;


    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    public JvnCoordImpl(String adresse, int port) throws Exception {
        this.adresse = adresse;
        this.port = port;
        this.serverToJvnObjectName = new HashMap<>();
        this.jvnObjectNameToLock = new HashMap<>();
    }

    private void RmiConnect() throws RemoteException, NotBoundException {
        this.registry = RmiConnection.RmiConnect(this.port);
        this.registry.rebind(ConfigManager.buildRmiAddr(rmiName, this.adresse), this);
    }

    /**
     * Allocate a NEW JVN object id (usually allocated to a
     * newly created JVN object)
     *
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized int jvnGetObjectUid() throws java.rmi.RemoteException, jvn.JvnException {
        return ++interceptorUid;
    }

    @Override
    public synchronized int jvnGetServerUid() throws RemoteException, JvnException {
        return this.serverUid++;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jvnObjectName : the JVN object name
     * @param jvnObject  : the JVN object
     * @param jvnRemoteServer  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    @Override
    public synchronized void jvnRegisterObject(String jvnObjectName, JvnObject jvnObject, JvnRemoteServer jvnRemoteServer) throws RemoteException, JvnException {
        if(!this.serverToJvnObjectName.containsKey(jvnRemoteServer))
            this.serverToJvnObjectName.put(jvnRemoteServer,new ArrayList<>());
        if(!this.jvnObjectNameToJvnObject.containsKey(jvnObjectName))
            this.jvnObjectNameToJvnObject.put(jvnObjectName, jvnObject);

        this.serverToJvnObjectName.get(jvnRemoteServer).add(jvnObjectName);
        this.jvnObjectNameToLock.put(jvnObjectName, new HashMap<>());
        this.jvnObjectNameToLock.get(jvnObjectName).put(jvnRemoteServer, jvnObject.getCurrentLockState());
    }


    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jvnObjectName : the JVN object name
     * @param jvnRemoteServer  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized JvnObject jvnLookupObject(String jvnObjectName, JvnRemoteServer jvnRemoteServer) throws java.rmi.RemoteException, jvn.JvnException {
        if(!this.jvnObjectNameToJvnObject.containsKey(jvnObjectName)) return null;

        if(!this.serverToJvnObjectName.get(jvnRemoteServer).contains(jvnObjectName)){
            this.serverToJvnObjectName.get(jvnRemoteServer).add(jvnObjectName);
        }
        return this.jvnObjectNameToJvnObject.get(jvnObjectName);
    }


    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        // to be completed
        return null;
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        // to be completed
        return null;
    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized void jvnTerminate(JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        // to be completed
    }

    @Override
    public int run() throws Exception {
        this.RmiConnect();
        return 0;
    }

    @Override
    public void stop() throws IOException, NotBoundException, SQLException, InterruptedException {
        this.registry.unbind(this.buildRmiAddr(this.rmiName, _Runnable.address));
    }
}

 
