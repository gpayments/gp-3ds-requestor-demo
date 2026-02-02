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

import com.gpayments.requestor.testlab.dto.AuthDataNoScriptDTO;
import com.gpayments.requestor.testlab.dto.Message;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class AuthControllerV2 {

  private final AuthServiceV2 authServiceV2;

  @Autowired
  public AuthControllerV2(AuthServiceV2 authServiceV2) {
    this.authServiceV2 = authServiceV2;
  }

  /**
   * Receives the initialise authentication request from the 3DS-web-adapter (Step 2) Send data to
   * ActiveServer to Initialise Authentication
   */
  @PostMapping("/v2/auth/init")
  @ResponseBody
  public Message initAuth(
      @RequestParam(value = "trans-type", required = false) String transType,
      @RequestBody Message request, HttpSession session) {
    return authServiceV2.initAuth(transType, request, session);
  }

  /**
   * Receives the Execute authentication request from the 3DS-web-adapter (Step 9) Send data to
   * ActiveServer to Execute Authentication
   */
  @PostMapping("/v2/auth")
  @ResponseBody
  public Message auth(@RequestBody Message request, HttpSession session) {
    return authServiceV2.auth(request, session);
  }

  @PostMapping("/v2/auth/challenge/status")
  @ResponseBody
  public Message challengeStatus(@RequestBody Message request) {
    return authServiceV2.challengeStatus(request);
  }

  /**
   * Receives the Request for authentication result request (Step 15(F) and Step 20(C)) Send data to
   * ActiveServer to Retrieve Authentication Results
   *
   * @param serverTransId The server transaction ID
   * @param extraParams Experimental params is for GPayments internal use only - not required, can be safely ignored
   */
  @ResponseBody
  @GetMapping("/v2/auth/brw/result")
  public Message resultBRW(@RequestParam("txid") String serverTransId, @RequestParam(value = "ep", required = false) String extraParams) {
    return authServiceV2.getBRWResult(serverTransId, extraParams);
  }

  @PostMapping("/v2/auth/3ri")
  @ResponseBody
  public Message threeRI(@RequestParam(value = "trans-type", required = false) String transType,
      @RequestBody Message request) {
    return authServiceV2.threeRI(transType, request);
  }

  /**
   * Retrieves the 3RI authentication result.
   *
   * @param serverTransId The server transaction ID
   * @param extraParams Experimental params is for GPayments internal use only - not required, can be safely ignored
   */
  @GetMapping("/v2/auth/3ri/result")
  public Message result3RI(@RequestParam("txid") String serverTransId, @RequestParam(value = "ep", required = false) String extraParams) {
    return authServiceV2.get3RIResult(serverTransId, extraParams);
  }

  @PostMapping("/v2/auth/app")
  @ResponseBody
  public Message app(@RequestParam(value = "trans-type", required = false) String transType,
      @RequestBody Message request) {
    return authServiceV2.app(transType, request);
  }

  @PostMapping("/v2/auth/enrol")
  @ResponseBody
  public Message enrol(@RequestParam(value = "trans-type", required = false) String transType,
      @RequestBody Message request) {
    return authServiceV2.enrol(transType, request);
  }


  /**
   * Receives the initialise authentication request from a no-javascript environment.
   */
  @PostMapping("/v2/auth/init/noscript")
  public String initAuthNoScript(
      @RequestParam(value = "trans-type", required = false) String transType,
      AuthDataNoScriptDTO authData, Model model, HttpSession session) {
    return authServiceV2.initAuthNoScript(transType, authData, model, session);
  }

  @GetMapping("/v2/auth/noscript")
  public String authNoScript(
      @RequestParam("transId") String transId,
      @RequestParam("param") String param,
      Model model,
      HttpSession session) {
    return authServiceV2.authNoScript(transId, param, model, session);
  }


  @GetMapping("/v2/auth/result/noscript/poll")
  public String pollResultNoScript(
      @RequestParam("transId") String transId,
      Model model,
      HttpSession session) {
    return authServiceV2.pollResultNoScript(transId, model, session);
  }

  @GetMapping("/v2/auth/brw/result/noscript")
  public String resultNoScript(
      @RequestParam("transId") String transId, Model model, HttpSession session) {
    return authServiceV2.resultNoScript(transId, model, session);
  }

}
