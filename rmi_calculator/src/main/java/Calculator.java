import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

public interface Calculator extends Remote {
    void pushValue(int val,String clientID) throws RemoteException;

    void pushOperation(String operator,String clientID) throws RemoteException, ServerNotActiveException;

    int pop(String clientID) throws RemoteException, ServerNotActiveException;

    boolean isEmpty(String clientID) throws RemoteException;

    int delayPop(int millis,String clientID) throws RemoteException, ServerNotActiveException;


}