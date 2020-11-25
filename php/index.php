<?php session_start();
echo session_save_path();

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
require_once "config/Config.php";
require_once "config/RestClientConfig.php";
require_once "config/TemplateResolver.php";
require_once "config/Router.php";
require_once "config/Utils.php";
require_once "consts/SessionKeys.php";

require_once "controllers/AuthControllerV1.php";
require_once "services/AuthServiceV2.php";
require_once "controllers/AuthControllerV2.php";
require_once "controllers/MainController.php";

require_once 'vendor/autoload.php';

$config = new Config();
$router = new Router($config);
$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$router->route($path);
