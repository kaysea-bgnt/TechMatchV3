package appdev.com.techmatch.dto;

public class EventAttendeeDTO {
    private String eventID;  
    private String userID;
    private String username;
    private String email;

    // Constructor
    public EventAttendeeDTO(String eventID, String userID, String username, String email) {
        this.eventID = eventID;
        this.userID = userID;
        this.username = username;
        this.email = email;
    }

    public String getEventId() {
        return eventID;
    }

    public String getUserId() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
