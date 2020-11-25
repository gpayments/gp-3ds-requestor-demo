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

package main

import (
	"encoding/json"
	"github.com/cbroglie/mustache"
	"github.com/gin-contrib/sessions"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"log"
	"net/http"
)

const (
	InitAuthRequest         = 1
	InitAuthRequestNoScript = 2
	InitAuthResponse        = 3
	AuthRequest             = 4
	AuthResponse            = 5

	ThreeDSRequestorTransId = "threeDSRequestorTransID"
	ThreeDSServerTransId    = "threeDSServerTransID"
)

//model for noscript initAuth form
type InitAuthNoScript struct {
	AcctNumber                 string `form:"acctNumber" json:"acctNumber"`
	MerchantId                 string `form:"merchantId" json:"merchantId"`
	TransType                  string `form:"trans-type" json:"trans-type"`
	MessageVersion             string `form:"messageVersion" json:"messageVersion"`
	AuthenticationInd          string `form:"authenticationInd" json:"authenticationInd"`
	PurchaseAmount             string `form:"purchaseAmount" json:"purchaseAmount"`
	PurchaseCurrency           string `form:"purchaseCurrency" json:"purchaseCurrency"`
	MessageCategory            string `form:"messageCategory" json:"messageCategory"`
	PurchaseDate               string `form:"purchaseDate" json:"purchaseDate"`
	ThreeDSRequestorDecReqInd  string `form:"threeDSRequestorDecReqInd" json:"threeDSRequestorDecReqInd"`
	ThreeDSRequestorDecMaxTime string `form:"threeDSRequestorDecMaxTime" json:"threeDSRequestorDecMaxTime"`
}

//AuthController routers v2
func authControllerV2(r *gin.Engine, config *Config, httpClient *http.Client, fp *mustache.FileProvider) {
	r.POST("/v2/auth/init", func(c *gin.Context) {

		var message Message

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		//adding requestorTransId
		message[ThreeDSRequestorTransId] = uuid.New()
		//add callback event url
		message["eventCallbackUrl"] = config.GPayments.BaseUrl + "/3ds-notify"

		//Add parameter trans-type=prod in the initAuthUrl to use prod DS, otherwise use testlab DS
		//For example, in this demo, the initAuthUrl for transactions with prod DS is https://api.as.testlab.3dsecure.cloud:7443/api/v2/auth/brw/init?trans-type=prod
		//For more details, refer to: https://docs.activeserver.cloud
		callASAPI(asSession{
			message:    message,
			url:        appendTransTypeIfNecessary("/api/v2/auth/brw/init", c),
			context:    c,
			httpClient: httpClient,
			config:     config,
			rHandler: func(resp []byte, contentType string, context *gin.Context) error {
				msg, err := parseMap(resp)
				if err != nil {
					return err
				}
				log.Printf("init auth response: %v\n", msg)
				//store the response in current session.
				session := sessions.Default(c)
				session.Set(InitAuthRequest, message)
				session.Set(InitAuthResponse, msg)
				err = session.Save()
				if err != nil {
					return err
				}

				//now return the data
				context.Data(http.StatusOK, contentType, resp)
				return nil
			}})
	})

	r.POST("/v2/auth", func(c *gin.Context) {

		//get authUrl from session
		authUrl := getSessionAttribute(c, InitAuthResponse, "authUrl").(string)
		if authUrl == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid authUrl"})
			return
		}

		var message Message

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		callASAPI(asSession{
			message:    message,
			url:        authUrl, //this url will be used as is
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.GET("/v2/auth/brw/result", func(c *gin.Context) {

		transId := getSessionAttribute(c, InitAuthResponse, ThreeDSServerTransId).(string)
		if transId == "" {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "invalid transId"})
			return

		}

		callASAPI(asSession{
			url:        "/api/v2/auth/brw/result?threeDSServerTransID=" + transId,
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v2/auth/app", func(c *gin.Context) {
		var message Message
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		//adding requestorTransId
		message[ThreeDSRequestorTransId] = uuid.New()

		callASAPI(asSession{
			message:    message,
			url:        appendTransTypeIfNecessary("/api/v2/auth/app", c),
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v2/auth/3ri", func(c *gin.Context) {
		var message Message
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		//adding requestorTransId
		message[ThreeDSRequestorTransId] = uuid.New()

		callASAPI(asSession{
			message:    message,
			url:        appendTransTypeIfNecessary("/api/v2/auth/3ri", c),
			context:    c,
			httpClient: httpClient,
			config:     config,
			rHandler: func(resp []byte, contentType string, context *gin.Context) error {

				//save the auth response
				r, err := parseMap(resp)
				if err != nil {
					return err
				}
				session := sessions.Default(c)
				session.Set(AuthResponse, r)
				_ = session.Save()

				context.Data(http.StatusOK, contentType, resp)
				return nil
			}})

	})

	r.GET("/v2/auth/3ri/result", func(c *gin.Context) {
		transId := getSessionAttribute(c, AuthResponse, ThreeDSServerTransId).(string)
		if transId == "" {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "invalid transId"})
			return

		}

		callASAPI(asSession{
			url:        "/api/v2/auth/3ri/result?threeDSServerTransID=" + transId,
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v2/auth/challenge/status", func(c *gin.Context) {
		var message Message
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(asSession{
			message:    message,
			url:        "/api/v2/auth/challenge/status",
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v2/auth/enrol", func(c *gin.Context) {
		var message Message
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		callASAPI(asSession{message: message,
			url:        appendTransTypeIfNecessary("/api/v2/auth/enrol", c),
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	//noscript entry points
	noscriptProcessTpl := loadTemplate("web/no_script_process.html", fp)
	r.POST("/v2/auth/init/noscript", func(c *gin.Context) {

		form := &InitAuthNoScript{}

		err := c.ShouldBind(form)
		if err != nil {
			c.String(http.StatusBadRequest, "invalid request")
			return
		}
		//convert the form struct to Message
		message, err := formToMessage(form)
		if err != nil {
			c.String(http.StatusInternalServerError, "can't convert the input data")
			return
		}

		//generate the initAuthReq
		initAuthReq := Message{}
		//adding requestorTransId
		requestorTransId := uuid.New()
		initAuthReq[ThreeDSRequestorTransId] = requestorTransId
		//save a copy in the initAuthRequestNoScript
		message[ThreeDSRequestorTransId] = requestorTransId
		//add callback event url
		initAuthReq["eventCallbackUrl"] = config.GPayments.BaseUrl + "/3ds-notify/noscript"

		initAuthReq["acctNumber"] = message["acctNumber"]
		initAuthReq["merchantId"] = message["merchantId"]

		log.Printf("init auth request: %v\n", message)

		callASAPI(asSession{
			message:    initAuthReq,
			url:        appendTransTypeIfNecessary("/api/v2/auth/brw/init", nil, form.TransType),
			context:    c,
			httpClient: httpClient,
			config:     config,
			rHandler: func(resp []byte, contentType string, context *gin.Context) error {
				msg, err := parseMap(resp)
				if err != nil {
					return err
				}
				log.Printf("init auth response: %v\n", msg)
				//store the response in current session.
				session := sessions.Default(c)
				session.Set(InitAuthResponse, msg)
				session.Set(InitAuthRequest, initAuthReq)
				//save the Initial Auth Info for later use.
				session.Set(InitAuthRequestNoScript, message)
				err = session.Save()
				if err != nil {
					return err
				}

				//now render the page
				renderPage(gin.H{"threeDSServerCallbackUrl": msg["threeDSServerCallbackUrl"].(string)}, noscriptProcessTpl, context)
				return nil
			}})
	})

	noscriptResultTpl := loadTemplate("web/no_script_results.html", fp)
	noscriptPollResultTpl := loadTemplate("web/no_script_poll_result.html", fp)
	r.GET("/v2/auth/noscript", func(c *gin.Context) {

		session := sessions.Default(c)
		authMessage := session.Get(InitAuthRequestNoScript).(Message)
		initAuthResponse := session.Get(InitAuthResponse).(Message)

		if authMessage == nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid init auth request for noscript"})
			return
		}
		if initAuthResponse == nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid init auth response"})
			return
		}

		//get authUrl from session
		authUrl := initAuthResponse["authUrl"].(string)
		if authUrl == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid authUrl"})
			return
		}

		param := c.Query("param")
		if param == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid browserInfo"})
			return
		}

		authMessage[ThreeDSServerTransId] = initAuthResponse[ThreeDSServerTransId]
		authMessage["browserInfo"] = param

		callASAPI(asSession{
			message:    authMessage,
			url:        authUrl, //this url will be used as is
			context:    c,
			httpClient: httpClient,
			config:     config,
			rHandler: func(resp []byte, contentType string, context *gin.Context) error {
				msg, err := parseMap(resp)
				if err != nil {
					return err
				}

				session := sessions.Default(c)
				session.Set(AuthResponse, msg)
				_ = session.Save()

				if "C" == msg["transStatus"] {
					//now redirect challenge page
					context.Redirect(http.StatusFound, msg["challengeUrl"].(string))
				} else if "D" == msg["transStatus"] {
					//decoupled, poll the result and show the result poll page
					transId := getSessionAttribute(c, InitAuthRequest, ThreeDSRequestorTransId).(uuid.UUID)
					pollResult(c, httpClient, config, noscriptPollResultTpl, transId.String())
				} else {
					showNoScriptResult(c, noscriptResultTpl, msg)
				}
				return nil
			}})

	})

	r.GET("/v2/auth/result/noscript/poll", func(c *gin.Context) {

		transId := c.Query("transId")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid transId"})
			return

		}

		pollResult(c, httpClient, config, noscriptPollResultTpl, transId)

	})

	r.GET("/v2/auth/brw/result/noscript", func(c *gin.Context) {

		transId := getSessionAttribute(c, InitAuthResponse, ThreeDSServerTransId)
		if transId == "" {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "invalid transId"})
			return

		}

		callASAPI(asSession{
			url:        "/api/v2/auth/brw/result?threeDSServerTransID=" + transId.(string),
			context:    c,
			httpClient: httpClient,
			config:     config,
			rHandler: func(resp []byte, contentType string, context *gin.Context) error {
				msg, err := parseMap(resp)
				if err != nil {
					return err
				}
				//show the result page
				showNoScriptResult(c, noscriptResultTpl, msg)
				return nil
			}})

	})

}

func pollResult(c *gin.Context, httpClient *http.Client, config *Config, showResultTemplate *mustache.Template, transId string) {
	//get resultMonUrl from session
	resultMonUrl := getSessionAttribute(c, AuthResponse, "resultMonUrl").(string)
	if resultMonUrl == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid resultMonUrl"})
		return
	}
	cardholderInfo := getSessionAttribute(c, AuthResponse, "cardholderInfo")
	if cardholderInfo == nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid cardholderInfo"})
		return
	}

	callASAPI(asSession{
		url:        resultMonUrl,
		context:    c,
		httpClient: httpClient,
		config:     config,
		rHandler: func(resp []byte, contentType string, context *gin.Context) error {
			msg, err := parseMap(resp)
			if err != nil {
				return err
			}

			event := msg["event"].(string)

			if "AuthResultNotReady" == event {
				renderPage(gin.H{"transId": transId, "cardholderInfo": cardholderInfo},
					showResultTemplate,
					context)
			} else if "AuthResultReady" == event {
				context.Redirect(http.StatusFound, "/v2/auth/brw/result/noscript?transId="+transId)
			} else {
				c.JSON(http.StatusBadRequest, gin.H{"error": "invalid mon url result event type"})
			}
			return nil
		}})

}

func showNoScriptResult(c *gin.Context, template *mustache.Template, result Message) {
	//show the result page, print it with indent
	bytes, _ := json.MarshalIndent(result, "", "    ")
	renderPage(gin.H{"result": string(bytes)}, template, c)
}

func formToMessage(form *InitAuthNoScript) (Message, error) {

	bytes, err := json.Marshal(form)
	if err != nil {
		return nil, err
	}

	message := Message{}
	err = json.Unmarshal(bytes, &message)
	if err != nil {
		return nil, err
	}

	//We remove the empty string fields so they won't be sent to AS.
	for k, v := range message {
		if v == "" {
			delete(message, k)
		}
	}

	return message, nil
}
