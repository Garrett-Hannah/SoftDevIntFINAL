package chNetwork.Client;

// Class for things required
public interface ChatView {
    void appendMessage(String message);
    void showErrorMessage(String title, String message);
    void clearInputField();
    void setWindowTitle(String title);
    void closeWindow();
}