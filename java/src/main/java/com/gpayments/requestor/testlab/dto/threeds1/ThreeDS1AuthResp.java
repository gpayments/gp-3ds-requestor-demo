package com.gpayments.requestor.testlab.dto.threeds1;

public class ThreeDS1AuthResp {


  private String threeDSRequestorTransID;
  private String challengeUrl;


  public String getThreeDSRequestorTransID() {
    return threeDSRequestorTransID;
  }

  public void setThreeDSRequestorTransID(String threeDSRequestorTransID) {
    this.threeDSRequestorTransID = threeDSRequestorTransID;
  }

  public String getChallengeUrl() {
    return challengeUrl;
  }

  public void setChallengeUrl(String challengeUrl) {
    this.challengeUrl = challengeUrl;
  }

  @Override
  public String toString() {
    return "ThreeDS1AuthResp{" +
        "challengeUrl='" + challengeUrl + '\'' +
        '}';
  }
}
