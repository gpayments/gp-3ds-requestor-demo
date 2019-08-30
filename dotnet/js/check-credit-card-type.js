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

function cc_brand_id(cur_val) {
  var sel_brand;

  // the regular expressions check for possible matches as you type, hence the OR operators based on the number of chars
  // regexp string length {0} provided for soonest detection of beginning of the card numbers this way it could be used for BIN CODE detection also

  //JCB
  jcb_regex = new RegExp('^(?:2131|1800|35)[0-9]{0,}$'); //2131, 1800, 35 (3528-3589)
  // American Express
  amex_regex = new RegExp('^3[47][0-9]{0,}$'); //34, 37
  // Diners Club
  diners_regex = new RegExp('^3(?:0[0-59]{1}|[689])[0-9]{0,}$'); //300-305, 309, 36, 38-39
  // Visa
  visa_regex = new RegExp('^4[0-9]{0,}$'); //4
  // MasterCard
  mastercard_regex = new RegExp(
      '^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[01]|2720)[0-9]{0,}$'); //2221-2720, 51-55
  // Maestro
  maestro_regex = new RegExp('^(5[06789]|6)[0-9]{0,}$'); //always growing in the range: 60-69, started with / not something else
  //Discover
  discover_regex = new RegExp(
      '^(6011|65|64|62212[6-9]|6221[3-9]|622[2-8]|6229[01]|62292[0-5])[0-9]{0,}$');
  ////6011, 622126-622925, 64, 65

  // get rid of anything but numbers
  cur_val = cur_val.replace(/\D/g, '');

  // checks per each, as their could be multiple hits
  //fix: ordering matter in detection, otherwise can give false results in rare cases
  if (cur_val.match(jcb_regex)) {
    sel_brand = "JCB";
  } else if (cur_val.match(amex_regex)) {
    sel_brand = "AMEX";
  } else if (cur_val.match(diners_regex)) {
    sel_brand = "Diners";
  } else if (cur_val.match(visa_regex)) {
    sel_brand = "Visa";
  } else if (cur_val.match(mastercard_regex)) {
    sel_brand = "Mastercard";
  } else if (cur_val.match(discover_regex)) {
    sel_brand = "Discover";
  } else if (cur_val.match(maestro_regex)) {
    sel_brand = "Maestro";
  } else {
    sel_brand = "Unknown";
  }

  return sel_brand;
}