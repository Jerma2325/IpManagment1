package com.example.backend.service;

import com.example.backend.contracts.SimpleStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BlockchainService {

    private final SimpleStorage contract;
    private final Web3j web3j;

    public BlockchainService(
            @Value("${blockchain.rpc.url}") String rpcUrl,
            @Value("${blockchain.contract.address}") String contractAddress,
            @Value("${blockchain.owner.privateKey}") String privateKey
    ) {
        try {
            this.web3j = Web3j.build(new HttpService(rpcUrl));

            Credentials credentials = Credentials.create(privateKey);
            ContractGasProvider gasProvider = new DefaultGasProvider();

            this.contract = SimpleStorage.load(contractAddress, web3j, credentials, gasProvider);
            System.out.println("Using account: " + credentials.getAddress());
        } catch (Exception e) {
            System.err.println("Error initializing blockchain service: " + e.getMessage());
            throw new RuntimeException("Failed to initialize blockchain service", e);
        }
    }
    public String registerIP(String name, String fileName, String owner) {
        try {
            var transactionReceipt = contract.registerIP(name).send();

            System.out.println("Transaction Receipt: " + transactionReceipt.toString());

            List<SimpleStorage.IPRegisteredEventResponse> events = contract.getIPRegisteredEvents(transactionReceipt);

            if (events.isEmpty()) {
                System.out.println("No events found in the transaction receipt.");
                return transactionReceipt.getTransactionHash();
            }

            String hash = Numeric.toHexString(events.get(0).hash);
            return hash;
        } catch (Exception e) {
            throw new RuntimeException("Error registering IP on blockchain: " + e.getMessage(), e);
        }
    }


    public String transferIP(String ipId, String newOwnerAddress) {
        try {
            byte[] hashBytes;

            if (ipId.startsWith("0x")) {
                hashBytes = Numeric.hexStringToByteArray(ipId);
            } else {

                throw new IllegalArgumentException("IP ID must be a valid hex hash");
            }

            var transactionReceipt = contract.transferOwnership(hashBytes, newOwnerAddress).send();
            return transactionReceipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("Error transferring IP ownership: " + e.getMessage(), e);
        }
    }


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