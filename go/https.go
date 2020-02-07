/*
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

package main

import (
	"crypto"
	"crypto/tls"
	"crypto/x509"
	"io/ioutil"
	"net/http"
	pkcs122 "software.sslmate.com/src/go-pkcs12"
	"time"
)

const (
	keystorePassword = "123456"
)

func createHttpClient(config *Config) (*http.Client, error) {
	// the CertPool wants to add a root as a []byte so we read the file ourselves
	caCert, err := ioutil.ReadFile("conf/cacerts.pem")
	if err != nil {
		return nil, err
	}

	pool := x509.NewCertPool()
	pool.AppendCertsFromPEM(caCert)

	p12, err := ioutil.ReadFile(config.GPayments.CertFileName)
	if err != nil {
		return nil, err
	}

	// Load the client certificate
	privateKey, clientCert, _, err := pkcs122.DecodeChain(p12, keystorePassword)

	if err != nil {
		return nil, err
	}

	cert := tls.Certificate{PrivateKey: privateKey.(crypto.PrivateKey), Certificate: [][]byte{clientCert.Raw}}
	tlsConfig := tls.Config{
		RootCAs: pool,
		Certificates: []tls.Certificate{

			cert},
	}
	transport := http.Transport{
		TLSClientConfig: &tlsConfig,
	}
	return &http.Client{
		Transport: &transport,
		Timeout:   10 * time.Second,
	}, nil
}
