package jvn.RmiServices;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public abstract class RmiConnection {
    public static Registry RmiConnect(int port) throws RemoteException, NotBoundException {
        LocateRegistry.createRegistry(port);
        Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
        System.setProperty("java.security.policy", ConfigManager.getConfig("securityManagerProp"));
        if (System.getSecurityManager() == null) System.setSecurityManager(new RMISecurityManager());
        return registry;
    }
}
