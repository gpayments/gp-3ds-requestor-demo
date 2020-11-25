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

/**
 3DS Server trans Id.
 **/
var serverTransId;

/**
 * Callback function for 3ds-web-adapter.
 * This function should be implemented by merchant side to handle callback from 3ds-web-adapter.
 * Two parameters expected: type and data.
 * @param type. Indicate the type of event. Values accepted:
 *                    onAuthResult: indicate an auth result
 *                    on3RIResult: indicate an 3ri result
 *                    onChallengeStart: indicate to start a challenge
 *                    onError: indicate an error
 * @param data. The data returned from 3ds-web-adapter. Data contains either the result of 'auth', '3ri', or the error message.
 */
var _callbackFn;

/**
 * iframe container
 */
var iframeContainer;

/**
 * iframe id, it is a random number of 6 digits
 */
var iframeId;

/**
 * other options
 */
var _options = {};

/**
 * Perform Browser-based authentication
 * @param authData
 * @param messageCategory
 * @param container: iframe container
 * @param callbackFn: callback function to return data
 * @param options: other options
 * @param transType: transType=prod to use production directory server
 */
function brw(authData, container, callbackFn, messageCategory, options,
    transType) {

  _callbackFn = callbackFn;
  iframeContainer = container;
  if (options) {
    _options = options;
  }
  //generate an random number for iframe Id
  iframeId = String(Math.floor(100000 + Math.random() * 900000));

  //3DS Requestor url for Initialise Authentication
  var initAuthUrl;
  if (messageCategory) {
    if (messageCategory === "pa" || messageCategory === "npa") {
      initAuthUrl = "/v1/auth/init/" + messageCategory;
    } else {
      _onError({"Error": "Invalid messageCategory"});
    }
  } else {
    initAuthUrl = "/v1/auth/init/" + "pa";
  }

  //add trans-type=prod to use production directory server. More details, refer to https://docs.activeserver.cloud
  if (transType === "prod") {
    initAuthUrl = initAuthUrl + "?trans-type=prod";
  }

  console.log('init authentication', authData);

  //Send data to /auth/init/{messageCategory} to do Initialise authentication (Step 2)
  doPost(initAuthUrl, authData, _onInitAuthSuccess, _onError);
}

/**
 * Send data to url /auth/3ri to do 3RI
 * @param authData
 * @param callbackFn
 * @param transType
 */
function threeRI(authData, callbackFn, transType) {
  var threeRIUrl = "/v1/auth/3ri";
  if (transType === "prod") {
    threeRIUrl = threeRIUrl + "?trans-type=prod";
  }
  _callbackFn = callbackFn;
  console.log('3ri: ', authData);
  doPost(threeRIUrl, authData, _on3RISuccess, _onError);
}

/**
 * Send data to url /auth/enrol to do enrol
 * @param authData
 * @param callbackFn
 * @param transType
 */
function enrol(authData, callbackFn, transType) {
  var enrolUrl = "/v1/auth/enrol";
  if (transType === "prod") {
    enrolUrl = enrolUrl + "?trans-type=prod";
  }
  _callbackFn = callbackFn;
  console.log('enrol: ', authData);
  doPost(enrolUrl, authData, _onEnrolSuccess, _onError);
}

/**
 * Get the authentication from Active Server
 * @param threeDSServerTransID
 * @param callbackFn
 */
function result(threeDSServerTransID, callbackFn) {
  getResult(threeDSServerTransID, callbackFn);
}

/**
 * Post authData to 3ds requestor with url
 * @param url
 * @param authData
 * @param onSuccess
 * @param onError
 */
function doPost(url, authData, onSuccess, onError) {
  $.ajax({
    url: url,
    type: 'POST',
    contentType: "application/json",
    data: JSON.stringify(authData),
    dataType: 'json',
    success: function (data) {
      onSuccess(data);
    },
    error: function (error) {
      onError(error.responseJSON);
    }
  });
}

/**
 * callback function for brw
 * @param data
 * @private
 */
function _onInitAuthSuccess(data) {
  console.log('init auth returns:', data);

  if (data.threeDSServerCallbackUrl) {

    serverTransId = data.threeDSServerTransID;
    $('<iframe id="' + "3ds_" + iframeId
        + '" width="0" height="0" style="border:0;visibility: hidden;" src="'
        + data.threeDSServerCallbackUrl + '"></iframe>')
    .appendTo(iframeContainer);

    if (data.monUrl) {
      // optionally append the monitoring iframe
      $('<iframe id="' + "mon_" + iframeId
          + '" width="0" height="0" style="border:0;visibility: hidden;" src="'
          + data.monUrl + '"></iframe>')
      .appendTo(iframeContainer);
    }

  } else {
    _onError(data);
  }
}

/**
 * Send data to url /auth to Execute authentication (Step 9)
 * @param transId
 * @private
 */
function _doAuth(transId) {

  console.log('Do Authentication for transId', transId);

  //first remove any 3dsmethod iframe
  $("#3ds_" + iframeId).remove();
  var authData = {};
  authData.threeDSRequestorTransID = transId;
  authData.threeDSServerTransID = serverTransId;

  doPost("/v1/auth", authData, _onDoAuthSuccess, _onError);
}

/**
 * callback function for _doAuth
 * @param data
 * @private
 */
function _onDoAuthSuccess(data) {
  console.log('auth returns:', data);

  if (data.transStatus) {
    if (data.transStatus === "C") {
      // 3D requestor can decide whether to proceed the challenge here
      if (_options.cancelChallenge) {
        if (_options.cancelReason) {
          var sendData = {};
          sendData.threeDSServerTransID = serverTransId;
          sendData.status = _options.cancelReason;
          doPost("/v1/auth/challenge/status", sendData, _onCancelSuccess,
              _onCancelError)
        } else {
          var returnData = _cancelMessage();
          _callbackFn("onAuthResult", returnData);
        }
      } else {
        data.challengeUrl ? startChallenge(data.challengeUrl) : _onError(
            {"Error": "Invalid Challenge Callback Url"});
      }

    } else {
      _callbackFn("onAuthResult", data);
    }
  } else {
    _onError(data);
  }
}

/**
 * Setup iframe for challenge flow (Step 14(C))
 * @param url is the challenge url returned from 3DS Server
 */
function startChallenge(url) {

  _callbackFn("onChallengeStart");
  //create the iframe
  $('<iframe src="' + url
      + '" width="100%" height="100%" style="border:0" id="' + "cha_" + iframeId
      + '"></iframe>')
  .appendTo(iframeContainer);

}

function _onCancelSuccess(data) {

  if (data.threeDSServerTransID) {
    var returnData = _cancelMessage();
    Object.keys(data).forEach(function (k) {
      returnData[k] = data[k];
    });
    _callbackFn("onAuthResult", returnData);
  } else {
    _onCancelError(data);
  }

}

function _onCancelError(data) {
  var returnData = _cancelMessage();
  Object.keys(data).forEach(function (k) {
    returnData[k] = data[k];
  });
  _onError(returnData);
}

function _cancelMessage() {
  var returnData = {"Challenge cancelled": "You can get further challenge results by select the \"Show results in separate page\" button after at least 30 seconds"};
  if (_options.cancelReason) {
    returnData["Cancel reason"] = _options.cancelReason;
  } else {
    returnData["Cancel reason"] = "Not sent";
  }
  return returnData;
}

/**
 Default _callbackFn method for challenge flow. 3DS Server will call this method to get auth result when the challenge finish
 **/
function _onAuthResult() {
  console.log('authentication result is ready: ');
  getResult(serverTransId, _callbackFn);
}

/**
 * Method to get authentication result
 * @param threeDSServerTransID
 * @param callbackFn
 */
function getResult(threeDSServerTransID, callbackFn) {
  console.log("Get authentication result for threeDSServerTransID: ",
      threeDSServerTransID);
  $.get("/v1/auth/result", {txid: threeDSServerTransID})
  .done(function (data) {
    console.log('returns:', data);
    if (data.transStatus) {
      callbackFn("onAuthResult", data);
    } else {
      callbackFn("onError", data);
    }
  })
  .fail(function (error) {
    callbackFn("onError", error.responseJSON);
  });
}

/**
 Default _callbackFn method for 3DSMethod Skipped Event.
 **/
function _on3DSMethodSkipped(transId) {

  console.log('3DS Method is skipped or not presented, now calling doInitAuth.',
      transId);

  _doAuth(transId);
}

/**
 Default _callbackFn method for 3DSMethod Finished Event.
 **/
function _on3DSMethodFinished(transId) {

  console.log('now calling _doAuth. transId=', transId);

  _doAuth(transId);
}

/**
 * Default _callbackFn method for InitAuthTimeout event .
 */
function _onInitAuthTimedOut(transId) {
  console.log('Init Auth has timed out due to 3DS method timing out or browser '
      + 'information collecting failed'
      + ' now continue the authentication. transId=', transId);
  _doAuth(transId);
}

/**
 * callback function for threeRI()
 * @param data
 * @private
 */
function _on3RISuccess(data) {
  console.log('returns:', data);
  if (data.transStatus) {
    _callbackFn("on3RIResult", data);
  } else {
    _onError(data);
  }
}

/**
 * callback function for enrol()
 * @param data
 * @private
 */
function _onEnrolSuccess(data) {
  console.log('returns:', data);
  if (data.enrolmentStatus) {
    _callbackFn("onEnrolResult", data);
  } else {
    _onError(data);
  }
}

function _onError(error) {
  if (error.status === 404) {
    error["New feature"] = "This feature is only supported by ActiveServer v1.1.2+";
  }
  _callbackFn("onError", error);
}

