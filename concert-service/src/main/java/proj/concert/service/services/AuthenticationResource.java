package proj.concert.service.services;

import proj.concert.common.dto.UserDTO;
import proj.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.UUID;

@Path("/concert-service/login")
public class AuthenticationResource {
    //private HashMap<String, User> AuthenticationTokens = new HashMap<>();
    private AuthenticationManager getAuthService() {
        return AuthenticationManager.getInstance();
    }

    private EntityManager getEntityManager() {
        return PersistenceManager.instance().createEntityManager();
    }

    @POST
  //  @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserDTO cre) {
        EntityManager em = getEntityManager();
        String username = cre.getUsername();
        String password = cre.getPassword();

        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.password = :password", User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

            if (user != null) {
                NewCookie ResCookie = generateToken(user);

                return Response.ok().cookie(ResCookie).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (NoResultException nre) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } finally {
            em.close();
        }
    }


    private NewCookie generateToken(User user) {
        AuthenticationManager authService = getAuthService();
        String token =UUID.randomUUID().toString();
        authService.addAuthToken(token, user);
        NewCookie authCookie = new NewCookie("auth", token);
        return authCookie;
    }

}


