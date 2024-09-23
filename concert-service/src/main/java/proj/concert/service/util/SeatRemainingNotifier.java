package proj.concert.service.util;

import proj.concert.service.domain.Booking;
import proj.concert.service.domain.Concert;
import proj.concert.service.services.PersistenceManager;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class SeatRemainingNotifier {
    private static SeatRemainingNotifier instance = null;
    private final Set<SeatRemainingObserver> observers = new HashSet<>();


    private SeatRemainingNotifier() {};

    public static SeatRemainingNotifier instance() {
        if (instance == null) {
            instance = new SeatRemainingNotifier();
        }
        return instance;
    }

    public void registerObserver(SeatRemainingObserver observer) {
        observers.add(observer);
    }

    public void notifyNewBooking(Booking booking) {
        notifyObserversSeatsRemaining(booking.getConcert(), booking.getDate());
    }

   public void notifyObserversSeatsRemaining(Concert concert, LocalDateTime date) {
       EntityManager em = PersistenceManager.instance().createEntityManager();

       long bookedSeats = (long) em.createQuery("SELECT COUNT(s.id) FROM Seat s WHERE s.booking != null and s.date = :date")
               .setParameter("date", date)
               .getSingleResult();

       long totalSeats = (long) em.createQuery("SELECT COUNT(s.id) FROM Seat s WHERE s.date = :date")
               .setParameter("date", date)
               .getSingleResult();

       double seatsRemaining = (double) bookedSeats / totalSeats * 100;

       for (SeatRemainingObserver observer: observers) {
           observer.onNewBooking(concert, date, seatsRemaining, totalSeats - bookedSeats);
       }
   }
}
