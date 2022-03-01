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
 *  Written by GPayments <techsupport@gpayments.com>, 2022
 *
 *
 */

package com.gpayments.requestor.testlab;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A separate Controller with business logic to demonstrate server-side BrowserInfo collection
 */
@Controller
public class BrowserInfoController {

  @GetMapping( "/v2/auth/brw/info")
  public String collectBrowserInfo(Model model, HttpServletRequest request) {
    model.addAttribute("browserAcceptHeader", getAcceptHeader(request));
    model.addAttribute("browserUserAgent", getBrowserAgent(request));
    model.addAttribute("browserIP", getRemoteAddress(request));
    return "brw_info_collect";
  }

  // Simplified version of obtaining the request IP address
  private String getRemoteAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-FORWARDED-FOR");
    if (ipAddress == null) {
      ipAddress = request.getRemoteAddr();
    }
    if (ipAddress.startsWith("[")) {
      return "127.0.0.1";
    }
    return ipAddress;
  }

  private String getBrowserAgent(HttpServletRequest request) {
    String browserUserAgent = request.getHeader("User-Agent");
    if (StringUtils.hasLength(browserUserAgent)) {
      browserUserAgent = browserUserAgent.substring(0, Math.min(browserUserAgent.length(), 2048));
    }

    return browserUserAgent;
  }

  private String getAcceptHeader(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    if (StringUtils.hasLength(accept)) {
      accept = accept.substring(0, Math.min(accept.length(), 2048));
    }

    return accept;
  }

}
