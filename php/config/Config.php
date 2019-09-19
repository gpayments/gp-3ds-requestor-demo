<?php
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
 *  Written by GPayments <techsupport@gpayments.com>, 2019
 *
 *
 */

class Config
{

    private $asAuthUrl;
    private $baseUrl;
    private $certFileName;
    private $keyFileName;
    private $groupAuth;
    private $merchantToken;

    function __construct()
    {
        $ini_array = parse_ini_file("resources/application.ini");
        $this->baseUrl = $ini_array['baseUrl'];
        $this->asAuthUrl = $ini_array['asAuthUrl'];
        $this->certFileName = $ini_array['certFileName'];
        $this->keyFileName = $ini_array['keyFileName'];
        $this->groupAuth = boolval($ini_array['groupAuth']);
        $this->merchantToken = $ini_array['merchantToken'];
    }

    public function getCertFileName()
    {
        return $this->certFileName;
    }


    public function getBaseUrl()
    {
        return $this->baseUrl;
    }

    public function getAsAuthUrl()
    {
        return $this->asAuthUrl;
    }

    public function getKeyFileName()
    {
        return $this->keyFileName;
    }

    public function isGroupAuth()
    {
        return $this->groupAuth;
    }

    public function getMerchantToken()
    {
        return $this->merchantToken;
    }

}
