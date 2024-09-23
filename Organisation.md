# Contributions and team organisation

We decided to primarily communicate on discord because this worked well for us in the previous labs. We decided that Eli would focus on the domain model, Chase would focus on the service implementation, and Jonas would focus on the authentication, as well as be a general helper when we needed to discuss ideas and get help.

### Eli Chandler (echa791)
* Discussed distribution of tasks with group, decided to primarily work on the domain model
* Investigated and created initial entities in domain model (Concert, Performer, Seat)
* Refined and expanded domain model and it's functionality
* Implemented Mapper for Performer
* Discuss with jonas about how to implement authentication and token management
* Worked with Chase for Seats and Bookings resources
* Implemented concert subscription

### Chase Kretschmar (cpho632)
* Discussed distribution of tasks with group, decided to primarily work on the middleware i.e. resources, DTO, mappers
* Added annotations to DTO classes
* Fully implemented ConcertResource and PerformerResource, worked with Eli for Seats and Bookings resources
* Implemented basic DTO mapper/s
* Implemented basic concurrency control
* Implemented AuthenticationManager

### Jonas Tang (jtan289)
* Discussed distribution of tasks with group, decided to primarily work on the authentication system
* Discussed with Eli about methods of storing the user tokens
* Implemented AuthenticationResource and assisted with AuthenticationManager
* Started resource for Subscription

# Domain model

Our domain model uses the following entities:
* **Concert** - A concert has many performers, and many seats.
* **Seat** - A seat has a price, label and one concert, and a booking that can be null, i.e. not booked
* **Performer** - A performer has many concerts
* **Booking** - a booking needs to be stored in the database. A booking has a concert and at least one seat, and a time it was booked.
* **User** - A user has a list of bookings, it's username and password is checked when logging in, and it's stored in the AuthenticationManager after logging in.

We tried to make the domain model representative of the real-world relationships.

We made sure to use cascading in cases where an child entity may not already have been persisted by a parent, or the child should be deleted if the parent is deleted,
for example if a User is deleted, all of their bookings should be deleted.

For lazy loading vs. eager fetching, we chose to use eager fetching for seats bookings and bookings seats. This is because checking if a seat is booked is done basically every time a seat is fetched, and for the booking, we most likeely always want to know what actual seats have been booked.

# Discussion points

1. How have you made an effort to improve the scalability of your web service?

In our domain model we made use of eager and lazy fetching when appropriate, for example eagerly fetching if a seat has been booked, since that is a very common operation. In addition, we used an event-based pattern for the subscription service, where the database is only checked when a new booking is created, instead of constantly, since we only check when the data has actually changed it massively minimises the amount of database queries required for the subscription.

2. What (implicit and explicit) uses of lazy loading and eager fetching are used within your web service. Why those uses are appropriate in the context of this web service?

See as above


3. How have you made an effort to remove the possibility of issues arising from concurrent access, such as double-bookings?

A booking and a seat both have a version field that is incremented when modified/accessed. In the case of a double booking, the user which commits last will cause a OptimisticLockException (HTTP 409 - Conflict) and could be routed accordingly. The commit is then rolled back.

4. How would you extend your web service to add support for the following new features?:

    * Support for different ticket prices per concert (currently ticket prices are identical for each concert)
   
    It would depend on how the prices worked, for example if there were 5 discrete price tiers, it would be good to have a Enum for the prices, and we could reference the ID in the other tables, however if the prices were all different for each concert, it would be better to just add a price field to the seat.

    * Support for multiple venues (currently all concerts are assumed to be at the same venue with an identical seat layout)

We would need to create a relationship between Concert and Seat, that way even if two concerts are at the same time, we can determine which concert a seat belongs to.

    * Support for "held" seats (where, after a user selects their seats, they are reserved for a period of time to allow the user time to pay. If the user cancels payment, or the time period elapses, the seats are automatically released, able to be booked again by other users).

Make a seperate "hold" request, that starts a thread that waits x seconds and then deletes the hold, shoud be displayed to users how long the hold lasts for. Then booking endpoint would need to check if seats are held by that user.