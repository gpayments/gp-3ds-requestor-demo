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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 * @author GPayments ON 31/07/2018 MainConroller class with handler methods.
 */
@Controller
@RequestMapping("/")
public class MainController {

  private static final Logger logger = LoggerFactory.getLogger(AuthControllerV1.class);
  private final Config config;
  private final RestTemplate restTemplate;

  @Autowired
  public MainController(Config config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }

  @RequestMapping
  public String main() {
    return "index";
  }

  @GetMapping("/shop")
  public String shop() {
    return "shop";
  }

  @GetMapping("/checkout")
  public String checkout() {
    return "checkout";
  }

  @GetMapping("/v1/process")
  public String processV1() {
    return "v1/process";
  }

  @GetMapping("/v2/process")
  public String processV2() {
    return "v2/process";
  }

  @GetMapping("/brw")
  public String authentication(Model model) {

    model.addAttribute("callbackUrl", config.getBaseUrl());
    model.addAttribute("serverUrl", config.getAsAuthUrl());

    return "brw";
  }

  @GetMapping("/enrol")
  public String enrol(Model model) {

    model.addAttribute("serverUrl", config.getAsAuthUrl());
    model.addAttribute("callbackUrl", config.getBaseUrl());

    return "enrol";
  }

  @GetMapping("/3ri")
  public String threeRI(Model model) {

    model.addAttribute("callbackUrl", config.getBaseUrl());
    model.addAttribute("serverUrl", config.getAsAuthUrl());

    return "3ri";
  }

  @GetMapping("/v1/result")
  public String resultV1() {
    return "v1/result";
  }

  @GetMapping("/v2/result")
  public String resultV2() {
    return "v2/result";
  }

  @PostMapping("/3ds-notify")
  public String notifyResult(
      @RequestParam("requestorTransId") String transId,
      @RequestParam("event") String callbackType,
      @RequestParam(name = "param", required = false) String param,
      Model model) {

    String callbackName;
    if ("3DSMethodFinished".equals(callbackType)) {

      callbackName = "_on3DSMethodFinished";

    } else if ("3DSMethodSkipped".equals(callbackType)) {

      callbackName = "_on3DSMethodSkipped";

    } else if ("AuthResultReady".equals(callbackType)) {

      callbackName = "_onAuthResult";

    } else if ("InitAuthTimedOut".equals(callbackType)) {

      callbackName = "_onInitAuthTimedOut";

    } else {
      throw new IllegalArgumentException("invalid callback type");
    }

    model.addAttribute("transId", transId);
    model.addAttribute("callbackName", callbackName);
    model.addAttribute("callbackParam", param);

    return "notify_3ds_events";
  }

  @PostMapping("/auth/enrol")
  @ResponseBody
  public Message enrolTest(@RequestBody Message request) {

    String enrolUrl = config.getAsAuthUrl() + "/api/v1/auth/enrol";
    logger.info("enrol on url: {}, body: \n{}", enrolUrl, request);

    Message response = restTemplate.postForObject(enrolUrl, request, Message.class);
    logger.info("enrolResponse: \n{}", response);
    return response;
  }
}
