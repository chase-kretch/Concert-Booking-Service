package proj.concert.service.util;

import proj.concert.service.domain.Concert;

import java.time.LocalDateTime;

public interface SeatRemainingObserver {
    public void onNewBooking(Concert concert, LocalDateTime date, double percentSeatsRemaining, long seatsRemaining);
}
