class TestClass {
  public static final org.web3j.abi.datatypes.Event TRANSFER_EVENT = new org.web3j.abi.datatypes.Event("Transfer", 
      java.util.Arrays.<org.web3j.abi.TypeReference<?>>asList(new org.web3j.abi.TypeReference<SomeStruct>(true) {}));
  ;

  public static java.util.List<TransferEventResponse> getTransferEvents(
      org.web3j.protocol.core.methods.response.TransactionReceipt transactionReceipt) {
    java.util.List<org.web3j.tx.Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
    java.util.ArrayList<TransferEventResponse> responses = new java.util.ArrayList<TransferEventResponse>(valueList.size());
    for (org.web3j.tx.Contract.EventValuesWithLog eventValues : valueList) {
      TransferEventResponse typedResponse = new TransferEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.myStruct = (byte[]) eventValues.getIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public static TransferEventResponse getTransferEventFromLog(
      org.web3j.protocol.core.methods.response.Log log) {
    org.web3j.tx.Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
    TransferEventResponse typedResponse = new TransferEventResponse();
    typedResponse.log = log;
    typedResponse.myStruct = (byte[]) eventValues.getIndexedValues().get(0).getValue();
    return typedResponse;
  }

  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(
      org.web3j.protocol.core.methods.request.EthFilter filter) {
    return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
  }

  public io.reactivex.Flowable<TransferEventResponse> transferEventFlowable(
      org.web3j.protocol.core.DefaultBlockParameter startBlock,
      org.web3j.protocol.core.DefaultBlockParameter endBlock) {
    org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(org.web3j.abi.EventEncoder.encode(TRANSFER_EVENT));
    return transferEventFlowable(filter);
  }

  public static class TransferEventResponse extends org.web3j.protocol.core.methods.response.BaseEventResponse {
    public byte[] myStruct;
  }
}
