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
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"net/http"
)

//AuthController routers v1
func authControllerV1(r *gin.Engine, config *Config, httpClient *http.Client) {
	r.POST("/v1/auth/init/:messageCategory", func(c *gin.Context) {

		var message Message

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		//adding requestorTransId
		message["threeDSRequestorTransID"] = uuid.New()
		//add callback event url
		message["eventCallbackUrl"] = config.GPayments.BaseUrl + "/3ds-notify"

		//Add parameter trans-type=prod in the initAuthUrl to use prod DS, otherwise use testlab DS
		//For example, in this demo, the initAuthUrl for transactions with prod DS is https://api.as.testlab.3dsecure.cloud:7443/api/v1/auth/brw/init?trans-type=prod
		//For more details, refer to: https://docs.activeserver.cloud
		callASAPI(asSession{
			message:    message,
			url:        appendTransTypeIfNecessary("/api/v1/auth/brw/init/"+c.Param("messageCategory"), c),
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v1/auth", func(c *gin.Context) {

		var message Message

		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		callASAPI(asSession{
			message:    message,
			url:        "/api/v1/auth/brw",
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.GET("/v1/auth/result", func(c *gin.Context) {

		transId := c.Query("txid")
		if transId == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid transId"})
			return

		}

		callASAPI(asSession{
			url:        "/api/v1/auth/brw/result?threeDSServerTransID=" + transId,
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v1/auth/3ri", func(c *gin.Context) {
		var message Message
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		//adding requestorTransId
		message["threeDSRequestorTransID"] = uuid.New()

		callASAPI(asSession{
			message:    message,
			url:        appendTransTypeIfNecessary("/api/v1/auth/3ri/npa", c),
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v1/auth/challenge/status", func(c *gin.Context) {
		var message Message
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(asSession{
			message:    message,
			url:        "/api/v1/auth/challenge/status",
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

	r.POST("/v1/auth/enrol", func(c *gin.Context) {
		var message Message
		err := c.ShouldBindJSON(&message)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		callASAPI(asSession{
			message:    message,
			url:        appendTransTypeIfNecessary("/api/v1/auth/enrol", c),
			context:    c,
			httpClient: httpClient,
			config:     config})

	})

}
