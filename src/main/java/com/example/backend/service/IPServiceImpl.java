package com.example.backend.service;

import com.example.backend.dto.IPDetailsDTO;
import com.example.backend.dto.IPRegistrationRequestDTO;
import com.example.backend.model.IntellectualProperty;
import com.example.backend.repository.IPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IPServiceImpl implements IPService {

    @Autowired
    private IPRepository ipRepository;

    @Override
    public List<IPDetailsDTO> getAllIPsByOwner(String owner) {
        List<IntellectualProperty> ips = ipRepository.findByOwner(owner);
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

        // Generate a unique ID (in a real app, this might be the blockchain hash)
        ip.setId(UUID.randomUUID().toString());
        ip.setName(registrationDTO.getName());
        ip.setDescription(registrationDTO.getDescription());
        ip.setOwner(registrationDTO.getOwner());
        ip.setFileUrl(registrationDTO.getFileUrl());
        ip.setFileType(registrationDTO.getFileType());
        ip.setFileName(registrationDTO.getFileName());
        ip.setStatus("REGISTERED");
        ip.setTransactionHash(registrationDTO.getTransactionHash());
        ip.setCreatedAt(LocalDateTime.now());
        ip.setUpdatedAt(LocalDateTime.now());

        IntellectualProperty savedIP = ipRepository.save(ip);
        return convertToDTO(savedIP);
    }

    @Override
    public IPDetailsDTO transferOwnership(String id, String newOwnerAddress, String transactionHash) {
        IntellectualProperty ip = ipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intellectual Property not found with id: " + id));

        ip.setOwner(newOwnerAddress);
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
        dto.setOwner(ip.getOwner());
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