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
 *  Written by GPayments <techsupport@gpayments.com>, 2020
 *
 *
 */

class Router
{
    private $mainController;
    private $authControllerV1;
    private $authControllerV2;

    function __construct($config)
    {
        $restTemplate = new RestClientConfig($config);
        $this->mainController = new MainController($config, $restTemplate);
        $this->authControllerV1 = new AuthControllerV1($config, $restTemplate);
        $this->authControllerV2 = new AuthControllerV2($config, $restTemplate);
    }

    public function route($path)
    {
        // It starts with '/auth'
        if (startsWith($path, '/v1/auth')) {
            if ($path === "/v1/auth/init/pa") {
                $this->authControllerV1->initAuth("pa");
            } else if ($path === "/v1/auth/init/npa") {
                $this->authControllerV1->initAuth("npa");
            } else if ($path === "/v1/auth/result") {
                $this->authControllerV1->result();
            } else if ($path === "/v1/auth") {
                $this->authControllerV1->auth();
            } else if ($path === "/v1/auth/3ri") {
                $this->authControllerV1->threeRI();
            } else if ($path === "/v1/auth/challenge/status") {
                $this->authControllerV1->challengeStatus();
            } else if ($path === "/v1/auth/enrol") {
                $this->authControllerV1->enrolCheck();
            }

        } else if (startsWith($path, '/v2/auth')) {
            if ($path === "/v2/auth/init") {
                $this->authControllerV2->initAuth();
            } else if ($path === "/v2/auth/result") {
                $this->authControllerV2->result();
            } else if ($path === "/v2/auth") {
                $this->authControllerV2->auth();
            } else if ($path === "/v2/auth/3ri") {
                $this->authControllerV2->threeRI();
            } else if ($path === "/v2/auth/challenge/status") {
                $this->authControllerV2->challengeStatus();
            } else if ($path === "/v2/auth/app") {
                $this->authControllerV2->app();
            } else if ($path === "/v2/auth/enrol") {
                $this->authControllerV2->enrolCheck();
            }
        } else {
            if (empty($path) || $path === "/") {
                $this->mainController->index();
            } else if ($path === "/shop") {
                $this->mainController->shop();
            } else if ($path === "/checkout") {
                $this->mainController->checkout();
            } else if ($path === "/v1/process") {
                $this->mainController->processV1();
            } else if ($path === "/v2/process") {
                $this->mainController->processV2();
            } else if ($path === "/brw") {
                $this->mainController->brw();
            } else if ($path === "/enrol") {
                $this->mainController->enrol();
            } else if ($path === "/3ri") {
                $this->mainController->threeRI();
            } else if ($path === "/v1/result") {
                $this->mainController->resultV1();
            } else if ($path === "/v2/result") {
                $this->mainController->resultV2();
            } else if ($path === "/3ds-notify") {
                $this->mainController->notifyResult();
            } else if ($path === "/auth/enrol") {
                $this->mainController->enrolCheck();
            } else if ($path === "/app") {
                $this->mainController->app();
            }

        }
    }
}

// Function to check string starting
// with given substring
function startsWith($string, $startString)
{
    $len = strlen($startString);
    return (substr($string, 0, $len) === $startString);
}