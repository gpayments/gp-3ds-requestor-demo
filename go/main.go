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
	"encoding/gob"
	"encoding/json"
	"github.com/cbroglie/mustache"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/memstore"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/kr/pretty"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
)

type Message = map[string]interface{}

type asSession struct {
	message    Message
	url        string
	context    *gin.Context
	httpClient *http.Client
	config     *Config
	rHandler   respHandler
}

type respHandler func(resp []byte, contentType string, context *gin.Context) error

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
	gob.Register(Message{})   //register type Message so the session can be persisted
	gob.Register(uuid.UUID{}) //register type UUID so the session can be persisted
	store := memstore.NewStore([]byte("secret"))
	store.Options(sessions.Options{
		Path:     "/",
		HttpOnly: true,
		Secure:   false, //Secure should be true for production environment
	})
	r.Use(sessions.Sessions("requestor-session", store))

	fp := &mustache.FileProvider{
		Paths:      []string{"web"},
		Extensions: []string{".html"},
	}

	//auth controller router v1
	authControllerV1(r, config, httpClient)

	//auth controller router v2
	authControllerV2(r, config, httpClient, fp)

	//Main Controller router
	mainController(r, config, fp)

	//3ds1 controller router
	threeDS1Controller(r, config, httpClient, fp)

	//static resources
	r.Static("/js", "web/js")
	r.Static("/css", "web/css")
	r.Static("/images", "web/images")

	err = r.Run(pretty.Sprintf(":%d", config.Server.Port))

}

//Main controller
func mainController(r *gin.Engine, config *Config, fp *mustache.FileProvider) {

	indexTpl := loadTemplate("web/index.html", fp)
	shopTpl := loadTemplate("web/shop.html", fp)
	brwTpl := loadTemplate("web/brw.html", fp)
	appTpl := loadTemplate("web/app.html", fp)
	threeRiTpl := loadTemplate("web/3ri.html", fp)
	enrolTpl := loadTemplate("web/enrol.html", fp)
	checkoutTpl := loadTemplate("web/checkout.html", fp)
	notify3DSEventsTpl := loadTemplate("web/notify_3ds_events.html", fp)
	noscriptTpl := loadTemplate("web/no_script.html", fp)

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

	r.GET("/app", func(c *gin.Context) {
		renderPage(gin.H{
			"callbackUrl": config.GPayments.BaseUrl,
			"serverUrl":   config.GPayments.AsAuthUrl,
		}, appTpl, c)
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

	r.GET("/noscript", func(c *gin.Context) {
		renderPage(gin.H{}, noscriptTpl, c)
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

		case "3DSMethodHasError":

      //Event 3DSMethodHasError is only for logging and troubleshooting purpose, this demo
      //sets the callbackName to be _NA so the frontend won't process it.
			callbackName = "_NA"

		default:

			// When unrecognised event has been received, a callbackName like "_NA" can be returned (so the frontend won't recognise it)
			// to make the callback process more robust and resilient.
			// Alternatively, the 3DS Requestor backend implementation may choose to throw an exception to indicate this error
			// however the frontend must be able to handle the exception so that the checkout page flow won't be interrupted
			callbackName = "_NA"

		}

		renderPage(gin.H{
			"transId":       transId,
			"callbackName":  callbackName,
			"callbackParam": param,
		}, notify3DSEventsTpl, c)
	})

	r.POST("/3ds-notify/noscript", func(c *gin.Context) {

		transId := c.PostForm("requestorTransId")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid requestorTransId"})
			return
		}

		event := c.PostForm("event")
		if event == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid event type"})
			return
		}
		param := c.PostForm("param")
		if param == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid param"})
			return
		}

		if event == "3DSMethodFinished" || event == "3DSMethodSkipped" || event == "InitAuthTimedOut" {
			//continue the authentication
			c.Redirect(http.StatusFound, "/v2/auth/noscript?param="+param+"&transId="+transId)
		} else if event == "AuthResultReady" {
			//continue to get result
			c.Redirect(http.StatusFound, "/v2/auth/brw/result/noscript")
		} else {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid callback event"})
		}
	})

}

func parseMap(bytes []byte) (Message, error) {
	//convert to map
	var msg Message
	err := json.Unmarshal(bytes, &msg)
	if err != nil {
		return nil, err
	}
	return msg, nil
}

func loadTemplate(page string, fp *mustache.FileProvider) *mustache.Template {
	tpl, err := mustache.ParseFilePartials(page, fp)
	if err != nil {
		panic(err)
	}

	return tpl

}

//call ActiveServer API, if message == nil, do GET otherwise POST, return response if any
func callASAPI(session asSession) {

	var r *http.Request
	var err error

	//generate the url, if the url starts with http, use it as is otherwise prefix with the base URL
	var url string
	if strings.HasPrefix(strings.ToLower(session.url), "http") {
		url = session.url
	} else {
		url = session.config.GPayments.AsAuthUrl + session.url
	}

	if session.message == nil {
		log.Println("Send GET request to 3DS Server, url: " + url)
		r, err = http.NewRequest("GET", url, nil)
		if err != nil {
			session.context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
	} else {
		var data []byte
		data, err = json.Marshal(session.message)
		if err != nil {
			session.context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

		log.Printf("Send POST request to 3DS Server, url: %s, body: %v\n", url, session.message)

		r, err = http.NewRequest("POST", url, bytes.NewBuffer(data))
		if err != nil {
			session.context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
		r.Header.Set("Content-Type", "application/json;charset=utf-8")

	}

	//if this is groupAuth
	if session.config.GPayments.GroupAuth {
		r.Header.Set("AS-Merchant-Token", session.config.GPayments.MerchantToken)
	}

	response, err := session.httpClient.Do(r)
	if err != nil {
		session.context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	defer response.Body.Close()

	contentType := response.Header.Get("Content-Type")
	responseBody, err := ioutil.ReadAll(response.Body)

	if err != nil {
		session.context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	log.Printf("Received response with content type: %s, content: %s\n", contentType, string(responseBody))

	if session.rHandler != nil {

		//process the response by the responseHandler. the handle returns the data as well.
		if err = session.rHandler(responseBody, contentType, session.context); err != nil {
			session.context.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
	} else {
		//if no response handler provided, return the data by default.
		session.context.Data(http.StatusOK, contentType, responseBody)
	}
}

func renderPage(data Message, tpl *mustache.Template, c *gin.Context) {
	page, err := tpl.Render(data)

	if err != nil {
		c.String(http.StatusInternalServerError, "failed to load page")
		return
	}

	c.Data(http.StatusOK, "text/html", []byte(page))

}

//check and append trans type to the destination url if the trans type parameter is provided
//if overriding has values, use the first string
func appendTransTypeIfNecessary(url string, c *gin.Context, overriding ...string) string {
	var transType string
	if len(overriding) > 0 {
		transType = overriding[0]
	} else {
		transType, _ = c.GetQuery("trans-type")
	}
	if "prod" == transType {
		return url + "?trans-type=prod"
	} else {
		return url
	}

}

//get session attribute from the session store, v2 only
func getSessionAttribute(ctx *gin.Context, msgType int, key string) interface{} {
	session := sessions.Default(ctx)
	m := session.Get(msgType)
	if m == nil {
		return nil
	}
	return m.(Message)[key]
}
