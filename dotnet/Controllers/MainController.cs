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
using System.Dynamic;
using System.Web.Mvc;

namespace GPayments.Requestor.TestLab.Controllers
{
    public class MainController : Controller
    {
        private static readonly log4net.ILog logger = log4net.LogManager.GetLogger(typeof(MainController));
        private AuthServiceV2 authServiceV2 = new AuthServiceV2();

        public ActionResult Index()
        {
            return View();
        }

        [HttpGet]
        public ActionResult Shop()
        {
            return View();
        }

        [HttpGet]
        public ActionResult Checkout()
        {
            return View();
        }

        [HttpGet, Route("v1/process")]
        public ActionResult ProcessV1()
        {
            return View("v1/process");
        }

        [HttpGet, Route("v2/process")]
        public ActionResult ProcessV2()
        {
            return View("v2/process");
        }

        [HttpGet]
        public ActionResult brw()
        {
            dynamic model = new ExpandoObject();
            model.callbackUrl = Config.BaseUrl;
            model.serverUrl = Config.AsAuthUrl;
            return View(model);
        }

        [HttpGet]
        public ActionResult enrol()
        {
            dynamic model = new ExpandoObject();
            model.callbackUrl = Config.BaseUrl;
            model.serverUrl = Config.AsAuthUrl;
            return View(model);
        }

        [HttpGet, ActionName("3ri")]
        public ActionResult threeRI()
        {
            dynamic model = new ExpandoObject();
            model.callbackUrl = Config.BaseUrl;
            model.serverUrl = Config.AsAuthUrl;
            return View(model);
        }

        [HttpGet]
        public ActionResult app()
        {
            dynamic model = new ExpandoObject();
            model.callbackUrl = Config.BaseUrl;
            model.serverUrl = Config.AsAuthUrl;
            return View(model);
        }
        
        /**
         * A separate endpoint with business logic to demonstrate server-side BrowserInfo collection
         */
        [HttpGet, Route("v2/auth/brw/info")]
        public ActionResult CollectBrowserInfo()
        {
            // Simplified version of obtaining the request IP address
            string ipAddress = Request.ServerVariables["REMOTE_ADDR"];
            if (ipAddress.StartsWith("["))
            {
                ipAddress = "127.0.0.1";
            }
            
            dynamic model = new ExpandoObject();
            model.browserIP = ipAddress;
            model.browserUserAgent = Request.Headers["User-Agent"];
            model.browserAcceptHeader = Request.Headers["Accept"];
            return View("brw_info_collect", model);
        }

        [HttpGet, Route("v1/result")]
        public ActionResult ResultV1()
        {
            return View("v1/result");
        }

        [HttpGet, Route("v2/result")]
        public ActionResult ResultV2()
        {
            return View("v2/result");
        }

        [HttpPost, ActionName("3ds-notify")]
        public ActionResult NotifyResult()
        {
            string transId = Request.Params["requestorTransId"];
            string callbackType = Request.Params["event"];
            string param = Request.Params["param"];

            String callbackName;
            if ("3DSMethodFinished".Equals(callbackType))
                callbackName = "_on3DSMethodFinished";
            else if ("3DSMethodSkipped".Equals(callbackType))
                callbackName = "_on3DSMethodSkipped";
            else if ("AuthResultReady".Equals(callbackType))
                callbackName = "_onAuthResult";
            else if ("InitAuthTimedOut".Equals(callbackType))
                callbackName = "_onInitAuthTimedOut";
            else if ("3DSMethodHasError".Equals(callbackType))
               //Event 3DSMethodHasError is only for logging and troubleshooting purpose, this demo
               //sets the callbackName to be _NA so the frontend won't process it.
                callbackName = "_NA";
            else
                // When unrecognised event has been received, a callbackName like "_NA" can be returned (so the frontend won't recognise it)
                // to make the callback process more robust and resilient.
                // Alternatively, the 3DS Requestor backend implementation may choose to throw an exception to indicate this error
                // however the frontend must be able to handle the exception so that the checkout page flow won't be interrupted
                callbackName = "_NA";

            dynamic model = new ExpandoObject();
            model.transId = transId;
            model.callbackName = callbackName;
            model.callbackParam = param;

            return View("notify_3ds_events", model);
        }

        [HttpGet, Route("noscript")]
        public ActionResult NoScript()
        {
            return View("no_script");
        }

        /**
        * Receives the initialise authentication request from a no-javascript environment.
        */
        [HttpPost, Route("v2/auth/init/noscript")]
        public ActionResult initAuthNoScript([System.Web.Http.FromBody] AuthDataNoScriptDTO authData)
        {
            Model model = new Model();
            string transType = Request.Params["trans-type"];
            string view = authServiceV2.initAuthNoScript(transType, authData, model);
            return View(view, model);
        }

        [HttpGet, Route("v2/auth/noscript")]
        public ActionResult authNoScript(String transId, String param)
        {
            Model model = new Model();
            string view = authServiceV2.authNoScript(transId, param, model);
            if (view.StartsWith("redirect:"))//handle redirect url
                return new RedirectResult(view.Substring(9).Trim());
            return View(view, model);
        }

        [HttpGet, Route("v2/auth/result/noscript/poll")]
        public ActionResult pollResultNoScript(String transId)
        {
            Model model = new Model();
            string view = authServiceV2.pollResultNoScript(transId, model);
            if (view.StartsWith("redirect:"))//handle redirect url
                return new RedirectResult(view.Substring(9).Trim());
            return View(view, model);
        }

        [HttpGet, Route("v2/auth/brw/result/noscript")]
        public ActionResult resultNoScript(String transId)
        {
            Model model = new Model();
            string view = authServiceV2.resultNoScript(transId, model);
            return View(view, model);
        }

        [HttpPost, Route("3ds-notify/noscript")]
        public ActionResult NotifyNoScript()
        {
            string transId = Request.Params["requestorTransId"];
            string callbackType = Request.Params["event"];
            string param = Request.Params["param"];

            if ("3DSMethodFinished".Equals(callbackType)
                || "3DSMethodSkipped".Equals(callbackType)
                || "InitAuthTimedOut".Equals(callbackType))
            {
                logger.Info(string.Format("{0}, continue to do authentication", callbackType));

                //redirect:/v2/auth/noscript
                return RedirectToAction("v2/auth/noscript", new { transId, param });

            }
            else if ("AuthResultReady".Equals(callbackType))
            {

                logger.Info(string.Format("{0}, continue to get result", callbackType));

                //redirect:/v2/auth/brw/result/noscript
                return RedirectToAction("v2/auth/brw/result/noscript", new { transId });
            }
            else
            {
                throw new ArgumentException("invalid notifyCallback");
            }
        }

        [HttpGet, Route("3ds1")]
        public ActionResult paymentPage()
        {
            dynamic model = new ExpandoObject();
            model.authUrl = Config.AsAuthUrl;
            model.callbackUrl = Config.BaseUrl + "/3ds1/result";

            logger.Info("3ds1 auth page called");
            return View("3ds1/auth", model);
        }

        [HttpPost, Route("3ds1/result")]
        public ActionResult resultPage()
        {
            var body = Request.Form;
            logger.Info(string.Format("received result: {0}", body));

            dynamic model = new ExpandoObject();
            model.errorCode = body["errorCode"];
            model.errorMessage = body["errorMessage"];
            model.txStatus = body["txStatus"];
            model.cavv = body["cavv"];
            model.cavvAlgo = body["cavvAlgo"];
            model.eci = body["eci"];
            model.threeDSRequestorTransID = body["threeDSRequestorTransID"];
            return View("3ds1/result", model);
        }
    }
}