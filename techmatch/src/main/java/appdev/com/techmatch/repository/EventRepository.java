package appdev.com.techmatch.repository;

import appdev.com.techmatch.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
import java.time.LocalDate;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByTopics_Name(String topic);

    @Query("SELECT e FROM Event e WHERE (:date IS NULL OR e.startDate <= :date AND e.endDate >= :date) AND (:type IS NULL OR e.eventType = :type)")
    List<Event> findByDateAndType(@Param("date") LocalDate date, @Param("type") String type);

    List<Event> findByStartDate(LocalDate date);

    List<Event> findByEventType(String type);
}

