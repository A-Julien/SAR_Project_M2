package jvn.Proxy;


import jvn.Server.JvnServerImpl;
import jvn.jvnOject.JvnObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JvnProxy implements InvocationHandler {
    private static final Logger logger = LogManager.getLogger(JvnProxy.class);


    private JvnObject jvnObject;

    private JvnProxy(JvnObject jvno){
        this.jvnObject= jvno;
    }

    /**
     * get server and add object
     * @param obj
     * @param name
     * @return
     */
    public static Object newInstance(Serializable obj, String name) {
        JvnObject jo = null;
        try {

            JvnServerImpl js = JvnServerImpl.jvnGetServer();

            jo = js.jvnLookupObject(name);

            if (jo == null) {
                jo = js.jvnCreateObject(obj);
                jo.jvnUnLock();
                js.jvnRegisterObject(name, jo);
            }

        } catch (Exception e) {}

        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new JvnProxy(jo));
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        try {
            if(method.isAnnotationPresent(JvnAnnotation.class)){
                if(method.getAnnotation(JvnAnnotation.class).type().equals("_R")){
                    jvnObject.jvnLockRead();
                }
                if(method.getAnnotation(JvnAnnotation.class).type().equals("_W")){
                    jvnObject.jvnLockWrite();
                }
            }
            Object res = method.invoke(jvnObject.getSharedObject(), objects);
            jvnObject.jvnUnLock();
            return res;

        } catch (Exception e){
            logger.error("Error when invoke method", e);
        }
        return null;
    }
}
