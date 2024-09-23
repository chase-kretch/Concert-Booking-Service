package proj.concert.service.services;

import proj.concert.common.dto.SeatDTO;
import proj.concert.common.types.BookingStatus;
import proj.concert.service.domain.Seat;
import proj.concert.service.mapper.DTOMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Path("/concert-service/seats")
public class SeatResource {

    private AuthenticationManager getAuthService() {
        return AuthenticationManager.getInstance();
    }

    private EntityManager getEntityManager() {
        return PersistenceManager.instance().createEntityManager();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{dateTime}")
    public Response getAllSeats(@CookieParam("auth") Cookie authCookie, @PathParam("dateTime") String dateTime, @QueryParam("status") BookingStatus status) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime time = LocalDateTime.parse(dateTime, formatter);

        TypedQuery<Seat> query;
        EntityManager em = getEntityManager();
        if(status == BookingStatus.Any) { // Checks for which status in the url and database query searches for the respective one.
            //query = em.createQuery("SELECT s FROM Seat s INNER JOIN s.concert c WHERE :date IN c.dates", Seat.class);
            query = em.createQuery("SELECT s FROM Seat s WHERE s.date = :date", Seat.class);
        } else if (status == BookingStatus.Booked) {
            query = em.createQuery("SELECT s FROM Seat s WHERE s.date = :date and s.booking != null", Seat.class);
        } else  { // Unbooked
            query = em.createQuery("SELECT s FROM Seat s WHERE s.date = :date and s.booking = null", Seat.class);
        }

        List<Seat> seats = query.setParameter("date", time).getResultList();

        List<SeatDTO> seatDTOS = new ArrayList<>();

        for (Seat seat: seats) { // Maps seats to seatDTOs
            seatDTOS.add(DTOMapper.seatToSeatDTO(seat));
        }

        return Response.ok(seatDTOS).build(); // return seats and 200
    }
}
