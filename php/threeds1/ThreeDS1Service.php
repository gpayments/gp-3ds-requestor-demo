<?php

class ThreeDS1Service
{

    private $config;
    private $restTemplate;
    private $authUrl;

    function __construct(Config $config, RestClientConfig $restTemplate)
    {
        $this->config = $config;
        $this->restTemplate = $restTemplate;
        $this->authUrl = $config->getAsAuthUrl() . "/api/v2/auth/3ds1";
    }

    public function handleAuthRequest($requestData)
    {

        $requestData->threeDSRequestorTransID = Utils::_getUUId();

        $response = $this->restTemplate->post($this->authUrl, $requestData);

        if ($response != null) {
            return $response->getBody();
        } else {
            echo "invalid 3ds1 response";
            exit;
        }

    }

}