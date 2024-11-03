package rw.webtech.BloodDonationManagementSystem.userRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.webtech.BloodDonationManagementSystem.model.ResetToken;
import rw.webtech.BloodDonationManagementSystem.model.User;

import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    void deleteByToken(String token);
    Optional<ResetToken> findByUser(User user);
    Optional<ResetToken> findByToken(String token);
}