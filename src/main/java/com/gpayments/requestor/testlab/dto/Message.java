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

package com.gpayments.requestor.testlab.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.Serializable;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message extends HashMap<String, Object> implements Serializable {

  private static final ObjectWriter objectWriter = new ObjectMapper()
      .writerWithDefaultPrettyPrinter();
  private static final Logger logger = LoggerFactory.getLogger(Message.class);

  @Override
  public String toString() {

    try {
      return objectWriter.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      logger.warn(e.getMessage());
      return super.toString();
    }
  }
}
