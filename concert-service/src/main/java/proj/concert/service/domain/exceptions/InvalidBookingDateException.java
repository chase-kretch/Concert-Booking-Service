package proj.concert.service.domain.exceptions;

public class InvalidBookingDateException extends Exception {
    public InvalidBookingDateException(String message) {
        super(message);
    }
}