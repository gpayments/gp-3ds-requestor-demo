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

class AuthController
{

    private $restTemplate;
    private $config;


    function __construct(Config $config, RestClientConfig $restTemplate)
    {
        $this->config = $config;
        $this->restTemplate = $restTemplate;
    }

    /**
     * Receives the initialise authentication request from the 3DS-web-adapter (Step 2)
     * Send data to ActiveServer to Initialise Authentication
     */
    public function initAuth($messageCategory)
    {
        $requestData = $this->_getJsonData();
        //Generate requestor trans ID
        $requestData->threeDSRequestorTransID = $this->_getUUId();
        //Fill the event call back url with requestor url + /3ds-notify
        $requestData->eventCallbackUrl = $this->config->getBaseUrl() . "/3ds-notify";

        //ActiveServer url for Initialise Authentication
        $initAuthUrl = "/api/v1/auth/brw/init/" . $messageCategory;
        //Send data to ActiveServer to Initialise authentication (Step 3)
        //Get the response data from ActiveServer (Step 4)
        $response = $this->restTemplate->post($initAuthUrl, $requestData);

        //Return data to 3ds-web-adapter (Step 5)
        $this->_returnJson($response->getBody());
    }

    /**
     * Receives the Execute authentication request from the 3DS-web-adapter (Step 9) Send data to
     * ActiveServer to Execute Authentication
     */
    public function auth()
    {
        $requestData = $this->_getJsonData();

        //ActiveServer url for Execute Authentication
        $authUrl = "/api/v1/auth/brw";

        //Send data to ActiveServer to Execute Authentication (Step 10)
        //Get the response data from ActiveServer (Step 12)
        $response = $this->restTemplate->post($authUrl, $requestData);

        //Return data to 3ds-web-adapter (Step 13)
        $this->_returnJson($response->getBody()->getContents());
    }

    /**
     * Receives the Request for authentication result request (Step 15(F) and Step 20(C))
     * Send data to ActiveServer to Retrieve Authentication Results
     */
    public function result()
    {
        $serverTransId = $_GET["txid"];
        //ActiveServer url for Retrieve Results
        $resultUrl = "/api/v1/auth/brw/result?threeDSServerTransID=" . $serverTransId;

        //Get authentication result from ActiveServer (Step 16(F) and Step 21(C))
        $response = $this->restTemplate->get($resultUrl);

        //Show authentication results on result.html (Step 17(F) and Step 22(C))
        $this->_returnJson($response->getBody()->getContents());
    }

    public function threeRI()
    {
        $requestData = $this->_getJsonData();
        //generate requestor trans ID
        $requestData->threeDSRequestorTransID = $this->_getUUId();

        $threeRIUrl = "/api/v1/auth/3ri/npa";
        $response = $this->restTemplate->post($threeRIUrl, $requestData);

        //Return data to 3ds-web-adapter (Step 13)
        $this->_returnJson($response->getBody()->getContents());
    }

    public function enrol()
    {
        $requestData = $this->_getJsonData();
        $requestData->threeDSRequestorTransID = $this->_getUUId();
        $enrolUrl = "/api/v1/auth/enrol";
        $response = $this->restTemplate->post($enrolUrl, $requestData);
        $this->_returnJson($response->getBody()->getContents());
    }

    private function _getJsonData()
    {
        $json = file_get_contents('php://input');
        return json_decode($json);
    }

    private function _getUUId()
    {
        return sprintf('%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
            // 32 bits for "time_low"
            mt_rand(0, 0xffff), mt_rand(0, 0xffff),

            // 16 bits for "time_mid"
            mt_rand(0, 0xffff),

            // 16 bits for "time_hi_and_version",
            // four most significant bits holds version number 4
            mt_rand(0, 0x0fff) | 0x4000,

            // 16 bits, 8 bits for "clk_seq_hi_res",
            // 8 bits for "clk_seq_low",
            // two most significant bits holds zero and one for variant DCE1.1
            mt_rand(0, 0x3fff) | 0x8000,

            // 48 bits for "node"
            mt_rand(0, 0xffff), mt_rand(0, 0xffff), mt_rand(0, 0xffff)
        );
    }

    private function _returnJson($response)
    {
        header('Content-Type: application/json;charset=utf-8');
        echo $response;
        exit;
    }

}