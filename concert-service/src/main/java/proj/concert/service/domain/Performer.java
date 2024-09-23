package proj.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.common.types.Genre;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "PERFORMERS")
public class Performer {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    public long getId() { return id; }
    @Column(name = "NAME")
    private String name;
    public String getName() { return name; }
    @Column(name = "IMAGE_NAME")
    private String imageName;
    public String getImageName() { return imageName; }
    @Column(name = "GENRE")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    public Genre getGenre() { return genre; }
    @Column(name = "BLURB", length = 2000)
    private String blurb;
    public String getBlurb() { return blurb; }
    @ManyToMany(mappedBy = "performers", cascade = CascadeType.PERSIST) // performers field of Performer class, do not cascade
    private Set<Concert> concerts = new HashSet<>();
    public Set<Concert> getConcerts() { return concerts; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Performer performer = (Performer) o;
        return id == performer.id && Objects.equals(name, performer.name) && Objects.equals(imageName, performer.imageName) && genre == performer.genre && Objects.equals(blurb, performer.blurb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, imageName, genre, blurb);
    }
}
