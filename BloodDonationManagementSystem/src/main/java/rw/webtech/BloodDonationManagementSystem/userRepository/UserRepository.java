package rw.webtech.BloodDonationManagementSystem.userRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.webtech.BloodDonationManagementSystem.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);

    List<User> findByUsernameContainingOrEmailContaining(String username, String email);
}