// Update the IPServiceImpl.java to use ethAddress when registering IP

package com.example.backend.service;

import com.example.backend.dto.IPDetailsDTO;
import com.example.backend.dto.IPRegistrationRequestDTO;
import com.example.backend.model.IntellectualProperty;
import com.example.backend.model.User;
import com.example.backend.repository.IPRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IPServiceImpl implements IPService {

    @Autowired
    private IPRepository ipRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<IPDetailsDTO> getAllIPsByOwner(String owner) {
        List<IntellectualProperty> ips;

        if (owner.startsWith("0x")) {
            ips = ipRepository.findByOwnerAddress(owner);
        } else {
            Optional<User> userOpt = userRepository.findByUsername(owner);
            if (userOpt.isPresent()) {
                ips = ipRepository.findByOwnerAddress(userOpt.get().getEthAddress());
            }
            else {
                ips = List.of();
            }
        }

        return ips.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public IPDetailsDTO getIPById(String id) {
        IntellectualProperty ip = ipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intellectual Property not found with id: " + id));
        return convertToDTO(ip);
    }

    @Override
    public IPDetailsDTO saveIP(IPRegistrationRequestDTO registrationDTO) {
        IntellectualProperty ip = new IntellectualProperty();

        String transactionInfo = registrationDTO.getTransactionHash();
        String transactionHash;
        String blockchainHash = null;

        if (transactionInfo.contains(":")) {
            String[] parts = transactionInfo.split(":", 2);
            blockchainHash = parts[0];
            transactionHash = parts[1];
        } else {
            transactionHash = transactionInfo;
        }


        ip.setId(UUID.randomUUID().toString());
        ip.setBlockchainHash(blockchainHash);
        ip.setName(registrationDTO.getName());
        ip.setDescription(registrationDTO.getDescription());
        ip.setOwnerUsername(registrationDTO.getOwnerUsername());
        ip.setOwnerAddress(registrationDTO.getOwnerAddress());
        ip.setFileUrl(registrationDTO.getFileUrl());
        ip.setFileType(registrationDTO.getFileType());
        ip.setFileName(registrationDTO.getFileName());
        ip.setStatus("REGISTERED");
        ip.setTransactionHash(transactionHash);
        ip.setCreatedAt(LocalDateTime.now());
        ip.setUpdatedAt(LocalDateTime.now());

        IntellectualProperty savedIP = ipRepository.save(ip);
        return convertToDTO(savedIP);
    }

    @Override
    public IPDetailsDTO transferOwnership(String id, String newOwnerAddress, String transactionHash) {
        IntellectualProperty ip = ipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intellectual Property not found with id: " + id));

        Optional<User> newOwnerUser = userRepository.findByEthAddress(newOwnerAddress);

        ip.setOwnerAddress(newOwnerAddress);
        if (newOwnerUser.isPresent()) {
            ip.setOwnerUsername(newOwnerUser.get().getUsername());
        } else {
            ip.setOwnerUsername("Unknown");
        }

        ip.setStatus("TRANSFERRED");
        ip.setTransactionHash(transactionHash);
        ip.setUpdatedAt(LocalDateTime.now());

        IntellectualProperty updatedIP = ipRepository.save(ip);
        return convertToDTO(updatedIP);
    }

    private IPDetailsDTO convertToDTO(IntellectualProperty ip) {
        IPDetailsDTO dto = new IPDetailsDTO();
        dto.setId(ip.getId());
        dto.setName(ip.getName());
        dto.setDescription(ip.getDescription());
        dto.setOwnerUsername(ip.getOwnerUsername());
        dto.setOwnerAddress(ip.getOwnerAddress());
        dto.setBlockchainHash(ip.getBlockchainHash());
        dto.setFileUrl(ip.getFileUrl());
        dto.setFileType(ip.getFileType());
        dto.setFileName(ip.getFileName());
        dto.setStatus(ip.getStatus());
        dto.setTransactionHash(ip.getTransactionHash());
        dto.setCreatedAt(ip.getCreatedAt());
        dto.setUpdatedAt(ip.getUpdatedAt());
        return dto;
    }
}