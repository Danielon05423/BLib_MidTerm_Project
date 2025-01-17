package gui.IPInputWindow;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import client.ChatClient;
import client.ClientController;
import client.ClientUI;
import common.ChatIF;
import gui.MainMenu.MainMenuController;
import gui.SubscriberRequestsWindows.SubscriberRequestsWindowsController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import logic.Subscriber;
import gui.baseController.*;

/**
 * Controller class for the IP Input window of the Library Management Tool.
 * 
 * <p>This class manages the user interactions for inputting the server IP address,
 * validating it, and navigating to the main menu if the connection is successful.</p>
 */
public class IPInputController extends BaseController {

    private IPInputController lipc;

    @FXML
    private Button btnExit = null;

    @FXML
    private Button btnSend = null;

    @FXML
    private TextField IPtxt;

    @FXML
    private Label awaitingLoginText;

    @FXML
    private Label welcomeLabel;

    /**
     * Retrieves the IP address entered by the user.
     *
     * @return the IP address entered in the {@link #IPtxt} TextField.
     */
    private String getIP() {
        return IPtxt.getText();
    }

    /**
     * Sends the entered IP address to the server for validation and handles the response.
     *
     * @param event the event triggered by clicking the send button.
     * @throws Exception if an error occurs while sending the IP address.
     */
    public void Send(ActionEvent event) throws Exception {
        String ip = getIP();
        if (ip.trim().isEmpty()) {
        	showColoredLabelMessageOnGUI(awaitingLoginText, "You must enter an IP address.", "-fx-text-fill: red;");
            System.out.println("You must enter an IP address.");
            return;
        }

        ClientUI.createChatConnection(ip);
        
        ClientUI.chat.accept("IP:" + ip);

        //TimeUnit.SECONDS.sleep(6); // XXX Optimal time to allow the client to get the information from the server.
    
        if (!ClientUI.isIPValid) {
        	showColoredLabelMessageOnGUI(awaitingLoginText, "Invalid IP address.", "-fx-text-fill: red;");
            System.out.println("ALERT: Invalid IP detected!");
        } else {
        	String labelMessage = "Connected successfully to IP: " + ip;
        	showColoredLabelMessageOnGUI(awaitingLoginText, labelMessage, "-fx-text-fill: green;");
            System.out.println(labelMessage);
            
            openMainMenu(event);
        }
    }

    /**
     * Opens the main menu of the application.
     *
     * @param event the event triggered by successfully connecting to the server.
     */
    private void openMainMenu(ActionEvent event) {
        openWindow(event, 
                   "/gui/MainMenu/MainMenuFrame.fxml", 
                   "/gui/MainMenu/MainMenuFrame.css", 
                   "MainMenu");
    }

    /**
     * Starts the IP Input window.
     *
     * @param primaryStage the primary stage of the application.
     * @throws Exception if an error occurs while starting the stage.
     */
    public void start(Stage primaryStage) throws Exception {
        start(primaryStage, 
              "/gui/IPInputWindow/IPInputFrame.fxml", 
              "/gui/IPInputWindow/IPInputFrame.css", 
              "Library Management Tool");
    }

    /**
     * Exits the application.
     *
     * @param event the event triggered by clicking the exit button.
     * @throws Exception if an error occurs while exiting.
     */
    public void getExitBtn(ActionEvent event) throws Exception {
        System.out.println("exit Library Tool");
        System.exit(1);
    }

    /**
     * Loads the subscriber data into the current controller.
     *
     * @param s1 the {@link Subscriber} object to load.
     */
    public void loadSubscriber(Subscriber s1) {
        this.lipc.loadSubscriber(s1);
    }

    /**
     * Displays a message in the console.
     *
     * @param message the message to display.
     */
    public void display(String message) {
        System.out.println(message);
    }
}