package com.example.backend.service;

import com.example.backend.contracts.SimpleStorage;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class BlockchainService {

    private final Web3j web3j;
    private final String contractAddress;
    private final Credentials adminCredentials;
    private final ContractGasProvider gasProvider;
    private  String userAddress;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletService walletService;

    public BlockchainService(
            @Value("${blockchain.rpc.url}") String rpcUrl,
            @Value("${blockchain.contract.address}") String contractAddress,
            @Value("${blockchain.owner.privateKey}") String privateKey

    ) {
        try {
            this.web3j = Web3j.build(new HttpService(rpcUrl));
            this.contractAddress = contractAddress;
            this.adminCredentials = Credentials.create(privateKey);
            this.gasProvider = new DefaultGasProvider();

            System.out.println("BlockchainService initialized with admin account: " + adminCredentials.getAddress());
        } catch (Exception e) {
            System.err.println("Error initializing blockchain service: " + e.getMessage());
            throw new RuntimeException("Failed to initialize blockchain service", e);
        }
    }

    public String registerIP(String name, String fileName, String address) {
        userAddress = address;
        try {
            SimpleStorage contract = SimpleStorage.load(
                    userAddress,
                    web3j,
                    adminCredentials,
                    gasProvider
            );

            var transactionReceipt = contract.registerIP(name, userAddress).send();
            return transactionReceipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("Error registering IP on blockchain: " + e.getMessage(), e);
        }
    }





    public String transferIP(String ipId, String newOwnerAddress) {
        try {
            SimpleStorage contract = SimpleStorage.load(
                    userAddress,
                    web3j,
                    adminCredentials,
                    gasProvider
            );

            byte[] hashBytes;
            if (ipId.startsWith("0x")) {
                hashBytes = Numeric.hexStringToByteArray(ipId);
            } else {
                byte[] originalBytes = ipId.getBytes(StandardCharsets.UTF_8);
                hashBytes = Hash.sha3(originalBytes);
            }

            var transactionReceipt = contract.transferOwnership(hashBytes, newOwnerAddress).send();
            return transactionReceipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("Error transferring IP ownership: " + e.getMessage(), e);
        }
    }



    public String getIPDetails(String hash) {
        try {
            SimpleStorage contract = SimpleStorage.load(
                    contractAddress,
                    web3j,
                    adminCredentials,
                    gasProvider
            );

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

    public String getTransactionSenderAddress() {
        return adminCredentials.getAddress();
    }
}