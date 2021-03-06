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
import jvn.jvnOject.LockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;


public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {
    private static final Logger logger = LogManager.getLogger(JvnServerImpl.class);

    private static final Integer cacheSize = 2;

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
            logger.error("Server Failed to start");
            System.exit(1);
        }
    }

    /**
     * allow server to connect to rmi
     * @throws RemoteException
     * @throws NotBoundException
     * @throws JvnException
     */
    private void RmiConnect() throws RemoteException, NotBoundException, JvnException {

        this.registry = RmiConnection.RmiConnect(_Runnable.port, false);
        this.lookupCoord();
    }

    /**
     * loockup for coord instance via rmi
     * @throws RemoteException
     * @throws JvnException
     * @throws NotBoundException
     */
    private synchronized void lookupCoord() throws RemoteException, JvnException, NotBoundException {
        this.jvnCoord =
                (JvnRemoteCoord) this.registry.lookup(
                        ConfigManager.buildRmiAddr(JvnRemoteCoord.rmiName, _Runnable.address, _Runnable.port));
        this.uid = this.jvnCoord.jvnGetServerUid();
        if(this.jvnCoord == null) throw new JvnException("Can not find coordinator");
        logger.info(this.jvnCoord.sayHello());
        logger.info("My Uid is : " + this.uid);
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


    }

    /**
     * creation of a JVN object
     *
     * @param object : the JVN object state
     * @throws JvnException
     **/
    public synchronized JvnObject jvnCreateObject(Serializable object) throws jvn.JvnException, RemoteException {
        JvnObject jvnObject = new JvnObjectImpl(object,this.jvnCoord.jvnGetObjectUid());
        this.interceptorList.put(jvnObject.getUid(), jvnObject);
        return jvnObject;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jvnObjectName : the JVN object name
     * @param jvnObject  : the JVN object
     * @throws JvnException
     **/
    public synchronized void jvnRegisterObject(String jvnObjectName, JvnObject jvnObject) throws jvn.JvnException, RemoteException {
        this.jvnCoord.jvnRegisterObject(jvnObjectName, jvnObject,  this);
    }

    /**
     * Delete a JvnObject of the server according to the lockstate priorities.
     * (W > R > NL)
     * @throws JvnException
     * @throws RemoteException
     */
    private synchronized void reduceCache() throws JvnException, RemoteException {
        logger.warn("Reduce cache ..");
        Integer ifR = null;
        Integer ifW = null;

        for (Map.Entry<Integer, JvnObject> entry : this.interceptorList.entrySet()) {
            switch(entry.getValue().getCurrentLockState()){
                case NL:
                    this.jvnCoord.ReduceServerCache(this.uid, entry.getKey(), false);
                    logger.info("remove object : " +  this.interceptorList.remove(entry.getKey()).getUid() + " in NL");
                    return;

                case RC:
                case R:
                    if(ifR == null) ifR = entry.getKey();
                    break;

                case WC:
                case W:
                    if(ifW == null && ifR == null) ifW = entry.getKey();
                    break;

                default:
                    break;
            }
        }

        if (ifR != null){

            this.jvnCoord.ReduceServerCache(this.uid, ifR, false);
            logger.info("remove object : " + this.interceptorList.remove(ifR).getUid() + " in read");

            return;
        }

        if(ifW != null){

            this.jvnCoord.ReduceServerCache(this.uid, ifW, true);
            logger.info("remove object : " + this.interceptorList.remove(ifW).getUid() + " in write");

            return;
        }

        logger.error("Hum.. application may crash soon :'(");

    }


    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jvnObjectName : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public synchronized JvnObject jvnLookupObject(String jvnObjectName) throws jvn.JvnException, RemoteException, NotBoundException {
        if(this.interceptorList.size() >= cacheSize) this.reduceCache();

        JvnObject findObject = null;
        try {
            findObject = this.jvnCoord.jvnLookupObject(jvnObjectName, this);
        } catch (ConnectException e){
            logger.error("Server disconnect, try to reconnect");
            this.lookupCoord();
            this.updateCache();
            findObject = this.jvnCoord.jvnLookupObject(jvnObjectName, this);
        }
        if (findObject != null) this.interceptorList.put(findObject.getUid(), findObject);

        return findObject;
    }

    public synchronized  void updateObjData(Integer uid, Serializable data){
        //this.jvnCoord.updateObjData( uid,  data);
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public synchronized Serializable jvnLockRead(int joi) throws JvnException, RemoteException, NotBoundException {
        this.ensureJoCached(joi);

        try {
            return this.jvnCoord.jvnLockRead(joi, this);
        } catch (ConnectException e){
            logger.error("Server disconnect, try to reconnect");
        } catch (RemoteException e) {
            throw new JvnException("Error when lock read : " + e.getMessage());
        }

        this.lookupCoord();
        this.updateCache();
        return this.jvnCoord.jvnLockRead(joi, this);
    }

    /**
     * Update server cache when coord crash
     * @throws RemoteException
     * @throws JvnException
     */
    private synchronized void updateCache() throws RemoteException, JvnException {
        StringBuilder data = new StringBuilder();
        data.append("Update cache - remove lock \n");
        for(Map.Entry<Integer, JvnObject> entry : this.interceptorList.entrySet()){
            data.append("update cached object ").append(entry.getKey());
            this.interceptorList.put(entry.getKey(),this.jvnCoord.jvnLookupObject(this.jvnCoord.getJvnObjectName(entry.getKey()),this));
            this.interceptorList.get(entry.getKey()).setCurrentLockState(LockState.NL); // remove lock
        }
        logger.warn(data.toString());

    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public synchronized Serializable jvnLockWrite(int joi) throws JvnException, RemoteException, NotBoundException {
        this.ensureJoCached(joi);
        try {
            return jvnCoord.jvnLockWrite(joi, this);
        } catch (ConnectException e){
            logger.error("Server disconnect, try to reconnect");
            this.lookupCoord();
            this.updateCache();
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
    public synchronized void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException, NotBoundException {
        logger.info("jvnInvalidateReader  joi : " + joi);
        this.ensureJoCached(joi);
        this.interceptorList.get(joi).jvnInvalidateReader();
    }


    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException, NotBoundException {
        this.ensureJoCached(joi);
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
    public synchronized Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException, NotBoundException {
        this.ensureJoCached(joi);
        return this.interceptorList.get(joi).jvnInvalidateWriterForReader();
    }

    /**
     * return server UID
     * @return uid
     * @throws java.rmi.RemoteException
     * @throws jvn.JvnException
     */
    public Integer getUid() throws java.rmi.RemoteException, jvn.JvnException{
        return this.uid;
    }

    /**
     * Ensure that jvnObj are in cache, if not, get it from coord,
     * and flush an jvnObj in cache if cache are full
     * @param joi jvnObj uid to check
     * @throws RemoteException
     * @throws JvnException
     * @throws NotBoundException
     */
    private void ensureJoCached(Integer joi) throws RemoteException, JvnException, NotBoundException {
        if(this.interceptorList.get(joi) == null) this.jvnLookupObject(this.jvnCoord.getJvnObjectName(joi));
    }
}

 
