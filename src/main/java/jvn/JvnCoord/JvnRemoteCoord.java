/***
 * JAVANAISE API
 * JvnRemoteCoord interface
 * This interface defines the remote interface provided by the Javanaise coordinator
 * Contact: 
 *
 * Authors: 
 */

package jvn.JvnCoord;

import jvn.JvnException;
import jvn.Server.JvnRemoteServer;
import jvn.jvnOject.JvnObject;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Remote Interface of the JVN Coordinator
 */

public interface JvnRemoteCoord extends Remote {
    public static String rmiName = "coordService";

    /**
     * Allocate a NEW JVN object id (usually allocated to a
     * newly created JVN object)
     *
     * @throws java.rmi.RemoteException,JvnException
     **/
    public int jvnGetObjectUid()
            throws java.rmi.RemoteException, jvn.JvnException;

    public int jvnGetServerUid()
            throws java.rmi.RemoteException, jvn.JvnException;

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException;

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException;

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException;

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException;

    /**
     * A JVN server terminates
     *
     * @param jsUid : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    public void jvnTerminate(Integer jsUid)
            throws java.rmi.RemoteException, JvnException;

    public String sayHello() throws RemoteException, JvnException;

    public void ReduceServerCache(Integer serverUid, Integer jvnObjectUid, boolean lockWrite) throws RemoteException, JvnException;

    public String getJvnObjectName(Integer jvnObjectUid) throws RemoteException, JvnException;

    public void updateObjData(Integer uid, Serializable data) throws RemoteException, JvnException;

    }


