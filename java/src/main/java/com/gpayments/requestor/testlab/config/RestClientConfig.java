
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
 *  Written by GPayments <techsupport@gpayments.com>, 2019
 *
 *
 */

package com.gpayments.requestor.testlab.config;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

  private static final String KEYSTORE_PASSWORD = "123456";
  private static final String KEY_ENTRY_PASSWORD = "123456";
  private static final String CA_CERTS_FILE_NAME = "certs/cacerts.pem";

  @Bean
  public RestTemplate restTemplate(Config config)
      throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
      KeyStoreException, KeyManagementException {

    SSLContext sslContext =
        SSLContextBuilder.create()
            .loadKeyMaterial(
                new File(config.getCertFileName()),
                KEYSTORE_PASSWORD.toCharArray(),
                KEY_ENTRY_PASSWORD.toCharArray())
            .loadTrustMaterial(getCAKeystore(CA_CERTS_FILE_NAME), new TrustSelfSignedStrategy())
            .build();

    CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext).build();
    HttpComponentsClientHttpRequestFactory httpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(client);

    return new RestTemplate(httpRequestFactory);
  }

  private KeyStore getCAKeystore(String fileName)
      throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {

    CertificateFactory factory = CertificateFactory.getInstance("X.509");

    Collection<? extends Certificate> certificates =
        factory.generateCertificates(new ClassPathResource(fileName).getInputStream());

    KeyStore keystore = KeyStore.getInstance("JKS");
    keystore.load(null);

    int id = 0;
    for (Certificate cert : certificates) {
      keystore.setCertificateEntry("cert-alias-" + (++id), cert);
    }

    return keystore;
  }
}
