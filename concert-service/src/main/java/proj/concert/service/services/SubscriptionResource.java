package proj.concert.service.services;

import org.jboss.logging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proj.concert.common.dto.ConcertInfoNotificationDTO;
import proj.concert.common.dto.ConcertInfoSubscriptionDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.util.SeatRemainingNotifier;
import proj.concert.service.util.SeatRemainingObserver;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

@Path("/concert-service/subscribe")
public class SubscriptionResource implements SeatRemainingObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionResource.class);

    private final Set<Subscription> subscribers = new HashSet<>();

    /**
     * Subscribes to be notified when the next message is received.
     */
    @POST
    @Path("/concertInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public void subscribeToInfo(@Suspended AsyncResponse sub, ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO, @CookieParam("auth") Cookie authCookie) {
        if (authCookie == null || ! AuthenticationManager.getInstance().isUserLoggedIn(authCookie.getValue())) {
            sub.resume(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();

        Concert concert = em.find(Concert.class, concertInfoSubscriptionDTO.getConcertId());

        if (concert == null || !concert.getDates().contains(concertInfoSubscriptionDTO.getDate()) || concertInfoSubscriptionDTO.getPercentageBooked() < 0 || concertInfoSubscriptionDTO.getPercentageBooked() > 100) {
            sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }

        long bookedSeats = (long) em.createQuery("SELECT COUNT(s.id) FROM Seat s WHERE s.booking != null and s.date = :date")
                .setParameter("date", concertInfoSubscriptionDTO.getDate())
                .getSingleResult();

        long totalSeats = (long) em.createQuery("SELECT COUNT(s.id) FROM Seat s WHERE s.date = :date")
                .setParameter("date", concertInfoSubscriptionDTO.getDate())
                .getSingleResult();

        double seatsRemaining = (double) bookedSeats / totalSeats;

        Subscription subscription = new Subscription(concertInfoSubscriptionDTO, sub);

        if (subscription.isSubscriptionFor(concert, concertInfoSubscriptionDTO.getDate(), seatsRemaining)) { // Check if the condition of the subscription is already filled
            sub.resume(Response.ok(new ConcertInfoNotificationDTO((int) totalSeats - (int) bookedSeats)).build());
            return;
        }

        subscribers.add(subscription);
        SeatRemainingNotifier.instance().registerObserver(this); // Register ourselves as an observer in case we aren't already
    }

    @Override
    public void onNewBooking(Concert concert, LocalDateTime date, double percentSeatsRemaining, long seatsRemaining) {
        for (Subscription subscription: subscribers) {
            if (subscription.isSubscriptionFor(concert, date, percentSeatsRemaining)) {
                ConcertInfoNotificationDTO concertInfoNotificationDTO = new ConcertInfoNotificationDTO((int) seatsRemaining);
                subscription.getSubscriber().resume(Response.ok(concertInfoNotificationDTO).build());
            }
        }
    }
}

class Subscription {
    private final ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO;
    private final AsyncResponse subscriber;

    public Subscription(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO, AsyncResponse subscriber) {
        this.concertInfoSubscriptionDTO = concertInfoSubscriptionDTO;
        this.subscriber = subscriber;
    }

    public ConcertInfoSubscriptionDTO getConcertInfoSubscriptionDTO() {
        return concertInfoSubscriptionDTO;
    }

    public AsyncResponse getSubscriber() {
        return subscriber;
    }

    public boolean isSubscriptionFor(Concert concert, LocalDateTime dateTime, double percentSeatsRemaining) {
        return (concertInfoSubscriptionDTO.getConcertId() == concert.getId()
                && concertInfoSubscriptionDTO.getDate().isEqual(dateTime)
                && concertInfoSubscriptionDTO.getPercentageBooked() < percentSeatsRemaining
                );
    }
}