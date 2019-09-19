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

class RestClientConfig
{

    const CA_CERTS_FILE_PATH = "resources/certs/cacerts.pem";

    private $client = null;
    private $headers = [];

    public function __construct(Config $config)
    {
        $this->client = new GuzzleHttp\Client([
            'base_uri' => $config->getAsAuthUrl(),
            'cert' => [$config->getCertFileName(), ''],
            'ssl_key' => $config->getKeyFileName(),
            "verify" => self::CA_CERTS_FILE_PATH,
        ]);

        if ($config->isGroupAuth()) {
            $this->headers = array('AS-Merchant-Token' => $config->getMerchantToken());
        }
    }

    function post($request_uri, $post_data)
    {
        $response = $this->client->request(
            "POST",
            $request_uri,
            ['json' => $post_data, 'headers' => $this->headers]);
        return $response;
    }

    function get($request_uri)
    {
        $response = $this->client->request("GET", $request_uri, ['headers' => $this->headers]);
        return $response;
    }

}


