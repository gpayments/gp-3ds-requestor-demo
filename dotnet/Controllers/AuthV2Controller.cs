/************************************************************************
* Copyright(C) GPayments Pty Ltd - All Rights Reserved
* Copying of this file, via any medium, is subject to the
* ActiveServer End User License Agreement (EULA)
*
* Proprietary code for use in conjunction with GPayments products only
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Written by GPayments<techsupport@gpayments.com>, 2020
************************************************************************/

using GPayments.Requestor.TestLab.Helpers;
using GPayments.Requestor.TestLab.Models.dto;
using System;
using System.Web;
using System.Web.Http;

namespace GPayments.Requestor.TestLab.Controllers
{
    public class AuthV2Controller : ApiController
    {
        private static log4net.ILog logger = log4net.LogManager.GetLogger(typeof(AuthV2Controller));

        /// <summary>
        /// Receives the initialise authentication request from the 3DS-web-adapter (Step 2)
        /// Send data to ActiveServer to Initialise Authentication
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost, Route("v2/auth/init")]
        public Message initAuth([FromBody]Message request)
        {
            //Generate requestor trans ID
            string transId = Guid.NewGuid().ToString();
            request["threeDSRequestorTransID"] = transId;
            //Fill the event call back url with requestor url + /3ds-notify
            string callBackUrl = Config.BaseUrl + "/3ds-notify";
            request["eventCallbackUrl"] = callBackUrl;

            //ActiveServer url for Initialise Authentication
            string initAuthUrl = Config.AsAuthUrl + "/api/v2/auth/brw/init";
            logger.Info(string.Format("initAuthRequest on url: {0}, body: \n{1}", initAuthUrl, request));

            //Send data to ActiveServer to Initialise authentication (Step 3)
            //Get the response data from ActiveServer (Step 4)
            Message response = (Message)RestClientHelper.PostForObject(initAuthUrl, request, typeof(Message));
            logger.Info(string.Format("initAuthResponseBRW: \n{0}", response));

            if (response != null)
                //save initAuth response into session storage
                HttpContext.Current.Session[(String)response["threeDSServerTransID"]] = response;
            else
                logger.Error("Error in initAuth response");

            //Return data to 3ds-web-adapter (Step 5)
            return response;
        }

        /// <summary>
        /// Receives the Execute authentication request from the 3DS-web-adapter(Step 9) Send data to
        /// ActiveServer to Execute Authentication
        /// </summary>
        /// <param name="trans"></param>
        /// <returns></returns>
        [HttpPost, Route("v2/auth")]
        public Message auth([FromBody]Message request)
        {
            //get authUrl from session storage
            Message initAuthResponse = (Message)HttpContext.Current.Session[(String)request["threeDSServerTransID"]];
            String authUrl = (String)initAuthResponse["authUrl"];
            logger.Info(string.Format("requesting BRW Auth API {0}, body: \n{1}", authUrl, request));

            //Send data to ActiveServer to Execute Authentication (Step 10)
            //Get the response data from ActiveServer (Step 12)
            Message response = (Message)RestClientHelper.PostForObject(authUrl, request, typeof(Message));
            logger.Info(string.Format("authResponseBRW: \n{0}", response));

            //Return data to 3ds-web-adapter (Step 13)
            return response;
        }

        [HttpPost, Route("v2/auth/challenge/status")]
        public Message challengeStatus([FromBody]Message request)
        {
            String challengeStatusUrl = Config.AsAuthUrl + "/api/v2/auth/challenge/status";
            logger.Info(string.Format("request challenge status API {0}, body: \n{1}", challengeStatusUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(challengeStatusUrl, request, typeof(Message));
            logger.Info(string.Format("challengeStatus response: \n{0}", response));

            return response;
        }

        /// <summary>
        /// Receives the Request for authentication result request (Step 15(F) and Step 20(C))
        /// Send data to ActiveServer to Retrieve Authentication Results
        /// </summary>
        /// <param name="txid"></param>
        /// <returns></returns>
        [HttpGet, Route("v2/auth/result")]
        public Message authResult(String txid)
        {
       
            string serverTransId = txid;
            //ActiveServer url for Retrieve Results
            string resultUrl = Config.AsAuthUrl + "/api/v2/auth/brw/result?threeDSServerTransID=" + serverTransId;

            //Get authentication result from ActiveServer (Step 16(F) and Step 21(C))
            Message result = (Message)RestClientHelper.GetForObject(resultUrl, typeof(Message));

            //Show authentication results on result.html (Step 17(F) and Step 22(C))
            return result;
        }

        [HttpPost, Route("v2/auth/3ri")]
        public Message threeRITest([FromBody] Message request)
        {
            //generate requestor trans ID
            String transId = Guid.NewGuid().ToString();
            request["threeDSRequestorTransID"] = transId;

            String threeRIUrl = Config.AsAuthUrl + "/api/v2/auth/3ri";
            logger.Info(string.Format("authRequest3RI on url: {0}, body: \n{1}", threeRIUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(threeRIUrl, request, typeof(Message));
            logger.Info(string.Format("authResponse3RI: \n{0}", response));
            return response;
        }
    }
}
