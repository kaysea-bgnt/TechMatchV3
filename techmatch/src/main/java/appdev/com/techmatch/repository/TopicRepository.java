package appdev.com.techmatch.repository;

import appdev.com.techmatch.model.Topic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {
    // Find a topic by its name
    Topic findByName(String name);



}
