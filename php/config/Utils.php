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
class Utils
{
    public static function _getJsonData()
    {
        $json = file_get_contents('php://input');
        return json_decode($json);
    }

    public static function _getUUId()
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

    public static function _returnJson($response, $statusCode = 200)
    {
        header('Content-Type: application/json;charset=utf-8', true, $statusCode);
        echo $response;
        exit;
    }

    /**
     * Redirects to the given URL
     * @param $url string be a path all whole domain
     * @param bool $permanent true if 301 permanent redirection, default false.
     */
    public static function _redirect($url, $permanent = false)
    {
//        echo "redirecting to ".'Location: ' . $url;

        header('Location: ' . $url, true, $permanent ? 301 : 302);

        exit();
    }

}