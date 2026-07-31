package uoc.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import uoc.edu.model.Role;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Value("${ADMIN_NAME}")
    private String adminName;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (userRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
                User admin = new User();

                admin.setName(adminName);
                admin.setEmail(adminEmail);
                admin.setPasswordHash(
                        passwordEncoder.encode(adminPassword)
                );
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }
        };
    }
}