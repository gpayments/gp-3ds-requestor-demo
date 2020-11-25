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

using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace GPayments.Requestor.TestLab.Models.dto
{
    public class AuthDataNoScriptDTO
    {
        public String acctNumber { get; set; }
        public String merchantId { get; set; }
        public String authenticationInd { get; set; }
        public String purchaseAmount { get; set; }
        public String purchaseCurrency { get; set; }
        public String messageCategory { get; set; }
        public String purchaseDate { get; set; }
        public String messageVersion { get; set; }
        public String threeDSRequestorDecReqInd { get; set; }
        public String threeDSRequestorDecMaxTime { get; set; }

        public Message ToMessage()
        {
            Message message = new Message();
            foreach (System.Reflection.PropertyInfo pInfo in this.GetType().GetProperties())
                message.Add(pInfo.Name, pInfo.GetValue(this));
            return message;
        }

        public override string ToString()
        {
            return JsonConvert.SerializeObject(this, Formatting.Indented, new JsonSerializerSettings() { NullValueHandling = NullValueHandling.Ignore });
        }
    }
}