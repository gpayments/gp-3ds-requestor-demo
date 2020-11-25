<?php session_start();
/**
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
class SessionKeys
{
    const INIT_AUTH_REQUEST = 'initAuthRequest';
    const INIT_AUTH_REQUEST_NOSCRIPT = "initAuthRequestNoscript";
    const INIT_AUTH_RESPONSE = "initAuthResponse";
    const AUTH_REQUEST = "authRequest";
    const AUTH_RESPONSE = "authResponse";
}
