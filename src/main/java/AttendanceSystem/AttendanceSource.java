package AttendanceSystem;

import AttendanceSystem.Model.ResultResponse;
import AttendanceSystem.Model.TransactionLive;
import AttendanceSystem.Service.TransactionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/attendance")
public class AttendanceSource {

    @Inject
    TransactionService transactionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTransactions() {
        try {
            List<TransactionLive> transactions = transactionService.getAllTransactions();

            if (transactions == null || transactions.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResultResponse.error("No transactions found"))
                        .build();
            }

            return Response.ok(ResultResponse.success(transactions)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Failed to fetch transactions: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransaction(@PathParam("id") String id) {
        try {
            TransactionLive transaction = transactionService.getTransactionById(id);

            if (transaction == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResultResponse.error("Transaction not found: " + id))
                        .build();
            }

            return Response.ok(ResultResponse.success(transaction)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error fetching transaction: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Attendance System is running!";
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "pong";
    }
}