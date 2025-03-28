package logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.ConnectToDb;
import gui.baseController.BaseController;
import server.EchoServer;

/**
 * The {@code ReserveRequestDailyTasksController} class handles daily tasks related to
 * reserved books in the library system. These tasks include:
 * <ul>
 *     <li>Deleting expired reservations where the time left to retrieve has passed.</li>
 *     <li>Notifying subscribers via an output/log message that a reserved book is available 
 *         and needs to be picked up within a certain time.</li>
 * </ul>
 * 
 * <p>It relies on the {@link EchoServer#taskSchedulerConnection} for database operations
 * and the {@link ServerTimeDiffController} to compute date/time differences.</p>
 * 
 * <p>Typical usage:
 * <pre>{@code
 * ReserveRequestDailyTasksController controller = new ReserveRequestDailyTasksController();
 * controller.reserveRequestsDailyActivity(); // performs the daily tasks
 * }</pre>
 * </p>
 * 
 * <p>Extends {@link BaseController} to utilize shared methods (e.g. parseBorrowedBook if needed).</p>
 */
public class ReserveRequestDailyTasksController extends BaseController {

    /**
     * A reference to the server-side time difference controller, used for date/time operations.
     */
    private final ServerTimeDiffController clock = EchoServer.clock;

    /**
     * Runs the daily reservation tasks:
     * <ol>
     *     <li>{@link #deleteOldRequests()}: remove any reservations past their retrieval date.</li>
     *     <li>{@link #sendMailToSubscriberThatNeedsToRetrieveBookFromTheLibrary()}: notify subscribers 
     *         about books that are available for pickup.</li>
     * </ol>
     * @throws SQLException 
     */
    public void reserveRequestsDailyActivity() throws SQLException {
        deleteRequests();
        sendMailToSubscriberThatNeedsToRetrieveBookFromTheLibrary();
    }

    /**
     * Deletes reservations that have expired (i.e., those past their "time_left_to_retrieve").
     * <p>
     * Specifically:
     * <ul>
     *   <li>Fetches all reserved books from {@code reserved_books} table.</li>
     *   <li>For each reservation, checks how many days remain until the "time_left_to_retrieve".</li>
     *   <li>If the time difference is 0 or negative, the reservation is removed, and 
     *       {@code reservedCopiesNum} is decremented for that book's ISBN.</li>
     * </ul>
     * </p>
     * @throws SQLException 
     */
    public void deleteRequests() throws SQLException{
    	Connection conn =EchoServer.taskSchedulerConnection;
        ConnectToDb.deleterequests(conn);
    }
    /**
     * Notifies subscribers (via log/output) that they have reserved books ready to be picked up.
     * <p>
     * The process:
     * <ul>
     *   <li>Fetches all "book is available" reservations from the database.</li>
     *   <li>Groups these reservations by subscriber ID.</li>
     *   <li>Builds a consolidated message listing each book and how many days remain to retrieve it.</li>
     *   <li>Logs/prints the message for each subscriber.</li>
     * </ul>
     * </p>
     * @throws SQLException 
     */
    public void sendMailToSubscriberThatNeedsToRetrieveBookFromTheLibrary() throws SQLException {
    	// 1. Fetch all "available" reservations
        List<String> reservationRequests = ConnectToDb.fetchAllReservedBooksWhereBookIsAvailable(EchoServer.taskSchedulerConnection);

        if (reservationRequests == null || reservationRequests.isEmpty()) {
            return;
        }

        // subscriberId -> list of reservation data
        Map<Integer, List<String>> subscriberReservationsMap = new HashMap<>();

        for (String request : reservationRequests) {
            // Format: reserve_id, subscriber_id, name(bookName), reserve_time, time_left_to_retrieve, ISBN
            String[] fields = request.split(",");
            int subscriberId = Integer.parseInt(fields[1]);
            subscriberReservationsMap.putIfAbsent(subscriberId, new ArrayList<>());
            subscriberReservationsMap.get(subscriberId).add(request);
        }

        // Build & send messages for each subscriber
        for (Map.Entry<Integer, List<String>> entry : subscriberReservationsMap.entrySet()) {
            int subscriberId = entry.getKey();
            List<String> requests = entry.getValue();

            // Fetch subscriber data
            String subscriberData = ConnectToDb.fetchSubscriberData(EchoServer.taskSchedulerConnection, String.valueOf(subscriberId));
            String subscriberName = parseSubscriberName(subscriberData);
            if ("No subscriber found".equals(subscriberName)) {
                continue;
            }

            // Group reservations by book name
            Map<String, List<Integer>> bookReservationsMap = new HashMap<>();

            for (String req : requests) {
                String[] fields = req.split(",");
                String bookName = fields[2];
                String timeLeftToRetrieve = fields[4];

                int daysLeftForRetrieval = clock.timeDateDifferenceBetweenTwoDates(clock.timeNow(), timeLeftToRetrieve);

                bookReservationsMap.putIfAbsent(bookName, new ArrayList<>());
                bookReservationsMap.get(bookName).add(daysLeftForRetrieval);
            }

            // Construct the consolidated message for this subscriber
            StringBuilder message = new StringBuilder();
            message.append("Dear ").append(subscriberName).append(",\n\n");
            message.append("You have the following reservations ready for retrieval:\n");

            for (Map.Entry<String, List<Integer>> bookEntry : bookReservationsMap.entrySet()) {
                String bookName = bookEntry.getKey();
                List<Integer> daysLeftList = bookEntry.getValue();

                message.append("- ").append(bookName).append(": ");
                for (int i = 0; i < daysLeftList.size(); i++) {
                    message.append(daysLeftList.get(i)).append(" day(s) left");
                    if (i < daysLeftList.size() - 1) {
                        message.append(", ");
                    }
                }
                message.append("\n");
            }

            message.append("\nPlease retrieve your books before the specified time to avoid cancellation.\n");

            // Log the message
            EchoServer.outputInOutputStreamAndLog(message.toString());
        }
    }

    /**
     * Extracts the subscriber's name from a comma-separated string returned by
     * {@code ConnectToDb.fetchSubscriberData()}.
     *
     * <p>
     * Example input format: 
     * {@code subscriber_id:123, subscriber_name:John Doe, ..., status:Not Frozen}
     * </p>
     *
     * @param subscriberData The raw subscriber data string to parse.
     * @return The subscriber's name, or "No subscriber found" if not present or if an error occurred.
     */
    private static String parseSubscriberName(String subscriberData) {
        // Check for errors or missing subscriber
        if (subscriberData.startsWith("No subscriber found") || subscriberData.startsWith("Error")) {
            return "No subscriber found";
        }

        // Attempt to extract the "subscriber_name" field
        String[] fields = subscriberData.split(", ");
        for (String field : fields) {
            if (field.startsWith("subscriber_name:")) {
                return field.split(":")[1]; // Return the actual name
            }
        }

        return "No subscriber found";
    }
}
