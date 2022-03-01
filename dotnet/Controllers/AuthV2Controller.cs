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

using GPayments.Requestor.TestLab.Models.dto;
using System;
using System.Web.Http;

namespace GPayments.Requestor.TestLab.Controllers
{
    public class AuthV2Controller : ApiController
    {
        private AuthServiceV2 authServiceV2 = new AuthServiceV2();

        /// <summary>
        /// Receives the initialise authentication request from the 3DS-web-adapter (Step 2)
        /// Send data to ActiveServer to Initialise Authentication
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost, Route("v2/auth/init")]
        public Message initAuth([FromBody]Message request, [FromUri(Name = "trans-type")] string transType = null)
        {
            return authServiceV2.initAuth(transType, request);
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
            return authServiceV2.auth(request);
        }

        [HttpPost, Route("v2/auth/challenge/status")]
        public Message challengeStatus([FromBody]Message request)
        {
            return authServiceV2.challengeStatus(request);
        }

        /// <summary>
        /// Receives the Request for authentication result request (Step 15(F) and Step 20(C))
        /// Send data to ActiveServer to Retrieve Authentication Results
        /// </summary>
        /// <param name="txid"></param>
        /// <returns></returns>
        [HttpGet, Route("v2/auth/brw/result")]
        public Message authResult(String txid)
        {
            return authServiceV2.getBRWResult(txid);
        }

        [HttpPost, Route("v2/auth/3ri")]
        public Message threeRITest([FromBody] Message request, [FromUri(Name = "trans-type")] string transType = null)
        {
            return authServiceV2.threeRI(transType, request);
        }

        [HttpPost, Route("v2/auth/3ri/result")]
        public Message result3ri(String txid)
        {
            return authServiceV2.get3RIResult(txid);
        }

        [HttpPost, Route("v2/auth/app")]
        public Message app([FromBody] Message request, [FromUri(Name = "trans-type")] string transType = null)
        {
            return authServiceV2.app(transType, request);
        }

        [HttpPost, Route("v2/auth/enrol")]
        public Message enrolTest([FromBody] Message request, [FromUri(Name = "trans-type")] string transType = null)
        {
            return authServiceV2.enrol(transType, request);
        }
    }
}
