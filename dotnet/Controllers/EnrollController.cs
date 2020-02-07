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
using System.Web.Http;

namespace GPayments.Requestor.TestLab.Controllers
{
    public class EnrollController : ApiController
    {
        private static log4net.ILog logger = log4net.LogManager.GetLogger(typeof(EnrollController));

        [HttpPost, Route("auth/enrol")]
        public Message enrolTest([System.Web.Http.FromBody] Message request)
        {
            String enrolUrl = Config.AsAuthUrl + "/api/v1/auth/enrol";
            logger.Info(string.Format("enrol on url: {0}, body: \n{1}", enrolUrl, request));

            Message response = (Message)RestClientHelper.PostForObject(enrolUrl, request, typeof(Message));
            logger.Info(string.Format("enrolResponse: \n{0}", response));
            return response;
        }
    }
}
