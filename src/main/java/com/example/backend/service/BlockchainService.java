package com.example.backend.service;

import com.example.backend.contracts.SimpleStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.util.List;

@Service
public class BlockchainService {

    private final SimpleStorage contract;

    public BlockchainService(
            Web3j web3j,
            @Value("${blockchain.contract.address}") String contractAddress,
            @Value("${blockchain.owner.privateKey}") String privateKey
    ) {
        // load the contract using credentials and RPC URL
        Credentials credentials = Credentials.create(privateKey);
        ContractGasProvider gasProvider = new DefaultGasProvider();

        this.contract = SimpleStorage.load(contractAddress, web3j, credentials, gasProvider);
        System.out.println("Using account: " + credentials.getAddress());
    }

    /**
     * Register a new IP on the blockchain
     *
     * @param name The name or title of the IP
     * @param fileName The stored file name
     * @param owner The username of the owner
     * @return The transaction hash
     */
    public String registerIP(String name, String fileName, String owner) {
        try {
            // Call the smart contract to register the IP
            var transactionReceipt = contract.registerIP(name).send();

            // Log the transaction receipt for debugging
            System.out.println("Transaction Receipt: " + transactionReceipt.toString());

            // Extract the event from the receipt
            List<SimpleStorage.IPRegisteredEventResponse> events = contract.getIPRegisteredEvents(transactionReceipt);

            // Debug: Print events found
            if (events.isEmpty()) {
                System.out.println("No events found in the transaction receipt.");
                return transactionReceipt.getTransactionHash();
            }

            // Extract and return the hash
            String hash = Numeric.toHexString(events.get(0).hash);
            return hash;
        } catch (Exception e) {
            throw new RuntimeException("Error registering IP on blockchain: " + e.getMessage(), e);
        }
    }

    /**
     * Transfer ownership of an IP
     *
     * @param ipId The ID of the IP to transfer
     * @param newOwnerAddress The address of the new owner
     * @return The transaction hash
     */
    public String transferIP(String ipId, String newOwnerAddress) {
        try {
            // In a real implementation, you would get the hash from the database using ipId
            // For now, we'll assume ipId is the hash for simplicity
            byte[] hashBytes;

            // Check if it's already in byte array format or needs conversion
            if (ipId.startsWith("0x")) {
                hashBytes = Numeric.hexStringToByteArray(ipId);
            } else {
                // If not a hex string, we might need another way to identify the IP
                // This is a placeholder - you should replace with actual logic
                throw new IllegalArgumentException("IP ID must be a valid hex hash");
            }

            var transactionReceipt = contract.transferOwnership(hashBytes, newOwnerAddress).send();
            return transactionReceipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("Error transferring IP ownership: " + e.getMessage(), e);
        }
    }

    /**
     * Get details of an IP from the blockchain
     *
     * @param hash The hash of the IP
     * @return A formatted string with IP details
     */
    public String getIPDetails(String hash) {
        try {
            byte[] hashBytes = Numeric.hexStringToByteArray(hash);
            var details = contract.getIPDetails(hashBytes).send();
            return String.format(
                    "Title: %s, Owner: %s, Timestamp: %s",
                    details.component1(), details.component2(), details.component3()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error fetching IP details: " + e.getMessage(), e);
        }
    }
}