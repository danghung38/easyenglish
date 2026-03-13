package com.dxh.Elearning.configuration;

import com.dxh.Elearning.entity.Role;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.repo.RoleRepository;
import com.dxh.Elearning.repo.UserRepository;
import com.sendgrid.SendGrid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Slf4j
public class ApplicationInitConfig {

    //lấy tt ng tạo
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

//    gửi thư
    @Bean
    public SendGrid sendGrid(@Value("${spring.sendGrid.apiHung}") String apiKey) {
        return new SendGrid(apiKey);
    }

    PasswordEncoder passwordEncoder;


    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()){
                var roles = new HashSet<Role>();
                Role role= Role.builder()
                        .name(com.dxh.Elearning.enums.Role.ADMIN.name())
                        .description("Admin")
                        .build();
                roleRepository.save(role);
                Role role2= Role.builder()
                        .name(com.dxh.Elearning.enums.Role.USER.name())
                        .description("User")
                        .build();
                roleRepository.save(role2);

                roles.add(role);

                User user = User.builder()
                        .username("admin")
                        .phoneNumber("0911581476")
                        .email("hunglockedk4@gmail.com")
                        .enabled(true)
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .build();

                User savedUser = userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}
