using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace GPayments.Requestor.TestLab.Models.dto.Threeds1
{
    public class ThreeDS1AuthResp
    {
        public String threeDSRequestorTransID;
        public String challengeUrl;

        public override string ToString()
        {
            return "ThreeDS1AuthResp{" +
                "challengeUrl='" + challengeUrl + '\'' +
                '}';
        }
    }
}