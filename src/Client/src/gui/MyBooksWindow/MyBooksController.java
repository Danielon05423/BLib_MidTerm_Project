package gui.MyBooksWindow;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import client.ChatClient;
import client.ClientUI;
import gui.SubscriberWindow.SubscriberWindowController;
import gui.baseController.BaseController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import logic.BorrowedBook;
import logic.ClientTimeDiffController;
import logic.Subscriber;

/**
 * Controller for the Search Window in the Library Management Tool.
 * 
 * <p>This class allows users to search for books based on name, description, 
 * and subject filters, and displays the results in a TableView. It also provides 
 * an option to navigate back to the Main Menu.</p>
 */
public class MyBooksController extends BaseController implements Initializable {
	private MyBooksController mbc;
    public static String FlagForSearch = "";
	
    @FXML
	private Label extensionDynamicLabel;
    @FXML
	private Label title;
    @FXML
    private Label LBLview;
    
	@FXML
    private Button btnExit;
	
    @FXML
    private Button btnBackF;
    @FXML
    private Button btnView;
    
    @FXML
    private Button btnHistory;
    
    @FXML
    private TableView<BorrowedBook> tableView;

    @FXML
    private TableColumn<BorrowedBook, Integer> tableID;
    
    @FXML
    private TableColumn<BorrowedBook, String> tableBorrowDate;
    
    @FXML
    private TableColumn<BorrowedBook, String> tableReturnDate;
    
    @FXML
    private TableColumn<BorrowedBook, String> tableName;
    
    @FXML
    private TableColumn<BorrowedBook, Integer> tableTimeLeft;
    
    @FXML
    private TableColumn<BorrowedBook, Void> tableActions;


    @FXML
    private TextField nameInput;
    @FXML
    private TextField TXTFview;

    @FXML
    private TextField descriptionInput;

    /**
     * Initializes the Search Window by setting up the TableView and ComboBox.
     * Populates the table with all available books when the page loads.
     *
     * @param url the location of the FXML file.
     * @param resourceBundle the resource bundle for internationalization.
     */
    public static Subscriber currentSub = new Subscriber (0,0,null,null,null,null);
    public static int librarianViewing=-1;
    public static String LibrarianName;
    public static Boolean viewing=false;
    @Override

    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize TableView columns
        tableID.setCellValueFactory(new PropertyValueFactory<>("ISBN")); // ISBN column
        tableName.setCellValueFactory(new PropertyValueFactory<>("name")); // Name column
        tableBorrowDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate")); // Borrow Date column
        tableReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate")); // Return Date column
        tableTimeLeft.setCellValueFactory(new PropertyValueFactory<>("timeLeftToReturn")); // Time Left column

        if (viewing) {
            currentSub = new Subscriber(0, 0, null, null, null, null);
            tableView.getItems().clear();
            title.setText("waiting for librarian input");
            btnView.setVisible(true);
            TXTFview.setVisible(true);
            LBLview.setVisible(true);

        } else {
            currentSub = SubscriberWindowController.currentSubscriber;
            title.setText("My Books");
            btnView.setVisible(false);
            TXTFview.setVisible(false);
            LBLview.setVisible(false);
            try {
				loadBooks();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        // Set up the actions column
        setupActionsColumn();
        tableView.getItems().clear(); // Clear any items before populating the table
    }


    
    private void loadBooks() throws InterruptedException {

            ClientUI.chat.accept("GetBorrowedBooks:" + currentSub.getSubscriber_id());
            if(viewing) {
            	addDelayInMilliseconds(100);
            	if (ChatClient.borrowedBookList != null && !ChatClient.borrowedBookList.isEmpty()) {
                    tableView.getItems().clear();
                    tableView.getItems().addAll(ChatClient.borrowedBookList);
                } else {
                    System.out.println("No borrowed books to display.");
                    tableView.getItems().clear();
                }
            }else {
                Platform.runLater(() -> {
                	if (ChatClient.borrowedBookList != null && !ChatClient.borrowedBookList.isEmpty()) {
                        tableView.getItems().clear();
                        tableView.getItems().addAll(ChatClient.borrowedBookList);
                    } else {
                        System.out.println("No borrowed books to display.");
                        tableView.getItems().clear();
                    }
                });

            }  
    }
    
    


    public void updateView(ActionEvent event) throws InterruptedException {

    		String subID = TXTFview.getText();
        	ClientUI.chat.accept("Fetch:"+subID);
        	addDelayInMilliseconds(100);
            currentSub = new Subscriber(
            	  ChatClient.s1.getSubscriber_id(),
            	  ChatClient.s1.getDetailed_subscription_history(),
            	  ChatClient.s1.getSubscriber_name(),
            	  ChatClient.s1.getSubscriber_phone_number(),
            	  ChatClient.s1.getSubscriber_email(),
            	  ChatClient.s1.getStatus()
            );
            addDelayInMilliseconds(100);
            if(currentSub.getSubscriber_id() == -1) {
            	title.setText("No ID Found");
            }else {
            	title.setText("Now Viewing Subscriber: "+currentSub.getSubscriber_id()+" , "+currentSub.getSubscriber_name());
            }
            
    	loadBooks();
    }

        // after TODO is done and subscriber's book are in DB we fetch them here into the table.
//        // Fetch and populate books
//        new Thread(() -> {
//        	ClientUI.chat.accept("GetBorrowedBooks:");
//            Platform.runLater(this::loadBooks); // Populate the table after data is fetched
//        }).start();
//    }
    /**
     * Loads borrowed books into the TableView by fetching them from the server.
     * If no books are borrowed, displays a message in the console.
     * TODO: Implement after adding subscriber's books to database. 
     */
//    private void loadBooks() {
//        // Send "GetBooks" request to the server to fetch the books
//        //ClientUI.chat.accept("GetBooks:");
//
//        // Ensure bookList is not empty
//        if (ChatClient.bookList != null && !ChatClient.bookList.isEmpty()) {
//            // Populate the TableView with all books initially
//            Platform.runLater(() -> {
//                tableView.getItems().clear();  // Clear any existing data
//                tableView.getItems().addAll(ChatClient.bookList);  // Add all books to the table
//            });
//        } else {
//            System.out.println("No books are currently borrowed.");
//        }
//    }
        private void setupActionsColumn() {
            tableActions.setCellFactory(param -> new TableCell<BorrowedBook, Void>() { // Explicitly specify the generic types
                private final Button extendButton = new Button("Extend borrowing length");
                private final Button returnButton = new Button("Return");
                private final HBox buttonBox = new HBox(10, extendButton, returnButton);

                {
                    buttonBox.setStyle("-fx-alignment: CENTER;");

                    extendButton.setOnAction(event -> {
                        BorrowedBook borrowedBook = getTableView().getItems().get(getIndex());
                    	ClientTimeDiffController clock = new ClientTimeDiffController();
                    	String extendedReturnDate;
                    	String ignore = "ignore";
                    	String librarianMessage= ",Extended Successfully by Librarian " + librarianViewing + ":" + LibrarianName;
                    	String body = currentSub.getSubscriber_name() + "," +
                                currentSub.getSubscriber_id() + "," +
                                borrowedBook.getName() + "," +
                                borrowedBook.getBorrowId() + "," +
                                borrowedBook.getBorrowDate() + "," +ignore;
                    	ClientUI.chat.accept("IsBookReserved:" + borrowedBook.getISBN());
                                
                    	
                    		if(viewing) {
                    			if(!(ChatClient.isBookReservedFlag)) {
                            		if(clock.hasEnoughTimeBeforeDeadline(borrowedBook.getReturnDate(), 7)) { // add condition - AND book is not reserved - we check if book reserved in DB.
                                    	extendedReturnDate = clock.extendReturnDate(borrowedBook.getReturnDate(), 14);
                                		ClientUI.chat.accept("UpdateReturnDate:"+borrowedBook.getBorrowId()+","+extendedReturnDate);
                                        ClientUI.chat.accept("UpdateHistoryInDB:"+body+librarianMessage);
                                		showColoredLabelMessageOnGUI(extensionDynamicLabel, "Extension approved!", "-fx-text-fill: green;");
                                		tableView.refresh();
                                	    tableReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
                                	    tableView.refresh();
                                	}else {
                                		showColoredLabelMessageOnGUI(extensionDynamicLabel, "Extension denied! Return time must be 7 days or less.", "-fx-text-fill: red;");
                                	}
                            	}
                            	else {
                            		showColoredLabelMessageOnGUI(extensionDynamicLabel, "Extension denied! Book already reserved.", "-fx-text-fill: red;");
                            	}
	                    		tableView.getItems().clear();
	                    		try {
									loadBooks();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                    		}else {
                    			if(!(ChatClient.isBookReservedFlag)) {
                            		if(clock.hasEnoughTimeBeforeDeadline(borrowedBook.getReturnDate(), 7)) { // add condition - AND book is not reserved - we check if book reserved in DB.
                                    	extendedReturnDate = clock.extendReturnDate(borrowedBook.getReturnDate(), 14);
                                		ClientUI.chat.accept("UpdateReturnDate:"+borrowedBook.getBorrowId()+","+extendedReturnDate);
                                        ClientUI.chat.accept("UpdateHistoryInDB:"+body+",Extended Successfully");
                                		showColoredLabelMessageOnGUI(extensionDynamicLabel, "Extension approved!", "-fx-text-fill: green;");
                                		tableView.refresh();
                                	    tableReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
                                	    tableView.refresh();
                                	}else {
                                		showColoredLabelMessageOnGUI(extensionDynamicLabel, "Extension denied! Return time must be 7 days or less.", "-fx-text-fill: red;");
                                	}
                            	}
                            	else {
                            		showColoredLabelMessageOnGUI(extensionDynamicLabel, "Extension denied! Book already reserved.", "-fx-text-fill: red;");
                            	}
	                    		tableView.getItems().clear();
	                    		try {
									loadBooks();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                    		}
                    	});

                    returnButton.setOnAction(event -> {
                        BorrowedBook borrowedBook = getTableView().getItems().get(getIndex());
                        System.out.println("Return book: " + borrowedBook.getName());
                        ClientUI.chat.accept("Return request: Subscriber ID is:"+currentSub.getSubscriber_id()+" "+currentSub.getSubscriber_name()+" Borrow info: "+borrowedBook);
                        //ClientUI.chat.accept("Return Book: Subscriber ID is:"+currentSub.getSubscriber_id()+" Book info is:"+borrowedBook);
                        
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null); // Clear the cell if empty
                    } else {
                        setGraphic(buttonBox); // Add the buttons to the cell
                    }
                }
            });
        }
        

        
        public void navigateToHistory(ActionEvent event) {
        	openWindow(event,
        			"/gui/HistoryWindow/HistoryFrame.fxml",
        			"/gui/HistoryWindow/HistoryFrame.css",
        			"History");
        	
        }
//
//        public void extendButton(ActionEvent event, BorrowedBook selectedBook) {
//        	System.out.println("nnoooo");
//
//
//        }

    /**
     * Searches for books based on filters entered in the name, description, and subject fields.
     * Displays the filtered results in the TableView.
     *
     * @param event the event triggered by clicking the search button.
     * @throws Exception if an error occurs during the search.
     */
//    public void Search(ActionEvent event) throws Exception {
//        ClientUI.chat.accept("GetBooks:");  // Get the latest books
//
//        // Ensure bookList is not empty
//        if (ChatClient.bookList != null && !ChatClient.bookList.isEmpty()) {
//            // If all filter fields are empty, show all books
//            if (nameInput.getText().isEmpty() && descriptionInput.getText().isEmpty() && (subjectInput.getValue() == null || subjectInput.getValue().toString().isEmpty())) {
//                // No filtering, show all books
//                Platform.runLater(() -> {
//                    tableView.getItems().clear();  // Clear any existing data
//                    tableView.getItems().addAll(ChatClient.bookList);  // Add all books to the table
//                });
//            } else {
//                // Otherwise, filter books based on the input fields
//                List<Book> filteredBooks = new ArrayList<>();
//
//                for (Book book : ChatClient.bookList) {
//                    boolean matches = true;
//
//                    // Check for name filter
//                    if (!nameInput.getText().isEmpty() && !book.getName().toLowerCase().contains(nameInput.getText().toLowerCase())) {
//                        matches = false;
//                    }
//
//                    // Check for description filter
//                    if (!descriptionInput.getText().isEmpty() && !book.getDescription().toLowerCase().contains(descriptionInput.getText().toLowerCase())) {
//                        matches = false;
//                    }
//
//                    // Check for subject filter from ComboBox
//                    if (subjectInput.getValue() != null && !subjectInput.getValue().toString().isEmpty() && !book.getSubject().toLowerCase().contains(subjectInput.getValue().toString().toLowerCase())) {
//                        matches = false;
//                    }
//
//                    // If all filters match, add the book to the filtered list
//                    if (matches) {
//                        filteredBooks.add(book);
//                    }
//                }
//
//                // Populate the TableView with the filtered books
//                Platform.runLater(() -> {
//                    tableView.getItems().clear();  // Clear any existing data
//                    tableView.getItems().addAll(filteredBooks);  // Add the filtered books to the table
//                });
//            }
//        } else {
//            System.out.println("No books to display.");
//        }
//    }


    /**
     * Handles the Exit button action, navigating back to the Main Menu.
     *
     * @param event the event triggered by clicking the exit button.
     */
    public void getExitBtn(ActionEvent event) {
    	openWindow(event,
    			"/gui/MainMenu/MainMenuFrame.fxml",
    			"/gui/MainMenu/MainMenuFrame.css",
    			"MainMenu");
    }
   
    public void backFromUser(ActionEvent event) {
    	if(viewing) {
    		tableView.getItems().clear();
        	openWindow(event,
        			"/gui/LibrarianWindow/LibrarianFrame.fxml",
        			"/gui/LibrarianWindow/LibrarianFrame.css",
        			"Librarian View");
    	}else {
        	openWindow(event,
        			"/gui/SubscriberWindow/SubscriberWindow.fxml",
        			"/gui/SubscriberWindow/SubscriberWindow.css",
        			"Subscriber View");
    	}
    }

}
