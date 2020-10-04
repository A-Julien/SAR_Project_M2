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
    Map<Integer, Map<JvnRemoteServer, LockState>> jvnObjectUidToLock;

    /**
     * JvnObjectName to JvnObjectUid
     */
    Map<String, Integer> jvnObjectNameToUid;

    /**
     * JvnObjectName to JvnObject
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
        this.jvnObjectUidToLock = new HashMap<>();
        this.jvnObjectNameToUid = new HashMap<>();
        this.jvnObjectNameToJvnObject = new HashMap<>();
    }

    private void RmiConnect() throws RemoteException, NotBoundException {
        this.registry = RmiConnection.RmiConnect(this.port, true);
        try {
            this.registry.rebind(ConfigManager.buildRmiAddr(this.rmiName, _Runnable.address, _Runnable.port), this);
        } catch (RemoteException e) {
            System.out.println("Coordinator failed to start");
            System.exit(1);
        }
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
    public synchronized void jvnRegisterObject(String jvnObjectName, JvnObject jvnObject, JvnRemoteServer jvnRemoteServer)
            throws RemoteException, JvnException {

        /**if(this.jvnLookupObject(jvnObjectName, jvnRemoteServer) == null)
            throw new JvnException("[COORD][Error] -> the name " + jvnObjectName + " is already associated to an object");**/

        if(!this.serverToJvnObjectName.containsKey(jvnRemoteServer)){
            this.serverToJvnObjectName.put(jvnRemoteServer, new ArrayList<>());
            this.serverToJvnObjectName.get(jvnRemoteServer).add(jvnObjectName);

            this.jvnObjectNameToJvnObject.put(jvnObjectName, jvnObject);
        }

        this.jvnObjectNameToUid.put(jvnObjectName,jvnObject.getUid());

        this.jvnObjectUidToLock.put(jvnObject.getUid(), new HashMap<>());
        switch (jvnObject.getCurrentLockState()){
            case RWC:
            case WC:
                this.jvnObjectUidToLock.get(jvnObject.getUid()).put(jvnRemoteServer, LockState.W);
                break;
            case RC:
                this.jvnObjectUidToLock.get(jvnObject.getUid()).put(jvnRemoteServer, LockState.R);
                break;
            default:
                this.jvnObjectUidToLock.get(jvnObject.getUid()).put(jvnRemoteServer, jvnObject.getCurrentLockState());
                break;
        }
    }


    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jvnObjectName : the JVN object name
     * @param jvnRemoteServer  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized JvnObject jvnLookupObject(String jvnObjectName, JvnRemoteServer jvnRemoteServer)
            throws java.rmi.RemoteException, jvn.JvnException {

        if(!this.jvnObjectNameToJvnObject.containsKey(jvnObjectName)) return null;

        if(!this.serverToJvnObjectName.containsKey(jvnRemoteServer))
            this.serverToJvnObjectName.put(jvnRemoteServer, new ArrayList<>());


        if(!this.serverToJvnObjectName.get(jvnRemoteServer).contains(jvnObjectName)){
            this.serverToJvnObjectName.get(jvnRemoteServer).add(jvnObjectName);
        }

        return this.jvnObjectNameToJvnObject.get(jvnObjectName);
    }


    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param jvnObjectUid : the JVN object identification
     * @param jvnRemoteServer  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockRead(int jvnObjectUid, JvnRemoteServer jvnRemoteServer)
            throws java.rmi.RemoteException, JvnException {

        if(!this.jvnObjectUidToLock.containsKey(jvnObjectUid))
            throw new JvnException("Error, object uid : " + jvnObjectUid + " not found");

        //case no lock on jvnObjectUid
        if(this.jvnObjectUidToLock.get(jvnObjectUid).isEmpty()){
            String name = this.getJvnObjectName(jvnObjectUid);

            if (name == null) throw new JvnException("Error, object uid : " + jvnObjectUid + " not found");

            this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer, LockState.R);
            return this.jvnObjectNameToJvnObject.get(name);
        }

        for (Map.Entry<JvnRemoteServer, LockState> entry : jvnObjectUidToLock.get(jvnObjectUid).entrySet()) {
            if (entry.getValue() == LockState.W) {
                Serializable newObject = entry.getKey().jvnInvalidateWriterForReader(jvnObjectUid);
                this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).updateSharedObject(newObject);

                this.jvnObjectUidToLock.get(jvnObjectUid).replace(entry.getKey(), LockState.R);
                if(entry.getKey() != jvnRemoteServer)
                        this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer, LockState.R);
                return newObject;
            }
        }

        return null;
    }

    private String getJvnObjectName(Integer jvnObjectUid) {
        for (String name : jvnObjectNameToUid.keySet())
            if (this.jvnObjectNameToUid.get(name).equals(jvnObjectUid)) return name;
        return null;
    }


    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param jvnObjectUid : the JVN object identification
     * @param jvnRemoteServer  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockWrite(int jvnObjectUid, JvnRemoteServer jvnRemoteServer)
            throws java.rmi.RemoteException, JvnException {

        if(!this.jvnObjectUidToLock.containsKey(jvnObjectUid)) //TODO INSURE
            throw new JvnException("Error, object uid : " + jvnObjectUid + " not found");

        //case no lock on jvnObjectUid
        if(this.jvnObjectUidToLock.get(jvnObjectUid).isEmpty()){
            String name = this.getJvnObjectName(jvnObjectUid);

            if (name == null) throw new JvnException("Error, object uid : " + jvnObjectUid + " not found");

            this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer, LockState.W);
            return this.jvnObjectNameToJvnObject.get(name);
        }

        for (Map.Entry<JvnRemoteServer, LockState> entry : jvnObjectUidToLock.get(jvnObjectUid).entrySet()) {
            switch (entry.getValue()){
                case R:
                    entry.getKey().jvnInvalidateReader(jvnObjectUid);
                    this.jvnObjectUidToLock.get(jvnObjectUid).remove(entry.getKey());
                    break;

                case W:
                    Serializable newObject = entry.getKey().jvnInvalidateWriter(jvnObjectUid);
                    this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).updateSharedObject(newObject);
                    this.jvnObjectUidToLock.get(jvnObjectUid).remove(entry.getKey());
                    break;
            }
        }

        this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer, LockState.W);

        return  this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid));
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

    @Override
    public String sayHello()  throws java.rmi.RemoteException, JvnException{
        return "Coordinator say hello to server, i'm in live";
    }
}

 
