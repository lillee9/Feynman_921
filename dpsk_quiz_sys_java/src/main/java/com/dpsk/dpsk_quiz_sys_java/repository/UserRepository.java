package com.dpsk.dpsk_quiz_sys_java.repository;
import java.util.Optional;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);
    Optional<User> findByEmail(String email);
}
