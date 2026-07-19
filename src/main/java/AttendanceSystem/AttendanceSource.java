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
    public Response hello() {
        try { 
            List<TransactionLive> transactions = transactionService.getAllTransactions(); 
            ResultResponse<List<TransactionLive>> response = ResultResponse.success(transactions); 
            return Response.ok(response).build(); 
        } catch (Exception e) { 
            ResultResponse<String> error = ResultResponse.error("Failed to fetch transactions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTransaction(@PathParam("id") String id) {  
    try {
        TransactionLive transaction = transactionService.getTransactionById(id); 
            if (transaction == null) {
                ResultResponse<String> error = ResultResponse.error("Transaction not found: " + id);
                return Response.status(Response.Status.NOT_FOUND).entity(error).build();
            }
            ResultResponse<TransactionLive> response = ResultResponse.success(transaction);
            return Response.ok(response).build();
        } catch (Exception e) {
            ResultResponse<String> error = ResultResponse.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error)
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
