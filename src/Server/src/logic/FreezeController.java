package logic;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.ConnectToDb;
import gui.baseController.BaseController;
import server.EchoServer;


 //TODO: imporve log&CMD messages.
public class FreezeController extends BaseController {
	ServerTimeDiffController clock = EchoServer.clock;
	
	public void freeze() throws SQLException {
		List<String> borrowedBooksList = ConnectToDb.fetchBorrowedBooksForTaskScheduler(EchoServer.taskSchedulerConnection);
		
		for (String borrowedBook : borrowedBooksList) {
			String[] bookData = parseBorrowedBook(borrowedBook);
			LocalDateTime returnDate = clock.convertStringToLocalDateTime(bookData[4]); 
			
			if (clock.hasWeekPassed(returnDate, LocalDateTime.now())) {
				int subscriberID = Integer.valueOf(bookData[1]); // Grab subscriber ID and convert it to integer.
				String bookName = bookData[2];
				
				ConnectToDb.freezeSubscriber(EchoServer.taskSchedulerConnection, subscriberID); // Freeze user given their id.

				// log on both CMD and Log that the user has been frozen.
				String subscriber = ConnectToDb.fetchSubscriberData(EchoServer.taskSchedulerConnection, bookData[1]); 
				EchoServer.outputInOutputStreamAndLog(clock.timeNow() + ": " + subscriber.toString() + " has been frozen because they were late at returning the book: " + bookName);
				
			}
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public void unfreeze() throws SQLException{
		List<String> subscribersList = ConnectToDb.fetchAllData(EchoServer.taskSchedulerConnection);
		for (String subscriber : subscribersList) {
			String[] subscriberInformation = subscriber.split(", ");
			String[] status = subscriberInformation[5].split(":");

			if (!status[1].equals("Not Frozen")) { // subscriber is frozen.
				int subscriberID = Integer.valueOf(subscriberInformation[0].split(":")[1]);  // Get the subscriber's ID.
				String frozenAt = status[2]; // Grab the date in which the subscriber got frozen,
				if (clock.hasMonthPassed(frozenAt)) { // Check if the subscriber is frozen for a month or more. if so, "unfreeze" him.
					
					ConnectToDb.unfreezeSubscriber(EchoServer.taskSchedulerConnection, subscriberID);
					// Log that the subscriber got unfrozen both in CMD and log.
					EchoServer.outputInOutputStreamAndLog(clock.timeNow() + ": " + subscriber.toString() + " unfrozen since a month has passed.");
					
				}
			}
		}
	}
	
	
	public void freezeControllerDailyActivities() throws SQLException {
		unfreeze(); // Unfreeze first in case the subscriber has to be frozen on the same day he's being unfreezed.
		freeze();
	}
}
