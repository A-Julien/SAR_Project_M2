package jvn.App;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface _Runnable {
    final static int port = 1099;
    final static String address = "127.0.0.1";
    final String ChatComponent = "talk";


    /**
     * Allows to start client and server
     * @return  return 1 if the start failed, and 1 if successful
     * @throws RemoteException
     */
    public int  run() throws Exception;

    /**
     * Allows to stop client and server
     * @throws RemoteException
     * @throws NotBoundException
     * @throws SQLException
     */
    public void stop() throws IOException, NotBoundException, SQLException, InterruptedException;


    /**
     * Build a rmi address for a distant object based on
     * a String name and String address
     * @param name name of the distant object
     * @param address address of the rmi server
     * @return
     */
    default String buildRmiAddr(String name, String address){
        return "rmi://" + address + "/"+ name;
    }
}
