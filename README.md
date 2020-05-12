# GPayments 3DS Requestor demo code

Repository containing the GPayments 3DS Requestor demo code, to be used to integrate with [**GPayments ActiveServer**](https://www.gpayments.com/solutions/3ds-server-activeserver/).

For full instructions on using the demo code, please refer to the [documentation](https://docs.activeserver.cloud/en/guides/integration/integration_overview/).

The master branch always contains the latest 3DS Requestor demo code version. For previous versions, check out the [releases tab](https://github.com/gpayments/gp-3ds-requestor-demo/releases). 

## Release notes

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
