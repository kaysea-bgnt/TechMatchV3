package appdev.com.techmatch.dto;

public class EventAttendeeDTO {
    private String eventID;
    private String eventName;  
    private String userID;
    private String username;
    private String email;

    public EventAttendeeDTO(String eventID, String eventName, String userID, String username, String email) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.userID = userID;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public String getEventId() { return eventID; }
    public void setEventId(String eventId) { this.eventID = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getUserId() { return userID; }
    public void setUserId(String userId) { this.userID = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
}
