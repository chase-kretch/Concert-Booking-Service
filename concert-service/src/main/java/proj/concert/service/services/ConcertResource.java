package proj.concert.service.services;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.print.attribute.standard.Media;
import javax.swing.text.html.parser.Entity;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.*;
import javax.ws.rs.core.*;


import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.service.domain.Concert;
import proj.concert.common.dto.ConcertDTO;
import proj.concert.service.mapper.DTOMapper;


    @Path("/concert-service/concerts")
public class ConcertResource {
    private EntityManager getEntityManager(){  // Gets an instance of entity manager for dealing with database info.
        return PersistenceManager.instance().createEntityManager();

    }

    @GET
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response retrieveAllCerts(@CookieParam("clientId") Cookie clientId) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            List<Concert> concerts = em.createQuery("select c from Concert c", Concert.class).getResultList(); // Gets concert from database
            if(concerts.isEmpty()) {
                throw new NotFoundException("No concerts"); // Throw an exception if no concerts are found.
            }
            List<ConcertDTO> concertDTOS = new ArrayList<>();
            for (Concert concert: concerts) {
                concertDTOS.add(DTOMapper.concertToConcertDTO(concert)); // Convert concerts to concertDTos
            }
            return Response.ok(concertDTOS).build(); // returns 200 and concert
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Close the entity manager to avoid issues.
            }
        }
    }

    @GET
    @Path("{id}")
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response retrieveConcert(@PathParam("id") long id) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            Concert concert =   em.find(Concert.class, id); // Gets the concert from the database
            if (concert == null) {
                throw new NotFoundException("No concert with id: " + id);
            }
            ConcertDTO concertDTO = DTOMapper.concertToConcertDTO(concert); // Maps the concert to concertDTO
            return Response.ok(concertDTO).build(); // returns 200 and concert
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @GET
    @Path("/summaries")
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response retrieveConcertSummaries() {
        EntityManager em = null;
        try {
            em = getEntityManager();
            List<Concert> concerts = em.createQuery("select c from Concert c", Concert.class).getResultList(); // Obtains the concerts from the database
            if(concerts.isEmpty()) {
                throw new NotFoundException("No concerts");
            }
            List<ConcertSummaryDTO> summaryDTOS = new ArrayList<>();
            for(Concert concert : concerts) {
                summaryDTOS.add(DTOMapper.concertToConcertSummaryDTO(concert)); // Maps concerts to ConcertSummaryDTO
            }
            return Response.ok(summaryDTOS).build(); // returns 200 and summaries
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }







    // TODO Implement this.

}
