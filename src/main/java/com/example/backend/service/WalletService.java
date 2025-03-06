package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private UserRepository userRepository;

    public String generateWallet(String username, String password) throws Exception {
        ECKeyPair keyPair = Keys.createEcKeyPair();
        BigInteger privateKeyInDec = keyPair.getPrivateKey();

        String address = Credentials.create(keyPair).getAddress();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new Exception("User not found");
        }

        User user = userOpt.get();

        String salt = generateRandomSalt();
        String encryptedPrivateKey = encryptPrivateKey(privateKeyInDec.toString(16), password, salt);

        user.setEthAddress(address);
        user.setEncryptedPrivateKey(encryptedPrivateKey);
        user.setKeySalt(salt);
        userRepository.save(user);

        return address;
    }

    public String importWallet(String username, String privateKey, String password) throws Exception {
        if (!privateKey.startsWith("0x")) {
            privateKey = "0x" + privateKey;
        }

        try {
            Credentials credentials = Credentials.create(privateKey);
            String address = credentials.getAddress();

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                throw new Exception("User not found");
            }

            User user = userOpt.get();

            String salt = generateRandomSalt();
            String encryptedPrivateKey = encryptPrivateKey(privateKey.substring(2), password, salt);

            user.setEthAddress(address);
            user.setEncryptedPrivateKey(encryptedPrivateKey);
            user.setKeySalt(salt);
            userRepository.save(user);

            return address;
        } catch (Exception e) {
            throw new Exception("Invalid private key: " + e.getMessage());
        }
    }

    public Credentials getCredentials(String username, String password) throws Exception {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new Exception("User not found");
        }

        User user = userOpt.get();

        if (user.getEncryptedPrivateKey() == null || user.getKeySalt() == null) {
            throw new Exception("User has no wallet");
        }

        try {
            String decryptedPrivateKey = decryptPrivateKey(user.getEncryptedPrivateKey(), password, user.getKeySalt());
            return Credentials.create("0x" + decryptedPrivateKey);
        } catch (Exception e) {
            throw new Exception("Failed to decrypt private key: " + e.getMessage());
        }
    }

    private String generateRandomSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String encryptPrivateKey(String privateKey, String password, String salt) throws Exception {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(privateKey.getBytes());

        byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedIVAndText);
    }

    private String decryptPrivateKey(String encryptedPrivateKey, String password, String salt) throws Exception {
        byte[] encryptedIVAndText = Base64.getDecoder().decode(encryptedPrivateKey);

        byte[] iv = new byte[16];
        System.arraycopy(encryptedIVAndText, 0, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        byte[] encrypted = new byte[encryptedIVAndText.length - iv.length];
        System.arraycopy(encryptedIVAndText, iv.length, encrypted, 0, encrypted.length);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }
}