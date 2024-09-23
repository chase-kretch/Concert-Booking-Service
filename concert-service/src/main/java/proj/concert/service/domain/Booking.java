package proj.concert.service.domain;

import proj.concert.service.domain.exceptions.InvalidBookingDateException;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOOKINGS")
public class Booking {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @Version
    private long version;

    @Column(name = "DATE")
    private LocalDateTime date;

    @ManyToOne()
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne()
    @JoinColumn(name = "CONCERT_ID")
    private Concert concert;

    @OneToMany(orphanRemoval = false, fetch = FetchType.EAGER) // Just because a booking is deleted, do not delete the seat
    @NotEmpty(message = "Booking must contain at least 1 seat.")
    private List<Seat> seats = new ArrayList<>();

    public Booking() {}

    public Booking(User user, Concert concert, LocalDateTime time) throws InvalidBookingDateException { // Create a new booking for a user
        if (! concert.getDates().contains(time)) { // Check if the reference concert is showing at the time the booking is for
            throw new InvalidBookingDateException("Time " + time + " is not a valid booking time for concert " + concert);
        }

        this.user = user;
        this.date = time;
        this.concert = concert;
    }

    public Boolean addSeat(Seat seat) {
        if (! seat.getDate().isEqual(date)) {
            return false; // Seats must match the bookings date
        }

        if (seat.isBooked()) {
            return false; // Can't book a booked seat
        }

        seats.add(seat);
        seat.addBooking(this);
        return true;
    }

    //<editor-fold desc="Dumb getters">
    public long getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public Concert getConcert() {
        return concert;
    }

    public List<Seat> getSeats() {
        return seats;
    }
    //</editor-fold>
}
