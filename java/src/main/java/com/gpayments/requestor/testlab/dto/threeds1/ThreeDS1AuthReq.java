package com.gpayments.requestor.testlab.dto.threeds1;

public class ThreeDS1AuthReq {

  private String acctNumber; // Length: Variable, 13–19 characters

  private String cardExpiryDate;

  private String threeDSRequestorTransID;

  private String merchantName;

  private String purchaseAmount;

  private String purchaseCurrency;


  private String purchaseDesc;


  private String recurringFrequency;


  private String recurringExpiry;


  private String callbackUrl;

  public String getThreeDSRequestorTransID() {
    return threeDSRequestorTransID;
  }

  public void setThreeDSRequestorTransID(String threeDSRequestorTransID) {
    this.threeDSRequestorTransID = threeDSRequestorTransID;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  public String getPurchaseAmount() {
    return purchaseAmount;
  }

  public void setPurchaseAmount(String purchaseAmount) {
    this.purchaseAmount = purchaseAmount;
  }

  public String getPurchaseCurrency() {
    return purchaseCurrency;
  }

  public void setPurchaseCurrency(String purchaseCurrency) {
    this.purchaseCurrency = purchaseCurrency;
  }

  public String getPurchaseDesc() {
    return purchaseDesc;
  }

  public void setPurchaseDesc(String purchaseDesc) {
    this.purchaseDesc = purchaseDesc;
  }

  public String getRecurringFrequency() {
    return recurringFrequency;
  }

  public void setRecurringFrequency(String recurringFrequency) {
    this.recurringFrequency = recurringFrequency;
  }

  public String getRecurringExpiry() {
    return recurringExpiry;
  }

  public void setRecurringExpiry(String recurringExpiry) {
    this.recurringExpiry = recurringExpiry;
  }

  public String getCardExpiryDate() {
    return cardExpiryDate;
  }

  public void setCardExpiryDate(String cardExpiryDate) {
    this.cardExpiryDate = cardExpiryDate;
  }

  public String getAcctNumber() {
    return acctNumber;
  }

  public void setAcctNumber(String acctNumber) {
    this.acctNumber = acctNumber;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  @Override
  public String toString() {
    return "ThreeDS1AuthReq{" +
        "acctNumber=XXXXXXXXXXXXX'" +
        ", cardExpiryDate=XXXX'" +
        ", threeDSRequestorTransId='" + threeDSRequestorTransID + '\'' +
        ", merchantName='" + merchantName + '\'' +
        ", purchaseAmount='" + purchaseAmount + '\'' +
        ", purchaseCurrency='" + purchaseCurrency + '\'' +
        ", purchaseDesc='" + purchaseDesc + '\'' +
        ", recurringFrequency='" + recurringFrequency + '\'' +
        ", recurringExpiry='" + recurringExpiry + '\'' +
        ", callbackUrl='" + callbackUrl + '\'' +
        '}';
  }
}
