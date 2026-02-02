# GPayments 3DS Requestor demo code

Repository containing the GPayments 3DS Requestor demo code, to be used to integrate with [**GPayments ActiveServer**](https://www.gpayments.com/solutions/3ds-server-activeserver/).

For full instructions on using the demo code, please refer contact GPayments at techsupport@gpayments.com.

The master branch always contains the latest 3DS Requestor demo code version. For previous versions, check out the [releases tab](https://github.com/gpayments/gp-3ds-requestor-demo/releases). 

## Release notes

### Version 3.0 (compatible with ActiveServer v2.0.x, v3.1.x)
* Update default Cardholder Expiry Date

### Version 2.9 (compatible with ActiveServer v2.0.11)
* Updated sdkEncData on the App page
* Removed form validation for phone number

### Version 2.8 (compatible with ActiveServer v2.0.11)
* Updated `sdkEncData` on the App page
* Added scenarios with 3DS Method

### Version 2.7 (compatible with ActiveServer v2.0.9)
* Updated the requestor to specify Base64Url encoded `threeDSSessionData`
* Minor UI changes

### Version 2.6 (compatible with ActiveServer v2.0.8)
* Added support for card scheme overriding through cardScheme parameter

### Version 2.5 (compatible with ActiveServer v2.0.7)
* Added browser info collection tab
    * **Use RBC**, 3DS Requestor Browser Info Collection option to set `skipAutoBrowserInfoCollect=true`
    * Input for collected info
* Added new demo code for browser info collection for Java and PHP, in `BrowserInfoController`
* Updated the main controller for Go and C#

### Version 2.4 (compatible with ActiveServer v2.0.6)
* Exclude empty fields in 3DS1 auth form
* Auto convert purchaseAmount to minor units without decimal
* Fix template path to include leading forward slash “/“
* Add support for new fields in the result notification page in 3DS1
    * txStatus, errorCode, errorMessage
* Enhancement of the event handling
* Disclaimer added to the source code headers

### Version 2.3 (compatible with ActiveServer v2.0.4)
* Fixed 3DS1 get result page template not found issue

### Version 2.2 (compatible with ActiveServer v2.0.4)
* Added 3DS1 sample code and BRW test pages (SaaS clients only)
* Added a new callback event, 3DSMethodHasError, to the backend demo code
* Added a new field, forceMessageVersion, to the sample code and the BRW Test Options page
* Added a new set of fields for Merchant Override to the sample code and the BRW Test Options page

### Version 2.1 (compatible with ActiveServer v2.0.1)
* Added sample code for the challenge page time out scenario
* Bug fix for dotnet requestor code related to non-javascript support

### Version 2.0 (compatible with ActiveServer v2.0.0)
* Addded support for **EMV message version 2.2.0**
	* Added a Message Version selection field on the test pages to select between EMV v2.1.0 and v2.2.0
	* Added additional message fields for EMV v2.2.0 transactions on the test pages
	* Added support in the 3ds-web-adapter for the decoupled authentication flow
* Added demo code to support a no java-script environment
* Added a Challenge Window Size dropdown box on the BRW test page to specify the challenge window size
* Change the _onInitAuthTimedOut function to handle the 3ds method timeout scenario
* Change the purchaseDate field to be UTC time
* Added a configuration for the certificate file password

### Version 1.5 (compatible with ActiveServer v1.4.0)
* Added a **Directory Server** selection field on the test pages to allow sending the transaction to either the Testlab or Production directory server
* Added an **App test page** with mock authentication data for APP channel testing
* Fixed the error stacktrace being shown when using a Master Auth API client certificate on a DS unsupported merchant for the dotnet backend

### Version 1.4 (compatible with ActiveServer v1.3.3)
* Added code comments to explain the Directory Server (DS) switching functionality between card scheme production DS and GPayments TestLabs DS  

### Version 1.3 (compatible with ActiveServer v1.3.0)
* Added functionality to support the **ActiveServer** Authentication API v2
  * Updated the **Java**, **.NET**, **PHP** and **Go** backends and the **3DS-web-adaptor** frontend to have separate v1 and v2 implementations
  * Added an API version toggle on the checkout and test pages on the sample 3DS Requestor

### Version 1.2 (compatible with ActiveServer v1.1.2)
* Added a [Test Options](http://docs.activeserver.cloud/en/guides/integration/integration-guide/front_end/#continue-challenge-process) page to sample 3DS Requestor, allowing users to not automatically start the challenge process, and optionally send a challenge cancel reason
* Added code to the 3DS-web-adapter to support the new [challenge status](https://docs.activeserver.cloud/en/api/auth/#/ThreeDS%20Authentication/Update%20Challenge%20Status). Details of the integration can be found on the [front-end](http://docs.activeserver.cloud/en/guides/integration/integration-guide/front_end.md#continue-challenge-process) and [back-end](http://docs.activeserver.cloud/en/guides/integration/integration-guide/back_end.md#cancel-challenge-flow) integration guides.
* Changed the back-end example code to support the new Master Auth API client certificate functionality. A description of the implementation can be found in the [Auth API Authentication](http://docs.activeserver.cloud/en/api_document_overview.md#auth-api-authentication) and [Demo 3DS Requestor Configuration](http://docs.activeserver.cloud/en/guides/integration/integration-guide/introduction.md#demo-3ds-requestor-configuration) guides 

### Version 1.1 (compatible with ActiveServer v1.1.1)
* Added test pages for Browser, 3RI and Enrol, to allow full testing of these endpoints with all API parameters
* Added example code for multiple back-end languages, now supported are **Java**, **.NET**, **PHP** and **Go**
* Enhanced result page to show more information and error handling
* Updated the front-end web-adapter web pages to have better content rendering structure as well as better event handling

## Disclaimer

While the sample code examples can be used as a guide for integration purposes, it cannot be fully tested for all production environments and therefore clients should adapt it is to ensure it is suitable before using it for production purposes.

## Legal

Copyright (C) GPayments Pty Ltd - All Rights Reserved
Copying of these files, via any medium, is subject to the 
ActiveServer End User License Agreement (EULA)
 
Proprietary code for use in conjunction with GPayments products only

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Written by GPayments <techsupport@gpayments.com>, 2020
