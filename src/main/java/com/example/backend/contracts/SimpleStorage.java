package com.example.backend.contracts;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.9.6.
 */
@SuppressWarnings("rawtypes")
public class SimpleStorage extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b50610d4b806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c8063dce6320d14610046578063ecef77ce14610078578063ef5d6bbb146100a8575b600080fd5b610060600480360381019061005b91906104d6565b6100c4565b60405161006f939291906105ed565b60405180910390f35b610092600480360381019061008d919061078c565b6101f9565b60405161009f91906107f7565b60405180910390f35b6100c260048036038101906100bd9190610812565b61031b565b005b606060008060008060008681526020019081526020016000206040518060600160405290816000820180546100f890610881565b80601f016020809104026020016040519081016040528092919081815260200182805461012490610881565b80156101715780601f1061014657610100808354040283529160200191610171565b820191906000526020600020905b81548152906001019060200180831161015457829003601f168201915b505050505081526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815250509050806000015181602001518260400151935093509350509193909250565b60008083834260405160200161021193929190610957565b60405160208183030381529060405280519060200120905060405180606001604052808581526020018473ffffffffffffffffffffffffffffffffffffffff1681526020014281525060008083815260200190815260200160002060008201518160000190816102819190610b3c565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550604082015181600201559050507f5a934c29c0b71c994ee9bf608a0ad1a8f0d4298623aa1787e4e2de21b3b82a9b81858560405161030993929190610c0e565b60405180910390a18091505092915050565b3373ffffffffffffffffffffffffffffffffffffffff1660008084815260200190815260200160002060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16146103be576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103b590610cbe565b60405180910390fd5b600080600084815260200190815260200160002060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508160008085815260200190815260200160002060010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f0b659dccc8eb950324170e8d9598af5ee04ee070883eb28651a96788721fbf8383828460405161047f93929190610cde565b60405180910390a1505050565b6000604051905090565b600080fd5b600080fd5b6000819050919050565b6104b3816104a0565b81146104be57600080fd5b50565b6000813590506104d0816104aa565b92915050565b6000602082840312156104ec576104eb610496565b5b60006104fa848285016104c1565b91505092915050565b600081519050919050565b600082825260208201905092915050565b60005b8381101561053d578082015181840152602081019050610522565b60008484015250505050565b6000601f19601f8301169050919050565b600061056582610503565b61056f818561050e565b935061057f81856020860161051f565b61058881610549565b840191505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006105be82610593565b9050919050565b6105ce816105b3565b82525050565b6000819050919050565b6105e7816105d4565b82525050565b60006060820190508181036000830152610607818661055a565b905061061660208301856105c5565b61062360408301846105de565b949350505050565b600080fd5b600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b61066d82610549565b810181811067ffffffffffffffff8211171561068c5761068b610635565b5b80604052505050565b600061069f61048c565b90506106ab8282610664565b919050565b600067ffffffffffffffff8211156106cb576106ca610635565b5b6106d482610549565b9050602081019050919050565b82818337600083830152505050565b60006107036106fe846106b0565b610695565b90508281526020810184848401111561071f5761071e610630565b5b61072a8482856106e1565b509392505050565b600082601f8301126107475761074661062b565b5b81356107578482602086016106f0565b91505092915050565b610769816105b3565b811461077457600080fd5b50565b60008135905061078681610760565b92915050565b600080604083850312156107a3576107a2610496565b5b600083013567ffffffffffffffff8111156107c1576107c061049b565b5b6107cd85828601610732565b92505060206107de85828601610777565b9150509250929050565b6107f1816104a0565b82525050565b600060208201905061080c60008301846107e8565b92915050565b6000806040838503121561082957610828610496565b5b6000610837858286016104c1565b925050602061084885828601610777565b9150509250929050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b6000600282049050600182168061089957607f821691505b6020821081036108ac576108ab610852565b5b50919050565b600081905092915050565b60006108c882610503565b6108d281856108b2565b93506108e281856020860161051f565b80840191505092915050565b60008160601b9050919050565b6000610906826108ee565b9050919050565b6000610918826108fb565b9050919050565b61093061092b826105b3565b61090d565b82525050565b6000819050919050565b61095161094c826105d4565b610936565b82525050565b600061096382866108bd565b915061096f828561091f565b60148201915061097f8284610940565b602082019150819050949350505050565b60008190508160005260206000209050919050565b60006020601f8301049050919050565b600082821b905092915050565b6000600883026109f27fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff826109b5565b6109fc86836109b5565b95508019841693508086168417925050509392505050565b6000819050919050565b6000610a39610a34610a2f846105d4565b610a14565b6105d4565b9050919050565b6000819050919050565b610a5383610a1e565b610a67610a5f82610a40565b8484546109c2565b825550505050565b600090565b610a7c610a6f565b610a87818484610a4a565b505050565b5b81811015610aab57610aa0600082610a74565b600181019050610a8d565b5050565b601f821115610af057610ac181610990565b610aca846109a5565b81016020851015610ad9578190505b610aed610ae5856109a5565b830182610a8c565b50505b505050565b600082821c905092915050565b6000610b1360001984600802610af5565b1980831691505092915050565b6000610b2c8383610b02565b9150826002028217905092915050565b610b4582610503565b67ffffffffffffffff811115610b5e57610b5d610635565b5b610b688254610881565b610b73828285610aaf565b600060209050601f831160018114610ba65760008415610b94578287015190505b610b9e8582610b20565b865550610c06565b601f198416610bb486610990565b60005b82811015610bdc57848901518255600182019150602085019450602081019050610bb7565b86831015610bf95784890151610bf5601f891682610b02565b8355505b6001600288020188555050505b505050505050565b6000606082019050610c2360008301866107e8565b8181036020830152610c35818561055a565b9050610c4460408301846105c5565b949350505050565b7f4f6e6c7920746865206f776e65722063616e207472616e73666572206f776e6560008201527f7273686970000000000000000000000000000000000000000000000000000000602082015250565b6000610ca860258361050e565b9150610cb382610c4c565b604082019050919050565b60006020820190508181036000830152610cd781610c9b565b9050919050565b6000606082019050610cf360008301866107e8565b610d0060208301856105c5565b610d0d60408301846105c5565b94935050505056fea2646970667358221220e2ce72100cca3edf02deda37a109eb88961f4cf629cf43cc66d9e8073a7f831464736f6c63430008140033";

    public static final String FUNC_GETIPDETAILS = "getIPDetails";

    public static final String FUNC_REGISTERIP = "registerIP";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event IPREGISTERED_EVENT = new Event("IPRegistered", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}));
    ;

    @Deprecated
    protected SimpleStorage(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SimpleStorage(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SimpleStorage(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SimpleStorage(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<IPRegisteredEventResponse> getIPRegisteredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(IPREGISTERED_EVENT, transactionReceipt);
        ArrayList<IPRegisteredEventResponse> responses = new ArrayList<IPRegisteredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            IPRegisteredEventResponse typedResponse = new IPRegisteredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.title = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.owner = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<IPRegisteredEventResponse> iPRegisteredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, IPRegisteredEventResponse>() {
            @Override
            public IPRegisteredEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(IPREGISTERED_EVENT, log);
                IPRegisteredEventResponse typedResponse = new IPRegisteredEventResponse();
                typedResponse.log = log;
                typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.title = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.owner = (String) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<IPRegisteredEventResponse> iPRegisteredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(IPREGISTERED_EVENT));
        return iPRegisteredEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.previousOwner = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.newOwner = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.previousOwner = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.newOwner = (String) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple3<String, String, BigInteger>> getIPDetails(byte[] hash) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETIPDETAILS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(hash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<String, String, BigInteger>>(function,
                new Callable<Tuple3<String, String, BigInteger>>() {
                    @Override
                    public Tuple3<String, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> registerIP(String title, String owner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REGISTERIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(title), 
                new org.web3j.abi.datatypes.Address(160, owner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(byte[] hash, String newOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(hash), 
                new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SimpleStorage load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SimpleStorage(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SimpleStorage load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SimpleStorage(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SimpleStorage load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SimpleStorage(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SimpleStorage load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SimpleStorage(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SimpleStorage> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SimpleStorage.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SimpleStorage> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SimpleStorage.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<SimpleStorage> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SimpleStorage.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SimpleStorage> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SimpleStorage.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class IPRegisteredEventResponse extends BaseEventResponse {
        public byte[] hash;

        public String title;

        public String owner;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public byte[] hash;

        public String previousOwner;

        public String newOwner;
    }
}
