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
import com.gpayments.requestor.testlab.dto.AuthDataNoScriptDTO;
import com.gpayments.requestor.testlab.dto.Message;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceV2 {

  private static final Logger logger = LoggerFactory.getLogger(AuthServiceV2.class);

  public static final String AS_MERCHANT_TOKEN_HEADER = "AS-Merchant-Token";

  private static final String THREE_DS_REQUESTOR_TRANS_ID = "threeDSRequestorTransID";
  private static final String THREE_DS_SERVER_TRANS_ID = "threeDSServerTransID";
  private static final String INIT_AUTH_REQUEST = "initAuthRequest";
  private static final String INIT_AUTH_REQUEST_NOSCRIPT = "initAuthRequestNoscript";
  private static final String INIT_AUTH_RESPONSE = "initAuthResponse";
  private static final String AUTH_REQUEST = "authRequest";
  private static final String AUTH_RESPONSE = "authResponse";
  private final RestTemplate restTemplate;
  private final Config config;

  @Autowired
  public AuthServiceV2(
      RestTemplate restTemplate, Config config) {
    this.restTemplate = restTemplate;
    this.config = config;
  }

  /**
   * Handle initAuth request
   */
  public Message initAuth(String transType, Message request, HttpSession session) {

    //Generate requestor trans ID
    String transId = UUID.randomUUID().toString();
    request.put(THREE_DS_REQUESTOR_TRANS_ID, transId);
    //Fill the event call back url with requestor url + /3ds-notify
    String callBackUrl = config.getBaseUrl() + "/3ds-notify";
    request.put("eventCallbackUrl", callBackUrl);

    //Send data to ActiveServer to Initialise authentication (Step 3)
    //Get the response data from ActiveServer (Step 4)
    Message response = sendInitAuthRequest(transType, request, session);
    response.put(THREE_DS_REQUESTOR_TRANS_ID, transId);
    logger.info("initAuthResponseBRW: \n{}", response);

    return response;
  }

  /**
   * Send data to ActiveServer to Initialise authentication and get the response data from
   * ActiveServer.
   *
   * @param transType: transType=prod to use prod DS, otherwise use TestLabs DS
   * @param request:   init auth request
   * @return init auth response
   */
  private Message sendInitAuthRequest(String transType, Message request, HttpSession session) {

    setSessionAttribute(INIT_AUTH_REQUEST, request, session);

    //ActiveServer url for Initialise Authentication
    String initAuthUrl = config.getAsAuthUrl() + "/api/v2/auth/brw/init";
    //Add parameter trans-type=prod in the initAuthUrl to use prod DS, otherwise use TestLabs DS
    //For example, in this demo, the initAuthUrl for transactions with prod DS is https://api.as.testlab.3dsecure.cloud:7443/api/v2/auth/brw/init?trans-type=prod
    //For more details, refer to: https://docs.activeserver.cloud
    if ("prod".equals(transType)) {
      initAuthUrl = initAuthUrl + "?trans-type=prod";
    }

    initAuthUrl = createUrlWithExtraParams(initAuthUrl, request);

    logger.info("initAuthRequest on url: {}, body: \n{}", initAuthUrl, request);

    Message response = sendRequest(initAuthUrl, request, HttpMethod.POST);

    if (response != null) {
      setSessionAttribute(INIT_AUTH_RESPONSE, response, session);
    } else {
      throw new IllegalArgumentException("Invalid init auth response");
    }

    return response;
  }


  /**
   * Handle auth request
   */
  public Message auth(Message request, HttpSession session) {

    verifySessionTransId((String) request.get(THREE_DS_REQUESTOR_TRANS_ID), session);

    //Send data to ActiveServer to Execute Authentication (Step 10)
    //Get the response data from ActiveServer (Step 12)
    Message response = sendAuthRequest(request, session);
    logger.info("authResponseBRW: \n{}", response);

    return response;
  }

  private Message sendAuthRequest(Message request, HttpSession session) {

    setSessionAttribute(AUTH_REQUEST, request, session);

    //get authUrl from session storage
    Message initAuthResponse = getSessionAttribute(INIT_AUTH_RESPONSE, session);
    String authUrl = (String) initAuthResponse.get("authUrl");
    logger.info("requesting BRW Auth API {}, body: \n{}", authUrl, request);

    Message response = sendRequest(authUrl, request, HttpMethod.POST);

    if (response != null) {
      setSessionAttribute(AUTH_RESPONSE, response, session);
    } else {
      throw new IllegalArgumentException("Invalid auth response");
    }

    return response;
  }

  public Message challengeStatus(Message request) {
    // Experimental params is for GPayments internal use only - not required, can be safely ignored
    String challengeStatusUrl = createUrlWithExtraParams(config.getAsAuthUrl() + "/api/v2/auth/challenge/status", request) ;

    logger.info("request challenge status API {}, body: \n{}", challengeStatusUrl, request);

    Message response =
        sendRequest(challengeStatusUrl, request, HttpMethod.POST);
    logger.info("challengeStatus response: \n{}", response);

    return response;
  }

  public Message getBRWResult(String serverTransId, String extraParams) {
    //ActiveServer url for Retrieve Results
    String resultUrl = config.getAsAuthUrl() +
            "/api/v2/auth/brw/result?threeDSServerTransID=" + serverTransId;

    // Experimental params is for GPayments internal use only - not required, can be safely ignored
    if(StringUtils.hasLength(extraParams)){
      resultUrl = resultUrl + "&" + extraParams;
    }


    //Get authentication result from ActiveServer
    Message response = sendRequest(resultUrl, null, HttpMethod.GET);
    logger.info("authResponse: \n{}", response);

    return response;
  }

  public Message threeRI(String transType, Message request) {
    //generate requestor trans ID
    String transId = UUID.randomUUID().toString();
    request.put(THREE_DS_REQUESTOR_TRANS_ID, transId);

    String threeRIUrl = config.getAsAuthUrl() + "/api/v2/auth/3ri";

    //Add parameter trans-type=prod to use prod DS, otherwise use TestLabs DS
    if ("prod".equals(transType)) {
      threeRIUrl = threeRIUrl + "?trans-type=prod";
    }

    // Experimental params is for GPayments internal use only - not required, can be safely ignored
    threeRIUrl = createUrlWithExtraParams(threeRIUrl, request);

    logger.info("authRequest3RI on url: {}, body: \n{}", threeRIUrl, request);

    Message response =
        sendRequest(threeRIUrl, request, HttpMethod.POST);
    logger.info("authResponse3RI: \n{}", response);
    return response;
  }

  /**
   * Retrieves the 3RI authentication result.
   *
   * @param serverTransId The server transaction ID
   * @param extraParams Experimental params is for GPayments internal use only - not required, can be safely ignored
   * @return The authentication result message
   */
  public Message get3RIResult(String serverTransId, String extraParams) {

    String resultUrl = config.getAsAuthUrl() +
        "/api/v2/auth/3ri/result?threeDSServerTransID=" +
        serverTransId;

    // Experimental params is for GPayments internal use only - not required, can be safely ignored
    if(StringUtils.hasLength(extraParams)){
      resultUrl = resultUrl + "&" + extraParams;
    }

    Message response =
        sendRequest(resultUrl, null, HttpMethod.GET);
    logger.info("authResponse: \n{}", response);

    return response;
  }

  public Message app(String transType, Message request) {

    //generate requestor trans ID
    String transId = UUID.randomUUID().toString();
    request.put(THREE_DS_REQUESTOR_TRANS_ID, transId);

    String appAuthUrl = config.getAsAuthUrl() + "/api/v2/auth/app";

    //Add parameter trans-type=prod in the appAuthUrl to use prod DS, otherwise use TestLabs DS
    //For more details, refer to: https://docs.activeserver.cloud
    if ("prod".equals(transType)) {
      appAuthUrl = appAuthUrl + "?trans-type=prod";
    }

    // Experimental params is for GPayments internal use only - not required, can be safely ignored
    appAuthUrl = createUrlWithExtraParams(appAuthUrl, request);

    logger.info("appAuthRequest on url: {}, body: \n{}", appAuthUrl, request);

    Message response =
        sendRequest(appAuthUrl, request, HttpMethod.POST);
    logger.info("appAuthResponse: \n{}", response);
    return response;
  }

  public Message enrol(String transType, Message request) {

    String enrolUrl = config.getAsAuthUrl() + "/api/v2/auth/enrol";

    //Add parameter trans-type=prod to use prod DS, otherwise use TestLabs DS
    if ("prod".equals(transType)) {
      enrolUrl = enrolUrl + "?trans-type=prod";
    }

    // Experimental params is for GPayments internal use only - not required, can be safely ignored
    enrolUrl = createUrlWithExtraParams(enrolUrl, request);

    logger.info("enrol on url: {}, body: \n{}", enrolUrl, request);

    Message response =
        sendRequest(enrolUrl, request, HttpMethod.POST);
    logger.info("enrolResponse: \n{}", response);
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
      headers.add(AS_MERCHANT_TOKEN_HEADER, config.getMerchantToken());
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

  private Message getSessionAttribute(String key, HttpSession session) {
    return (Message) session.getAttribute(key);
  }

  private void setSessionAttribute(String key, Message data, HttpSession session) {
    session.setAttribute(key, data);
  }

  /**
   * Handle noscript initAuth request
   */
  public String initAuthNoScript(String transType, AuthDataNoScriptDTO authData, Model model,
      HttpSession session) {

    setSessionAttribute(INIT_AUTH_REQUEST_NOSCRIPT, authData.toMessage(), session);

    Message request = new Message();
    request.put("acctNumber", authData.getAcctNumber());
    request.put("merchantId", authData.getMerchantId());

    String transId = UUID.randomUUID().toString();
    request.put(THREE_DS_REQUESTOR_TRANS_ID, transId);
    String callBackUrl = config.getBaseUrl() + "/3ds-notify/noscript";
    request.put("eventCallbackUrl", callBackUrl);

    Message response = sendInitAuthRequest(transType, request, session);
    logger.info("initAuthResponseBRW: \n{}", response);

    model.addAttribute("threeDSServerCallbackUrl", response.get("threeDSServerCallbackUrl"));
    return "no_script_process";
  }

  /**
   * Handle noscript auth request
   */
  public String authNoScript(String transId, String param, Model model, HttpSession session) {

    logger.info("Do authentication for transId:{}", transId);

    verifySessionTransId(transId, session);

    Message authData = getSessionAttribute(INIT_AUTH_REQUEST_NOSCRIPT, session);

    authData.put(THREE_DS_REQUESTOR_TRANS_ID, transId);
    authData.put(THREE_DS_SERVER_TRANS_ID,
        getSessionAttribute(INIT_AUTH_RESPONSE, session).get(THREE_DS_SERVER_TRANS_ID));
    authData.put("browserInfo", param);

    Message response = sendAuthRequest(authData, session);

    logger.info("authResponseBRW: \n{}", response);

    if ("C".equals(response.get("transStatus"))) {

      return "redirect:" + response.get("challengeUrl");

    } else if ("D".equals(response.get("transStatus"))) {

      return pollResultNoScript(transId, model, session);
    }

    model.addAttribute("result", response);
    return "no_script_results";
  }

  /**
   * Handle noscript polling request
   */
  public String pollResultNoScript(String transId, Model model, HttpSession session) {

    logger.info("Polling result for transId:{}", transId);

    Message authResponse = getSessionAttribute(AUTH_RESPONSE, session);

    Message monResponse =
        sendRequest((String) authResponse.get("resultMonUrl"), null, HttpMethod.GET);
    logger.info("monResponse: \n{}", monResponse);

    if (monResponse != null && "AuthResultNotReady".equals(monResponse.get("event"))) {

      model.addAttribute("transId", transId);
      model.addAttribute("cardholderInfo", authResponse.get("cardholderInfo"));

      //the no_script_poll_result.html page will auto-fresh every second.
      return "no_script_poll_result";

    } else if (monResponse != null && "AuthResultReady".equals(monResponse.get("event"))) {

      return "redirect:/v2/auth/brw/result/noscript?transId=" + transId;

    } else {
      throw new IllegalArgumentException("Invalid mon url result event type");
    }
  }

  /**
   * Handle noscript result request
   */
  public String resultNoScript(String transId, Model model, HttpSession session) {

    logger.info("Get result for transId:{}", transId);

    verifySessionTransId(transId, session);

    String serverTransId = (String) getSessionAttribute(INIT_AUTH_RESPONSE, session)
        .get(THREE_DS_SERVER_TRANS_ID);
    // Experimental params is for GPayments internal use only - not required, can be safely ignored
    Message response = getBRWResult(serverTransId, null);
    model.addAttribute("result", response);
    return "no_script_results";
  }

  private void verifySessionTransId(String transId, HttpSession session) {
    Message initAuthRequest = getSessionAttribute(INIT_AUTH_REQUEST, session);
    if (!transId.equals(initAuthRequest.get(THREE_DS_REQUESTOR_TRANS_ID))) {
      throw new IllegalArgumentException("Invalid 3DS Requestor trans Id");
    }
  }

  /**
   * Experimental params is for GPayments internal use only - not required, can be safely ignored.
   * Appends extra parameters to the given URL if present.
   *
   * @param url The base URL to append parameters to
   * @param request The request message that may contain "extraParams" key
   * @return The URL with extra parameters appended, or the original URL if no extra params exist
   */
  private String createUrlWithExtraParams(String url, Message request){
    if (request != null && request.containsKey("extraParams")){
      String extraParam = (String) request.get("extraParams");
      return url + (url.contains("?") ? "&" : "?") + extraParam;
    }
    return url;
  }

}
