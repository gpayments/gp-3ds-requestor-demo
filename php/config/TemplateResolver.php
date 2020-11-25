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
class TemplateResolver
{

    private $engine;

    function __construct()
    {
        Mustache_Autoloader::register();

        $options = array('extension' => '.html');
        $this->engine = new Mustache_Engine(array(
            'loader' => new Mustache_Loader_FilesystemLoader(dirname(__DIR__) . '/resources/templates', $options),
        ));
    }

    public function _render($template, $model = array())
    {
        echo $this->engine->render($template, $model);
        exit;
    }

}