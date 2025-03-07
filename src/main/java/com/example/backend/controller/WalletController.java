package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.*;


@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Web3j web3j;

    @Value("${blockchain.chain.id}")
    private String chainId;

    @Value("${blockchain.chain.id}")
    private String chainName;

    @Value("${blockchain.rpc.url}")
    private String explorerUrl;

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty() || userOpt.get().getEthAddress() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No wallet associated with this account"));
            }

            String address = userOpt.get().getEthAddress();

            EthGetBalance balanceResponse = web3j.ethGetBalance(
                    address, DefaultBlockParameterName.LATEST).send();

            BigDecimal balanceInEth = Convert.fromWei(
                    new BigDecimal(balanceResponse.getBalance()), Convert.Unit.ETHER);

            return ResponseEntity.ok(Map.of(
                    "address", address,
                    "balance", balanceInEth.toString(),
                    "chain", Map.of(
                            "id", chainId,
                            "name", chainName
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get balance: " + e.getMessage()));
        }
    }
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty() || userOpt.get().getEthAddress() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No wallet associated with this account"));
            }

            String address = userOpt.get().getEthAddress();


            List<Map<String, Object>> transactions = new ArrayList<>();

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get transactions: " + e.getMessage()));
        }
    }
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
    @PostMapping("/remove")
    public ResponseEntity<?> removeWallet() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            user.setEthAddress(null);
            user.setEncryptedPrivateKey(null);
            user.setKeySalt(null);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Wallet removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to remove wallet: " + e.getMessage()));
        }
    }
}