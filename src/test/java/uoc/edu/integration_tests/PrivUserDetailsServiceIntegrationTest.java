package uoc.edu.integration_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.model.Role;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;
import uoc.edu.security.PrivUserDetailsService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PrivUserDetailsServiceIntegrationTest {

    @Autowired
    private PrivUserDetailsService privUserDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private User technician;

    @BeforeEach
    void setUp() {

        userRepository.findByEmailIgnoreCase("security-test@test.com").ifPresent(userRepository::delete);

        technician = new User();
        technician.setName("Ataulfo");
        technician.setEmail("ataulfo-test@test.com");
        technician.setPasswordHash(passwordEncoder.encode("Password123!"));
        technician.setRole(Role.USER);

        technician = userRepository.save(technician);
    }

    @Test
    void loadUserByUsernameReturnsCorrectUserDetails() {

        UserDetails userDetails = privUserDetailsService.loadUserByUsername("ataulfo-test@test.com");

        assertAll(() -> assertEquals("ataulfo-test@test.com", userDetails.getUsername()),
                () -> assertEquals(technician.getPasswordHash(), userDetails.getPassword()),
                () -> assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))),
                () -> assertTrue(userDetails.isAccountNonExpired()),
                () -> assertTrue(userDetails.isAccountNonLocked()),
                () -> assertTrue(userDetails.isCredentialsNonExpired()),
                () -> assertTrue(userDetails.isEnabled())
        );
    }

    @Test
    void loadUserByUsernameIsCaseInsensitive() {

        UserDetails userDetails = privUserDetailsService.loadUserByUsername("ATAULFO-TEST@TEST.COM");

        assertEquals("ataulfo-test@test.com", userDetails.getUsername());
    }

    @Test
    void loadUserByUsernameReturnsAdminAuthority() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("security-admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("Password123!"));
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);
        UserDetails userDetails = privUserDetailsService.loadUserByUsername("security-admin@test.com");
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsernameThrowsExceptionWhenUserDoesNotExist() {
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> privUserDetailsService.loadUserByUsername("notexist@test.com"));
        assertEquals("User not found", exception.getMessage());
    }
}