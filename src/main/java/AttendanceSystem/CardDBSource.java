package AttendanceSystem;

import AttendanceSystem.Model.CardDB;
import AttendanceSystem.Model.ResultResponse;
import AttendanceSystem.Service.CardService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.FormParam; // ← TAMBAHKAN INI!

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.PartType;
 

@Path("/cards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CardDBSource {

    @Inject
    CardService cardService;

    @GET
    public Response getAllCards(@QueryParam("id") String id) {
        try {
            List<CardDB> cards = cardService.getAllCard(id);

            if (cards.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResultResponse.error("No cards found"))
                        .build();
            }

            return Response.ok(ResultResponse.success(cards)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getCardById(@PathParam("id") String id) {
        try {
            CardDB card = cardService.getCardById(id);

            if (card == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResultResponse.error("Card not found: " + id))
                        .build();
            }

            return Response.ok(ResultResponse.success(card)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/search")
    public Response searchCards(@QueryParam("cardNo") String cardNo) {
        try {
            List<CardDB> cards = cardService.searchCardsByCardNo(cardNo);

            if (cards.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResultResponse.error("No cards found"))
                        .build();
            }

            return Response.ok(ResultResponse.success(cards)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    public Response insertCard(CardDB card) {
        try {
            boolean success = cardService.insertCard(card);

            if (success) {
                return Response.status(Response.Status.CREATED)
                        .entity(ResultResponse.success("Card created successfully"))
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ResultResponse.error("Failed to create card"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateCard(@PathParam("id") String id, CardDB card) {
        try {
            card.setID(id);
            boolean success = cardService.updateCard(card);

            if (success) {
                return Response.ok(ResultResponse.success("Card updated successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResultResponse.error("Card not found: " + id))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCard(@PathParam("id") String id) {
        try {
            boolean success = cardService.deleteCard(id);

            if (success) {
                return Response.ok(ResultResponse.success("Card deleted successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResultResponse.error("Card not found: " + id))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{id}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImage(
            @PathParam("id") String id,
            @MultipartForm FileUploadForm form) {
        try {
            if (form == null || form.fileData == null || form.fileData.length == 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ResultResponse.error("File is required"))
                        .build();
            }

            boolean success = cardService.uploadImage(id, form.fileData);

            if (success) {
                return Response.ok(ResultResponse.success("Image uploaded successfully: " + form.fileName))
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ResultResponse.error("Failed to upload image"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ResultResponse.error("Error: " + e.getMessage()))
                    .build();
        }
    }

    // Form class for multipart upload
    public static class FileUploadForm {
        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public byte[] fileData;

        @FormParam("fileName")
        public String fileName;

        @FormParam("fileType")
        public String fileType;
    }

    @GET
    @Path("/{id}/image")
    @Produces({ "image/jpeg", "image/png", "image/gif", "image/webp" })
    public Response getImage(@PathParam("id") String id) {
        try {
            byte[] imageData = cardService.getImage(id);

            if (imageData == null || imageData.length == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Image not found")
                        .build();
            }

            String mediaType = detectImageType(imageData);
            return Response.ok(imageData, mediaType).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }

    private String detectImageType(byte[] imageData) {
        if (imageData.length < 4)
            return "application/octet-stream";

        if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8 && imageData[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50 &&
                imageData[2] == (byte) 0x4E && imageData[3] == (byte) 0x47) {
            return "image/png";
        }
        if (imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49 && imageData[2] == (byte) 0x46) {
            return "image/gif";
        }

        return "application/octet-stream";
    }
}