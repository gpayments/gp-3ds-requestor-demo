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
using System.Collections.Generic;
using System.Web;
using System.Web.Http;

namespace GPayments.Requestor.TestLab.Controllers
{
    public class Model : Dictionary<string, object> { }
    public class AuthServiceV2
    {
        private static log4net.ILog logger = log4net.LogManager.GetLogger(typeof(AuthServiceV2));
        private const string THREE_DS_REQUESTOR_TRANS_ID = "threeDSRequestorTransID";
        private const string THREE_DS_SERVER_TRANS_ID = "threeDSServerTransID";
        private const string INIT_AUTH_REQUEST = "initAuthRequest";
        private const string INIT_AUTH_REQUEST_NOSCRIPT = "initAuthRequestNoscript";
        private const string INIT_AUTH_RESPONSE = "initAuthResponse";
        private const string AUTH_REQUEST = "authRequest";
        private const string AUTH_RESPONSE = "authResponse";

        /**
         * Handle initAuth request
         */
        public Message initAuth(string transType, Message request)
        {
            //Generate requestor trans ID
            string transId = Guid.NewGuid().ToString();
            request[THREE_DS_REQUESTOR_TRANS_ID] = transId;
            //Fill the event call back url with requestor url + /3ds-notify
            string callBackUrl = Config.BaseUrl + "/3ds-notify";
            request["eventCallbackUrl"] = callBackUrl;

            //Send data to ActiveServer to Initialise authentication (Step 3)
            //Get the response data from ActiveServer (Step 4)
            Message response = sendInitAuthRequest(transType, request);
            logger.Info(string.Format("initAuthResponseBRW: \n{0}", response));

            return response;
        }

        /**
         * Send data to ActiveServer to Initialise authentication and get the response data from
         * ActiveServer.
         *
         * @param transType: transType=prod to use prod DS, otherwise use TestLabs DS
         * @param request:   init auth request
         * @return init auth response
         */
        private Message sendInitAuthRequest(String transType, Message request)
        {
            setSessionAttribute(INIT_AUTH_REQUEST, request);

            //ActiveServer url for Initialise Authentication
            string initAuthUrl = Config.AsAuthUrl + "/api/v2/auth/brw/init";
            //Add parameter trans-type=prod in the initAuthUrl to use prod DS, otherwise use testlab DS
            //For example, in this demo, the initAuthUrl for transactions with prod DS is https://api.as.testlab.3dsecure.cloud:7443/api/v2/auth/brw/init?trans-type=prod
            //For more details, refer to: https://docs.activeserver.cloud
            if ("prod".Equals(transType))
                initAuthUrl = initAuthUrl + "?trans-type=prod";

            logger.Info(string.Format("initAuthRequest on url: {0}, body: \n{1}", initAuthUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(initAuthUrl, request, typeof(Message));

            if (response != null)
                setSessionAttribute(INIT_AUTH_RESPONSE, response);
            else
                throw new ArgumentException("Invalid init auth response");

            return response;
        }

        /**
        * Handle auth request
        */
        public Message auth(Message request)
        {
            verifySessionTransId((String)request[THREE_DS_REQUESTOR_TRANS_ID]);

            //Send data to ActiveServer to Execute Authentication (Step 10)
            //Get the response data from ActiveServer (Step 12)
            Message response = sendAuthRequest(request);
            logger.Info(string.Format("authResponseBRW: \n{0}", response));

            return response;
        }

        private Message sendAuthRequest(Message request)
        {
            setSessionAttribute(AUTH_REQUEST, request);

            //get authUrl from session storage
            Message initAuthResponse = getSessionAttribute(INIT_AUTH_RESPONSE);
            String authUrl = (String)initAuthResponse["authUrl"];
            logger.Info(string.Format("requesting BRW Auth API {0}, body: \n{1}", authUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(authUrl, request, typeof(Message));

            if (response != null)
            {
                setSessionAttribute(AUTH_RESPONSE, response);
            }
            else
            {
                throw new ArgumentException("Invalid auth response");
            }

            return response;
        }

        public Message challengeStatus(Message request)
        {
            String challengeStatusUrl = Config.AsAuthUrl + "/api/v2/auth/challenge/status";
            logger.Info(string.Format("request challenge status API {0}, body: \n{1}", challengeStatusUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(challengeStatusUrl, request, typeof(Message));
            logger.Info(string.Format("challengeStatus response: \n{0}", response));

            return response;
        }

        public Message getBRWResult(String serverTransId)
        {
            //ActiveServer url for Retrieve Results
            string resultUrl = Config.AsAuthUrl + "/api/v2/auth/brw/result?threeDSServerTransID=" + serverTransId;

            //Get authentication result from ActiveServer
            Message result = (Message)RestClientHelper.GetForObject(resultUrl, typeof(Message));

            return result;
        }

        public Message threeRI(String transType, Message request)
        {
            //generate requestor trans ID
            String transId = Guid.NewGuid().ToString();
            request[THREE_DS_REQUESTOR_TRANS_ID] = transId;

            String threeRIUrl = Config.AsAuthUrl + "/api/v2/auth/3ri";

            //Add parameter trans-type=prod to use prod DS, otherwise use Testlabs DS
            if ("prod".Equals(transType))
                threeRIUrl = threeRIUrl + "?trans-type=prod";

            logger.Info(string.Format("authRequest3RI on url: {0}, body: \n{1}", threeRIUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(threeRIUrl, request, typeof(Message));
            logger.Info(string.Format("authResponse3RI: \n{0}", response));
            return response;
        }

        public Message get3RIResult(String serverTransId)
        {
            string resultUrl = Config.AsAuthUrl + "/api/v2/auth/3ri/result?threeDSServerTransID=" + serverTransId;

            Message response = (Message)RestClientHelper.GetForObject(resultUrl, typeof(Message));
            logger.Info(string.Format("authResponse: \n{0}", response));

            return response;
        }

        public Message app(String transType, Message request)
        {
            //generate requestor trans ID
            String transId = Guid.NewGuid().ToString();
            request[THREE_DS_REQUESTOR_TRANS_ID] = transId;

            String appAuthUrl = Config.AsAuthUrl + "/api/v2/auth/app";

            //Add parameter trans-type=prod in the appAuthUrl to use prod DS, otherwise use Testlabs DS
            //For more details, refer to: https://docs.activeserver.cloud
            if ("prod".Equals(transType))
                appAuthUrl = appAuthUrl + "?trans-type=prod";

            logger.Info(string.Format("appAuthRequest on url: {0}, body: \n{1}", appAuthUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(appAuthUrl, request, typeof(Message));
            logger.Info(string.Format("appAuthResponse: \n{0}", response));
            return response;
        }

        public Message enrol(String transType, Message request)
        {
            String enrolUrl = Config.AsAuthUrl + "/api/v2/auth/enrol";

            //Add parameter trans-type=prod to use prod DS, otherwise use Testlabs DS
            if ("prod".Equals(transType))
                enrolUrl = enrolUrl + "?trans-type=prod";

            logger.Info(string.Format("enrol on url: {0}, body: \n{1}", enrolUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(enrolUrl, request, typeof(Message));
            logger.Info(string.Format("enrolResponse: \n{0}", response));
            return response;
        }

        private Message getSessionAttribute(String key)
        {
            return (Message)HttpContext.Current.Session[key];
        }

        private void setSessionAttribute(String key, Message data)
        {
            HttpContext.Current.Session[key] = data;
        }

        /**
         * Handle noscript initAuth request
         */
        public String initAuthNoScript(String transType, AuthDataNoScriptDTO authData, Model model)
        {

            setSessionAttribute(INIT_AUTH_REQUEST_NOSCRIPT, authData.ToMessage());

            Message request = new Message();
            request["acctNumber"] = authData.acctNumber;
            request["merchantId"] = authData.merchantId;

            string transId = Guid.NewGuid().ToString();
            request[THREE_DS_REQUESTOR_TRANS_ID] = transId;
            String callBackUrl = Config.BaseUrl + "/3ds-notify/noscript";
            request["eventCallbackUrl"] = callBackUrl;

            Message response = sendInitAuthRequest(transType, request);
            logger.Info(string.Format("initAuthResponseBRW: \n{0}", response));

            model.Add("threeDSServerCallbackUrl", response["threeDSServerCallbackUrl"]);
            return "no_script_process";
        }

        /**
         * Handle noscript auth request
         */
        public String authNoScript(String transId, String param, Model model)
        {

            logger.Info(string.Format("Do authentication for transId:{0}", transId));

            verifySessionTransId(transId);

            Message authData = getSessionAttribute(INIT_AUTH_REQUEST_NOSCRIPT);

            authData[THREE_DS_REQUESTOR_TRANS_ID] = transId;
            authData[THREE_DS_SERVER_TRANS_ID] = getSessionAttribute(INIT_AUTH_RESPONSE)[THREE_DS_SERVER_TRANS_ID];
            authData["browserInfo"] = param;

            Message response = sendAuthRequest(authData);

            logger.Info(string.Format("authResponseBRW: \n{0}", response));

            if ("C".Equals(response["transStatus"]))
            {

                return "redirect:" + response["challengeUrl"];

            }
            else if ("D".Equals(response["transStatus"]))
            {

                return pollResultNoScript(transId, model);
            }

            model.Add("result", response);
            return "no_script_results";
        }

        /**
         * Handle noscript polling request
         */
        public String pollResultNoScript(String transId, Model model)
        {

            logger.Info(string.Format("Polling result for transId:{0}", transId));

            Message authResponse = getSessionAttribute(AUTH_RESPONSE);

            Message monResponse = (Message)RestClientHelper.GetForObject((string)authResponse["resultMonUrl"], typeof(Message));
            logger.Info(string.Format("monResponse: \n{0}", monResponse));

            if (monResponse != null && "AuthResultNotReady".Equals(monResponse["event"]))
            {

                model.Add("transId", transId);
                model.Add("cardholderInfo", authResponse["cardholderInfo"]);

                //the no_script_poll_result.html page will auto-fresh every second.
                return "no_script_poll_result";

            }
            else if (monResponse != null && "AuthResultReady".Equals(monResponse["event"]))
            {

                return "redirect:/v2/auth/brw/result/noscript?transId=" + transId;

            }
            else
            {
                throw new ArgumentException("Invalid mon url result event type");
            }
        }

        /**
         * Handle noscript result request
         */
        public String resultNoScript(String transId, Model model)
        {

            logger.Info(string.Format("Get result for transId:{0}", transId));

            verifySessionTransId(transId);

            String serverTransId = (String)getSessionAttribute(INIT_AUTH_RESPONSE)[THREE_DS_SERVER_TRANS_ID];
            Message response = getBRWResult(serverTransId);
            model.Add("result", response);
            return "no_script_results";
        }

        private void verifySessionTransId(String transId)
        {
            Message initAuthRequest = getSessionAttribute(INIT_AUTH_REQUEST);
            if (!transId.Equals(initAuthRequest[THREE_DS_REQUESTOR_TRANS_ID]))
            {
                throw new ArgumentException("Invalid 3DS Requestor trans Id");
            }
        }
    }
}
