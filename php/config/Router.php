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

class Router
{
    private $mainController;
    private $authController;

    function __construct($config)
    {
        $restTemplate = new RestClientConfig($config);
        $this->mainController  = new MainController($config);
        $this->authController = new AuthController($config, $restTemplate);
    }

    public function route($path) {
        // It starts with '/auth'
        if (startsWith($path, '/auth')) {
            if ($path === "/auth/init/pa") {
                $this->authController->initAuth("pa");
            } else if ($path === "/auth/init/npa") {
                $this->authController->initAuth("npa");
            } else if ($path === "/auth/result") {
                $this->authController->result();
            } else if ($path === "/auth") {
                $this->authController->auth();
            }  else if ($path ==="/auth/3ri") {
                $this->authController->threeRI();
            } else if ($path === "/auth/enrol") {
                $this->authController->enrol();
            } else if ($path === "/auth/challenge/status") {
                $this->authController->challengeStatus();
            }

        } else {
            if (empty($path) || $path === "/") {
                $this->mainController->index();
            } else if ($path === "/shop") {
                $this->mainController->shop();
            } else if ($path === "/checkout") {
                $this->mainController->checkout();
            } else if ($path === "/process") {
                $this->mainController->process();
            } else if ($path === "/brw") {
                $this->mainController->brw();
            } else if ($path ===  "/enrol") {
                $this->mainController->enrol();
            } else if ($path ===  "/3ri") {
                $this->mainController->threeRI();
            } else if ($path === "/result") {
                $this->mainController->result();
            } else if ($path === "/3ds-notify") {
                $this->mainController->notifyResult();
            }
        }
    }
}
// Function to check string starting
// with given substring
function startsWith ($string, $startString)
{
    $len = strlen($startString);
    return (substr($string, 0, $len) === $startString);
}