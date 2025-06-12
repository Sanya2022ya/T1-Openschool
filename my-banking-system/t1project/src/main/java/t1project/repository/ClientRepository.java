package t1project.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import t1project.model.Client;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findByClientId(UUID clientId);
    List<Client> findByBlockedTrue();
    long countByBlockedTrue();
}
