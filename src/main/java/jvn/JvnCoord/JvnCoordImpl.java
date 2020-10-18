/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn.JvnCoord;

import com.thoughtworks.xstream.XStream;
import irc.Sentence;
import irc.SentenceImpl;
import jvn.App._Runnable;
import jvn.JvnException;
import jvn.RmiServices.ConfigManager;
import jvn.RmiServices.RmiConnection;
import jvn.Server.JvnRemoteServer;
import jvn.jvnOject.JvnObject;
import jvn.jvnOject.JvnObjectImpl;
import jvn.jvnOject.LockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.*;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord, _Runnable{

    private static final Logger logger = LogManager.getLogger(JvnCoordImpl.class);


    private static final long serialVersionUID = 1L;
    private static int interceptorUid = 0;

    /** RMI parameter **/
    private Registry registry;
    private String adresse;
    private int port;

    /** Server parameter **/
    private int serverUid = 0;


    private XStream xstream ;

    private final String pathSaveData = "src/main/resources/";
    private final String saveDataExtention = ".coordData";


    /**
     * JvnServerID to JvnServer
     */
    Map<Integer, JvnRemoteServer> uidToJvnRemoteServer;

    /**
     * JvnServerID to {List of JvnObjectName}
     */
    Map<Integer, List<String>> serverUidToJvnObjectName;

    /**
     * JvnObjectID to {JvnServerID to JvnLockState}
     */
    Map<Integer, Map<Integer, LockState>> jvnObjectUidToLock;

    /**
     * JvnObjectName to JvnObjectID
     */
    Map<String, Integer> jvnObjectNameToUid;

    /**
     * JvnObjectName to JvnObject
     */
    Map<String, JvnObject> jvnObjectNameToJvnObject;

    private final String  uidToJvnRemoteServer_fileName = "uidToJvnRemoteServer";
    private final String  serverUidToJvnObjectName_fileName = "serverUidToJvnObjectName";
    private final String  jvnObjectUidToLock_fileName = "jvnObjectUidToLock";
    private final String  jvnObjectNameToUid_fileName = "jvnObjectNameToUid";
    private final String  jvnObjectNameToJvnObject_fileName = "jvnObjectNameToJvnObject";

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    public JvnCoordImpl(String adresse, int port) throws Exception {
        this.adresse = adresse;
        this.port = port;
        this.configureXStream();
        this.restoredata();
        logger.info(this.printMap());
    }

    private void configureXStream(){
        Class<?>[] classes = new Class[] { JvnObjectImpl.class, JvnObject.class, SentenceImpl.class};
        xstream = new XStream();
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypes(classes);
    }

    /**
     * Load all datas from save files if they exists, allocate new hasmap for otherwise
     */
    private void restoredata(){
       /* try {
            this.uidToJvnRemoteServer = (Map<Integer, JvnRemoteServer>) this.xstream.fromXML(this.readFile(this.uidToJvnRemoteServer_fileName));
        } catch (FileNotFoundException e) {
            logger.warn("Can not found " + this.uidToJvnRemoteServer_fileName + " create new map");
            this.uidToJvnRemoteServer = new HashMap<>();
        }
        try {
            this.serverUidToJvnObjectName = (Map<Integer, List<String>>) this.xstream.fromXML(this.readFile(this.serverUidToJvnObjectName_fileName));
        } catch (FileNotFoundException e) {
            logger.warn("Can not found " + this.serverUidToJvnObjectName_fileName + " create new map");
            this.serverUidToJvnObjectName = new HashMap<>();
        }
        try {
            this.jvnObjectUidToLock = (Map<Integer, Map<Integer, LockState>>) this.xstream.fromXML(this.readFile(this.jvnObjectUidToLock_fileName));
        } catch (FileNotFoundException e) {
            logger.warn("Can not found " + this.jvnObjectUidToLock_fileName + " create new map");
            this.jvnObjectUidToLock = new HashMap<>();
        }*/
        this.uidToJvnRemoteServer = new HashMap<>();
        this.serverUidToJvnObjectName = new HashMap<>();
        this.jvnObjectUidToLock = new HashMap<>();
        try {
            this.jvnObjectNameToUid = (Map<String, Integer>) this.xstream.fromXML(this.readFile(this.jvnObjectNameToUid_fileName));
            for (Integer entry : this.jvnObjectNameToUid.values()) this.jvnObjectUidToLock.put(entry, new HashMap<>());
        } catch (FileNotFoundException e) {
            logger.warn("Can not found " + this.jvnObjectNameToUid_fileName + " create new map");
            this.jvnObjectNameToUid = new HashMap<>();
        }
        try {
            this.jvnObjectNameToJvnObject = (Map<String, JvnObject>) this.xstream.fromXML(this.readFile(this.jvnObjectNameToJvnObject_fileName));
        } catch (FileNotFoundException e) {
            logger.warn("Can not found " + this.jvnObjectNameToJvnObject_fileName + " create new map");
            this.jvnObjectNameToJvnObject = new HashMap<>();
        }
    }

    private void RmiConnect() throws RemoteException, NotBoundException {
        this.registry = RmiConnection.RmiConnect(this.port, true);
        try {
            this.registry.rebind(ConfigManager.buildRmiAddr(this.rmiName, _Runnable.address, _Runnable.port), this);
        } catch (RemoteException e) {
            logger.error("Coordinator failed to start");
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

    /**
     * Allocate a new JvnServer ID.
     * @return a new id
     * @throws RemoteException
     * @throws JvnException
     */
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
        logger.info("Server <" + jvnRemoteServer.getUid() +"> want register object <" + jvnObjectName + ">");

        if(this.jvnObjectNameToJvnObject.containsKey(jvnObjectName)){
            logger.error("object <" + jvnObjectName + "> already exist");
            return;
        }

        // add server if not exist
        if(!this.uidToJvnRemoteServer.containsKey(jvnRemoteServer.getUid())){
            this.uidToJvnRemoteServer.put(jvnRemoteServer.getUid(), jvnRemoteServer);

            this.serverUidToJvnObjectName.put(jvnRemoteServer.getUid(), new ArrayList<>());
            this.serverUidToJvnObjectName.get(jvnRemoteServer.getUid()).add(jvnObjectName);
        }

        //add object
        this.jvnObjectNameToJvnObject.put(jvnObjectName, jvnObject);

        //add jvnobjname to id
        this.jvnObjectNameToUid.put(jvnObjectName,jvnObject.getUid());

        this.jvnObjectUidToLock.put(jvnObject.getUid(), new HashMap<>());
        switch (jvnObject.getCurrentLockState()){
            case RWC:
            case WC:
                this.jvnObjectUidToLock.get(jvnObject.getUid()).put((jvnRemoteServer.getUid()), LockState.W);
                break;
            case RC:
                this.jvnObjectUidToLock.get(jvnObject.getUid()).put((jvnRemoteServer.getUid()), LockState.R);
                break;
            default:
                this.jvnObjectUidToLock.get(jvnObject.getUid()).put((jvnRemoteServer.getUid()), jvnObject.getCurrentLockState());
                break;
        }
        this.saveAllData();
    }

    /**
     * Write the changes in datas to save them.
     */
    private synchronized void saveAllData(){
        /*this.writeFile(xstream.toXML(this.uidToJvnRemoteServer), this.uidToJvnRemoteServer_fileName);
        this.writeFile(xstream.toXML(this.serverUidToJvnObjectName), this.serverUidToJvnObjectName_fileName);
        this.writeFile(xstream.toXML(this.jvnObjectUidToLock), this.jvnObjectUidToLock_fileName);*/

        this.writeFile(xstream.toXML(this.jvnObjectNameToJvnObject), this.jvnObjectNameToJvnObject_fileName);
        this.writeFile(xstream.toXML(this.jvnObjectNameToUid), this.jvnObjectNameToUid_fileName);
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
        logger.info("Server <" + jvnRemoteServer.getUid() +"> lookup for object  <" + jvnObjectName + ">");

        if(!this.jvnObjectNameToJvnObject.containsKey(jvnObjectName)) {
            logger.warn("Object <" + jvnObjectName + "> not register");
            return null;
        }

        if(!this.serverUidToJvnObjectName.containsKey(jvnRemoteServer.getUid())){
            logger.info("adding server <" +jvnRemoteServer.getUid() + "> to database" );
            this.serverUidToJvnObjectName.put(jvnRemoteServer.getUid(), new ArrayList<>());
            this.uidToJvnRemoteServer.put(jvnRemoteServer.getUid(), jvnRemoteServer);
        }

        if(!this.serverUidToJvnObjectName.get(jvnRemoteServer.getUid()).contains(jvnObjectName)){
            this.serverUidToJvnObjectName.get(jvnRemoteServer.getUid()).add(jvnObjectName);
        }
        this.jvnObjectNameToJvnObject.get(jvnObjectName).setCurrentLockState(LockState.NL);

        /*this.writeFile(xstream.toXML(this.uidToJvnRemoteServer), this.uidToJvnRemoteServer_fileName);
        this.writeFile(xstream.toXML(this.jvnObjectNameToJvnObject), this.jvnObjectNameToJvnObject_fileName);
        this.writeFile(xstream.toXML(this.serverUidToJvnObjectName), this.serverUidToJvnObjectName_fileName);*/
        this.saveAllData();

        logger.info("Return object " + jvnObjectName + " from database");
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
        logger.info("server " + jvnRemoteServer.getUid() + " want ReadLock on " + jvnObjectUid);

        if(!this.jvnObjectUidToLock.containsKey(jvnObjectUid)){
            throw new JvnException("Error, object uid : " + jvnObjectUid + " not found - status : " + this.printMap());
        }

        //case no lock on jvnObjectUid
        if(this.jvnObjectUidToLock.get(jvnObjectUid).isEmpty()){
            logger.info("no lock on object " + jvnObjectUid);
            String name = this.getJvnObjectName(jvnObjectUid);

            if (name == null) throw new JvnException("Error, object uid : " + jvnObjectUid + " not found");

            this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer.getUid(), LockState.R);
            //this.writeFile(xstream.toXML(this.jvnObjectUidToLock), this.jvnObjectUidToLock_fileName);
            logger.info("getlock" + this.printMap());
            return this.jvnObjectNameToJvnObject.get(name).getSharedObject();
        }

        for (Map.Entry<Integer, LockState> entry : jvnObjectUidToLock.get(jvnObjectUid).entrySet()) {
            if (entry.getValue() == LockState.W) {
                Serializable newObject;

                try {
                    newObject = this.uidToJvnRemoteServer.get(entry.getKey()).jvnInvalidateWriterForReader(jvnObjectUid);
                    this.jvnObjectUidToLock.get(jvnObjectUid).replace(entry.getKey(), LockState.R);
                }catch (RemoteException | NotBoundException e){
                    logger.warn("Server uid " + entry.getKey() + " disconnect");
                    this.jvnTerminate(entry.getKey());
                    newObject = this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid));
                }

                this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).updateSharedObject(newObject);

                if(!entry.getKey().equals(jvnRemoteServer.getUid()))
                        this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer.getUid(), LockState.R);
                this.saveAllData();
                return newObject;
            }
        }

        this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer.getUid(), LockState.R);
        this.saveAllData();
        return this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).getSharedObject();
    }

    /**
     * Return the name of a JvnObject associated to its ID.
     * @param jvnObjectUid the JvnObject's ID
     * @return the JvnObject' name
     */
    @Override
    public String getJvnObjectName(Integer jvnObjectUid) {
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
         logger.info("server " + jvnRemoteServer.getUid() + " want writeLock on " + jvnObjectUid);
         logger.info(this.printMap());

        if(!this.jvnObjectUidToLock.containsKey(jvnObjectUid)) //TODO INSURE
            throw new JvnException("Error, object uid : " + jvnObjectUid + " not found");

        //case no lock on jvnObjectUid
        if(this.jvnObjectUidToLock.get(jvnObjectUid).isEmpty()){
            String name = this.getJvnObjectName(jvnObjectUid);

            if (name == null) throw new JvnException("Error, object uid : " + jvnObjectUid + " not found");

            this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer.getUid(), LockState.W);
            //this.writeFile(xstream.toXML(this.jvnObjectUidToLock), this.jvnObjectUidToLock_fileName);

            return this.jvnObjectNameToJvnObject.get(name);
        }

         List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, LockState> entry : jvnObjectUidToLock.get(jvnObjectUid).entrySet()) {
            if (!entry.getKey().equals(jvnRemoteServer.getUid())){
                switch (entry.getValue()){
                    case R:
                        try {
                            this.uidToJvnRemoteServer.get(entry.getKey()).jvnInvalidateReader(jvnObjectUid);
                            toRemove.add(entry.getKey());
                        } catch (RemoteException | NotBoundException e) {
                            logger.warn("Server uid " + entry.getKey() + " disconnect");
                            this.jvnTerminate(entry.getKey());
                        }
                        break;

                    case W:
                        try {
                            Serializable newObject = this.uidToJvnRemoteServer.get(entry.getKey()).jvnInvalidateWriter(jvnObjectUid);
                            toRemove.add(entry.getKey());
                            this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).updateSharedObject(newObject);
                        } catch (RemoteException | NotBoundException e){
                            logger.warn("Server uid " + entry.getKey() + " disconnect");
                            this.jvnTerminate(entry.getKey());
                        }
                        break;
                    default:
                        logger.error("jvnLockWrite error" );
                        throw new JvnException("[Coord] error ");
                }
            }
        }

        for (Integer serverUid : toRemove) this.jvnObjectUidToLock.get(jvnObjectUid).remove(serverUid);

        this.jvnObjectUidToLock.get(jvnObjectUid).remove(jvnRemoteServer.getUid());

        this.jvnObjectUidToLock.get(jvnObjectUid).put(jvnRemoteServer.getUid(), LockState.W);

        this.saveAllData();
        return this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).getSharedObject();
    }

    /**
     * A JVN server terminates
     *
     * @param jsUid : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized void jvnTerminate(Integer jsUid)
            throws java.rmi.RemoteException, JvnException {
        this.uidToJvnRemoteServer.remove(jsUid);
        for ( Integer objUid : this.jvnObjectUidToLock.keySet() ) jvnObjectUidToLock.get(objUid).remove(jsUid);
        this.serverUidToJvnObjectName.remove(jsUid);

        /*this.writeFile(xstream.toXML(this.uidToJvnRemoteServer), this.uidToJvnRemoteServer_fileName);
        this.writeFile(xstream.toXML(this.serverUidToJvnObjectName), this.serverUidToJvnObjectName_fileName);*/
    }


    @Override
    public int run() throws Exception {
        this.RmiConnect();
        logger.info("RMI OK");//Formatter.log(lvl.INFO, "Rmi connection ok"));
        return 0;
    }

    @Override
    public void stop() throws IOException, NotBoundException, SQLException, InterruptedException {
        this.registry.unbind(this.buildRmiAddr(this.rmiName, _Runnable.address));
    }

    @Override
    public synchronized String sayHello()  throws java.rmi.RemoteException, JvnException{
        return "Coordinator say hello to server, i'm in live";
    }

    /**
     * Delete a JvnObject on a JvnServer.
     * @param serverUid JvnServer's ID
     * @param jvnObjectUid JnvObject's ID
     * @param lockWrite true if JvnObject's lockstate is W, false otherwise
     * @throws RemoteException
     * @throws JvnException
     */
    @Override
    public synchronized void ReduceServerCache(Integer serverUid, Integer jvnObjectUid, boolean lockWrite) throws RemoteException, JvnException {
        if (lockWrite){
            Serializable newObject = this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).jvnInvalidateWriter();
            this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(jvnObjectUid)).updateSharedObject(newObject);
            //this.writeFile(xstream.toXML(this.jvnObjectNameToJvnObject), this.jvnObjectNameToJvnObject_fileName);
        }
        this.serverUidToJvnObjectName.get(serverUid).remove(this.getJvnObjectName(jvnObjectUid));

        //this.writeFile(xstream.toXML(this.serverUidToJvnObjectName), this.serverUidToJvnObjectName_fileName);
        this.saveAllData();

    }

    private synchronized void writeFile(String data, String dataName){
        try {
            FileWriter myWriter = new FileWriter(pathSaveData.concat(dataName).concat(this.saveDataExtention));
            myWriter.write(data);
            myWriter.close();
            logger.info("Save data " + dataName);
        } catch (IOException e) {
            logger.error("An error when saving " + dataName);
            e.printStackTrace();
        }
    }

    private String readFile(String fileName) throws FileNotFoundException {
        logger.info("Try to reading " + this.pathSaveData.concat(fileName));
        File myObj = new File(this.pathSaveData.concat(fileName).concat(this.saveDataExtention));
        Scanner myReader = new Scanner(myObj);
        StringBuilder data = new StringBuilder();
        while (myReader.hasNextLine()) {
            data.append(myReader.nextLine());

        }
        myReader.close();
        return data.toString();
    }

    /**
     * Print datas of the JvnCoordinator.
     * @return
     */
    private synchronized String printMap() {
        StringBuilder data = new StringBuilder();
        data.append("\n");
        data.append("Object in memory :").append("\n");
        for (Map.Entry<String,JvnObject> entry :this.jvnObjectNameToJvnObject.entrySet()) {
            data.append("\t* ").append(entry.getKey()).append(" - id <").append(
                    this.jvnObjectNameToUid.get(entry.getKey())
            ).append(">\n");
        }
        data.append("Server lock Status : ").append("\n");
        for(Map.Entry<Integer, Map<Integer, LockState>> entry : this.jvnObjectUidToLock.entrySet()){
            data.append("\t* Objet id : ").append(entry.getKey()).append(" - <" + this.getJvnObjectName(entry.getKey())).append(">\n");
            for(Map.Entry<Integer, LockState> entry2 : entry.getValue().entrySet()){
                data.append("\t\t- Server ").append(entry2.getKey()).append(" -> ").append(entry2.getValue()).append("\n");
                }
        }
        return data.toString();
    }

    public synchronized void updateObjData(Integer uid, Serializable data) throws RemoteException, JvnException{
        this.jvnObjectNameToJvnObject.get(this.getJvnObjectName(uid)).updateSharedObject(data);
    }

}
