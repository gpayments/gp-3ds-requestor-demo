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

package com.gpayments.requestor.testlab;

import com.gpayments.requestor.testlab.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author GPayments ON 31/07/2018 MainConroller class with handler methods.
 */
@Controller
@RequestMapping("/")
public class MainController {

  private final Config config;

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

  @GetMapping("/process")
  public String process() {
    return "process";
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

  @GetMapping("/result")
  public String result() {
    return "result";
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

}
