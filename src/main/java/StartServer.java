import chkNetwork.Server.ServerLogic;
import chkNetwork.Server.ServerView;
import chkNetwork.Server.ServerWindow;

import java.io.IOException;

public class StartServer {

    public static void main(String[] args) {
        ServerLogic serverLogic = new ServerLogic(8000);

        try {
            serverLogic.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ServerWindow serverWindow = new ServerWindow(serverLogic);
    }

}
