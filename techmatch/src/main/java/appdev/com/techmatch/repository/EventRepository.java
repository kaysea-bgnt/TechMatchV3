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

    @Query("SELECT e FROM Event e WHERE (:date IS NULL OR e.startDate <= :date AND e.endDate >= :date) AND (:type IS NULL OR e.eventType = :type)")
    List<Event> findByDateAndType(@Param("date") LocalDate date, @Param("type") String type);

    List<Event> findByTopics_NameIn(List<String> topics);

   @Query("SELECT e FROM Event e JOIN e.topics t WHERE t.name IN :topics AND e.eventType = :eventType")
    List<Event> findByTopicsNameInAndEventType(@Param("topics") List<String> topics, @Param("eventType") String eventType);


    List<Event> findByStartDate(LocalDate date);

    @Query("SELECT new appdev.com.techmatch.dto.EventAttendeeDTO(e.eventID, e.eventName, u.userID, u.username, u.email) " +
    "FROM Event e JOIN e.attendees u " +
    "WHERE e.eventID = :eventID")
    List<EventAttendeeDTO> getEventAttendeesWithDetails(@Param("eventID") String eventID);

    List<Event> findByUserUserID(String userID);

}

