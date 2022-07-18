<?php session_start();
/**
 *  Copyright (C) GPayments Pty Ltd - All Rights Reserved
 *  Copying of this file, via any medium, is subject to the
 *  ActiveServer End User License Agreement (EULA)
 *
 *  Proprietary code for use in conjunction with GPayments products only
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Written by GPayments <techsupport@gpayments.com>, 2022
 *
 *
 */
class BrowserInfoController
{
    private $templateResolver;

    function __construct(TemplateResolver $templateResolver)
    {
        $this->templateResolver = $templateResolver;
    }

    public function collectBrwInfo()
    {
        // Simplified version of obtaining the request IP address
        $ipAddress = $_SERVER['REMOTE_ADDR'];
        if (substr($ipAddress, 0, 3) === "::1") {
            $ipAddress = "127.0.0.1";
        }

        $model = array();
        $model["browserIP"] = $ipAddress;
        $model["browserUserAgent"] = $_SERVER['HTTP_USER_AGENT'];
        $model["browserAcceptHeader"] = $_SERVER['HTTP_ACCEPT'];

        $this->templateResolver->_render("brw_info_collect", $model);
    }
}