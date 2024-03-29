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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author GPayments ON 31/07/2018 MainConroller class with handler methods.
 */
@Controller
@RequestMapping("/")
public class MainController {

  private static final Logger logger = LoggerFactory.getLogger(MainController.class);
  private final Config config;

  private static final String CALLBACK_URL = "callbackUrl";
  private static final String SERVER_URL = "serverUrl";
  private static final String TRANS_ID = "transId";

  @Autowired
  public MainController(Config config) {
    this.config = config;
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

    model.addAttribute(CALLBACK_URL, config.getBaseUrl());
    model.addAttribute(SERVER_URL, config.getAsAuthUrl());

    return "brw";
  }

  @GetMapping("/enrol")
  public String enrol(Model model) {

    model.addAttribute(SERVER_URL, config.getAsAuthUrl());
    model.addAttribute(CALLBACK_URL, config.getBaseUrl());

    return "enrol";
  }

  @GetMapping("/3ri")
  public String threeRI(Model model) {

    model.addAttribute(CALLBACK_URL, config.getBaseUrl());
    model.addAttribute(SERVER_URL, config.getAsAuthUrl());

    return "3ri";
  }

  @GetMapping("/app")
  public String app(Model model) {

    model.addAttribute(CALLBACK_URL, config.getBaseUrl());
    model.addAttribute(SERVER_URL, config.getAsAuthUrl());

    return "app";
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
      @RequestParam(name = "threeDSSessionData", required = false) String threeDSSessionData,
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

    } else if ("3DSMethodHasError".equals(callbackType)) {

      //Event 3DSMethodHasError is only for logging and troubleshooting purpose, this demo
      //sets the callbackName to be _NA so the frontend won't process it.
      callbackName = "_NA";

    } else {
      // When unrecognised event has been received, a callbackName like "_NA" can be returned (so the frontend won't recognise it)
      // to make the callback process more robust and resilient.
      // Alternatively, the 3DS Requestor backend implementation may choose to throw an exception to indicate this error
      // however the frontend must be able to handle the exception so that the checkout page flow won't be interrupted
      callbackName = "_NA";


    }

    model.addAttribute(TRANS_ID, transId);
    model.addAttribute("callbackName", callbackName);
    model.addAttribute("callbackParam", param);

    // threeDSSessionData will only be available in the Challenge result callbacks.
    // The code below is to avoid a Mustache template error when the threeDSSessionData is null
    model.addAttribute("callbackThreeDSSessionData",
        StringUtils.hasLength(threeDSSessionData) ? threeDSSessionData : "");

    return "notify_3ds_events";
  }

  @GetMapping("/noscript")
  public String noScript() {
    return "no_script";
  }


  @PostMapping("/3ds-notify/noscript")
  public String notify(
      @RequestParam("requestorTransId") String transId,
      @RequestParam("event") String callbackType,
      @RequestParam("param") String param,
      RedirectAttributes ra) {

    if ("3DSMethodFinished".equals(callbackType)
        || "3DSMethodSkipped".equals(callbackType)
        || "InitAuthTimedOut".equals(callbackType)) {

      logger.info("{}, continue to do authentication", callbackType);

      ra.addAttribute(TRANS_ID, transId);
      ra.addAttribute("param", param);
      return "redirect:/v2/auth/noscript";

    } else if ("AuthResultReady".equals(callbackType)) {

      logger.info("{}, continue to get result", callbackType);

      ra.addAttribute(TRANS_ID, transId);
      return "redirect:/v2/auth/brw/result/noscript";

    } else {
      throw new IllegalArgumentException("invalid notifyCallback");
    }

  }
}
