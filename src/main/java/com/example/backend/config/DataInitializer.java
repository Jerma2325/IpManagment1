package com.example.backend.config;

import com.example.backend.model.IntellectualProperty;
import com.example.backend.model.User;
import com.example.backend.repository.IPRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class DataInitializer {

    @Autowired
    private BlockchainService blockchainService;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, IPRepository ipRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                System.out.println("Initializing test data...");

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setEthAddress(blockchainService.getTransactionSenderAddress());
                userRepository.save(admin);
                System.out.println("Created admin user with ETH address: " + admin.getEthAddress());

                User testUser = new User();
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");
                testUser.setPassword(encoder.encode("test123"));
                testUser.setEthAddress("0xb3a381B7151ebdCf59a70E2336b11dFa904A4009");
                userRepository.save(testUser);

                if (ipRepository.count() == 0) {
                    IntellectualProperty ip1 = new IntellectualProperty();
                    ip1.setId(UUID.randomUUID().toString());
                    ip1.setName("Sample Logo Design");
                    ip1.setDescription("A logo design for a tech company");
                    ip1.setOwnerUsername(admin.getUsername());
                    ip1.setFileUrl("http://localhost:8080/api/intellectual-properties/files/sample-logo.png");
                    ip1.setFileType("image/png");
                    ip1.setFileName("sample-logo.png");
                    ip1.setStatus("REGISTERED");
                    ip1.setOwnerAddress(admin.getEthAddress());
                    ip1.setTransactionHash("0x" + UUID.randomUUID().toString().replace("-", ""));
                    ip1.setCreatedAt(LocalDateTime.now());
                    ip1.setUpdatedAt(LocalDateTime.now());
                    ipRepository.save(ip1);

                    IntellectualProperty ip2 = new IntellectualProperty();
                    ip2.setId(UUID.randomUUID().toString());
                    ip2.setName("Research Paper");
                    ip2.setDescription("Original research on blockchain applications");
                    ip2.setOwnerUsername(testUser.getUsername());
                    ip2.setFileUrl("http://localhost:8080/api/intellectual-properties/files/research-paper.pdf");
                    ip2.setFileType("application/pdf");
                    ip2.setFileName("research-paper.pdf");
                    ip2.setStatus("REGISTERED");
                    ip2.setOwnerAddress(testUser.getEthAddress());
                    ip2.setTransactionHash("0x" + UUID.randomUUID().toString().replace("-", ""));
                    ip2.setCreatedAt(LocalDateTime.now().minusDays(5));
                    ip2.setUpdatedAt(LocalDateTime.now().minusDays(5));
                    ipRepository.save(ip2);

                    System.out.println("Created sample IP records");
                }
            }
        };
    }
}