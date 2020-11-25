/*
 *  Copyright (C) GPayments Pty Ltd - All Rights Reserved
 *  Copying of this file, via any medium, is subject to the
 *  ActiveServer End User License Agreement (EULA)
 *
 *  Proprietary code for use in conjunction with GPayments products only
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Written by GPayments <techsupport@gpayments.com>, 2020
 *
 *
 */

package com.gpayments.requestor.testlab.dto;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthDataNoScriptDTO {

  private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(
      Include.NON_EMPTY);

  private String acctNumber;
  private String merchantId;
  private String authenticationInd;
  private String purchaseAmount;
  private String purchaseCurrency;
  private String messageCategory;
  private String purchaseDate;
  private String messageVersion;
  private String threeDSRequestorDecReqInd;
  private String threeDSRequestorDecMaxTime;

  public String getAcctNumber() {
    return acctNumber;
  }

  public void setAcctNumber(String acctNumber) {
    this.acctNumber = acctNumber;
  }

  public String getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

  public String getAuthenticationInd() {
    return authenticationInd;
  }

  public void setAuthenticationInd(String authenticationInd) {
    this.authenticationInd = authenticationInd;
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

  public String getMessageCategory() {
    return messageCategory;
  }

  public void setMessageCategory(String messageCategory) {
    this.messageCategory = messageCategory;
  }

  public String getPurchaseDate() {
    return purchaseDate;
  }

  public void setPurchaseDate(String purchaseDate) {
    this.purchaseDate = purchaseDate;
  }

  public String getMessageVersion() {
    return messageVersion;
  }

  public void setMessageVersion(String messageVersion) {
    this.messageVersion = messageVersion;
  }

  public String getThreeDSRequestorDecReqInd() {
    return threeDSRequestorDecReqInd;
  }

  public void setThreeDSRequestorDecReqInd(String threeDSRequestorDecReqInd) {
    this.threeDSRequestorDecReqInd = threeDSRequestorDecReqInd;
  }

  public String getThreeDSRequestorDecMaxTime() {
    return threeDSRequestorDecMaxTime;
  }

  public void setThreeDSRequestorDecMaxTime(String threeDSRequestorDecMaxTime) {
    this.threeDSRequestorDecMaxTime = threeDSRequestorDecMaxTime;
  }

  public Message toMessage() {
    return objectMapper.convertValue(this, Message.class);
  }

  @Override
  public String toString() {
    return "InitAuthNoScriptDTO{" +
        "acctNumber='" + acctNumber + '\'' +
        ", merchantId='" + merchantId + '\'' +
        ", authenticationInd='" + authenticationInd + '\'' +
        ", purchaseAmount='" + purchaseAmount + '\'' +
        ", purchaseCurrency='" + purchaseCurrency + '\'' +
        ", messageCategory='" + messageCategory + '\'' +
        ", purchaseDate='" + purchaseDate + '\'' +
        ", messageVersion='" + messageVersion + '\'' +
        ", threeDSRequestorDecReqInd='" + threeDSRequestorDecReqInd + '\'' +
        ", threeDSRequestorDecMaxTime='" + threeDSRequestorDecMaxTime + '\'' +
        '}';
  }
}
