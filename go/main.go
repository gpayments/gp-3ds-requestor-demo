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

package main

import (
	"bytes"
	"encoding/json"
	"github.com/cbroglie/mustache"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/kr/pretty"
	"io/ioutil"
	"net/http"
)

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

	//auth controller router
	authController(r, config, httpClient)

	//Main Controller router
	mainController(r, config)

	//static resources
	r.Static("/js", "web/js")
	r.Static("/css", "web/css")
	r.Static("/images", "web/images")

	err = r.Run(pretty.Sprintf(":%d", config.Server.Port))

}

//Main controller
func mainController(r *gin.Engine, config *Config) {
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
	processTpl := loadTemplate("web/process.html", fp)
	notify3DSEventsTpl := loadTemplate("web/notify_3ds_events.html", fp)
	resultTpl := loadTemplate("web/result.html", fp)

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

	r.GET("/process", func(c *gin.Context) {
		renderPage(gin.H{}, processTpl, c)
	})
	r.GET("/result", func(c *gin.Context) {
		renderPage(gin.H{}, resultTpl, c)
	})

	r.POST("/3ds-notify", func(c *gin.Context) {

		transId := c.PostForm("requestorTransId")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid parameter"})
		}

		event := c.PostForm("event")
		if event == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid parameter"})
		}

		param := c.Param("param")

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

}

//AuthController routers
func authController(r *gin.Engine, config *Config, httpClient *http.Client) {
	r.POST("/auth/init/:messageCategory", func(c *gin.Context) {

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

		callASAPI(message, "/api/v1/auth/brw/init/"+c.Param("messageCategory"), c, httpClient, config)

	})

	r.POST("/auth", func(c *gin.Context) {

		var message map[string]interface{}

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		callASAPI(message, "/api/v1/auth/brw", c, httpClient, config)

	})

	r.GET("/auth/result", func(c *gin.Context) {

		transId := c.Query("txid")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid transId"})
			return

		}

		callASAPI(nil, "/api/v1/auth/brw/result?threeDSServerTransID="+transId, c, httpClient, config)

	})

	r.POST("/auth/3ri", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		//adding requestorTransId
		message["threeDSRequestorTransID"] = uuid.New()

		callASAPI(message, "/api/v1/auth/3ri/npa", c, httpClient, config)

	})

	r.POST("/auth/enrol", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(message, "/api/v1/auth/enrol", c, httpClient, config)

	})

	r.POST("/auth/challenge/status", func(c *gin.Context) {
		var message map[string]interface{}
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(message, "/api/v1/auth/challenge/status", c, httpClient, config)

	})

}

func loadTemplate(page string, fp *mustache.FileProvider) *mustache.Template {
	tpl, err := mustache.ParseFilePartials(page, fp)
	if err != nil {
		panic(err)
	}

	return tpl

}

//call ActiveServer API, if message == nil, do GET otherwise POST
func callASAPI(message map[string]interface{}, url string, c *gin.Context, httpClient *http.Client, config *Config) {

	var r *http.Request
	var err error

	//add ActiveServer base URL
	url = config.GPayments.AsAuthUrl + url

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
