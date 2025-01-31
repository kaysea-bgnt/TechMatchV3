package appdev.com.techmatch.repository;

import appdev.com.techmatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsernameOrEmail(String username, String email);

    @Query("SELECT MAX(u.userID) FROM User u")
    String findMaxUserID();
}