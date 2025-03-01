package com.example.backend;

import org.web3j.codegen.SolidityFunctionWrapperGenerator;

public class WrapperGenerator {
    public static void main(String[] args) throws Exception {
        String abiFilePath = "src/main/resources/contracts/SimpleStorage.abi";
        String binFilePath = "src/main/resources/contracts/SimpleStorage.bin";
        String outputDir = "src/main/java";
        String packageName = "com.example.backend.contracts";

        SolidityFunctionWrapperGenerator.main(new String[]{
            "--abiFile", abiFilePath,
            "--binFile", binFilePath,
            "--outputDir", outputDir,
            "--package", packageName
        });
    }
}
