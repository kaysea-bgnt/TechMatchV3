let currentEventID = null; // Store the current event ID
let userID = null; // Store the current user ID
let allEvents = []; // Store all events fetched from the server
let lastSearchQuery = "";


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

    // Setup search functionality after DOM is ready
    setupSearchFunctionality();
});


// Function to attach click event listeners to event cards
function attachEventListeners() {
    const eventContainer = document.querySelector('.row.row-cols-1.row-cols-md-3.g-4'); // Correct selector for popups

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
                    try {
                      const [hour, minute] = time.split(":");
                        let ampm = "AM";
                        let formattedHour = parseInt(hour, 10);
                        if (formattedHour >= 12) {
                             ampm = "PM";
                             if (formattedHour > 12){
                                formattedHour -= 12;
                             }
                        }
                       if (formattedHour === 0){
                            formattedHour = 12;
                       }
                    return `${formattedHour}:${minute} ${ampm}`;
                  } catch (error){
                      console.error("Error formatting time", error);
                       return "N/A";
                    }
                  }

                // Populate modal with event details
                document.getElementById("eventTime").textContent = (event.startTime && event.endTime) ? `${formatTime(event.startTime)} - ${formatTime(event.endTime)}` : "N/A";

                // Populate modal with event details
                document.getElementById("eventType").textContent = event.eventType || "N/A";
                 // Correctly populating the topics using javascript, and that the data is an array
                    const eventTopicsElement = document.getElementById("eventTopics");
                    eventTopicsElement.textContent = ""; // Clear the HTML before setting it
                    eventTopicsElement.textContent = (event.topics && Array.isArray(event.topics) && event.topics.length > 0) ? event.topics.map(topic => topic.name).join(", ") : "N/A";

                // Populate modal with event details
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
    const applyFilterButton = document.getElementById("applyFilter");
    const resetFilterButton = document.getElementById("resetFilter");

    let selectedTopics = [];

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

        if (selectedTopics && selectedTopics.length > 0) {
             selectedTopics.forEach((topic, index) => {
                url += `topic=${topic}`;
                if(index < selectedTopics.length - 1){
                    url += "&";
                }
             })
           if (selectedType) {
              url += `&eventType=${selectedType}`;
            }
        }

        else if(selectedType){
             url += `eventType=${selectedType}`;
        }

       // Load entire page when filter changes
        window.location.href = url;
    }

    // Function to reset filters
    function resetFilters() {
         eventTypeFilter.value = "";
         selectedTopics = [];
         highlightButton(resetFilterButton, applyFilterButton);


        // Load entire page when reset filters
         window.location.href = "/events";


        // Clear Button styles
        const topicButtons = document.querySelectorAll('.topic-button');
        topicButtons.forEach(button => {
            button.style.backgroundColor = "";
            button.style.color = "";
        })
    }


       // Get all elements with class "topic-button"
       const topicButtons = document.querySelectorAll('.topic-button');

        // Attach a click event listener to each topic button
       topicButtons.forEach(button => {
           button.addEventListener('click', function() {
             const topic = this.dataset.topic;
              if (selectedTopics.includes(topic)) {
                selectedTopics = selectedTopics.filter(t => t !== topic);
                 this.style.backgroundColor = "";
                 this.style.color = "";
            } else {
                selectedTopics.push(topic);
                this.style.backgroundColor = "orange";
                 this.style.color = "white";
            }
        });
       });


    // Event listeners
    applyFilterButton.addEventListener("click", loadFilteredEvents);
    resetFilterButton.addEventListener("click", resetFilters);
}


// SEARCH BAR FUNCTIONALITY
function setupSearchFunctionality() {
    const searchInput = document.getElementById('searchInput');
    const searchForm = document.getElementById('searchForm');

    // Function to handle search submission
    function handleSearch(event) {
        event.preventDefault(); // Prevent form submission
        const query = searchInput.value.trim();
        if (query) {
            window.location.href = `/events?search=${query}`;
        } else {
            window.location.href = `/events`;
        }
    }

    // Submit event listener for the search form (Only triggers on Enter)
    searchForm.addEventListener("submit", handleSearch);
}


function updateEventCards(events) {
    const eventContainer = document.getElementById('event-container');
    eventContainer.innerHTML = ''; // Clear existing cards

    if(events.length === 0){
         const noEventsMessage = document.createElement('div');
         noEventsMessage.classList.add('col-12', 'text-center', 'text-white');
         noEventsMessage.innerHTML = "<h3>No events found with this search term.</h3>";
         eventContainer.appendChild(noEventsMessage);
        return;
    }

    events.forEach(event => {
        const eventCard = document.createElement('div');
        eventCard.classList.add('col');
        eventCard.innerHTML = `
        <div class="card h-100 event-card" data-event-id="${event.eventID}">
                <img src="data:image/jpeg;base64,${event.base64Image}" class="card-img-top" alt="Event Image">
              <div class="card-body">
                <h5 class="card-title">${event.eventName}</h5>
                <p class="card-text"><strong>Topic:</strong> 
                    ${event.topics.map(topic => topic.name).join(', ')}
                </p>
                <p class="card-text"><strong>Type:</strong> ${event.eventType}</p>
                 <p class="card-text"><strong>Date:</strong> ${event.startDate} to ${event.endDate}</p>
                <p class="card-text"><strong>Capacity:</strong> ${event.capacity}</p>
              </div>
            </div>
        `;
        eventContainer.appendChild(eventCard);
    });
}