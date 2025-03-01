package com.example.backend.controller;

import com.example.backend.dto.IPDetailsDTO;
import com.example.backend.dto.IPRegistrationRequestDTO;
import com.example.backend.dto.TransferRequestDTO;
import com.example.backend.service.BlockchainService;
import com.example.backend.service.FileStorageService;
import com.example.backend.service.IPService;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/intellectual-properties")
public class IPController {


    private final FileStorageService fileStorageService;
    private final BlockchainService blockchainService;
    private final IPService ipService;

    public IPController(FileStorageService fileStorageService,
                        BlockchainService blockchainService,
                        IPService ipService) {
        this.fileStorageService = fileStorageService;
        this.blockchainService = blockchainService;
        this.ipService = ipService;
    }

    @GetMapping
    public ResponseEntity<List<IPDetailsDTO>> getAllIPs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUsername = authentication.getName();
        List<IPDetailsDTO> userIPs = ipService.getAllIPsByOwner(currentUsername);
        return ResponseEntity.ok(userIPs);
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

            if (!ip.getOwner().equals(currentUsername)) {
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

            String storedFileName = fileStorageService.storeFile(file);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/intellectual-properties/files/")
                    .path(storedFileName)
                    .toUriString();

            String transactionHash = blockchainService.registerIP(name, storedFileName, currentUsername);

            IPRegistrationRequestDTO registrationDTO = new IPRegistrationRequestDTO();
            registrationDTO.setName(name);
            registrationDTO.setDescription(description);
            registrationDTO.setOwner(currentUsername);
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

            if (!ip.getOwner().equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (transferRequest == null || transferRequest.getNewOwnerAddress() == null
                    || transferRequest.getNewOwnerAddress().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            String transactionHash = blockchainService.transferIP(id, transferRequest.getNewOwnerAddress());

            IPDetailsDTO updatedIP = ipService.transferOwnership(id, transferRequest.getNewOwnerAddress(), transactionHash);

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