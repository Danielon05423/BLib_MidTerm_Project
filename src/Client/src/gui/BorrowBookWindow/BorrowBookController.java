package gui.BorrowBookWindow;

import java.net.URL;
import java.util.ResourceBundle;

import client.ChatClient;
import client.ClientUI;
import gui.SearchWindow.SearchFrameController;
import gui.SubscriberWindow.SubscriberWindowController;
import gui.baseController.BaseController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.ClientTimeDiffController;
import logic.Subscriber;

/**
 * Controller class for the Borrow Book Window of the Library Management Tool.
 * 
 * <p>This class manages the user interactions for borrowing books, including searching for books,
 * submitting borrow requests, and reserving unavailable books. It provides feedback to users and
 * communicates with the server to handle book information and requests.</p>
 */
public class BorrowBookController extends BaseController implements Initializable {
	
	
    /** The currently logged-in subscriber. */
    Subscriber currentSub = SubscriberWindowController.currentSubscriber;
    
    /** Controller for managing time differences and calculating dates. */
    ClientTimeDiffController clockController = new ClientTimeDiffController();
    
    
    public static String result = "";
    
    /** Exit button to close the application. */
    @FXML
    private Button btnExit = null;

    /** Submit button to fetch book details based on the inputted ID. */
    @FXML
    private Button btnSubmit = null;

    /** Back button to navigate to the previous screen. */
    @FXML
    private Button btnBack = null;

    /** Search button to open the search window. */
    @FXML
    private Button btnSearch = null;

    /** Submit button to send the borrow request to the librarian. */
    @FXML
    private Button btnSubmitToLibrarian = null;

    /** Main menu button to return to the main menu. */
    @FXML
    private Button btnMainMenu = null;

    /** Text field for entering the book ID. */
    @FXML
    private TextField IDtxt = null;

    /** Label to display messages or prompts related to the book ID. */
    @FXML
    private Label awaitingTextID = null;

    /** Reserve button to reserve a book when no copies are available. */
    @FXML
    private Button btnReserve = null;

    /** Label to display the book's description and details. */
    @FXML
    private Label Book_Description = null;

    /** Label to display the status of the borrow or reservation request. */
    @FXML
    private Label RequestStatus = null;

    /** Stores the book ID entered by the user. */
    String bookId = "";
    
    /** Stores the name of the selected book. */
    String bookName = "";
    
    /** Number of copies of the selected book. */
    int copiesNum;
    
    /** Number of reserved copies of the selected book. */
    int reservedCopiesNum;
    
    /** Stores the available copies in the library in the current moment. */
    int availables;
    
    /** Status indicating whether the book can be borrowed. */
    String borrowStatus = "CAN_BORROW";  // String to hold the borrow status

    /**
     * Initializes the controller and sets up event handlers.
     * 
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        awaitingTextID.setText("");
        btnReserve.setVisible(false);
        
        btnSubmitToLibrarian.setDisable(true); // Disable the borrow request button by default
        
        // Add an event handler to clear the details when the text field is clicked
        IDtxt.setOnMouseClicked(event -> Clear());
    }

    /**
     * Starts the Borrow Book Window.
     * 
     * @param primaryStage the primary stage for the application
     * @throws Exception if an error occurs during initialization
     */
    public void start(Stage primaryStage) throws Exception {
        start(primaryStage, 
              "/gui/BorrowBookWindow/BorrowBookFrame.fxml", 
              "/gui/BorrowBookWindow/BorrowBookFrame.css", 
              "Borrow a Book");
    }

    /**
     * Handles the submission of a book ID to retrieve book details.
     * 
     * @param event the action event triggered by the user
     * @throws Exception if an error occurs while communicating with the server
     */
    public void Submit(ActionEvent event) throws Exception {
    	showColoredLabelMessageOnGUI(RequestStatus, "", "-fx-text-fill: black;");
        bookId = IDtxt.getText();
        ChatClient.awaitResponse = true; // Ensure waiting for response
        ClientUI.chat.accept("GetBookInfo:" + bookId);  // Request book info

        waitForServerResponse();

        if (ChatClient.BorrowedBookInfo != null) {
            Book_Description.setText(
            	"Book Details:\n" +
                "Book ID: " + bookId + "\n" +
                "Book Name: " + ChatClient.BorrowedBookInfo[1] + "\n" +
                "Subject: " + ChatClient.BorrowedBookInfo[2] +"\n" +
                "Description: " + ChatClient.BorrowedBookInfo[3] + "\n" +
                "Number of Copies: " + ChatClient.BorrowedBookInfo[4] +"\n" +
                "Location on Shelf: " + ChatClient.BorrowedBookInfo[5] + "\n" +
                "Available Copies Number: " + ChatClient.BorrowedBookInfo[6] + "\n" +
                "Reserved Copies Number: " + ChatClient.BorrowedBookInfo[7] + "\n"
            );
            bookName = ChatClient.BorrowedBookInfo[1];
            copiesNum = Integer.parseInt(ChatClient.BorrowedBookInfo[4]);
            availables=Integer.parseInt(ChatClient.BorrowedBookInfo[6]);
            reservedCopiesNum = Integer.parseInt(ChatClient.BorrowedBookInfo[7]);

            // Update borrow status based on available copies
            if (Integer.parseInt(ChatClient.BorrowedBookInfo[6]) <= 0) {
            	if(copiesNum==0) {
                    awaitingTextID.setText(bookName + " is currently unavailable. Please try again later");
                    btnSubmitToLibrarian.setDisable(true); // Disable if there are no copies
                    borrowStatus = "NO_COPIES";

            	}
            	else {            		
            		borrowStatus = "NO_COPIES";
            		btnReserve.setVisible(true);
            		awaitingTextID.setText("There are no more Copies of the book " + bookName + "\nWould you like to Reserve it?");
            		btnSubmitToLibrarian.setDisable(true); // Disable if there are no copies available
            	}
            	
            } else if (availables < reservedCopiesNum){
            	    borrowStatus = "ALL_COPIES_RESERVED";
            	    awaitingTextID.setText("All copies of " + bookName + " are reserved. Please try again later.");
            	    btnSubmitToLibrarian.setDisable(true); // Disable the button if no available copies
            	    btnReserve.setVisible(false);

            }  else if (availables == reservedCopiesNum){
            	borrowStatus = "NO_COPIES";
        		btnReserve.setVisible(true);
        		awaitingTextID.setText("Someone already has a reservation on this book " + bookName + "\nWould you like to Reserve the next copy?");
        		btnSubmitToLibrarian.setDisable(true); // Disable if there are no copies available

        }
            else {
                borrowStatus = "CAN_BORROW";
                awaitingTextID.setText("");
                btnReserve.setVisible(false);
                btnSubmitToLibrarian.setDisable(false); // Enable the button if book is available
            }
        } else {
            borrowStatus = "BOOK_NOT_FOUND";
            Book_Description.setText("No Book Found");
            btnSubmitToLibrarian.setDisable(true); // Disable the button if no book is found 
        }

    }


    /**
     * Submits a borrow request for the selected book.
     * 
     * @param event the action event triggered by the user
     * @throws Exception if an error occurs during the submission process
     */
    public void Submit_Borrow_Request(ActionEvent event) throws Exception {
        if ("CAN_BORROW".equals(borrowStatus)) {
            // Collect subscriber and book details
            String subscriberId = "" + SubscriberWindowController.currentSubscriber.getSubscriber_id();
            String subscriberName = SubscriberWindowController.currentSubscriber.getSubscriber_name();
            
            String returnDate = clockController.calculateReturnDate(14);
            String borrowDate = clockController.timeNow();
            
            String borrowRequest = "" + subscriberId + "," + subscriberName + "," + bookId + "," + bookName + "," + borrowDate + "," + returnDate;
            ClientUI.chat.accept("BorrowRequest:" + borrowRequest);
            waitForServerResponse();
            if(ChatClient.alreadyborrowed) {
            	showColoredLabelMessageOnGUIAndMakeItDisappearAfterDelay(RequestStatus, "Book already borrowed", "-fx-text-fill: red;", 4);
            	ChatClient.alreadyborrowed=false;
            	return;
            }
            // Feedback to the user
            showColoredLabelMessageOnGUI(RequestStatus, "Borrow request submitted successfully!\nAwaiting Librarian approval", "-fx-text-fill: green;");
            btnSubmitToLibrarian.setDisable(true); // Optionally disable the button after submitting
        } else {
            String errorMessage = getErrorMessage(borrowStatus); // Get appropriate error message
            showColoredLabelMessageOnGUI(RequestStatus, errorMessage, "-fx-text-fill: red;");
        }
    }

    /**
     * Submits a reservation request for the selected book.
     * 
     * @param event the action event triggered by the user
     * @throws Exception if an error occurs during the reservation process
     */
    public void Submit_Reserve_Request(ActionEvent event) throws Exception {
     	result = "";
    	String subscriberId1 = "" + SubscriberWindowController.currentSubscriber.getSubscriber_id();
    	
    	
    	// Check if subscriber already has a borrow request in-progress
    	String checkAlreadyRequested = "AlreadyRequestedCheck:" + subscriberId1 + "," + bookId;
    	ClientUI.chat.accept(checkAlreadyRequested);
    	
    	waitForServerResponse(); // Wait for server response
    	
    	if (result.equals("AlreadyRequested")) {
            // Subscriber already has a reservation for this book
            showColoredLabelMessageOnGUI(RequestStatus, 
                "You already have a borrow request\nin progress, wait until you get an\nanswer from the librarian.\nYou can't reserve a copy!", 
                "-fx-text-fill: red;");
            return;
    	}
    	
    	result = "";
    	// Check if the subscriber already has the book
    	String checkAlreadyBorrowed = "AlreadyBorrowedCheck:" + subscriberId1 + "," + bookId;
    	ClientUI.chat.accept(checkAlreadyBorrowed);
    	
    	waitForServerResponse(); // Wait for server response
    	
    	if (result.equals("AlreadyBorrowed")) {
            // Subscriber already has a reservation for this book
            showColoredLabelMessageOnGUI(RequestStatus, 
                "You already borrowed and \nhave this book.\nYou can't reserve a copy!", 
                "-fx-text-fill: red;");
            return;
    	}
 
    	result = "";
    	// Check if the subscriber already has a reservation for this book
        String checkReservationRequest = "ExistingReservationCheck:" + subscriberId1 + "," + bookId;
        ClientUI.chat.accept(checkReservationRequest);

        waitForServerResponse(); // Wait for server response

        if (result.equals("AlreadyReserved")) {
            // Subscriber already has a reservation for this book
            showColoredLabelMessageOnGUI(RequestStatus, 
                "You already have a reservation\non this book.\nYou can't reserve another copy!", 
                "-fx-text-fill: red;");
            return;
        }
        result = "";
        if (reservedCopiesNum >= copiesNum) {
            // All copies are reserved
            showColoredLabelMessageOnGUI(RequestStatus, 
                "All the current copies of the \nbook are reserved.\nPlease try to reserve at a\n later time when a copy is\navailable to be reserved.", 
                "-fx-text-fill: red;");
        } else if ("NO_COPIES".equals(borrowStatus)) {
            // Proceed with reservation logic
        	String subscriberName = SubscriberWindowController.currentSubscriber.getSubscriber_name();
            // Collect subscriber and book details
            String subscriberId = "" + SubscriberWindowController.currentSubscriber.getSubscriber_id();
            //String subscriberName = SubscriberWindowController.currentSubscriber.getSubscriber_name();
            String borrowDate = clockController.timeNow();
            
            String reserveDate = clockController.timeNow();
            String body = "" + subscriberName + "," + subscriberId + "," + bookName + "," + bookId + "," + borrowDate + "," + "temp";
            
            String reservation = "" + subscriberId + "," + bookName + "," + reserveDate + ","  + bookId;
            
            ClientUI.chat.accept("Reserve:" + reservation);
            waitForServerResponse();
            ClientUI.chat.accept("UpdateHistoryInDB:" + body + ",Reserved Successfully");
            waitForServerResponse();
            
            // Feedback to the user
            showColoredLabelMessageOnGUI(RequestStatus, 
                "You have successfully reserved the book.", 
                "-fx-text-fill: green;");
            
            // Disable the Reserve button
            btnReserve.setDisable(true);

        } else if (reservedCopiesNum == availables ){
        	
        	showColoredLabelMessageOnGUI(RequestStatus, 
                    "Book already has a reservation on it\nPlease wait until the reserver\nretrieves it, then you can reserve it\nOr you can submit a borrow request\nin 2 days if he doesn't retrieve it.", 
                    "-fx-text-fill: red;");
        	
        	
        } else {
            showColoredLabelMessageOnGUI(RequestStatus, 
                "Book is available, no need to reserve.", 
                "-fx-text-fill: red;");
        }
       
    }
    
    /**
     * Clears the input fields and resets the view.
     */
    public void Clear() {
        // Clear the text field for the book ID
        IDtxt.clear();
        
        // Clear the details displayed on the window
        Book_Description.setText("");
        awaitingTextID.setText("");
        RequestStatus.setText("");
        
        // Reset the Reserve button
        btnReserve.setVisible(false); // Hide the Reserve button if visible
        btnReserve.setDisable(false); // Re-enable the Reserve button
        
        // Disable the borrow request button
        btnSubmitToLibrarian.setDisable(true);
    }

    /**
     * Opens the search window for finding books.
     * 
     * @param event the action event triggered by the user
     * @throws Exception if an error occurs while opening the search window
     */
    public void openSearchWindow(ActionEvent event) throws Exception {
        SearchFrameController.FlagForSearch = "SubscriberBorrower";
        openWindow(event,
                "/gui/SearchWindow/SearchFrame.fxml",
                "/gui/SearchWindow/SearchFrame.css",
                "Search a Book");
    }

    /**
     * Navigates back to the Subscriber View.
     * 
     * @param event the action event triggered by the user
     * @throws Exception if an error occurs during navigation
     */
    public void Back(ActionEvent event) throws Exception {
        openWindow(event,
                "/gui/SubscriberWindow/SubscriberWindow.fxml",
                "/gui/SubscriberWindow/SubscriberWindow.css",
                "Subscriber View");
    }

    /**
     * Navigates to the Main Menu.
     * 
     * @param event the action event triggered by the user
     * @throws Exception if an error occurs during navigation
     */
    public void Main_Menu(ActionEvent event) throws Exception {
        SearchFrameController.FlagForSearch = "";
        openWindow(event, 
                "/gui/MainMenu/MainMenuFrame.fxml", 
                "/gui/MainMenu/MainMenuFrame.css", 
                "MainMenu");
    }

    /**
     * Returns the appropriate error message based on the borrow status.
     *
     * @param status the borrow status
     * @return the error message
     */
    private String getErrorMessage(String status) {
        switch (status) {
            case "NO_COPIES":
                return "There are no copies available for this book.";
            case "BOOK_NOT_FOUND":
                return "Book not found.";
            default:
                return "Unable to borrow this book due to an unknown error.";
        }
    }
}
