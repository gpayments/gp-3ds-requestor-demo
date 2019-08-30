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

using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Web;

namespace GPayments.Requestor.TestLab.Helpers
{
    public class Config
    {
        public static string AsAuthUrl { get { return ConfigurationManager.AppSettings["AsAuthUrl"] ?? "https://api.as.testlab.3dsecure.cloud:7443"; } }
        public static string BaseUrl { get { return ConfigurationManager.AppSettings["BaseUrl"] ?? "http://localhost:8082"; } }
        public static string CertFileName { get { return ConfigurationManager.AppSettings["CertFileName"] ?? "<Full file name and path of the client certificate>"; } }
    }
}