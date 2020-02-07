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
	"bytes"
	"encoding/json"
	"github.com/cbroglie/mustache"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/kr/pretty"
	"io/ioutil"
	"net/http"
)

type respHandler func([]byte) error

func main() {

	//load configuration
	config, err := loadConf()

	if err != nil {
		panic(err)
	}

	//create httpClient with client certificate
	httpClient, err := createHttpClient(config)
	if err != nil {
		panic(err)
	}

	r := gin.Default()

	//use session
	store := cookie.NewStore([]byte("secret"))
	r.Use(sessions.Sessions("requestor-session", store))

	//auth controller router v1
	authControllerV1(r, config, httpClient)

	//auth controller router v2
	authControllerV2(r, config, httpClient)

	//Main Controller router
	mainController(r, config, httpClient)

	//static resources
	r.Static("/js", "web/js")
	r.Static("/css", "web/css")
	r.Static("/images", "web/images")

	err = r.Run(pretty.Sprintf(":%d", config.Server.Port))

}

//Main controller
func mainController(r *gin.Engine, config *Config, httpClient *http.Client) {
	fp := &mustache.FileProvider{
		Paths:      []string{"web"},
		Extensions: []string{".html"},
	}

	indexTpl := loadTemplate("web/index.html", fp)
	shopTpl := loadTemplate("web/shop.html", fp)
	brwTpl := loadTemplate("web/brw.html", fp)
	threeRiTpl := loadTemplate("web/3ri.html", fp)
	enrolTpl := loadTemplate("web/enrol.html", fp)
	checkoutTpl := loadTemplate("web/checkout.html", fp)
	notify3DSEventsTpl := loadTemplate("web/notify_3ds_events.html", fp)

	//v1 process and result pages
	resultTplv1 := loadTemplate("web/v1/result.html", fp)
	processTplv1 := loadTemplate("web/v1/process.html", fp)

	//v2 process and result pages
	resultTplv2 := loadTemplate("web/v2/result.html", fp)
	processTplv2 := loadTemplate("web/v2/process.html", fp)

	r.GET("/", func(c *gin.Context) {
		renderPage(gin.H{}, indexTpl, c)
	})
	r.GET("/shop", func(c *gin.Context) {
		renderPage(gin.H{}, shopTpl, c)
	})
	r.GET("/brw", func(c *gin.Context) {
		renderPage(gin.H{
			"callbackUrl": config.GPayments.BaseUrl,
			"serverUrl":   config.GPayments.AsAuthUrl,
		}, brwTpl, c)
	})

	r.GET("/3ri", func(c *gin.Context) {
		renderPage(gin.H{
			"callbackUrl": config.GPayments.BaseUrl,
			"serverUrl":   config.GPayments.AsAuthUrl,
		}, threeRiTpl, c)
	})

	r.GET("/enrol", func(c *gin.Context) {
		renderPage(gin.H{
			"callbackUrl": config.GPayments.BaseUrl,
			"serverUrl":   config.GPayments.AsAuthUrl,
		}, enrolTpl, c)
	})

	r.GET("/checkout", func(c *gin.Context) {
		renderPage(gin.H{}, checkoutTpl, c)
	})

	r.GET("/v1/process", func(c *gin.Context) {
		renderPage(gin.H{}, processTplv1, c)
	})
	r.GET("/v1/result", func(c *gin.Context) {
		renderPage(gin.H{}, resultTplv1, c)
	})

	r.GET("/v2/process", func(c *gin.Context) {
		renderPage(gin.H{}, processTplv2, c)
	})
	r.GET("/v2/result", func(c *gin.Context) {
		renderPage(gin.H{}, resultTplv2, c)
	})

	r.POST("/3ds-notify", func(c *gin.Context) {

		transId := c.PostForm("requestorTransId")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid parameter"})
			return
		}

		event := c.PostForm("event")
		if event == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid parameter"})
			return
		}

		param := c.PostForm("param")

		var callbackName string

		switch event {
		case "3DSMethodFinished":

			callbackName = "_on3DSMethodFinished"

		case "3DSMethodSkipped":

			callbackName = "_on3DSMethodSkipped"

		case "AuthResultReady":

			callbackName = "_onAuthResult"

		case "InitAuthTimedOut":

			callbackName = "_onInitAuthTimedOut"

		default:

			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid callback type"})
			return

		}

		renderPage(gin.H{
			"transId":       transId,
			"callbackName":  callbackName,
			"callbackParam": param,
		}, notify3DSEventsTpl, c)
	})

	r.POST("/auth/enrol", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(message, "/api/v1/auth/enrol", c, httpClient, config, nil)

	})

}

//AuthController routers v1
func authControllerV1(r *gin.Engine, config *Config, httpClient *http.Client) {
	r.POST("/v1/auth/init/:messageCategory", func(c *gin.Context) {

		var message map[string]interface{}

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		//adding requestorTransId
		message["threeDSRequestorTransID"] = uuid.New()
		//add callback event url
		message["eventCallbackUrl"] = config.GPayments.BaseUrl + "/3ds-notify"

		callASAPI(message, "/api/v1/auth/brw/init/"+c.Param("messageCategory"), c, httpClient, config, nil)

	})

	r.POST("/v1/auth", func(c *gin.Context) {

		var message map[string]interface{}

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		callASAPI(message, "/api/v1/auth/brw", c, httpClient, config, nil)

	})

	r.GET("/v1/auth/result", func(c *gin.Context) {

		transId := c.Query("txid")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid transId"})
			return

		}

		callASAPI(nil, "/api/v1/auth/brw/result?threeDSServerTransID="+transId, c, httpClient, config, nil)

	})

	r.POST("/v1/auth/3ri", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		//adding requestorTransId
		message["threeDSRequestorTransID"] = uuid.New()

		callASAPI(message, "/api/v1/auth/3ri/npa", c, httpClient, config, nil)

	})

	r.POST("/v1/auth/challenge/status", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(message, "/api/v1/auth/challenge/status", c, httpClient, config, nil)

	})

}

//AuthController routers v2
func authControllerV2(r *gin.Engine, config *Config, httpClient *http.Client) {
	r.POST("/v2/auth/init", func(c *gin.Context) {

		var message map[string]interface{}

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		//adding requestorTransId
		message["threeDSRequestorTransID"] = uuid.New()
		//add callback event url
		message["eventCallbackUrl"] = config.GPayments.BaseUrl + "/3ds-notify"

		callASAPI(message, "/api/v2/auth/brw/init", c, httpClient, config, func(resp []byte) error {
			//store the response in current session.

			authUrl, err := getAuthUrl(resp)

			if err != nil {
				return err
			}
			session := sessions.Default(c)
			session.Set("authUrl", authUrl)
			_ = session.Save()
			return nil
		})
	})

	r.POST("/v2/auth", func(c *gin.Context) {

		//get authUrl from session
		session := sessions.Default(c)
		authUrl := session.Get("authUrl").(string)
		if authUrl == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid authUrl"})
			return
		}

		var message map[string]interface{}

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		callASAPIWithUrl(message, authUrl, c, httpClient, config, nil)

	})

	r.GET("/v2/auth/result", func(c *gin.Context) {

		transId := c.Query("txid")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid transId"})
			return

		}

		callASAPI(nil, "/api/v2/auth/brw/result?threeDSServerTransID="+transId, c, httpClient, config, nil)

	})

	r.POST("/v2/auth/3ri", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		//adding requestorTransId
		message["threeDSRequestorTransID"] = uuid.New()

		callASAPI(message, "/api/v2/auth/3ri", c, httpClient, config, nil)

	})

	r.POST("/v2/auth/challenge/status", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(message, "/api/v2/auth/challenge/status", c, httpClient, config, nil)

	})

}

//return the authUrl from initAuthResponse, v2 only
func getAuthUrl(bytes []byte) (string, error) {
	var msg map[string]interface{}
	err := json.Unmarshal(bytes, &msg)
	if err != nil {
		return "", err
	}

	return msg["authUrl"].(string), nil

}

func loadTemplate(page string, fp *mustache.FileProvider) *mustache.Template {
	tpl, err := mustache.ParseFilePartials(page, fp)
	if err != nil {
		panic(err)
	}

	return tpl

}

//call ActiveServer API, if message == nil, do GET otherwise POST, return response if any
func callASAPI(message map[string]interface{},
	url string,
	c *gin.Context,
	httpClient *http.Client,
	config *Config,
	rHandler respHandler) {
	//add ActiveServer base URL
	callASAPIWithUrl(message, config.GPayments.AsAuthUrl+url, c, httpClient, config, rHandler)
}
func callASAPIWithUrl(
	message map[string]interface{},
	url string,
	c *gin.Context,
	httpClient *http.Client,
	config *Config,
	rHandler respHandler) {

	var r *http.Request
	var err error

	if message == nil {
		r, err = http.NewRequest("GET", url, nil)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
	} else {
		var data []byte
		data, err = json.Marshal(message)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

		r, err = http.NewRequest("POST", url, bytes.NewBuffer(data))
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
		r.Header.Set("Content-Type", "application/json;charset=utf-8")

	}

	//if this is groupAuth
	if config.GPayments.GroupAuth {
		r.Header.Set("AS-Merchant-Token", config.GPayments.MerchantToken)
	}

	response, err := httpClient.Do(r)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	defer response.Body.Close()

	contentType := response.Header.Get("Content-Type")
	responseBody, err := ioutil.ReadAll(response.Body)

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	if rHandler != nil {

		if err = rHandler(responseBody); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
	}

	c.Data(http.StatusOK, contentType, responseBody)

}

func renderPage(data map[string]interface{}, tpl *mustache.Template, c *gin.Context) {
	page, err := tpl.Render(data)

	if err != nil {
		c.String(http.StatusInternalServerError, "failed to load page")
		return
	}

	c.Data(http.StatusOK, "text/html", []byte(page))

}
