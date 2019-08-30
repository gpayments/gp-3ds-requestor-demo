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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author GPayments ON 31/07/2018
 */
@SpringBootApplication
public class RequestorApplication {

  public static void main(String[] args) {
    SpringApplication.run(RequestorApplication.class, args);
  }
}
