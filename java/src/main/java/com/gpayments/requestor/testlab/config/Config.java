
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "gpayments")
public class Config {

  private String asAuthUrl;
  private String baseUrl;
  private String certFileName;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getAsAuthUrl() {
    return asAuthUrl;
  }

  public void setAsAuthUrl(String asAuthUrl) {
    this.asAuthUrl = asAuthUrl;
  }

  public String getCertFileName() {
    return certFileName;
  }

  public void setCertFileName(String certFileName) {
    this.certFileName = certFileName;
  }
}
