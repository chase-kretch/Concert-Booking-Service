package proj.concert.service.services;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.print.attribute.standard.Media;
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


import org.dom4j.Entity;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.common.dto.ConcertDTO;
import proj.concert.service.domain.Performer;
import proj.concert.service.mapper.DTOMapper;

@Path("/concert-service/performers")
public class PerformerResource {

    private EntityManager getEntityManager() {
        return PersistenceManager.instance().createEntityManager();
    }

    @GET
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response retrieveAllPerformers() {
        EntityManager em = null;
        try {
            em = getEntityManager();
            List<Performer> performers = em.createQuery("select p from Performer p", Performer.class).getResultList(); // Gets performers from database
            List<PerformerDTO> performerDTOs = new ArrayList<>();
            for (Performer performer : performers) {
                performerDTOs.add(DTOMapper.performerToPerformerDTO(performer)); // Maps performers to performerDTOs
            }
            if (performers.isEmpty()) {
                throw new NotFoundException("No performers");
            }
            return Response.ok(performerDTOs).build(); // returns performers and 200
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

    }

    @GET
    @Path("{id}")
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response retrievePerformer(@PathParam("id") long id) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            Performer performer = em.find(Performer.class, id); // Gets performer from database
            if (performer == null) {
                throw new NotFoundException("No performer with id: " + id);
            }
            PerformerDTO performerDTO = DTOMapper.performerToPerformerDTO(performer); // Maps performer to performerDTO

            return Response.ok(performerDTO).build(); // returns 200 and performer.
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}