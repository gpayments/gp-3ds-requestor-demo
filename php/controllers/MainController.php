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

use GuzzleHttp\Client;

class MainController
{

    private $config;
    private $model = array();

    function __construct(Config $config)
    {
        $this->config = $config;
    }

    public function index()
    {
        $this->_render("index");
    }

    public function shop()
    {
        $this->_render("shop");
    }

    public function checkout()
    {
        $this->_render("checkout");
    }

    public function process()
    {
        $this->_render("process");
    }

    public function brw()
    {
        $this->model["callbackUrl"] = $this->config->getBaseUrl();
        $this->model["serverUrl"] = $this->config->getAsAuthUrl();
        $this->_render("brw");
    }

    public function enrol()
    {
        $this->model["callbackUrl"] = $this->config->getBaseUrl();
        $this->model["serverUrl"] = $this->config->getAsAuthUrl();
        $this->_render("enrol");
    }

    public function threeRI()
    {
        $this->model["callbackUrl"] = $this->config->getBaseUrl();
        $this->model["serverUrl"] = $this->config->getAsAuthUrl();
        $this->_render("3ri");
    }

    public function result()
    {
        $this->_render("result");
    }

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

        $this->model["transId"] = $requestorTransId;
        $this->model["callbackName"] = $callbackName;
        $this->model["callbackParam"] = $param;

        $this->_render("notify_3ds_events");
    }

    private function _render($template)
    {
        Mustache_Autoloader::register();
        $options = array('extension' => '.html');

        $m = new Mustache_Engine(array(
            'loader' => new Mustache_Loader_FilesystemLoader(dirname(__DIR__) . '/resources/templates', $options),
        ));

        echo $m->render($template, $this->model);
        exit;
    }
}