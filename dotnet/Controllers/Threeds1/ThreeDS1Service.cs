using GPayments.Requestor.TestLab.Helpers;
using GPayments.Requestor.TestLab.Models.dto.Threeds1;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Web;

namespace GPayments.Requestor.TestLab.Controllers.Threeds1
{
    /**
	 * 3DSecure 1.0.2 service class
	 *
	 * @author GPayments
	 */
    public class ThreeDS1Service
    {
        private static readonly log4net.ILog logger = log4net.LogManager.GetLogger(typeof(ThreeDS1Service));
        private string authUrl;

        public ThreeDS1Service()
        {
            this.authUrl = Config.AsAuthUrl + "/api/v2/auth/3ds1";
        }

        public ThreeDS1AuthResp HandleAuthRequest(ThreeDS1AuthReq request)
        {
            //generate the transaction id, this is optional.
            request.threeDSRequestorTransID = Guid.NewGuid().ToString();

            logger.Info(string.Format("sending 3ds1 auth request to ActiveServer: {0}", authUrl));

            ThreeDS1AuthResp response = (ThreeDS1AuthResp)RestClientHelper.PostForObject(authUrl, request, typeof(ThreeDS1AuthResp));

            logger.Info(string.Format("server returns ok, content: {0}", response));

            return response;
        }
    }
}