package uoc.edu.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;
import org.jspecify.annotations.NonNull;

@Service
public class PrivUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public PrivUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles(user.getRole().name()).build();
    }
}
