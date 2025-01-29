let currentEventID = null; // Store the current event ID
let userID = 'USER-000001'; // Replace this with the logged-in user's ID from the session

// Register for the event
function registerForEvent() {
    if (!currentEventID || !userID) {
        console.error('Missing event ID or user ID');
        alert('Unable to register. Please refresh the page and try again.');
        return;
    }

    console.log("Sending registration data:", { userID, eventID: currentEventID });

    fetch(`/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userID, eventID: currentEventID }),
    })
        .then(response => {
            if (response.ok) {
                console.log('Successfully registered for event');
                // Update the button text and state
                const registerButton = document.getElementById('registerButton');
                registerButton.textContent = 'Going';
                registerButton.classList.remove('btn-primary');
                registerButton.classList.add('btn-success');
                registerButton.disabled = true;
            } else {
                console.error('Failed to register for event');
                alert('Registration failed. Please try again.');
            }
        })
        .catch(error => {
            console.error('Error registering for event:', error);
            alert('An error occurred during registration. Please try again later.');
        });
}

// Filter events by topic
function filterByTopic(topic) {
    fetch(`/events?topic=${topic}`)
        .then(response => response.json())
        .then(events => updateEventList(events))
        .catch(err => {
            console.error('Error fetching events by topic:', err);
            alert('Failed to filter events. Please try again later.');
        });
}

// Apply date and type filters
function applyFilters() {
    const date = document.getElementById('filterDate').value;
    const eventType = document.getElementById('filterEventType').value;

    const query = new URLSearchParams();
    if (date) query.append('date', date);
    if (eventType) query.append('type', eventType);

    fetch(`/events?${query.toString()}`)
        .then(response => response.json())
        .then(events => updateEventList(events))
        .catch(err => {
            console.error('Error applying filters:', err);
            alert('Failed to apply filters. Please try again later.');
        });
}

// Reset filters
function resetFilters() {
    document.getElementById('filterDate').value = '';
    document.getElementById('filterEventType').value = '';
    fetch('/events')
        .then(response => response.json())
        .then(events => updateEventList(events))
        .catch(err => {
            console.error('Error resetting filters:', err);
            alert('Failed to reset filters. Please try again later.');
        });
}

// Update the event list dynamically
function updateEventList(events) {
    const eventList = document.getElementById('event-list');
    eventList.innerHTML = ''; // Clear current events

    events.forEach(event => {
        const col = document.createElement('div');
        col.className = 'col';

        const card = `
            <div class="card h-100" data-event-id="${event.eventID}">
                <img src="${event.base64Image ? `data:image/jpeg;base64,${event.base64Image}` : '/path/to/default/image.jpg'}" class="card-img-top" alt="Event Image">
                <div class="card-body">
                    <h5 class="card-title">${event.eventName}</h5>
                    <p class="card-text">${event.description}</p>
                </div>
            </div>
        `;
        col.innerHTML = card;
        eventList.appendChild(col);
    });
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.event-card').forEach(card => {
        card.addEventListener("click", function () {
            let eventID = card.getAttribute('data-event-id'); // Get Event ID from card

            if (!eventID) {
                console.error("Event ID is missing.");
                return;
            }

            // Fetch event details from the server
            fetch(`/events/${eventID}`)
                .then(response => response.json())
                .then(event => {
                    if (!event) {
                        console.error("Event not found");
                        return;
                    }

                    // Populate modal with event details (handle undefined/null cases)
                    document.getElementById("eventTitle").textContent = event.eventName || "N/A";
                    document.getElementById("eventDescription").textContent = event.description || "N/A";
                    document.getElementById("eventLocation").textContent = event.location || "N/A";
                    document.getElementById("eventDate").textContent = (event.startDate && event.endDate) ? `${event.startDate} to ${event.endDate}` : "N/A";
                    
                    // Format Time (Convert to AM/PM Format)
                    function formatTime(time) {
                        if (!time) return "N/A";
                        const [hour, minute] = time.split(":");
                        const ampm = hour >= 12 ? "PM" : "AM";
                        const formattedHour = hour % 12 || 12;
                        return `${formattedHour}:${minute} ${ampm}`;
                    }

                    document.getElementById("eventTime").textContent = (event.startTime && event.endTime) ? `${formatTime(event.startTime)} - ${formatTime(event.endTime)}` : "N/A";

                    document.getElementById("eventType").textContent = event.eventType || "N/A";

                    // Handle multiple topics (array)
                    document.getElementById("eventTopics").textContent = (event.topics && event.topics.length > 0) ? event.topics.join(", ") : "N/A";
                    
                    document.getElementById("eventOrganizer").textContent = event.organization || "N/A";

                    // Ensure Capacity is displayed correctly
                    document.getElementById("eventCapacity").textContent = event.capacity || "N/A";
                    
                    // Correctly display "Yes" or "No" for isFree
                    document.getElementById("eventIsFree").textContent = (event.isFree === true || event.isFree === "true") ? "Yes" : "No";

                    // Show the modal
                    let eventModal = new bootstrap.Modal(document.getElementById("eventDetailModal"));
                    eventModal.show();
                })
                .catch(error => console.error("Error fetching event details:", error));
        });
    });
});




