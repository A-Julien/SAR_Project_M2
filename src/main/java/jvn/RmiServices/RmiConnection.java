package jvn.RmiServices;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public abstract class RmiConnection {
    public static Registry RmiConnect(int port, boolean creatRegistry) throws RemoteException, NotBoundException {

        System.setProperty("java.security.policy", ConfigManager.getConfig("securityManagerProp"));
        if (System.getSecurityManager() == null) System.setSecurityManager(new RMISecurityManager());

        if(creatRegistry){
            try {
               return LocateRegistry.createRegistry(port);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try {
             return LocateRegistry.getRegistry(port);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
