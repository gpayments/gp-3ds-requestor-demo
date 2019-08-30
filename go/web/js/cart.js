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

function displayCart(itemList) {
  var cartDiv = $('#cartItem');
  cartDiv.empty();

  var totalPrice = 0;
  itemList.forEach(function (item) {
    if (item.quantity > 0) {
      totalPrice += item.price * item.quantity;
      cartDiv.append(
          "<li class=\"list-group-item d-flex justify-content-between lh-condensed \" >"
          +
          "<div class=\"row cart-detail\">" +
          "<div class=\"col-lg-4 col-sm-4 col-4 cart-detail-img\">" +
          "<img src=" + item.picture + " alt=\"\">" +
          "</div>" +
          "<div class=\"col-lg-8 col-sm-8 col-8 cart-detail-product\">" +
          "<p>" + item.name + "</p>" +
          "<span class=\"price text-info\">$" + (item.price
          * item.quantity).toFixed(2)
          + "</span>" +
          "<span class=\"count\">Quantity: " + item.quantity + "</span>" +
          "</div>" +
          "</div>" +
          "</li>"
      );
    }
  });

  if (totalPrice > 0) {
    $("#totalPrice").text("Total $" + totalPrice.toFixed(2));
    $("#checkoutButton").prop("disabled", false);
  }
}

