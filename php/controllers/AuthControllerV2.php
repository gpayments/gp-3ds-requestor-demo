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
    private $authService;
    private $templateResolver;


    function __construct(Config $config, RestClientConfig $restTemplate, TemplateResolver $templateResolver)
    {
        $this->config = $config;
        $this->restTemplate = $restTemplate;
        $this->authService = new AuthServiceV2($config, $restTemplate, $templateResolver);
        $this->templateResolver = $templateResolver;
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

        $transType = $_GET["trans-type"];

        $responseBody = $this->authService->sendInitAuthRequest($transType, $requestData);

        //Return data to 3ds-web-adapter (Step 5)
        Utils::_returnJson($responseBody);
    }

    /**
     * Receives the Execute authentication request from the 3DS-web-adapter (Step 9) Send data to
     * ActiveServer to Execute Authentication
     */
    public function auth()
    {
        $requestData = Utils::_getJsonData();

        //Send data to ActiveServer to Execute Authentication (Step 10)
        //Get the response data from ActiveServer (Step 12)
        $response = $this->authService->sendAuthRequest($requestData);

        //Return data to 3ds-web-adapter (Step 13)
        Utils::_returnJson($response);
    }

    /**
     * Receives the Request for authentication result request (Step. 15(F), Step. 26(C) or Step. 19(D))
     * Send data to ActiveServer to Retrieve Authentication Results
     */
    public function brwResult()
    {
        $serverTransId = $_GET["txid"];

        $response = $this->authService->getBrwResult($serverTransId);

        //Show authentication results on result.html (Step. 17(F), Step. 28(C) or Step. 21(D), 22(D)).
        Utils::_returnJson($response);
    }


    public function threeRI()
    {
        $requestData = Utils::_getJsonData();
        //generate requestor trans ID
        $requestData->threeDSRequestorTransID = Utils::_getUUId();

        $threeRIUrl = "/api/v2/auth/3ri";

        $transType = $_GET["trans-type"];

        if (!empty($transType) && $transType == "prod") {
            $threeRIUrl = $threeRIUrl . "?trans-type=prod";
        }

        $response = $this->restTemplate->post($threeRIUrl, $requestData);

        //Return data to 3ds-web-adapter (Step 13)
        Utils::_returnJson($response->getBody()->getContents());
    }

    public function threeRIResult()
    {
        $serverTransId = $_GET["txid"];
        $result = $this->authService->getThreeRIResult($serverTransId);
        //Show authentication results on result.html (Step 17(F) and Step 22(C))
        Utils::_returnJson($result);
    }


    public function challengeStatus()
    {
        $requestData = Utils::_getJsonData();
        $challengeStatusUrl = "/api/v2/auth/challenge/status";
        $response = $this->restTemplate->post($challengeStatusUrl, $requestData);
        Utils::_returnJson($response->getBody()->getContents());
    }


    public function app()
    {
        $requestData = Utils::_getJsonData();
        $authAppUrl = "/api/v2/auth/app";

        $transType = $_GET["trans-type"];

        if (!empty($transType) && $transType == "prod") {
            $authAppUrl = $authAppUrl . "?trans-type=prod";
        }

        $response = $this->restTemplate->post($authAppUrl, $requestData);
        Utils::_returnJson($response->getBody()->getContents());
    }

    public function enrolCheck()
    {
        $requestData = Utils::_getJsonData();
        $enrolUrl = "/api/v2/auth/enrol";
        $transType = $_GET["trans-type"];

        if (!empty($transType) && $transType == "prod") {
            $enrolUrl = $enrolUrl . "?trans-type=prod";
        }

        $response = $this->restTemplate->post($enrolUrl, $requestData);
        Utils::_returnJson($response->getBody()->getContents());
    }

    public function initAuthNoScript()
    {
        $initAuthNoScript = new stdClass();
        $initAuthNoScript->acctNumber = $_POST["acctNumber"];
        $initAuthNoScript->merchantId = $_POST["merchantId"];
        $initAuthNoScript->transType = $_POST["transType"];
        $initAuthNoScript->messageVersion = $_POST["messageVersion"];
        $initAuthNoScript->authenticationInd = $_POST["authenticationInd"];
        $initAuthNoScript->purchaseAmount = $_POST["purchaseAmount"];
        $initAuthNoScript->purchaseCurrency = $_POST["purchaseCurrency"];
        $initAuthNoScript->messageCategory = $_POST["messageCategory"];
        $initAuthNoScript->purchaseDate = $_POST["purchaseDate"];
        $initAuthNoScript->threeDSRequestorDecReqInd = $_POST["threeDSRequestorDecReqInd"];
        $initAuthNoScript->threeDSRequestorDecMaxTime = $_POST["threeDSRequestorDecMaxTime"];

        $transType = $_POST["trans-type"];
        $this->authService->initAuthNoScript($transType, $initAuthNoScript);
    }

    public function authNoScript()
    {
        $this->authService->authNoScript($_GET["transId"], $_GET["param"]);
    }

    public function resultNoScript()
    {
        $transId = $_GET["transId"];;

        AuthServiceV2::verifySessionTransId($transId);

        $threeDSServerTransID = $_SESSION[SessionKeys::INIT_AUTH_RESPONSE]->threeDSServerTransID;

        $response = $this->authService->getBrwResult($threeDSServerTransID);

        $model = array();
        $model["result"] = json_encode(json_decode($response), JSON_PRETTY_PRINT);

        $this->templateResolver->_render("no_script_results", $model);
    }

    public function pollResult()
    {
        $transId = $_GET["transId"];;

        AuthServiceV2::verifySessionTransId($transId);

        $this->authService->pollRequestNoScript($transId);
    }

}