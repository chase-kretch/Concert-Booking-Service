package proj.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

@Entity
@Table(name = "CONCERTS")
public class Concert {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    long id;
    public long getId() { return id; }


    @Column(name = "TITLE")
    private String title;
    public String getTitle() { return title; }


    @Column(name = "IMAGE_NAME")
    private String imageName;
    public String getImageName() { return imageName; }


    @Column(name = "BLURB", length = 2000)
    private String blurb;
    public String getBlurb() { return blurb; }


    @ElementCollection
    @CollectionTable(
        name = "CONCERT_DATES",
        joinColumns = { @JoinColumn(name = "CONCERT_ID") }
    )
    @Column(name = "DATE")
    private Set<LocalDateTime> dates = new HashSet<>();
    public Set<LocalDateTime> getDates() { return dates;}


    @NotEmpty(message = "At least one performer is required.") // Ensure Concert cannot be persisted if it has no performers
    @ManyToMany
    @JoinTable(
        name = "CONCERT_PERFORMER",
        joinColumns = { @JoinColumn(name = "CONCERT_ID") },
        inverseJoinColumns = { @JoinColumn(name = "PERFORMER_ID") }
    )
    private Set<Performer> performers = new HashSet<>();
    public Set<Performer> getPerformers() { return performers; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concert concert = (Concert) o;
        return id == concert.id && Objects.equals(title, concert.title) && Objects.equals(imageName, concert.imageName) && Objects.equals(blurb, concert.blurb) && Objects.equals(dates, concert.dates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, imageName, blurb, dates);
    }
}
