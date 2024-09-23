package proj.concert.service.mapper;

import proj.concert.common.dto.*;
import proj.concert.service.domain.Booking;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;
import proj.concert.service.domain.Seat;

import java.util.ArrayList;
import java.util.List;

public class DTOMapper {
    public static ConcertDTO concertToConcertDTO(Concert concert) { // Maps concert to ConcertDTO.
        ConcertDTO concertDTO = new ConcertDTO(concert.getId(), concert.getTitle(), concert.getImageName(), concert.getBlurb());
        concertDTO.getDates().addAll(concert.getDates()); // create a new concertDTO using concert attributes
        for (Performer performer: concert.getPerformers()) {
            concertDTO.getPerformers().add(performerToPerformerDTO(performer)); // Iterates through a concerts performers and also converts performers to performerDTO
        }
        return concertDTO;
    }
    public static PerformerDTO performerToPerformerDTO(Performer performer) { // Maps performer to performerDTO
        return new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb());
    }

    public static ConcertSummaryDTO concertToConcertSummaryDTO(Concert concert) { // Maps concert to concertSummaryDTO
        return new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());
    }

    public static SeatDTO seatToSeatDTO(Seat seat) { // Maps seat to Seat DTO
        return new SeatDTO(seat.getLabel(), seat.getPrice());
    }

    public static BookingDTO bookingToBookingDTO(Booking booking) { // Maps booking to bookingDTO
        List<SeatDTO> seatDTOS = new ArrayList<>();
        for (Seat seat: booking.getSeats()) { // Also converts each bookings seat to seatDTO
            seatDTOS.add(seatToSeatDTO(seat));
        }

        return new BookingDTO(
                booking.getConcert().getId(),
                booking.getDate(),
                seatDTOS
        );
    }
}
