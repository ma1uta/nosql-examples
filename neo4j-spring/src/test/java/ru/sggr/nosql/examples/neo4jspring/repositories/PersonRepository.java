package ru.sggr.nosql.examples.neo4jspring.repositories;

import ru.sggr.nosql.examples.neo4jspring.domain.Person;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends GraphRepository<Person> {
    
}
