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

var apiVersions = ["v1", "v2"];

var defaultAPIVersion = "v2";

var transStatusReasonDesc = {
  "01": "Card authentication failed",
  "02": "Unknown device",
  "03": "Unsupported device",
  "04": "Exceeds authentication frequency limit",
  "05": "Expired card",
  "06": "Invalid card number",
  "07": "Invalid transaction",
  "08": "No card record",
  "09": "Security failure",
  "10": "Stolen card",
  "11": "Suspected fraud",
  "12": "Transaction not permitted to cardholder",
  "13": "Cardholder not enrolled in service",
  "14": "Transaction timed out at the ACS",
  "15": "Low confidence",
  "16": "Medium confidence"
};

function formatTransStatusReason(transStatusReason) {
  if (transStatusReasonDesc.hasOwnProperty(transStatusReason)) {
    return transStatusReason + " (" + transStatusReasonDesc[transStatusReason]
        + ")";
  } else {
    console.log("transStatusReason %s not in the list", transStatusReason);
    return transStatusReason;
  }
}

function showAdvancedDetails(toShow) {
  var advancedDiv = document.getElementById("advanced-details");

  if (toShow) {
    makeShipAddrSameAsBillAddr();
    advancedDiv.classList.remove("d-none");
  } else {
    makeShipAddrSameAsBillAddr();
    advancedDiv.classList.add("d-none");
  }
}

function makeShipAddrSameAsBillAddr() {
  $("#shipAddrLine1").val($("#billAddrLine1").val());
  $("#shipAddrLine2").val($("#billAddrLine2").val());
  $("#shipAddrLine3").val($("#billAddrLine3").val());
  $("#shipAddrCity").val($("#billAddrCity").val());
  $("#shipAddrState").val($("#billAddrState").val());
  $("#shipAddrPostCode").val($("#billAddrPostCode").val());
  $("#shipAddrCountry").val($("#billAddrCountry").val());
}

function getCardHolderInfo(channel) {

  var cardHolderInfo = {};

  var shipAddrSameCheck = document.getElementById("shipping-radio-yes");

  if (shipAddrSameCheck.checked) {
    makeShipAddrSameAsBillAddr();
    channel !== "3ri" ? cardHolderInfo.addrMatch = "Y" : null;
  } else {
    channel !== "3ri" ? cardHolderInfo.addrMatch = "N" : null;
  }

  var billAddrLine1 = $('#billAddrLine1').val();
  if (billAddrLine1) {
    cardHolderInfo.billAddrLine1 = billAddrLine1;
  }

  var billAddrLine2 = $('#billAddrLine2').val();
  if (billAddrLine2) {
    cardHolderInfo.billAddrLine2 = billAddrLine2;
  }

  var billAddrLine3 = $('#billAddrLine3').val();
  if (billAddrLine3) {
    cardHolderInfo.billAddrLine3 = billAddrLine3;
  }

  var billAddrCity = $('#billAddrCity').val();
  if (billAddrCity) {
    cardHolderInfo.billAddrCity = billAddrCity;
  }

  var billAddrState = $('#billAddrState').val();
  if (billAddrState) {
    cardHolderInfo.billAddrState = billAddrState
  }

  var billAddrCountry = $('#billAddrCountry').val();
  if (billAddrCountry) {
    cardHolderInfo.billAddrCountry = billAddrCountry;
  }

  var billAddrPostCode = $('#billAddrPostCode').val();
  if (billAddrPostCode) {
    cardHolderInfo.billAddrPostCode = billAddrPostCode;
  }

  var shipAddrLine1 = $('#shipAddrLine1').val();
  if (shipAddrLine1) {
    cardHolderInfo.shipAddrLine1 = shipAddrLine1;
  }

  var shipAddrLine2 = $('#shipAddrLine2').val();
  if (shipAddrLine2) {
    cardHolderInfo.shipAddrLine2 = shipAddrLine2;
  }

  var shipAddrLine3 = $('#shipAddrLine3').val();
  if (shipAddrLine3) {
    cardHolderInfo.shipAddrLine3 = shipAddrLine3;
  }

  var shipAddrCity = $('#shipAddrCity').val();
  if (shipAddrCity) {
    cardHolderInfo.shipAddrCity = shipAddrCity;
  }

  var shipAddrState = $('#shipAddrState').val();
  if (shipAddrState) {
    cardHolderInfo.shipAddrState = shipAddrState;
  }

  var shipAddrCountry = $('#shipAddrCountry').val();
  if (shipAddrCountry) {
    cardHolderInfo.shipAddrCountry = shipAddrCountry;
  }

  var shipAddrPostCode = $('#shipAddrPostCode').val();
  if (shipAddrPostCode) {
    cardHolderInfo.shipAddrPostCode = shipAddrPostCode;
  }

  var cardholderName = $('#cardholderName').val();
  if (cardholderName) {
    cardHolderInfo.cardholderName = cardholderName;
  }

  var mobileNumberSubscriber = $('#mobileNumberSubscriber').val();
  var mobileNumberCountryCode = $('#mobileNumberCountryCode').val();
  if (mobileNumberSubscriber && mobileNumberCountryCode) {
    cardHolderInfo.mobilePhone = {
      subscriber: mobileNumberSubscriber,
      cc: mobileNumberCountryCode
    };
  }

  var homeNumberSubscriber = $('#homeNumberSubscriber').val();
  var homeNumberCountryCode = $('#homeNumberCountryCode').val();
  if (homeNumberSubscriber && homeNumberCountryCode) {
    cardHolderInfo.homePhone = {
      subscriber: homeNumberSubscriber,
      cc: homeNumberCountryCode
    };
  }

  var workNumberSubscriber = $('#workNumberSubscriber').val();
  var workNumberCountryCode = $('#workNumberCountryCode').val()
  if (workNumberSubscriber && workNumberCountryCode) {
    cardHolderInfo.workPhone = {
      subscriber: workNumberSubscriber,
      cc: workNumberCountryCode
    };
  }

  var email = $('#email').val();
  if (email) {
    cardHolderInfo.email = email;
  }
  return $.isEmptyObject(cardHolderInfo) ? null : cardHolderInfo;
}

function getAcctInfo() {

  var acctInfo = {};

  var chAccAgeInd = $('#chAccAgeInd').val();
  if (chAccAgeInd) {
    acctInfo.chAccAgeInd = chAccAgeInd;
  }

  var chAccChange = $('#chAccChange').val();
  if (chAccChange) {
    acctInfo.chAccChange = chAccChange;
  }

  var chAccChangeInd = $('#chAccChangeInd').val();
  if (chAccChangeInd) {
    acctInfo.chAccChangeInd = chAccChangeInd;
  }

  var chAccDate = $('#chAccDate').val();
  if (chAccDate) {
    acctInfo.chAccDate = chAccDate;
  }

  var chAccPwChange = $('#chAccPwChange').val();
  if (chAccPwChange) {
    acctInfo.chAccPwChange = chAccPwChange;
  }

  var chAccPwChangeInd = $('#chAccPwChangeInd').val();
  if (chAccPwChangeInd) {
    acctInfo.chAccPwChangeInd = chAccPwChangeInd;
  }

  var nbPurchaseAccount = $('#nbPurchaseAccount').val();
  if (nbPurchaseAccount) {
    acctInfo.nbPurchaseAccount = nbPurchaseAccount;
  }

  var paymentAccAge = $('#paymentAccAge').val();
  if (paymentAccAge) {
    acctInfo.paymentAccAge = paymentAccAge;
  }

  var paymentAccInd = $('#paymentAccInd').val();
  if (paymentAccInd) {
    acctInfo.paymentAccInd = paymentAccInd;
  }

  var provisionAttemptsDay = $('#provisionAttemptsDay').val();
  if (provisionAttemptsDay) {
    acctInfo.provisionAttemptsDay = provisionAttemptsDay;
  }

  var shipAddressUsage = $('#shipAddressUsage').val();
  if (shipAddressUsage) {
    acctInfo.shipAddressUsage = shipAddressUsage;
  }

  var shipAddressUsageInd = $('#shipAddressUsageInd').val();
  if (shipAddressUsageInd) {
    acctInfo.shipAddressUsageInd = shipAddressUsageInd;
  }

  var shipNameIndicator = $('#shipNameIndicator').val();
  if (shipNameIndicator) {
    acctInfo.shipNameIndicator = shipNameIndicator;
  }

  var suspiciousAccActivity = $('#suspiciousAccActivity').val();
  if (suspiciousAccActivity) {
    acctInfo.suspiciousAccActivity = suspiciousAccActivity;
  }

  var txnActivityDay = $('#txnActivityDay').val();
  if (txnActivityDay) {
    acctInfo.txnActivityDay = txnActivityDay;
  }

  var txnActivityYear = $('#txnActivityYear').val();
  if (txnActivityYear) {
    acctInfo.txnActivityYear = txnActivityYear;
  }

  return $.isEmptyObject(acctInfo) ? null : acctInfo;
}

function getMerchantRiskIndicator() {
  var merchantRiskIndicator = {};

  var deliveryEmailAddress = $('#deliveryEmailAddress').val();
  if (deliveryEmailAddress) {
    merchantRiskIndicator.deliveryEmailAddress = deliveryEmailAddress;
  }

  var deliveryTimeframe = $('#deliveryTimeframe').val();
  if (deliveryTimeframe) {
    merchantRiskIndicator.deliveryTimeframe = deliveryTimeframe;
  }

  var giftCardAmount = $('#giftCardAmount').val();
  if (giftCardAmount) {
    merchantRiskIndicator.giftCardAmount = giftCardAmount;
  }

  var giftCardCount = $('#giftCardCount').val();
  if (giftCardCount) {
    merchantRiskIndicator.giftCardCount = giftCardCount;
  }

  var giftCardCurr = $('#giftCardCurr').val();
  if (giftCardCurr) {
    merchantRiskIndicator.giftCardCurr = giftCardCurr;
  }

  var preOrderDate = $('#preOrderDate').val();
  if (preOrderDate) {
    merchantRiskIndicator.preOrderDate = preOrderDate
  }

  var preOrderPurchaseInd = $('#preOrderPurchaseInd').val();
  if (preOrderPurchaseInd) {
    merchantRiskIndicator.preOrderPurchaseInd = preOrderPurchaseInd;
  }

  var reorderItemsInd = $('#reorderItemsInd').val();
  if (reorderItemsInd) {
    merchantRiskIndicator.reorderItemsInd = reorderItemsInd;
  }

  var shipIndicator = $('#shipIndicator').val();
  if (shipIndicator) {
    merchantRiskIndicator.shipIndicator = shipIndicator;
  }
  return $.isEmptyObject(merchantRiskIndicator) ? null : merchantRiskIndicator;
}

function getAuthenticationInfo() {

  var authenticationInfo = {};

  var threeDSReqAuthData = $('#threeDSReqAuthData').val();
  if (threeDSReqAuthData) {
    authenticationInfo.threeDSReqAuthData = threeDSReqAuthData;
  }

  var threeDSReqAuthMethod = $('#threeDSReqAuthMethod').val();
  if (threeDSReqAuthMethod) {
    authenticationInfo.threeDSReqAuthMethod = threeDSReqAuthMethod;
  }

  var threeDSReqAuthTimestamp = $('#threeDSReqAuthTimestamp').val();
  if (threeDSReqAuthTimestamp) {
    authenticationInfo.threeDSReqAuthTimestamp = threeDSReqAuthTimestamp;
  }
  return $.isEmptyObject(authenticationInfo) ? null : authenticationInfo;
}

/**
 * Change Back Button to go to relative pages
 * @param type
 */
function updateBackButton(type) {
  var backButton = $('#backButton');
  switch (type) {
    case "toShop":
      backButton.text("Back to Shop").attr("href", "/shop");
      break;

    case "toBrw":
      backButton.text("Back to BRW").attr("href", "/brw");
      break;

    case "toEnrol":
      backButton.text("Back to Enrol").attr("href", "/enrol");
      break;

    case "to3ri":
      backButton.text("Back to 3RI").attr("href", "/3ri");
      break;

    default:
      backButton.text("Back").attr("href", "/");
      break;
  }
}

/**
 * Show card logo in the spinner according to the card number
 * @param cardNumber
 */
function showCardLogo(cardNumber) {
  var cardLogo = $("#cardLogo");
  cardLogo.removeClass('d-none');
  switch (cc_brand_id(cardNumber)) {
    case "Visa":
      cardLogo.attr("src", "/images/visa-logo.png");
      break;
    case "Mastercard":
      cardLogo.attr("src", "/images/mastercard-logo.png");
      break;
    case "JCB":
      cardLogo.attr("src", "/images/jcb-logo.png");
      break;
    case "AMEX":
      cardLogo.attr("src", "/images/amex-logo.png");
      break;
    case "Discover":
      cardLogo.attr("src", "/images/discover-logo.png");
      break;
    default:
      cardLogo.attr("src", "");
      break;
  }
}

function changeApiVersion(apiVersion, buttonId, buttonText) {
  setApiVersion(apiVersion);
  updateActionButtonText(apiVersion, $("#" + buttonId), buttonText);
  $("#apiVersionForm").val(apiVersion);
}

function setApiVersion(apiVersion) {
  console.log("change apiVersion to ", apiVersion);
  if (apiVersions.indexOf(apiVersion) > -1) {
    sessionStorage.setItem("apiVersion", apiVersion);
  } else {
    console.error("Invalid apiVersion", apiVersion);
  }
}

function updateActionButtonText(apiVersion, button, buttonPrefix) {
  button.text(buttonPrefix + " (" + apiVersion + ")");
}

function getApiVersion() {
  var apiVersion = sessionStorage.getItem("apiVersion");
  if (apiVersion) {
    console.log("apiVersion is ", apiVersion);
    return apiVersion;
  } else {
    console.log("apiVersion is null, use default version ", defaultAPIVersion);
    return defaultAPIVersion;
  }
}

function showActionButton(buttonId, buttonText, onclickFnName) {
  var apiVersion = getApiVersion();
  var buttonGroup = $('#do3DS');
  buttonGroup.empty();

  var dropDownSelect = "";
  apiVersions.forEach(function (item) {
    dropDownSelect = dropDownSelect +
        "<a class=\"dropdown-item\" href=\"#\" onclick=\"changeApiVersion('"
        + item + "','" + buttonId
        + "','" + buttonText + "')\">with API " + item + "</a>";
  });

  buttonGroup.append(
      "<button type=\"button\" class=\"btn btn-primary btn-lg\" id=\""
      + buttonId
      + "\"\n"
      + "                onclick=\"" + onclickFnName + "()\">" + buttonText
      + " ("
      + apiVersion + ")\n"
      + "        </button>\n"
      + "        <button type=\"button\" class=\"btn btn-primary dropdown-toggle dropdown-toggle-split\"\n"
      + "                data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">\n"
      + "          <span class=\"sr-only\">Toggle Dropdown</span>\n"
      + "        </button>"
      + "<div class=\"dropdown-menu dropdown-menu-right\">\n"
      + dropDownSelect
      + "</div>"
  );

}
