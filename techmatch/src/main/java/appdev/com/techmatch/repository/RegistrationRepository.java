package appdev.com.techmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import appdev.com.techmatch.model.Registration;
import java.util.List;


@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {
    boolean existsByUserIDAndEventID(String userID, String eventID);

    List<Registration> findByEventID(String eventID);

}



