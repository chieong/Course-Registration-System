package org.cityuhk.CourseRegistrationSystem.Config;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/error", "/timetable").permitAll()
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .httpBasic(httpBasic -> {});

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminRepository adminRepository, StudentRepository studentRepository) {
        return username -> adminRepository.findByUserEID(username)
                .map(admin -> {
                    if (admin.getPassword() == null || admin.getPassword().isBlank()) {
                        throw new UsernameNotFoundException("User has no password set");
                    }
                    return User.builder()
                            .username(admin.getUserEID())
                            .password(admin.getPassword())
                            .roles("ADMIN")
                            .build();
                })
                .or(() -> studentRepository.findByUserEID(username)
                        .map(student -> {
                            if (student.getPassword() == null || student.getPassword().isBlank()) {
                                throw new UsernameNotFoundException("User has no password set");
                            }
                            return User.builder()
                                    .username(student.getUserEID())
                                    .password(student.getPassword())
                                    .roles("STUDENT")
                                    .build();
                        }))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

        @Bean
        public CommandLineRunner seedDefaultAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
                return args -> {
                        if (adminRepository.count() == 0) {
                                Admin admin = (Admin) new Admin.AdminBuilder()
                                                .withUserEID("admin")
                                                .withName("System Admin")
                                                .withPassword(passwordEncoder.encode("admin123"))
                                                .build();
                                adminRepository.save(admin);
                        }
                };
        }
}
