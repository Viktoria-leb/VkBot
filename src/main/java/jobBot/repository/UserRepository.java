package jobBot.repository;

import jobBot.dto.VkUser;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<VkUser> findById(Long id);

    boolean exists(Long id);

    void save(VkUser user);

    void delete(Long id);

    List<VkUser> findAll();

}
