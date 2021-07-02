<?php

class MainController3DS1
{
    private $restTemplate;
    private $config;
    private $templateResolver;
    private $threeDs1Service;

    function __construct(Config $config, RestClientConfig $restTemplate, TemplateResolver $templateResolver)
    {
        $this->config = $config;
        $this->restTemplate = $restTemplate;
        $this->templateResolver = $templateResolver;
        $this->threeDs1Service = new ThreeDS1Service($config, $restTemplate);
    }


    public function paymentPage()
    {
        $model = array();
        $model["authUrl"] = $this->config->getAsAuthUrl();
        $model["callbackUrl"] = $this->config->getBaseUrl() . "/3ds1/result";

        $this->templateResolver->_render("3ds1/auth", $model);
    }

    public function resultPage()
    {
        $model = array();
        $model["cavv"] = $_POST["cavv"];
        $model["cavvAlgo"] = $_POST["cavvAlgo"];
        $model["eci"] = $_POST["eci"];
        $model["threeDSRequestorTransID"] = $_POST["threeDSRequestorTransID"];

        $this->templateResolver->_render("3ds1/result", $model);
    }

    public function threeds1()
    {
        $requestData = Utils::_getJsonData();

        $response = $this->threeDs1Service->handleAuthRequest($requestData);

        Utils::_returnJson($response);
    }


}