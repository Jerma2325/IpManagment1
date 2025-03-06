package com.example.backend.controller;

import com.example.backend.dto.IPDetailsDTO;
import com.example.backend.dto.IPRegistrationRequestDTO;
import com.example.backend.dto.TransferRequestDTO;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.BlockchainService;
import com.example.backend.service.FileStorageService;
import com.example.backend.service.IPService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/intellectual-properties")
public class IPController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private IPService ipService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<IPDetailsDTO>> getAllIPs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal().toString())) {
            System.out.println("Authentication failed: " + (authentication == null ? "null" : authentication.getPrincipal().toString()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUsername = authentication.getName();
        System.out.println("Authenticated user: " + currentUsername);

        try {
            Optional<User> userOpt = userRepository.findByUsername(currentUsername);
            if (!userOpt.isPresent()) {
                System.out.println("User not found in database: " + currentUsername);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userOpt.get();
            System.out.println("Found user: " + user.getUsername() + " with eth address: " + user.getEthAddress());

            List<IPDetailsDTO> userIPs = ipService.getAllIPsByOwner(currentUsername);
            System.out.println("Found " + userIPs.size() + " IPs for user");

            return ResponseEntity.ok(userIPs);
        } catch (Exception e) {
            System.err.println("Error getting IPs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<IPDetailsDTO> getIPById(@PathVariable String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String currentUsername = authentication.getName();
            IPDetailsDTO ip = ipService.getIPById(id);

            if (!ip.getOwnerUsername().equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(ip);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<IPDetailsDTO> registerIP(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String currentUsername = authentication.getName();

            Optional<User> userOpt = userRepository.findByUsername(currentUsername);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }
            User user = userOpt.get();

            if (user.getEthAddress() == null || user.getEthAddress().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            String storedFileName = fileStorageService.storeFile(file);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/intellectual-properties/files/")
                    .path(storedFileName)
                    .toUriString();

            String transactionHash = blockchainService.registerIP(name, storedFileName, user.getEthAddress());

            IPRegistrationRequestDTO registrationDTO = new IPRegistrationRequestDTO();
            registrationDTO.setName(name);
            registrationDTO.setDescription(description);
            registrationDTO.setOwnerUsername(currentUsername);
            registrationDTO.setOwnerAddress(user.getEthAddress());
            registrationDTO.setFileUrl(fileDownloadUri);
            registrationDTO.setFileType(file.getContentType());
            registrationDTO.setFileName(storedFileName);
            registrationDTO.setTransactionHash(transactionHash);
            IPDetailsDTO savedIP = ipService.saveIP(registrationDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedIP);
        } catch (Exception e) {
            System.err.println("Error registering IP: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<IPDetailsDTO> transferIP(
            @PathVariable String id,
            @RequestBody TransferRequestDTO transferRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String currentUsername = authentication.getName();
            IPDetailsDTO ip = ipService.getIPById(id);

            if (!ip.getOwnerUsername().equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (transferRequest == null || transferRequest.getNewOwnerAddress() == null
                    || transferRequest.getNewOwnerAddress().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (!transferRequest.getNewOwnerAddress().matches("^0x[a-fA-F0-9]{40}$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            String transactionHash = blockchainService.transferIP(id,
                    transferRequest.getNewOwnerAddress());

            IPDetailsDTO updatedIP = ipService.transferOwnership(id, transferRequest.getNewOwnerAddress(),
                    transactionHash);

            return ResponseEntity.ok(updatedIP);
        } catch (Exception e) {
            System.err.println("Error transferring IP: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            String contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}