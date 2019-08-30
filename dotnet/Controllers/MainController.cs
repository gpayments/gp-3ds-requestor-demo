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
* Written by GPayments<techsupport@gpayments.com>, 2019
************************************************************************/

using GPayments.Requestor.TestLab.Helpers;
using GPayments.Requestor.TestLab.Models.dto;
using System;
using System.Dynamic;
using System.Linq;
using System.Web.Mvc;

namespace GPayments.Requestor.TestLab.Controllers
{
    public class MainController : Controller
    {
        private static readonly log4net.ILog logger = log4net.LogManager.GetLogger(typeof(MainController));

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

        [HttpGet]
        public ActionResult Process()
        {
            return View();
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

        [HttpGet, ActionName("result")]
        public ActionResult Result()
        {
            return View();
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
            else
                throw new ArgumentException("invalid callback type");

            dynamic model = new ExpandoObject();
            model.transId = transId;
            model.callbackName = callbackName;
            model.callbackParam = param;

            return View("notify_3ds_events", model);
        }
    }
}