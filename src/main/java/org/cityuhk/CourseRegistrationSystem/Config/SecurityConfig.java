package org.cityuhk.CourseRegistrationSystem.Config;

import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
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
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/login", "/setup").permitAll()
                        .requestMatchers("/api/session/me").authenticated()
                        .requestMatchers("/api/plans/**").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .requestMatchers("/ManageCourse", "/ManageCourse/**").hasRole("ADMIN")
                        .requestMatchers("/ManageUser", "/ManageUser/**").hasRole("ADMIN")
                        .requestMatchers("/manageplan", "/manageplan/**").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers("/studentlist").hasAnyRole("ADMIN", "INSTRUCTOR")
                        .requestMatchers("/timetable").hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")
                        .requestMatchers("/ViewMasterClassSchedule").hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .httpBasic(httpBasic -> {});

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            AdminRepositoryPort adminRepository,
            StudentRepositoryPort studentRepository,
            InstructorRepositoryPort instructorRepository) {
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
                .or(() -> instructorRepository.findByUserEID(username)
                        .map(instructor -> {
                            if (instructor.getPassword() == null || instructor.getPassword().isBlank()) {
                                throw new UsernameNotFoundException("User has no password set");
                            }
                            return User.builder()
                                    .username(instructor.getUserEID())
                                    .password(instructor.getPassword())
                                    .roles("INSTRUCTOR")
                                    .build();
                        }))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

