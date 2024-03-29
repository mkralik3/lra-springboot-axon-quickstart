package org.mkralik.lra.quickstart.hotel;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mkralik.lra.quickstart.hotel.command.api.command.CompensateHotelCmd;
import org.mkralik.lra.quickstart.hotel.command.api.command.CompleteHotelCmd;
import org.mkralik.lra.quickstart.hotel.command.api.command.CreateHotelCmd;
import org.mkralik.lra.quickstart.hotel.model.Booking;
import org.mkralik.lra.quickstart.hotel.query.AllBookingSummaryQuery;
import org.mkralik.lra.quickstart.hotel.query.BookingSummaryQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Service
@Path("/")
@LRA(LRA.Type.SUPPORTS)
@Slf4j
public class HotelEndpoints {
    @Inject
    private CommandGateway cmdGateway;

    @Inject
    private QueryGateway queryGateway;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(value = LRA.Type.REQUIRED, end = false)
    public Booking bookRoom(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId,
                            @QueryParam("hotelName") @DefaultValue("Default") String hotelName) throws InterruptedException {
        cmdGateway.sendAndWait(new CreateHotelCmd(lraId, hotelName, "Hotel"));
        Thread.sleep(500);
        return getBookingFromQueryBus(lraId);
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) throws NotFoundException, InterruptedException, JsonProcessingException {
        log.info("Complete work in Axon hotel service");
        cmdGateway.sendAndWait(new CompleteHotelCmd(lraId));
        Thread.sleep(500);
        return Response.ok(getBookingFromQueryBus(lraId).toJson()).build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) throws NotFoundException, InterruptedException, JsonProcessingException {
        log.info("Compensate work in Axon hotel service");
        cmdGateway.sendAndWait(new CompensateHotelCmd(lraId));
        Thread.sleep(500);
        return Response.ok(getBookingFromQueryBus(lraId).toJson()).build();
    }

    @GET
    @Path("{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return getBookingFromQueryBus(bookingId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Booking> getAll() {
        return queryGateway.query(new AllBookingSummaryQuery(), ResponseTypes.multipleInstancesOf(Booking.class)).join();
    }

    private Booking getBookingFromQueryBus(String lraId) {
        Booking join = queryGateway.query(new BookingSummaryQuery(lraId),
                ResponseTypes.instanceOf(Booking.class))
                .join();
        log.debug("returned class is {}", join);
        return join;
    }
}
