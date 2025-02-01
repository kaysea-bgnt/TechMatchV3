package appdev.com.techmatch.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer topicID;

    private String name;

    @ManyToMany(mappedBy = "topics")
    private Set<Event> events = new HashSet<>();

    // Getters and setters
    public Integer getTopicID() { return topicID; }
    public void setTopicID(Integer topicID) { this.topicID = topicID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Event> getEvents() { return events; }
    public void setEvents(Set<Event> events) { this.events = events; }

    
}
