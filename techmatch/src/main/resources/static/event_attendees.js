document.addEventListener("DOMContentLoaded", function () {
    const deleteButtons = document.querySelectorAll(".delete-attendee");

    deleteButtons.forEach(button => {
        button.addEventListener("click", function () {
            const userId = this.getAttribute("data-user-id");
            const eventId = this.getAttribute("data-event-id");

            if (!confirm("Are you sure you want to remove this attendee?")) {
                return;
            }

            fetch(`/events/attendees/delete?userId=${userId}&eventId=${eventId}`, {
                method: "DELETE"
            })
            .then(response => response.text())
            .then(message => {
                alert(message);
                location.reload(); // Refresh page after deletion
            })
            .catch(error => {
                console.error("Error deleting attendee:", error);
                alert("Failed to delete attendee.");
            });
        });
    });
});
