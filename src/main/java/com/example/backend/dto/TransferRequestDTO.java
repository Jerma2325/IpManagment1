package com.example.backend.dto;

public class TransferRequestDTO {
    private String newOwnerAddress;

    public String getNewOwnerAddress() {
        return newOwnerAddress;
    }

    public void setNewOwnerAddress(String newOwnerAddress) {
        this.newOwnerAddress = newOwnerAddress;
    }
}