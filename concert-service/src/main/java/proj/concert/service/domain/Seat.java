package proj.concert.service.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "SEATS")
public class Seat {
	@Id
	@GeneratedValue
	private long id;

	@Version
	private long version;

	private String label;
	private BigDecimal price;

	@ManyToOne(fetch = FetchType.EAGER) // If we are fetching the seat, we usually want to know if it's booked
	@JoinColumn(name = "BOOKING_ID", nullable = true) // Nullable because a seat can have no booking
	private Booking booking;

	private LocalDateTime date;

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal cost) {
		this.label = label;
		this.price = cost;
		this.date = date;
		// Don't need isBooked, we can check this dynamically
	}

	public Seat() {}

	public Boolean isBooked() {
		return booking != null; // If booking is null return False, otherwise return True
	}

	public boolean addBooking(Booking booking) {
		if (! booking.getSeats().contains(this)) {
			return false; // If the booking does not already contain this seat, don't set it
		}

		this.booking = booking;
		return true;
	}


	//<editor-fold desc="Dumb getters">
	public long getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public Booking getBooking() {
		return booking;
	}
	//</editor-fold>
}
