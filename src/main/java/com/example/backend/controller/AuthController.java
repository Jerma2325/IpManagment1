package com.example.backend.controller;

import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.JwtService;
import com.example.backend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private WalletService walletService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDTO registerRequest) {
        try {
            // Validate eth address format
            if (registerRequest.getEthAddress() == null || !registerRequest.getEthAddress().matches("^0x[a-fA-F0-9]{40}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid Ethereum address format"));
            }

            // Validate private key if provided
            if (registerRequest.getPrivateKey() != null &&
                    !registerRequest.getPrivateKey().matches("^(0x)?[0-9a-fA-F]{64}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid private key format"));
            }

            Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());
            if (userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }

            Optional<User> emailOptional = userRepository.findByEmail(registerRequest.getEmail());
            if (emailOptional.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            Optional<User> ethAddressOptional = userRepository.findByEthAddress(registerRequest.getEthAddress());
            if (ethAddressOptional.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ethereum address already registered"));
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(encoder.encode(registerRequest.getPassword()));
            user.setEthAddress(registerRequest.getEthAddress());

            // Store private key if provided
            if (registerRequest.getPrivateKey() != null && !registerRequest.getPrivateKey().isEmpty()) {
                // Generate salt for encryption
                String salt = walletService.generateRandomSalt();

                // Encrypt the private key with the user's password
                String encryptedKey = walletService.encryptPrivateKey(
                        registerRequest.getPrivateKey().replaceFirst("^0x", ""),
                        registerRequest.getPassword(),
                        salt
                );

                user.setEncryptedPrivateKey(encryptedKey);
                user.setKeySalt(salt);
            }

            User savedUser = userRepository.save(user);

            String token = jwtService.generateToken(savedUser.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", savedUser.getId());
            userData.put("username", savedUser.getUsername());
            userData.put("email", savedUser.getEmail());
            userData.put("ethAddress", savedUser.getEthAddress());
            userData.put("hasPrivateKey", savedUser.getEncryptedPrivateKey() != null);

            response.put("user", userData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
            }

            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty() || !encoder.matches(password, userOptional.get().getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
            }

            User user = userOptional.get();

            String token = jwtService.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("ethAddress", user.getEthAddress());

            response.put("user", userData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or missing authorization token"));
            }

            String token = authHeader.substring(7); // remove "bearer"

            String username;
            try {
                username = jwtService.extractUsername(token);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
            }

            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }

            User user = userOptional.get();

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("ethAddress", user.getEthAddress());

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching profile: " + e.getMessage()));
        }
    }
}