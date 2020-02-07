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

package com.gpayments.requestor.testlab;

import com.gpayments.requestor.testlab.config.Config;
import com.gpayments.requestor.testlab.dto.Message;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/")
public class AuthControllerV2 {

  private static final Logger logger = LoggerFactory.getLogger(AuthControllerV2.class);
  private final RestTemplate restTemplate;
  private final Config config;

  @Autowired
  public AuthControllerV2(
      RestTemplate restTemplate,
      Config config) {
    this.restTemplate = restTemplate;
    this.config = config;
  }

  /**
   * Receives the initialise authentication request from the 3DS-web-adapter (Step 2) Send data to
   * ActiveServer to Initialise Authentication
   */
  @PostMapping("/v2/auth/init")
  public Message initAuth(@RequestBody Message request, HttpSession session) {

    //Generate requestor trans ID
    String transId = UUID.randomUUID().toString();
    request.put("threeDSRequestorTransID", transId);
    //Fill the event call back url with requestor url + /3ds-notify
    String callBackUrl = config.getBaseUrl() + "/3ds-notify";
    request.put("eventCallbackUrl", callBackUrl);

    //ActiveServer url for Initialise Authentication
    String initAuthUrl = config.getAsAuthUrl() + "/api/v2/auth/brw/init";
    logger.info("initAuthRequest on url: {}, body: \n{}", initAuthUrl, request);

    //Send data to ActiveServer to Initialise authentication (Step 3)
    //Get the response data from ActiveServer (Step 4)
    Message response =
        sendRequest(initAuthUrl, request, HttpMethod.POST);
    logger.info("initAuthResponseBRW: \n{}", response);

    if (response != null) {
      //save initAuth response into session storage
      session.setAttribute((String) response.get("threeDSServerTransID"),
          response);
    } else {
      logger.error("Error in initAuth response");
    }

    //Return data to 3ds-web-adapter (Step 5)
    return response;
  }


  /**
   * Receives the Execute authentication request from the 3DS-web-adapter (Step 9) Send data to
   * ActiveServer to Execute Authentication
   */
  @PostMapping("/v2/auth")
  public Message auth(@RequestBody Message request, HttpSession session) {
    //get authUrl from session storage
    Message initAuthResponse = (Message) session
        .getAttribute((String) request.get("threeDSServerTransID"));
    String authUrl = (String) initAuthResponse.get("authUrl");
    logger.info("requesting BRW Auth API {}, body: \n{}", authUrl, request);

    //Send data to ActiveServer to Execute Authentication (Step 10)
    //Get the response data from ActiveServer (Step 12)
    Message response =
        sendRequest(authUrl, request, HttpMethod.POST);
    logger.info("authResponseBRW: \n{}", response);

    //Return data to 3ds-web-adapter (Step 13)
    return response;
  }

  @PostMapping("/v2/auth/challenge/status")
  public Message challengeStatus(@RequestBody Message request) {
    String challengeStatusUrl = config.getAsAuthUrl() + "/api/v2/auth/challenge/status";
    logger.info("request challenge status API {}, body: \n{}", challengeStatusUrl, request);

    Message response =
        sendRequest(challengeStatusUrl, request, HttpMethod.POST);
    logger.info("challengeStatus response: \n{}", response);

    return response;
  }

  /**
   * Receives the Request for authentication result request (Step 15(F) and Step 20(C)) Send data to
   * ActiveServer to Retrieve Authentication Results
   */
  @GetMapping("/v2/auth/result")
  public Message result(@RequestParam("txid") String serverTransId) {
    //ActiveServer url for Retrieve Results
    String resultUrl = config.getAsAuthUrl() +
        "/api/v2/auth/brw/result?threeDSServerTransID=" +
        serverTransId;

    //Get authentication result from ActiveServer (Step 16(F) and Step 21(C))
    Message response =
        sendRequest(resultUrl, null, HttpMethod.GET);
    logger.info("authResponse: \n{}", response);

    //Show authentication results on result.html (Step 17(F) and Step 22(C))
    return response;
  }


  @PostMapping("/v2/auth/3ri")
  public Message threeRITest(@RequestBody Message request) {
    //generate requestor trans ID
    String transId = UUID.randomUUID().toString();
    request.put("threeDSRequestorTransID", transId);

    String threeRIUrl = config.getAsAuthUrl() + "/api/v2/auth/3ri";
    logger.info("authRequest3RI on url: {}, body: \n{}", threeRIUrl, request);

    Message response =
        sendRequest(threeRIUrl, request, HttpMethod.POST);
    logger.info("authResponse3RI: \n{}", response);
    return response;
  }

  /**
   * Send request to ActiveServer, if groupAuth is enabled, workout the required header.
   */
  private Message sendRequest(String url, Message request, HttpMethod method) {

    HttpEntity<Message> req;
    HttpHeaders headers = null;

    if (config.isGroupAuth()) {
      //the certificate is for groupAuth, work out the header.
      headers = new HttpHeaders();
      headers.add("AS-Merchant-Token", config.getMerchantToken());
    }

    switch (method) {
      case POST:
        req = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(url, req, Message.class);
      case GET:
        if (headers == null) {
          return restTemplate.getForObject(url, Message.class);
        } else {
          req = new HttpEntity<>(headers);
          return restTemplate.exchange(url, HttpMethod.GET, req, Message.class).getBody();
        }
      default:
        return null;
    }


  }
}
