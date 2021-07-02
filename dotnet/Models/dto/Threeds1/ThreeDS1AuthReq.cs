using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace GPayments.Requestor.TestLab.Models.dto.Threeds1
{
    public class ThreeDS1AuthReq
    {
        public String acctNumber; // Length: Variable, 13–19 characters
        public String cardExpiryDate;
        public String threeDSRequestorTransID;
        public String merchantName;
        public String purchaseAmount;
        public String purchaseCurrency;
        public String purchaseDesc;
        public String recurringFrequency;
        public String recurringExpiry;
        public String callbackUrl;

        public override string ToString()
        {
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
}