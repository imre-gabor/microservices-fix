package hu.webuni.security.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class AirportUserDetailsService implements UserDetailsService {

	private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
	private final PasswordEncoder passwordEncoder;

	public AirportUserDetailsService() {
		this.passwordEncoder = new BCryptPasswordEncoder();
		this.inMemoryUserDetailsManager = new InMemoryUserDetailsManager(
				List.of(
						User.withUsername("user")
								.password(passwordEncoder.encode("pass"))
								.roles("user")
								.build(),
						User.withUsername("admin")
								.password(passwordEncoder.encode("pass"))
								.roles("user", "search")
								.build()
				)
		);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails user = inMemoryUserDetailsManager.loadUserByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found: " + username);
		}
		return new User(user.getUsername(), "",
				user.getAuthorities().stream().map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
						.collect(Collectors.toList()));
	}
}
