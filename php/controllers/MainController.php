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
class MainController
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

    public function index()
    {
        $this->templateResolver->_render("index");
    }

    public function shop()
    {
        $this->templateResolver->_render("shop");
    }

    public function app()
    {
        $model = array();
        $model["callbackUrl"] = $this->config->getBaseUrl();
        $model["serverUrl"] = $this->config->getAsAuthUrl();
        $this->templateResolver->_render("app", $model);
    }


    public function checkout()
    {
        $this->templateResolver->_render("checkout");
    }

    public function processV1()
    {
        $this->templateResolver->_render("v1/process");
    }

    public function processV2()
    {
        $this->templateResolver->_render("v2/process");
    }


    public function brw()
    {
        $model = array();
        $model["callbackUrl"] = $this->config->getBaseUrl();
        $model["serverUrl"] = $this->config->getAsAuthUrl();
        $this->templateResolver->_render("brw", $model);
    }

    public function enrol()
    {
        $model = array();
        $model["callbackUrl"] = $this->config->getBaseUrl();
        $model["serverUrl"] = $this->config->getAsAuthUrl();
        $this->templateResolver->_render("enrol", $model);
    }

    public function threeRI()
    {
        $model = array();
        $model["callbackUrl"] = $this->config->getBaseUrl();
        $model["serverUrl"] = $this->config->getAsAuthUrl();
        $this->templateResolver->_render("3ri", $model);
    }

    public function resultV1()
    {
        $this->templateResolver->_render("v1/result");
    }

    public function resultV2()
    {
        $this->templateResolver->_render("v2/result");
    }

    public function noScript()
    {
        $this->templateResolver->_render("no_script");
    }


    /**
     * Handles the notification event from the iframe in no javascript mode.
     * threeDSServerCallbackURL or the challengeUrl.
     */
    public function notifyNoScript()
    {
        $requestorTransId = $_POST["requestorTransId"];
        $callbackType = $_POST["event"];
        $param = (isset($_POST["param"]) && !empty($_POST["param"])) ? $_POST["param"] : "";

//        var_dump($_SESSION);
        if ($callbackType === "3DSMethodFinished"
            || $callbackType === "3DSMethodSkipped"
            || $callbackType === "InitAuthTimedOut") {

            Utils::_redirect("/v2/auth/noscript?transId=" . $requestorTransId . "&param=" . $param);

        } else if ($callbackType === "AuthResultReady") {
            Utils::_redirect("/v2/auth/brw/result/noscript?transId=" . $requestorTransId);
        } else if ($callbackType === "3DSMethodHasError") {
          //Event 3DSMethodHasError is only for logging and troubleshooting purpose, this demo
          //just log and exit current notify process.
            echo "3DSMethodHasError callback is received";
            exit;
        } else {
            echo "invalid callback type";
            exit;
        }
    }


    /**
     * Handles the notification event from the iframe.
     * threeDSServerCallbackURL or the challengeUrl.
     */
    public function notifyResult()
    {
        $requestorTransId = $_POST["requestorTransId"];
        $callbackType = $_POST["event"];
        $param = (isset($_POST["param"]) && !empty($_POST["param"])) ? $_POST["param"] : "";

        if ($callbackType === "3DSMethodFinished") {

            $callbackName = "_on3DSMethodFinished";

        } else if ($callbackType === "3DSMethodSkipped") {

            $callbackName = "_on3DSMethodSkipped";

        } else if ($callbackType === "AuthResultReady") {
            $callbackName = "_onAuthResult";

        } else if ($callbackType === "InitAuthTimedOut") {

            $callbackName = "_onInitAuthTimedOut";
        } else {
            echo "invalid callback type";
            exit;
        }

        $model = array();
        $model["transId"] = $requestorTransId;
        $model["callbackName"] = $callbackName;
        $model["callbackParam"] = $param;

        $this->templateResolver->_render("notify_3ds_events", $model);
    }
}