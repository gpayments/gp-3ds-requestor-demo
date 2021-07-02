package main

import (
	"github.com/cbroglie/mustache"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"log"
	"net/http"
)

//3DSecure 1.0.2 controller and API entry points
func threeDS1Controller(r *gin.Engine, config *Config, httpClient *http.Client, fp *mustache.FileProvider) {

	threeDS1Tpl := loadTemplate("web/3ds1/auth.html", fp)
	threeDS1ResultTpl := loadTemplate("web/3ds1/result.html", fp)

	r.GET("/3ds1", func(c *gin.Context) {
		renderPage(gin.H{
			"callbackUrl": config.GPayments.BaseUrl + "/3ds1/result",
			"authUrl":     config.GPayments.AsAuthUrl},
			threeDS1Tpl, c)
	})

	r.POST("/3ds1/result", func(c *gin.Context) {
		renderPage(gin.H{
			"cavv":                    c.PostForm("cavv"),
			"cavvAlgo":                c.PostForm("cavvAlgo"),
			"eci":                     c.PostForm("eci"),
			"threeDSRequestorTransID": c.PostForm("threeDSRequestorTransID"),
		},
			threeDS1ResultTpl, c)
	})

	//3ds1 auth api entry point
	r.POST("/3ds1/auth", func(c *gin.Context) {

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
			url:        "/api/v2/auth/3ds1",
			context:    c,
			httpClient: httpClient,
			config:     config,
			rHandler: func(resp []byte, contentType string, context *gin.Context) error {
				msg, err := parseMap(resp)
				if err != nil {
					return err
				}
				log.Printf("3ds1 auth response: %v\n", msg)
				//now return the data
				context.Data(http.StatusOK, contentType, resp)
				return nil
			}})
	})

}
