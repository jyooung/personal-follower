package com.springcloud.personalfollower.repository;

import com.springcloud.personalfollower.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
 * Retrieves a user by their email address.
 *
 * @param email the email address to search for
 * @return an {@code Optional} containing the user if found, or empty if no user exists with the given email
 */
Optional<User> findByEmail(String email);
}
