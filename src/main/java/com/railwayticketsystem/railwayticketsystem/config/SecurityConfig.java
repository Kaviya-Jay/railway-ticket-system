package com.railwayticketsystem.railwayticketsystem.config;

import com.railwayticketsystem.railwayticketsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ACCESS
                        .requestMatchers("/", "/index", "/search", "/search/**", "/error").permitAll()
                        .requestMatchers("/login", "/register", "/logout", "/otp-verify").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/tickets/**").permitAll()

                        // 2. ROLE-BASED ACCESS
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 3. AUTHENTICATED ACCESS
                        .requestMatchers("/booking/**", "/book", "/dashboard", "/payment/**").authenticated()

                        // 4. CATCH-ALL
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", false) // false මගින් කලින් හිටපු පිටුවටම redirect කරයි
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // මෙතන කලින් තිබුණේ findByEmail - දැන් එය findByNic ලෙස නිවැරදි කර ඇත
        return username -> userRepository.findByNic(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}