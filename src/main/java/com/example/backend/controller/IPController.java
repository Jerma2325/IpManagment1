package com.example.backend.controller;

import com.example.backend.dto.IPDetailsDTO;
import com.example.backend.dto.IPRegistrationRequestDTO;
import com.example.backend.dto.TransferRequestDTO;
import com.example.backend.model.IntellectualProperty;
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
import java.util.Map;

@RestController
@RequestMapping("/intellectual-properties")
@CrossOrigin(origins = "*")
public class IPController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private IPService ipService;


    @GetMapping
    public ResponseEntity<List<IPDetailsDTO>> getAllIPs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<IPDetailsDTO> userIPs = ipService.getAllIPsByOwner(currentUsername);
        return ResponseEntity.ok(userIPs);
    }


    @GetMapping("/{id}")
    public ResponseEntity<IPDetailsDTO> getIPById(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        IPDetailsDTO ip = ipService.getIPById(id);

        // Check if the current user is the owner
        if (!ip.getOwner().equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(ip);
    }


    @PostMapping
    public ResponseEntity<IPDetailsDTO> registerIP(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
            String currentUsername = authentication.getName();

            IPDetailsDTO ip = ipService.getIPById(id);

            if (!ip.getOwner().equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String transactionHash = blockchainService.transferIP(id, transferRequest.getNewOwnerAddress());

            IPDetailsDTO updatedIP = ipService.transferOwnership(id, transferRequest.getNewOwnerAddress(), transactionHash);

            return ResponseEntity.ok(updatedIP);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            //determin content type
            String contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}