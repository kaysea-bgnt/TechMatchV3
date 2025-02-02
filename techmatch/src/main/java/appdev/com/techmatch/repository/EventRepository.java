package appdev.com.techmatch.repository;

import appdev.com.techmatch.dto.EventAttendeeDTO; // Ensure this class exists in the specified package
import appdev.com.techmatch.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
import java.time.LocalDate;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByEventType(String type);
    List<Event> findByTopics_Name(String topic);
    List<Event> findByTopics_NameIn(List<String> topics);
    List<Event> findByStartDate(LocalDate date);
    List<Event> findByUserUserID(String userID);

   @Query("SELECT e FROM Event e JOIN e.topics t WHERE t.name IN :topics AND e.eventType = :eventType")
    List<Event> findByTopicsNameInAndEventType(@Param("topics") List<String> topics, @Param("eventType") String eventType);

    @Query("SELECT new appdev.com.techmatch.dto.EventAttendeeDTO(e.eventID, e.eventName, u.userID, u.username, u.email) " +
    "FROM Event e JOIN e.attendees u " +
    "WHERE e.eventID = :eventID")
    List<EventAttendeeDTO> getEventAttendeesWithDetails(@Param("eventID") String eventID);


    /* 
    @Query("SELECT e FROM Event e WHERE LOWER(e.eventName) LIKE LOWER(concat('%', :searchQuery, '%')) OR LOWER(e.organization) LIKE LOWER(concat('%', :searchQuery, '%'))")
    List<Event> findByEventNameContainingIgnoreCaseOrOrganizationContainingIgnoreCase(@Param("searchQuery") String searchQuery1, @Param("searchQuery") String searchQuery2); */

    //Existing queries
    @Query("SELECT e FROM Event e WHERE LOWER(e.eventName) LIKE LOWER(concat('%', :searchQuery, '%')) OR LOWER(e.organization) LIKE LOWER(concat('%', :searchQuery, '%')) OR LOWER(e.location) LIKE LOWER(concat('%', :searchQuery, '%'))")
    List<Event> findByEventNameContainingIgnoreCaseOrOrganizationContainingIgnoreCaseOrLocationContainingIgnoreCase(@Param("searchQuery") String searchQuery1, @Param("searchQuery") String searchQuery2, @Param("searchQuery") String searchQuery3);

    // CALENDAR 
    // DATE ONLY
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :startDate AND :endDate ORDER BY e.startDate ASC, e.eventName ASC")
    List<Event> findByStartDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Event e WHERE e.startDate <= :endDate AND e.endDate >= :startDate")
    List<Event> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    // TOPIC, TYPE, AND RANGE DATE FILTER
    @Query("SELECT e FROM Event e JOIN e.topics t WHERE t.name IN :topics AND e.startDate BETWEEN :startDate AND :endDate AND e.eventType = :eventType")
    List<Event> findByTopicsNameInAndStartDateBetweenAndEventType(
        @Param("topics") List<String> topics,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("eventType") String eventType
        );

    // TOPIC AND RANGE DATE FILTER
     @Query("SELECT e FROM Event e JOIN e.topics t WHERE t.name IN :topics AND e.startDate BETWEEN :startDate AND :endDate ORDER BY e.startDate ASC")
     List<Event> findByTopicsNameInAndStartDateBetween(
         @Param("topics") List<String> topics,
         @Param("startDate") LocalDate startDate,
         @Param("endDate") LocalDate endDate
     );

    //TYPE AND SINGLE DATE
    @Query("SELECT e FROM Event e WHERE e.startDate = :date AND e.eventType = :type")
    List<Event> findByDateAndType(@Param("date") LocalDate date, @Param("type") String type);

    // RANGE DATE AND TYPE FILTER
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :startDate AND :endDate AND e.eventType = :eventType")
    List<Event> findByStartDateBetweenAndEventType(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("eventType") String eventType
        );
    


    @Query("SELECT e FROM Event e JOIN e.topics t WHERE t.name IN :topics AND e.startDate = :date AND e.eventType = :eventType")
    List<Event> findByTopicsNameInAndStartDateAndEventType(@Param("topics") List<String> topics, @Param("date") LocalDate date, @Param("eventType") String eventType);

    @Query("SELECT e FROM Event e JOIN e.topics t WHERE t.name IN :topics AND e.startDate = :date")
    List<Event> findByTopicsNameInAndStartDate(@Param("topics") List<String> topics, @Param("date") LocalDate date);

    @Query("SELECT e FROM Event e WHERE e.endDate >= :today")
    List<Event> findByEndDateAfterOrEndDateEquals(@Param("today") LocalDate today);

    @Query("SELECT MAX(e.eventID) FROM Event e")
    String findMaxEventID();

    //UPCOMING AND PAST EVENTS
    //Using the name `attendees` for the collection on the query.
    @Query("SELECT e FROM Event e JOIN e.attendees ru WHERE ru.userID = :userId")
    List<Event> findRegisteredEventsByUserId(@Param("userId") String userId);
}
