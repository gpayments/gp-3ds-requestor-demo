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
 *  Written by GPayments <techsupport@gpayments.com>, 2020
 *
 *
 */
class AuthControllerV2
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
    public function initAuth()
    {
        $requestData = Utils::_getJsonData();
        //Generate requestor trans ID
        $requestData->threeDSRequestorTransID = Utils::_getUUId();
        //Fill the event call back url with requestor url + /3ds-notify
        $requestData->eventCallbackUrl = $this->config->getBaseUrl() . "/3ds-notify";

        //ActiveServer url for Initialise Authentication
        //Add parameter trans-type=prod in the initAuthUrl to use prod DS, otherwise use testlab DS
        //For example, in this demo, the initAuthUrl for transactions with prod DS is https://api.as.testlab.3dsecure.cloud:7443/api/v2/auth/brw/init?trans-type=prod
        //For more details, refer to: https://docs.activeserver.cloud
        $initAuthUrl = "/api/v2/auth/brw/init";
        //Send data to ActiveServer to Initialise authentication (Step 3)
        //Get the response data from ActiveServer (Step 4)
        $response = $this->restTemplate->post($initAuthUrl, $requestData);
        $responseBody = json_decode($response->getBody());
        $_SESSION[$responseBody->threeDSServerTransID] = $responseBody;
        //Return data to 3ds-web-adapter (Step 5)
        Utils::_returnJson($response->getBody());
    }

    /**
     * Receives the Execute authentication request from the 3DS-web-adapter (Step 9) Send data to
     * ActiveServer to Execute Authentication
     */
    public function auth()
    {
        $requestData = Utils::_getJsonData();
        //ActiveServer url for Execute Authentication
        $authUrl = $_SESSION[$requestData->threeDSServerTransID]->authUrl;

        //Send data to ActiveServer to Execute Authentication (Step 10)
        //Get the response data from ActiveServer (Step 12)
        $response = $this->restTemplate->post($authUrl, $requestData);

        //Return data to 3ds-web-adapter (Step 13)
        Utils::_returnJson($response->getBody()->getContents());
    }

    /**
     * Receives the Request for authentication result request (Step 15(F) and Step 20(C))
     * Send data to ActiveServer to Retrieve Authentication Results
     */
    public function result()
    {
        $serverTransId = $_GET["txid"];
        //ActiveServer url for Retrieve Results
        $resultUrl = "/api/v2/auth/brw/result?threeDSServerTransID=" . $serverTransId;

        //Get authentication result from ActiveServer (Step 16(F) and Step 21(C))
        $response = $this->restTemplate->get($resultUrl);

        //Show authentication results on result.html (Step 17(F) and Step 22(C))
        Utils::_returnJson($response->getBody()->getContents());
    }

    public function threeRI()
    {
        $requestData = Utils::_getJsonData();
        //generate requestor trans ID
        $requestData->threeDSRequestorTransID = Utils::_getUUId();

        $threeRIUrl = "/api/v2/auth/3ri";
        $response = $this->restTemplate->post($threeRIUrl, $requestData);

        //Return data to 3ds-web-adapter (Step 13)
        Utils::_returnJson($response->getBody()->getContents());
    }

    public function challengeStatus()
    {
        $requestData = Utils::_getJsonData();
        $challengeStatusUrl = "/api/v2/auth/challenge/status";
        $response = $this->restTemplate->post($challengeStatusUrl, $requestData);
        Utils::_returnJson($response->getBody()->getContents());
    }
}