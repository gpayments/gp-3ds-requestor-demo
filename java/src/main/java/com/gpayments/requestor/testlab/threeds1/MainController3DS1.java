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

package com.gpayments.requestor.testlab.threeds1;

import com.gpayments.requestor.testlab.config.Config;
import com.gpayments.requestor.testlab.dto.threeds1.ThreeDS1AuthReq;
import com.gpayments.requestor.testlab.dto.threeds1.ThreeDS1AuthResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * MainController class for 3DSecure 1.0.2 transactions.
 *
 * @author GPayments
 */
@Controller
@RequestMapping("/")
public class MainController3DS1 {

  private static final Logger logger = LoggerFactory.getLogger(MainController3DS1.class);
  private final Config config;
  private final ThreeDS1Service threeDS1Service;

  @Autowired
  public MainController3DS1(Config config,
      ThreeDS1Service threeDS1Service) {
    this.config = config;
    this.threeDS1Service = threeDS1Service;
  }

  @GetMapping("/3ds1")
  public String paymentPage(Model model) {
    model.addAttribute("authUrl", config.getAsAuthUrl());
    model.addAttribute("callbackUrl", config.getBaseUrl() + "/3ds1/result");

    logger.info("3ds1 auth page called");
    return "3ds1/auth";
  }

  @PostMapping("/3ds1/result")
  public String resultPage(Model model, @RequestBody MultiValueMap<String, String> body) {
    logger.info("received result: {}", body);

    model.addAttribute("errorCode", body.getFirst("errorCode"));
    model.addAttribute("errorMessage", body.getFirst("errorMessage"));
    model.addAttribute("txStatus", body.getFirst("txStatus"));
    model.addAttribute("cavv", body.getFirst("cavv"));
    model.addAttribute("cavvAlgo", body.getFirst("cavvAlgo"));
    model.addAttribute("eci", body.getFirst("eci"));
    model.addAttribute("threeDSRequestorTransID", body.getFirst("threeDSRequestorTransID"));
    return "3ds1/result";
  }

  /**
   * Return JSON response
   */
  @ResponseBody
  @PostMapping(value = "/3ds1/auth")
  public ThreeDS1AuthResp auth(@RequestBody ThreeDS1AuthReq req) {
    logger.info("3ds1 auth request received: {}", req);
    return threeDS1Service.handleAuthRequest(req);
  }


}
