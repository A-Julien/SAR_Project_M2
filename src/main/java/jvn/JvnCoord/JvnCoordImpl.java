/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn.JvnCoord;

import jvn.JvnException;
import jvn.Server.JvnRemoteServer;
import jvn.RmiServices.ConfigManager;
import jvn.RmiServices.RmiConnection;
import jvn.jvnOject.JvnObject;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.sql.SQLException;

import jvn.App._Runnable;

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
     * Default constructor
     *
     * @throws JvnException
     **/
    public JvnCoordImpl(String adresse, int port) throws Exception {
        this.adresse = adresse;
        this.port = port;
        // to be completed
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
    public int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException {
        return ++interceptorUid;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    @Override
    public void jvnRegisterObject(String jon, JvnObject jo, int joi, JvnRemoteServer js) throws RemoteException, JvnException {

    }


    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed
        return null;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public Serializable jvnLockRead(int joi, JvnRemoteServer js)
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
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
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
    public void jvnTerminate(JvnRemoteServer js)
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
        this.registry.unbind(this.buildRmiAddr("Coord", this.adresse));
    }
}

 
