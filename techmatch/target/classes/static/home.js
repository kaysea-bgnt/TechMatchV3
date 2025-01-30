let currentEventID = null; // Store the current event ID
let userID = null; // Store the current user ID

// Fetch user details when the page loads
document.addEventListener("DOMContentLoaded", async () => {
    try {
        const userResponse = await fetch("/user/details");
        const userData = await userResponse.json();

        if (userData.userID) {
            userID = userData.userID;
            console.log("Logged-in user:", userData);
        } else {
            console.error("User not logged in.");
        }
    } catch (error) {
        console.error("Error fetching user details:", error);
    }

    // Attach event listeners for event cards dynamically
    attachEventListeners();

    // Setup filter functionality after DOM is ready
    setupFilterFunctionality();
});


// Function to attach click event listeners to event cards
function attachEventListeners() {
    const eventContainer = document.querySelector('.row.row-cols-1.row-cols-md-3.g-4');
    
    eventContainer.addEventListener('click', function(event) {
        const card = event.target.closest('.event-card');
        if (!card) return;

        currentEventID = card.getAttribute("data-event-id");

        if (!currentEventID) {
            console.error("Event ID is missing.");
            return;
        }

        console.log("Selected Event ID:", currentEventID);

        // Fetch event details
        fetch(`/events/${currentEventID}`)
            .then((response) => response.json())
            .then((event) => {
                if (!event) {
                    console.error("Event not found");
                    return;
                }

                // Populate modal with event details
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
                document.getElementById("eventTopics").textContent = (event.topics && event.topics.length > 0) ? event.topics.join(", ") : "N/A";
                document.getElementById("eventOrganizer").textContent = event.organization || "N/A";
                document.getElementById("eventCapacity").textContent = event.capacity || "N/A";
                document.getElementById("eventIsFree").textContent = (event.isFree === true || event.isFree === "true") ? "Yes" : "No";

                // Reset Register button state
                let registerButton = document.getElementById("registerButton");
                registerButton.textContent = "Register";
                registerButton.classList.add("btn-primary");
                registerButton.classList.remove("btn-success");
                registerButton.disabled = false;

                // Show the modal
                let eventModal = new bootstrap.Modal(document.getElementById("eventDetailModal"));
                eventModal.show();
            })
            .catch((error) => console.error("Error fetching event details:", error));
    });
}


// Functionality for Register button
document.getElementById("registerButton").addEventListener("click", function () {
    if (!currentEventID || !userID) {
        console.error("Event ID or User ID is missing.");
        alert("Failed to register for the event. Please try again later.");
        return;
    }

    console.log("Registering user for event:", { userID, eventID: currentEventID });

    fetch(`/events/register`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ userID, eventID: currentEventID }),
    })
    .then((response) => {
        if (response.ok) {
            alert("Successfully registered for the event!");
            let registerButton = document.getElementById("registerButton");
            registerButton.textContent = "Registered";
            registerButton.classList.remove("btn-primary");
            registerButton.classList.add("btn-success");
            registerButton.disabled = true;
        } else {
            alert("Failed to register for the event. Please try again later.");
        }
    })
    .catch((error) => {
        console.error("Error registering user for event:", error);
        alert("Failed to register for the event. Please try again later.");
    });
});


// FUNCTIONALITY FOR APPLY FILTERS AND RESET FILTERS
function setupFilterFunctionality() {
    const eventTypeFilter = document.getElementById('eventTypeFilter');
    const eventContainer = document.querySelector('.row.row-cols-1.row-cols-md-3.g-4');
    const applyFilterButton = document.getElementById("applyFilter");
    const resetFilterButton = document.getElementById("resetFilter");
    const eventCardsContainer = document.querySelector('.row.row-cols-1.row-cols-md-3.g-4');

    let selectedTopic = null;

    // Function to highlight the clicked button and reset the other
    function highlightButton(clickedButton, otherButton) {
        clickedButton.style.backgroundColor = "orange";
        clickedButton.style.color = "white";

        otherButton.style.backgroundColor = "";
        otherButton.style.color = "";
    }

     // Function to fetch and display events based on filters
    function loadFilteredEvents() {
        const selectedType = eventTypeFilter.value;
        highlightButton(applyFilterButton, resetFilterButton);

        let url = `/events?`;
        if (selectedType) {
            url += `eventType=${selectedType}`;
        }
        if (selectedTopic) {
            url += `${selectedType ? '&' : ''}topic=${selectedTopic}`;
        }

        // Load entire page when filter changes
        window.location.href = url;

    }


    // Function to reset filters
    function resetFilters() {
        eventTypeFilter.value = "";
        selectedTopic = null;
        highlightButton(resetFilterButton, applyFilterButton);

        // Load entire page when reset filters
        window.location.href = "/events";
    }


       // Get all elements with class "topic-button"
       const topicButtons = document.querySelectorAll('.topic-button');
       
        // Attach a click event listener to each topic button
       topicButtons.forEach(button => {
           button.addEventListener('click', function() {
             selectedTopic = this.dataset.topic;
             loadFilteredEvents();
          });
       });


    // Event listeners
    applyFilterButton.addEventListener("click", loadFilteredEvents);
    resetFilterButton.addEventListener("click", resetFilters);
}