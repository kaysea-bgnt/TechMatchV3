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

    // Initialize DateRangePicker and store its reference
     const dateRangePicker = new DateRangePicker();

    // Setup filter functionality after DOM is ready
    setupFilterFunctionality(dateRangePicker);

    // Setup search functionality after DOM is ready
    setupSearchFunctionality();
});


// Function to attach click event listeners to event cards
// Function to attach click event listeners to event cards
function attachEventListeners() {
  const tabContent = document.getElementById('myTabContent');
   // Use event delegation: Attach the listener to the parent container
   tabContent.addEventListener('click', function(event) {
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
               if (!event || !event.user) {
                   console.error("Event or creator details are missing.");
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

               // Check if the logged-in user is the event creator
               if (event.user.userID === userID) {
                   console.log("User is the creator, disabling Register button.");
                   registerButton.textContent = "You are the organizer";
                   registerButton.classList.add("btn-secondary");
                   registerButton.classList.remove("btn-primary");
                   registerButton.disabled = true;
               } else {
                   console.log("User is NOT the creator, enabling Register button.");
                   registerButton.textContent = "Register";
                   registerButton.classList.add("btn-primary");
                   registerButton.classList.remove("btn-secondary");
                   registerButton.disabled = false;
               }

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

     // Fetch event details to check if the user is the creator
     fetch(`/events/${currentEventID}`)
     .then(response => response.json())
     .then(event => {
         if (event.user && event.user.userID === userID) {
             alert("You cannot register for your own event.");
             return;
         }

    // Proceed with registration if not the creator
     console.log("Registering user for event:", { userID, eventID: currentEventID });

    fetch("/events/register", {
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
});


// FUNCTIONALITY FOR APPLY FILTERS AND RESET FILTERS
function setupFilterFunctionality(dateRangePicker) { //Receives DateRangePicker
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

         if (dateRangePicker.startDateInput.value) { //Read values from dateRangePicker
              url += `startDate=${dateRangePicker.startDateInput.value}&`;
         }
           if (dateRangePicker.endDateInput.value) { //Read values from dateRangePicker
              url += `endDate=${dateRangePicker.endDateInput.value}&`;
         }
          
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
          dateRangePicker.startDateInput.value = ""; //Reset values from dateRangePicker
          dateRangePicker.endDateInput.value = ""; //Reset values from dateRangePicker
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


// CALENDAR DATE PICKER
class DateRangePicker {
    constructor() {
        this.currentDate = new Date();
        this.startDate = null;
        this.endDate = null;
        this.isSelectingRange = false;
        this.months = [
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        ];
        
        this.init();
    }

    init() {
        this.setupElements();
        this.setupEventListeners();
        this.render();
    }

    setupElements() {
        const container = document.querySelector('.date-picker-container');
        container.innerHTML = 
            `<div class="date-picker-header">
                <button id="prevMonth" class="month-nav"><</button>
                <span id="currentMonthYear"></span>
                <button id="nextMonth" class="month-nav">></button>
            </div>
            <div class="weekdays-container">
                <div>Su</div>
                <div>Mo</div>
                <div>Tu</div>
                <div>We</div>
                <div>Th</div>
                <div>Fr</div>
                <div>Sa</div>
            </div>
            <div id="daysContainer" class="days-container"></div>
            <div class="date-range-display">
                <span id="dateRangeText">No date range selected</span>
                <button id="clearDates" class="clear-dates">Clear</button>
            </div>
            <input type="hidden" id="startDate" name="startDate">
            <input type="hidden" id="endDate" name="endDate">
        `;

        this.prevMonthBtn = document.getElementById('prevMonth');
        this.nextMonthBtn = document.getElementById('nextMonth');
        this.currentMonthYearElem = document.getElementById('currentMonthYear');
        this.daysContainer = document.getElementById('daysContainer');
        this.dateRangeText = document.getElementById('dateRangeText');
        this.clearDatesBtn = document.getElementById('clearDates');
        this.startDateInput = document.getElementById('startDate');
        this.endDateInput = document.getElementById('endDate');
    }

    setupEventListeners() {
        this.prevMonthBtn.addEventListener('click', () => this.navigateMonth(-1));
        this.nextMonthBtn.addEventListener('click', () => this.navigateMonth(1));
        this.clearDatesBtn.addEventListener('click', () => this.clearSelection());
    }

    navigateMonth(delta) {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth() + delta;
    
        this.currentDate = new Date(year, month, 1); // Ensure it always starts at the 1st of the month
        this.render();
    }
    

    formatDate(date) {
        if (!date) return '';
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        return `${date.getFullYear()}-${month}-${day}`;
    }

    isSameDate(date1, date2) {
        return date1 && date2 && 
            date1.getFullYear() === date2.getFullYear() &&
            date1.getMonth() === date2.getMonth() &&
            date1.getDate() === date2.getDate();
    }

    isInRange(date) {
        if (!this.startDate || !this.endDate) return false;
        return date >= this.startDate && date <= this.endDate;
    }

    clearSelection() {
        this.startDate = null;
        this.endDate = null;
        this.isSelectingRange = false;
        this.updateDateRangeText();
        this.startDateInput.value = '';
        this.endDateInput.value = '';
        this.render();
    }

    updateDateRangeText() {
         if (!this.startDate) {
            this.dateRangeText.textContent = 'No date range selected';
            return;
        }

        const formatDateDisplay = (date) => {
            return date.toLocaleDateString('en-US', { 
                month: 'short', 
                day: 'numeric',
                year: 'numeric'
            });
        };

        if (this.startDate && this.endDate) {
            this.dateRangeText.textContent = 
                `${formatDateDisplay(this.startDate)} - ${formatDateDisplay(this.endDate)}`;
        } else if (this.startDate) {
            this.dateRangeText.textContent = 
                `${formatDateDisplay(this.startDate)} - Select end date`;
        }
    }

    handleDateClick(day) {
        const clickedDate = new Date(
            this.currentDate.getFullYear(),
            this.currentDate.getMonth(),
            day
        );

        if (!this.startDate || (this.startDate && this.endDate) || clickedDate < this.startDate) {
            // Start new selection
            this.startDate = clickedDate;
            this.endDate = null;
            this.isSelectingRange = true;
        } else {
            // Complete the range
            this.endDate = clickedDate;
            this.isSelectingRange = false;
            
            // Swap dates if end is before start
            if (this.endDate < this.startDate) {
                [this.startDate, this.endDate] = [this.endDate, this.startDate];
            }
        }

        this.startDateInput.value = this.formatDate(this.startDate);
        this.endDateInput.value = this.formatDate(this.endDate);
        this.updateDateRangeText();
        this.render();
       console.log("Start Date Input Value:", this.startDateInput.value);
        console.log("End Date Input Value:", this.endDateInput.value);
    }


    render() {
        this.currentMonthYearElem.textContent = 
            `${this.months[this.currentDate.getMonth()]} ${this.currentDate.getFullYear()}`;

        this.daysContainer.innerHTML = '';
        
        const firstDay = new Date(
            this.currentDate.getFullYear(),
            this.currentDate.getMonth(),
            1
        ).getDay();

        const daysInMonth = new Date(
            this.currentDate.getFullYear(),
            this.currentDate.getMonth() + 1,
            0
        ).getDate();

        // Add empty cells for days before start of month
        for (let i = 0; i < firstDay; i++) {
            const emptyDay = document.createElement('div');
            emptyDay.className = 'day empty';
            this.daysContainer.appendChild(emptyDay);
        }

        // Add days of month
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(
                this.currentDate.getFullYear(),
                this.currentDate.getMonth(),
                day
            );

            const dayElem = document.createElement('button');
            dayElem.className = 'day';
            dayElem.textContent = day;

            // Add appropriate classes based on selection state
            if (this.isSameDate(date, this.startDate)) {
                dayElem.classList.add('range-start');
            }
            if (this.isSameDate(date, this.endDate)) {
                dayElem.classList.add('range-end');
            }
            if (this.isInRange(date)) {
                dayElem.classList.add('in-range');
            }

            dayElem.addEventListener('click', () => this.handleDateClick(day));
            this.daysContainer.appendChild(dayElem);
        }
    }
}

// Initialize DateRangePicker when document is ready
document.addEventListener('DOMContentLoaded', () => {
    new DateRangePicker();
});
    


// SCROLL TOPICS
document.addEventListener('DOMContentLoaded', function() {
    const topicsWrapper = document.querySelector('.topics-wrapper');
    const topicsRow = document.querySelector('.topics-row');
    const scrollLeftBtn = document.getElementById('scrollLeft');
    const scrollRightBtn = document.getElementById('scrollRight');
    
    // Scroll amount for each click (adjust as needed)
    const scrollAmount = 200;

    // Handle left scroll
    scrollLeftBtn.addEventListener('click', () => {
        topicsWrapper.scrollBy({
            left: -scrollAmount,
            behavior: 'smooth'
        });
    });

    // Handle right scroll
    scrollRightBtn.addEventListener('click', () => {
        topicsWrapper.scrollBy({
            left: scrollAmount,
            behavior: 'smooth'
        });
    });

    // Show/hide arrows based on scroll position
    function updateArrowVisibility() {
        const { scrollLeft, scrollWidth, clientWidth } = topicsWrapper;
        
        scrollLeftBtn.style.opacity = scrollLeft > 0 ? '1' : '0.5';
        scrollRightBtn.style.opacity = 
            scrollLeft < (scrollWidth - clientWidth - 1) ? '1' : '0.5';
    }

    // Update arrow visibility on scroll and resize
    topicsWrapper.addEventListener('scroll', updateArrowVisibility);
    window.addEventListener('resize', updateArrowVisibility);

    // Initial arrow visibility check
    updateArrowVisibility();
});



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
        eventCard.innerHTML = 
        `<div class="card h-100 event-card" data-event-id="${event.eventID}">
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



// Function to update events list
function updateEventsList(events) {
    const eventsContainer = document.querySelector('.row.row-cols-1.row-cols-md-3.g-4');
    eventsContainer.innerHTML = ''; // Clear existing events

    if (events.length === 0) {
        const noEventsMessage = document.createElement('div');
        noEventsMessage.classList.add('col-12', 'text-center', 'text-white');
        noEventsMessage.innerHTML = "<h3>No events matched with the date selected.</h3>";
        eventsContainer.appendChild(noEventsMessage);
        return;
    }

    // Add your logic to render events based on your template structure
    events.forEach(event => {
        const eventCard = createEventCard(event);
        eventsContainer.appendChild(eventCard);
    });
}

function createEventCard(event) {
    const eventCard = document.createElement('div');
    eventCard.classList.add('col');
    eventCard.innerHTML = `
        <div class="card h-100 event-card" data-event-id="${event.eventID}">
            <img src="data:image/jpeg;base64,${event.base64Image}" class="card-img-top" alt="Event Image">
            <div class="card-body">
                <h5 class="card-title">${event.eventName}</h5>
                <p class="card-text"><strong>Topic:</strong> ${event.topics.map(topic => topic.name).join(', ')}</p>
                <p class="card-text"><strong>Type:</strong> ${event.eventType}</p>
                <p class="card-text"><strong>Date:</strong> ${event.startDate} to ${event.endDate}</p>
                <p class="card-text"><strong>Capacity:</strong> ${event.capacity}</p>
            </div>
        </div>
    `;
    return eventCard;
}