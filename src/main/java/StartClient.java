import chkMVC.chView.BoardView;
import chkNetwork.Client.ChatWindow;
import chkNetwork.Client.ClientLogic;

public class StartClient {

    public static void main(String[] args) {
        ClientLogic clientLogic = new ClientLogic("localhost", 8000);
        clientLogic.connect("TestClient");
        ChatWindow chatWindow = new ChatWindow(clientLogic);



    }
}
