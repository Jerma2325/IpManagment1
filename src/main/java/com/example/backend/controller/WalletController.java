package com.example.backend.controller;

import com.example.backend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateWallet(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            String username = authentication.getName();
            String password = request.get("password");

            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            String address = walletService.generateWallet(username, password);

            return ResponseEntity.ok(Map.of(
                    "message", "Wallet generated successfully",
                    "address", address
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate wallet: " + e.getMessage()));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importWallet(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            String username = authentication.getName();
            String privateKey = request.get("privateKey");
            String password = request.get("password");

            if (privateKey == null || privateKey.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Private key is required"));
            }

            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            String address = walletService.importWallet(username, privateKey, password);

            return ResponseEntity.ok(Map.of(
                    "message", "Wallet imported successfully",
                    "address", address
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to import wallet: " + e.getMessage()));
        }
    }
}