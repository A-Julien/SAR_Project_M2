package jvn.RmiServices;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public abstract class RmiConnection {
    public static Registry RmiConnect() throws RemoteException, NotBoundException {
        LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
        System.setProperty("java.security.policy", ConfigManager.getConfig("securityManagerProp"));
        if (System.getSecurityManager() == null) System.setSecurityManager(new RMISecurityManager());
        return registry;
    }
}
