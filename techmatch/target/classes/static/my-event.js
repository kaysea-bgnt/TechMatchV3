document.addEventListener("DOMContentLoaded", function () {
    const buttons = document.querySelectorAll(".delete-event");

    buttons.forEach(button => {
        button.addEventListener("click", function () {
            const eventID = this.getAttribute("data-event-id");

            if (!confirm("Are you sure you want to delete this event? This action cannot be undone.")) {
                return;
            }

            fetch(`/events/delete/${eventID}`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                }
            })
            .then(response => response.text())
            .then(message => {
                alert(message);
                
                //Stay on the page and refresh
                location.reload();

            })
            .catch(error => {
                console.error("Error deleting event:", error);
                alert("Failed to delete event. Please try again.");
            });
        });
    });
});