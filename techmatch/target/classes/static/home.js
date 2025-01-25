let currentEventID = null; // Store the current event ID
let userID = 'USER-000001'; // Replace this with the logged-in user's ID from the session

document.addEventListener('DOMContentLoaded', function () {
    const modal = new bootstrap.Modal(document.getElementById('eventModal'));

    // Open the modal and fetch event details
    document.querySelectorAll('.card').forEach(card => {
        card.addEventListener('click', function () {
            currentEventID = this.getAttribute('data-event-id');

            fetch(`/events/${currentEventID}`)
                .then(response => response.json())
                .then(event => {
                    // Populate the modal with event details
                    document.getElementById('eventName').textContent = event.eventName;
                    document.getElementById('eventDate').textContent = `${event.startDate} to ${event.endDate}`;
                    document.getElementById('eventDescription').textContent = event.description;
                    document.getElementById('eventLocation').textContent = event.location;

                    // Set event image or a placeholder
                    if (event.base64Image) {
                        document.getElementById('eventImage').src = `data:image/jpeg;base64,${event.base64Image}`;
                    } else {
                        document.getElementById('eventImage').src = '/path/to/default/image.jpg'; // Placeholder image
                    }

                    modal.show();
                })
                .catch(error => {
                    console.error('Error fetching event details:', error);
                    alert('Failed to load event details. Please try again later.');
                });
        });
    });
});

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
