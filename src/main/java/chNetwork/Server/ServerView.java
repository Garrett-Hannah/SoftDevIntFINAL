package chNetwork.Server;

import java.util.ArrayList;

//Idk this is important...
public interface ServerView {
    void appendMessage(String message);
    void showErrorMessage(String title, String message);
    void setWindowTitle(String title);
    void updateUserList(ArrayList<String> userlist);
}
