/************************************************************************
* Copyright (C) GPayments Pty Ltd - All Rights Reserved
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
* Written by GPayments <techsupport@gpayments.com>, 2020
************************************************************************/

using GPayments.Requestor.TestLab.Models.dto;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;
using System.Web.Http.Filters;

namespace GPayments.Requestor.TestLab.exception
{
    public class GlobalExceptionHandler : ExceptionFilterAttribute
    {
        private static log4net.ILog logger = log4net.LogManager.GetLogger(typeof(GlobalExceptionHandler));
        public override void OnException(HttpActionExecutedContext actionExecutedContext)
        {
            if (actionExecutedContext.Exception is WebException && ((WebException)actionExecutedContext.Exception).Response != null)
            {
                WebException exp = (WebException)actionExecutedContext.Exception;

                string result = null;
                using (StreamReader streamIn = new StreamReader(exp.Response.GetResponseStream()))
                    result = streamIn.ReadToEnd();

                logger.Error(string.Format("failed, {0}, {1}", ((HttpWebResponse)exp.Response).StatusCode, result));

                actionExecutedContext.Response = new HttpResponseMessage(((HttpWebResponse)exp.Response).StatusCode)
                {
                    Content = new StringContent(result),
                    ReasonPhrase = exp.Message
                };
            }
            else
                logger.Error(actionExecutedContext.Exception.Message, actionExecutedContext.Exception);

            base.OnException(actionExecutedContext);
        }
    }
}