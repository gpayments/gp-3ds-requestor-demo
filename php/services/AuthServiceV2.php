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
class AuthServiceV2
{
    private $config;
    private $restTemplate;
    private $templateResolver;

    function __construct(Config $config, RestClientConfig $restTemplate, TemplateResolver $templateResolver)
    {
        $this->config = $config;
        $this->restTemplate = $restTemplate;
        $this->templateResolver = $templateResolver;
    }


    public function getBrwResult($serverTransId)
    {
        //ActiveServer url for Retrieve Results
        $resultUrl = "/api/v2/auth/brw/result?threeDSServerTransID=" . $serverTransId;

        //Get authentication result from ActiveServer (Step 16(F), Step 27(C) or Step 20(D))
        return $this->restTemplate->get($resultUrl)->getBody();
    }


    public function getThreeRIResult($serverTransId)
    {
        //ActiveServer url for Retrieve Results
        $resultUrl = "/api/v2/auth/3ri/result?threeDSServerTransID=" . $serverTransId;

        //Get authentication result from ActiveServer (Step 16(F), Step 27(C) or Step 20(D))
        return $this->restTemplate->get($resultUrl)->getBody();
    }

    public function sendInitAuthRequest($transType, $requestData)
    {
        $_SESSION[SessionKeys::INIT_AUTH_REQUEST] = $requestData;
//        var_dump($_SESSION[SessionKeys::INIT_AUTH_REQUEST]);

        //ActiveServer url for Initialise Authentication
        //Add parameter trans-type=prod in the initAuthUrl to use prod DS, otherwise use testlab DS
        //For example, in this demo, the initAuthUrl for transactions with prod DS is https://api.as.testlab.3dsecure.cloud:7443/api/v2/auth/brw/init?trans-type=prod
        //For more details, refer to: https://docs.activeserver.cloud
        $initAuthUrl = "/api/v2/auth/brw/init";

        if (!empty($transType) && $transType == "prod") {
            $initAuthUrl = $initAuthUrl . "?trans-type=prod";
        }

        //Send data to ActiveServer to Initialise authentication (Step 3)
        //Get the response data from ActiveServer (Step 4)
        $response = $this->restTemplate->post($initAuthUrl, $requestData);
        $responseBody = json_decode($response->getBody());

        if ($responseBody != null) {
            $_SESSION[SessionKeys::INIT_AUTH_RESPONSE] = $responseBody;
            return $response->getBody();
        } else {
            echo "invalid init auth response";
            exit;
        }
    }

    function sendAuthRequest($requestData)
    {

        $_SESSION[SessionKeys::AUTH_REQUEST] = $requestData;

//        var_dump($_SESSION[SessionKeys::INIT_AUTH_RESPONSE]);
        //ActiveServer url for Execute Authentication
        $authUrl = $_SESSION[SessionKeys::INIT_AUTH_RESPONSE]->authUrl;

        //Send data to ActiveServer to Execute Authentication (Step 10)
        //Get the response data from ActiveServer (Step 12)
        $response = $this->restTemplate->post($authUrl, $requestData);

        if ($response != null) {
            $_SESSION[SessionKeys::AUTH_RESPONSE] = json_decode($response->getBody());
            return $response->getBody();
        } else {
            echo "invalid auth response";
            exit;
        }
    }

    /**
     * NOTE: please use proper session management recommended by your PHP framework in production 3DS requestor.
     * The session management used in this sample code is only for demo purpose.
     **/
    public static function verifySessionTransId($transId)
    {
        $requestorTransId = $_SESSION[SessionKeys::INIT_AUTH_REQUEST]->threeDSRequestorTransID;

        if ($requestorTransId != $transId) {
            echo "session lost, please restart authentication";
            exit();
        }
    }


    public function initAuthNoScript($transType, $authData)
    {
        $_SESSION[SessionKeys::INIT_AUTH_REQUEST_NOSCRIPT] = $authData;

        $message = new stdClass();
        $message->acctNumber = $authData->acctNumber;
        $message->merchantId = $authData->merchantId;
        $message->threeDSRequestorTransID = Utils::_getUUId();
        $callbackUrl = $this->config->getBaseUrl() . "/3ds-notify/noscript";
        $message->eventCallbackUrl = $callbackUrl;

        $response = json_decode($this->sendInitAuthRequest($transType, $message));

        $model = array();
        $model["threeDSServerCallbackUrl"] = $response->threeDSServerCallbackUrl;

        $this->templateResolver->_render("no_script_process", $model);
    }

    public function authNoScript($transId, $param)
    {
        AuthServiceV2::verifySessionTransId($transId);


        $authData = $_SESSION[SessionKeys::INIT_AUTH_REQUEST_NOSCRIPT];

        $authData->threeDSRequestorTransID = $transId;
        $authData->browserInfo = $param;

        $initAuthResp = $_SESSION[SessionKeys::INIT_AUTH_RESPONSE];
        $authData->threeDSServerTransID = $initAuthResp->threeDSServerTransID;

        $response = $this->sendAuthRequest($authData);

        $responseBody = json_decode($response);

        if ($responseBody->transStatus == "C") {

            Utils::_redirect($responseBody->challengeUrl);
            return;

        } else if ($responseBody->transStatus == "D") {

            $this->pollRequestNoScript($transId);

        } else {
            $model = array();
            $model["result"] = json_encode($responseBody, JSON_PRETTY_PRINT);
            $this->templateResolver->_render("no_script_results", $model);
        }

    }


    public function pollRequestNoScript($transId)
    {
        // polling the result availability

        $authResponse = $_SESSION[SessionKeys::AUTH_RESPONSE];

        $monResponse = $this->restTemplate->get($authResponse->resultMonUrl);
        $monResponseBody = json_decode($monResponse->getBody());
        if ($monResponseBody == null) {
            echo "monitoring response is empty";
            exit();
        }

//        var_dump($monResponseBody);

        if ("AuthResultNotReady" == $monResponseBody->event) {
            $model = array();
            $model["transId"] = $transId;
            $model["cardholderInfo"] = $authResponse->cardholderInfo;
            $this->templateResolver->_render("no_script_poll_result", $model);
        } else if ("AuthResultReady" == $monResponseBody->event) {
            Utils::_redirect("/v2/auth/brw/result/noscript?transId=" . $transId);
        } else {
            echo "invalid mon url result event type";
            exit();
        }
    }


}