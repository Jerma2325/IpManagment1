package com.example.backend.service;

import com.example.backend.dto.IPDetailsDTO;
import com.example.backend.dto.IPRegistrationRequestDTO;

import java.util.List;

public interface IPService {
    /**
     * @param owner
     * @return
     */
    List<IPDetailsDTO> getAllIPsByOwner(String owner);

    /**
     * @param id
     * @return
     */
    IPDetailsDTO getIPById(String id);

    /**
     * @param registrationDTO
     * @return
     */
    IPDetailsDTO saveIP(IPRegistrationRequestDTO registrationDTO);

    /**
     * @param id
     * @param newOwner
     * @param transactionHash
     * @return
     */
    IPDetailsDTO transferOwnership(String id, String newOwner, String transactionHash);
}