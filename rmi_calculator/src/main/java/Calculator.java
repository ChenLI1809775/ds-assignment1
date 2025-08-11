import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

public interface Calculator extends Remote {
    void pushValue(int val) throws RemoteException;
    void pushOperation(String operator) throws RemoteException, ServerNotActiveException;
    int pop() throws RemoteException, ServerNotActiveException;
    boolean isEmpty() throws RemoteException;
    int delayPop(int millis) throws RemoteException, ServerNotActiveException;
}