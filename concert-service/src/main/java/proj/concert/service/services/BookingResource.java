package proj.concert.service.services;


import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import proj.concert.common.dto.BookingDTO;
import proj.concert.common.dto.BookingRequestDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Seat;
import proj.concert.service.domain.User;
import proj.concert.service.domain.Booking;
import proj.concert.service.domain.exceptions.InvalidBookingDateException;
import proj.concert.service.mapper.DTOMapper;
import proj.concert.service.util.SeatRemainingNotifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@Path("/concert-service/bookings")
public class BookingResource {

    AuthenticationManager authService = AuthenticationManager.getInstance();

    private EntityManager getEntityManager() {
        return PersistenceManager.instance().createEntityManager();

    }

    @GET
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response getAllBookingsForUser(@CookieParam("auth") Cookie authCookie) {
        AuthenticationManager am = AuthenticationManager.getInstance();
        EntityManager em = getEntityManager();

        if (authCookie == null || ! am.isUserLoggedIn(authCookie.getValue())) { // Checks whether user is logged in
            return Response.status(Response.Status.UNAUTHORIZED).build(); // return 401 if user is not logged in
        }

        User user = authService.getUserFromToken(authCookie.getValue()); // gets User via authCookie

        List<Booking> bookings = em.createQuery("SELECT b from Booking b where b.user = :user", Booking.class) // Fetches bookings from the database by user logged in.
                .setParameter("user", user)
                .getResultList();

        List<BookingDTO> bookingDTOS = new ArrayList<>();
        for (Booking booking: bookings) { // Maps bookings to bookingDTOs
            bookingDTOS.add(DTOMapper.bookingToBookingDTO(booking));
        }

        return Response.ok().entity(bookingDTOS).build(); // return 200 and bookings
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getBookingForUserById(@CookieParam("auth") Cookie authCookie, @PathParam("id") long id) {
        AuthenticationManager am = AuthenticationManager.getInstance();
        EntityManager em = getEntityManager();

        if (authCookie == null || ! am.isUserLoggedIn(authCookie.getValue())) { // check whether user is logged in
            return Response.status(Response.Status.UNAUTHORIZED).build(); // return 401 if user is not logged in
        }

        Booking booking = em.createQuery("SELECT b FROM Booking b where b.id = :id", Booking.class)
                .setParameter("id", id)
                .getSingleResult();

        if (booking == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        User user = am.getUserFromToken(authCookie.getValue());

        if (! booking.getUser().equals(user)) {
            return Response.status(Response.Status.FORBIDDEN).build(); // return 403 if booking is not for logged-in user
        }

        return Response.ok(DTOMapper.bookingToBookingDTO(booking)).build(); // return 200 and booking
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createBooking(@CookieParam("auth") Cookie authCookie, BookingRequestDTO bookingRequest) {
        EntityManager em = getEntityManager();

        if (authCookie == null || !authService.isUserLoggedIn(authCookie.getValue())) { // Checks whether the user is logged in
            return Response.status(Response.Status.UNAUTHORIZED).build(); // return 401 if user is not logged in
        }

        User user = authService.getUserFromToken(authCookie.getValue()); // Fetches the user via cookie
        Concert concert = em.find(Concert.class, bookingRequest.getConcertId()); // Fetches a bookings respective concert from the database

        if (concert == null) {
            return Response.status(Response.Status.BAD_REQUEST).build(); // 400 error if concert does not exist
        }

        Booking booking;
        try {
            booking = new Booking(user, concert, bookingRequest.getDate());
        } catch (InvalidBookingDateException e) {
            return Response.status(Response.Status.BAD_REQUEST).build(); // 400 error if concert is not showing on that date.
        }

        for (String seatLabel : bookingRequest.getSeatLabels()) {
            Seat seat = em.createQuery("SELECT s from Seat s where s.date = :date and s.label = :label", Seat.class) // Get the seat from the database
                    .setParameter("date", bookingRequest.getDate())
                    .setParameter("label", seatLabel)
                    .getSingleResult();

            if (seat == null) {
                return Response.status(Response.Status.BAD_REQUEST).build(); // Seat does not exist
            }

            if (seat.isBooked()) {
                return Response.status(Response.Status.FORBIDDEN).build(); // All the seats are required to be available
            }

            if (! booking.addSeat(seat) ) {
                return Response.status(Response.Status.BAD_REQUEST).build(); // Concert or date is wrong
            }
        }

        user.addBooking(booking);

        try {
            em.getTransaction().begin();
            em.persist(booking);
            em.getTransaction().commit();

            SeatRemainingNotifier.instance().notifyNewBooking(booking); // Notify that a booking has been made.
            // We only notify when new bookings are made to prevent unnecessary database lookups

            return Response.created(URI.create("/concert-service/bookings/" + booking.getId())).build(); // return 201
        } catch (OptimisticLockException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return Response.status(Response.Status.CONFLICT).entity("Another user booked this seat/s").build();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occured while creating booking").build();

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
