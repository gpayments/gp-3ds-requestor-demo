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
 * authentication data
 * @type {{}}
 * @private
 */
var _authData = {};

/**
 * other options
 */
var _options = {};

/**
 * challengeResultRetrieved flag to indicate if the challenge authentication result has been retrieved.
 */
var challengeResultReady = false;

/**
 * Perform Browser-based authentication
 * @param authData
 * @param container: iframe container
 * @param callbackFn: callback function to return data
 * @param options: other options
 * @param transType: transType=prod to use production directory server
 */
function brw(authData, container, callbackFn, options, transType) {

  _callbackFn = callbackFn;
  iframeContainer = container;
  _authData = authData;

  if (options) {
    _options = options;
  }

  //generate an random number for iframe Id
  iframeId = String(Math.floor(100000 + Math.random() * 900000));

  //3DS Requestor url for Initialise Authentication
  var initAuthUrl = "/v2/auth/init";

  //add trans-type=prod to use production directory server. More details, refer to https://docs.activeserver.cloud
  if (transType === "prod") {
    initAuthUrl = initAuthUrl + "?trans-type=prod";
  }

  var initAuthData = {};
  initAuthData.acctNumber = authData.acctNumber;
  initAuthData.merchantId = authData.merchantId;
  if (options && options.challengeWindowSize) {
    initAuthData.challengeWindowSize = options.challengeWindowSize;
  }

  if (authData.skipAutoBrowserInfoCollect) {
    initAuthData.skipAutoBrowserInfoCollect = true;
  }

  /*
   To use a specific cardScheme, for example, eftpos instead of Visa,
   it can be specified in the InitAuth call
   */
  if (authData.cardScheme) {
    initAuthData.cardScheme = authData.cardScheme;
  }

  console.log('init authentication', initAuthData);

  //Send data to /auth/init to do Initialise authentication
  doPost(initAuthUrl, initAuthData, _onInitAuthSuccess, _onError);
}

/**
 * Perform App-based authentication
 * @param authData
 * @param callbackFn: callback function to return data
 * @param transType: transType=prod to use production directory server
 */
function app(authData, callbackFn, transType) {
  _callbackFn = callbackFn;

  var appUrl = "/v2/auth/app";
  //add trans-type=prod to use production directory server. More details, refer to https://docs.activeserver.cloud
  if (transType === "prod") {
    appUrl = appUrl + "?trans-type=prod";
  }

  doPost(appUrl, authData, _onAppSuccess, _onError);
}

/**
 * Send data to url /auth/3ri to do 3RI
 * @param authData
 * @param callbackFn
 * @param transType
 */
function threeRI(authData, callbackFn, transType) {
  var threeRIUrl = "/v2/auth/3ri";
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
  var enrolUrl = "/v2/auth/enrol";
  if (transType === "prod") {
    enrolUrl = enrolUrl + "?trans-type=prod";
  }
  _callbackFn = callbackFn;
  console.log('enrol: ', authData);
  doPost(enrolUrl, authData, _onEnrolSuccess, _onError);
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
 * Start doing the decouple authentication
 * @Param data: data returned form auth url
 * @Param authReadyCallback: callback function when get the AuthResultReady event
 */
function _startDecoupledAuth(data, authReadyCallback) {
  if (data.resultMonUrl) {
    _callbackFn("onDecoupledAuthStart", data);
    _doPolling(data.resultMonUrl, authReadyCallback)
  } else {
    _onError({"Error": "Invalid Result Mon Url"});
  }
}

/**
 * Start polling result
 * @Param url: polling url
 * @Param authReadyFn: callback function when get the AuthResultReady event
 */
function _doPolling(url, authReadyCallback) {
  console.log("call mon url: ", url);
  $.get(url)
  .done(function (data) {
    console.log('returns:', data);

    if (!data.event) {
      _onError({"Error": "Invalid mon url result"});
    }

    if (data.event === "AuthResultNotReady") {
      console.log("AuthResultNotReady, retry in 2 seconds");
      setTimeout(function () {
        _doPolling(url, authReadyCallback)
      }, 2000);
    } else if (data.event === "AuthResultReady") {
      console.log('AuthResultReady');
      authReadyCallback(serverTransId, _callbackFn);
    } else {
      _onError({"Error": "Invalid mon url result event type"});
    }
  })
  .fail(function (error) {
    callbackFn("onError", error.responseJSON);
  });
}

/**
 * callback function for brw
 * @param data
 * @private
 */
function _onInitAuthSuccess(data) {
  console.log('init auth returns:', data);

  if (!data.authUrl) {
    _onError(data);
    return;
  }

  serverTransId = data.threeDSServerTransID;

  if (_authData.skipAutoBrowserInfoCollect) {
    // If BrowserInfo collection is skipped, 3DSMethod can still be executed
    if (data.threeDSServerCallbackUrl) {
      executeIframes(data)
    } else {
      // If 3DSMethod is not available, go straight to Auth call
      _doAuth(data.threeDSRequestorTransID, _authData.browserInfoCollected)
    }
  } else {
    // Execute iframes as normal and use default BrowserInfo collection
    if (data.threeDSServerCallbackUrl) {
      executeIframes(data)
    } else {
      _onError(data);
    }
  }
}

/**
 * Send data to url /auth to Execute authentication
 * @param transId
 * @private
 */
function _doAuth(transId, param) {

  console.log('Do Authentication for transId', transId);

  //first remove any 3dsmethod iframe
  $("#3ds_" + iframeId).remove();

  var authData = _authData;
  if (!authData.skipAutoBrowserInfoCollect) {
    // If skipAutoBrowserInfoCollect is not used, collect BrowserInfo as normal
    authData.browserInfo = param;
  }

  /*
   As this method is only used for BRW transactions and the following fields
   are not required in the Auth call, they can be removed from the authData.
   */
  if (authData.skipAutoBrowserInfoCollect) {
    delete authData.skipAutoBrowserInfoCollect;
  }
  if (authData.cardScheme) {
    delete authData.cardScheme;
  }

  authData.threeDSRequestorTransID = transId;
  authData.threeDSServerTransID = serverTransId;
  console.log("authData: ", authData);
  doPost("/v2/auth", authData, _onDoAuthSuccess, _onError);
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
          doPost("/v2/auth/challenge/status", sendData, _onCancelSuccess,
              _onCancelError)
        } else {
          var returnData = _cancelMessage();
          _callbackFn("onAuthResult", returnData);
        }
      } else {
        startChallenge(data);
      }

    } else if (data.transStatus === "D") {
      _startDecoupledAuth(data, getBrwResult);
    } else {
      _callbackFn("onAuthResult", data);
    }
  } else {
    _onError(data);
  }
}

/**
 * Setup iframe for challenge flow
 */
function startChallenge(data) {

  if (data.challengeUrl) {
    challengeResultReady = false;
    _callbackFn("onChallengeStart");
    //create the iframe
    $('<iframe src="' + data.challengeUrl
        + '" width="100%" height="100%" style="border:0" id="' + "cha_"
        + iframeId
        + '"></iframe>')
    .appendTo(iframeContainer);

    if (data.resultMonUrl) {
      console.log("Start polling for challenge result");
      _doPolling(data.resultMonUrl, getChallengeAuthResult);
    } else {
      console.log(
          "No resultMonUrl provided, challenge timout monitoring is skipped.");
    }

  } else {
    _onError({"Error": "Invalid Challenge Callback Url"});
  }
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
  getChallengeAuthResult(serverTransId, _callbackFn);
}

/**
 * Method used to get challenge authentication result
 * @param threeDSServerTransID
 * @param callbackFn
 */
function getChallengeAuthResult(threeDSServerTransID, callbackFn) {

  if (!challengeResultReady) {
    //remove challenge iframe if exist
    if (document.getElementById("cha_" + iframeId)) {
      console.log("Remove challenge iframe");
      $("#cha_" + iframeId).remove();
    }
    challengeResultReady = true;
    getBrwResult(threeDSServerTransID, callbackFn);
  } else {
    console.log("Auth result has been retrieved.");
  }

}

/**
 * Method to get brw authentication result
 * @param threeDSServerTransID
 * @param callbackFn
 */
function getBrwResult(threeDSServerTransID, callbackFn) {
  console.log("Get brw authentication result for threeDSServerTransID: ",
      threeDSServerTransID);
  _doGetResult("/v2/auth/brw/result", threeDSServerTransID, callbackFn,
      "onAuthResult");
}

/**
 * Method to 3ri brw authentication result
 * @param threeDSServerTransID
 * @param callbackFn
 */
function get3riResult(threeDSServerTransID, callbackFn) {
  console.log("Get 3ri authentication result for threeDSServerTransID: ",
      threeDSServerTransID);
  _doGetResult("/v2/auth/3ri/result", threeDSServerTransID, callbackFn,
      "on3RIResult");
}

function _doGetResult(url, threeDSServerTransID, callbackFn, eventType) {
  $.get(url, {txid: threeDSServerTransID})
  .done(function (data) {
    console.log('returns:', data);
    if (data.transStatus) {
      callbackFn(eventType, data);
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
function _on3DSMethodSkipped(transId, param) {

  console.log('3DS Method is skipped or not presented, now calling doInitAuth.',
      transId);

  _doAuth(transId, param);
}

/**
 Default _callbackFn method for 3DSMethod Finished Event.
 **/
function _on3DSMethodFinished(transId, param) {

  console.log('now calling _doAuth. transId=', transId);

  _doAuth(transId, param);
}

/**
 * Default _callbackFn method for InitAuthTimeout event .
 */
function _onInitAuthTimedOut(transId, param) {
  console.log('Init Auth has timed out due to 3DS method timing out or browser '
      + 'information collecting failed'
      + ' now continue the authentication. transId=', transId);
  _doAuth(transId, param);

}

/**
 * callback function for threeRI()
 * @param data
 * @private
 */
function _on3RISuccess(data) {
  console.log('returns:', data);
  if (data.transStatus) {
    if (data.transStatus === "D") {
      _startDecoupledAuth(data, get3riResult);
    } else {
      _callbackFn("on3RIResult", data);
    }
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

/**
 * callback function for threeRI()
 * @param data
 * @private
 */
function _onAppSuccess(data) {
  console.log('returns:', data);
  if (data.transStatus) {
    _callbackFn("onAppResult", data);
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

/**
 * Method to execute both 3DSServerCallback and monUrl iframes, if available
 * @param data
 */
function executeIframes(data) {
  $('<iframe id="' + "3ds_" + iframeId
      + '" width="0" height="0" style="border:0;visibility: hidden;" src="'
      + data.threeDSServerCallbackUrl + '"></iframe>')
  .appendTo(iframeContainer);

  if (data.monUrl) {
    // Optionally append the monitoring iframe
    $('<iframe id="' + "mon_" + iframeId
        + '" width="0" height="0" style="border:0;visibility: hidden;" src="'
        + data.monUrl + '"></iframe>')
    .appendTo(iframeContainer);
  }
}

