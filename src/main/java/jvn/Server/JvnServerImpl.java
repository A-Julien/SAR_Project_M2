/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn.Server;

import jvn.JvnCoord.JvnRemoteCoord;
import jvn.JvnException;
import jvn.RmiServices.ConfigManager;
import jvn.jvnOject.JvnObject;
import jvn.jvnOject.JvnObjectImpl;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
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

    private String adresse;
    private Registry registry;
    private JvnRemoteCoord jvnCoord;

    private HashMap<String, JvnObject> interceptorList;

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnServerImpl(String address) throws Exception {
        super();
        this.interceptorList = new HashMap<>();

        this.adresse = address;

        try {
            this.RmiConnect();
        } catch (RemoteException e) {
            System.out.println("Server Failed to start");
            System.exit(1);
        }
    }

    private void RmiConnect() throws RemoteException, NotBoundException {
        LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        this.registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
        System.setProperty("java.security.policy", ConfigManager.getConfig("securityManagerProp"));
        if (System.getSecurityManager() == null) System.setSecurityManager(new RMISecurityManager());

        this.registry.rebind(ConfigManager.buildRmiAddr("NOMSERV", this.adresse), this);

        this.jvnCoord = (JvnRemoteCoord) this.registry.lookup(ConfigManager.buildRmiAddr("Coord", this.adresse));
        if(this.jvnCoord == null) throw new NotBoundException("null not excepted");
    }

    /**
     * Static method allowing an application to get a reference to
     * a JVN server instance
     *
     * @throws JvnException
     **/
    public static JvnServerImpl jvnGetServer(String address) {
        if (js == null) {
            try {
                js = new JvnServerImpl(address);
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
        int id  = this.jvnCoord.jvnGetObjectId();
        JvnObject jvnObject = new JvnObjectImpl(o,id,this);
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
        this.interceptorList.put(jon, jo);
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
        // to be completed
        return null;
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockRead(int joi)
            throws JvnException {
        // to be completed
        return null;

    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockWrite(int joi)
            throws JvnException {

        // to be completed
        return null;
    }


    /**
     * Invalidate the Read lock of the JVN object identified by id
     * called by the JvnCoord
     *
     * @param joi : the JVN object id
     * @return void
     * @throws java.rmi.RemoteException,JvnException
     **/
    public void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed
    }

    ;

    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed
        return null;
    }

    ;

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed
        return null;
    }
}

 
