package proj.concert.service.domain;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.awt.print.Book;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "VERSION")
    private String version;
    @OneToMany(mappedBy = "user", orphanRemoval = true) // user field of Booking class
    @Cascade(org.hibernate.annotations.CascadeType.ALL) // If a user is deleted, we should delete their bookings
    private Set<Booking> bookings = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public String getUsername() {
        return username;
    }

    public boolean addBooking(Booking booking) {
        if (booking.getUser() != this) {
            return false;
        }

        bookings.add(booking);
        return true;
    }
    public long getId() {
        return id;
    }
}
