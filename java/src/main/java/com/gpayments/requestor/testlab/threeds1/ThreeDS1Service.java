package com.gpayments.requestor.testlab.threeds1;

import com.gpayments.requestor.testlab.AuthServiceV2;
import com.gpayments.requestor.testlab.config.Config;
import com.gpayments.requestor.testlab.dto.threeds1.ThreeDS1AuthReq;
import com.gpayments.requestor.testlab.dto.threeds1.ThreeDS1AuthResp;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 3DSecure 1.0.2 service class
 *
 * @author GPayments
 */
@Service
public class ThreeDS1Service {

  private static final Logger logger = LoggerFactory.getLogger(ThreeDS1Service.class);

  private final RestTemplate restTemplate;
  private final String authUrl;
  private final Config config;

  @Autowired
  public ThreeDS1Service(RestTemplate restTemplate,
      Config config) {
    this.restTemplate = restTemplate;
    this.authUrl = config.getAsAuthUrl() + "/api/v2/auth/3ds1";
    this.config = config;
  }


  ThreeDS1AuthResp handleAuthRequest(ThreeDS1AuthReq request) {

    //generate the transaction id, this is optional.
    request.setThreeDSRequestorTransID(UUID.randomUUID().toString());

    logger.info("sending 3ds1 auth request to ActiveServer: {}", authUrl);

    HttpEntity<ThreeDS1AuthReq> httpRequest;
    if (config.isGroupAuth()) {
      HttpHeaders headers = new HttpHeaders();
      headers.add(AuthServiceV2.AS_MERCHANT_TOKEN_HEADER, config.getMerchantToken());
      httpRequest = new HttpEntity<>(request, headers);
    } else {

      httpRequest = new HttpEntity<>(request);
    }

    ResponseEntity<ThreeDS1AuthResp> response = restTemplate
        .postForEntity(authUrl, httpRequest, ThreeDS1AuthResp.class);

    if (response.getStatusCode() == HttpStatus.OK) {

      ThreeDS1AuthResp body = response.getBody();
      logger.info("server returns ok, content: {}", body);

      return body;

    } else {
      ThreeDS1AuthResp body = response.getBody();
      logger.error("server returns error code: {}, content: {}", response.getStatusCode(),
          body);
      return body;
    }

  }


}
