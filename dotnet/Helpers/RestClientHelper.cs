/************************************************************************
* Copyright(C) GPayments Pty Ltd - All Rights Reserved
* Copying of this file, via any medium, is subject to the
* ActiveServer End User License Agreement (EULA)
*
* Proprietary code for use in conjunction with GPayments products only
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Written by GPayments<techsupport@gpayments.com>, 2020
************************************************************************/

using Newtonsoft.Json;
using System;
using System.IO;
using System.Net;
using System.Security.Cryptography.X509Certificates;

namespace GPayments.Requestor.TestLab.Helpers
{
    public class RestClientHelper
    {
        private const string DEFAULT_CERT_FILE_PASSWORD = "123456";
        private static readonly log4net.ILog logger = log4net.LogManager.GetLogger(typeof(RestClientHelper));
        private static X509Certificate2 caCert = null;

        private static string keyStorePassword
        {
            get { return !string.IsNullOrEmpty(Config.CertFilePassword) ? Config.CertFilePassword : DEFAULT_CERT_FILE_PASSWORD; }
        }

        static RestClientHelper()
        {
            string certsPath = System.Web.Hosting.HostingEnvironment.MapPath("~/Certs");
            caCert = new X509Certificate2(string.Format(@"{0}\{1}", certsPath, "cacerts.pem"), keyStorePassword);

            ServicePointManager.SecurityProtocol = SecurityProtocolType.Ssl3 | SecurityProtocolType.Tls | SecurityProtocolType.Tls11 | SecurityProtocolType.Tls12;
            ServicePointManager.ServerCertificateValidationCallback = (sender, certificate, chain, sslPolicyErrors) =>
            {
                // If the certificate is a valid, signed certificate, return true
                if (sslPolicyErrors == System.Net.Security.SslPolicyErrors.None)
                    return true;

                // If there are errors in the certificate chain
                if ((sslPolicyErrors & System.Net.Security.SslPolicyErrors.RemoteCertificateChainErrors) != 0)
                {
                    X509Chain caChain = new X509Chain();
                    caChain.ChainPolicy.ExtraStore.Add(caCert);
                    caChain.ChainPolicy.VerificationFlags = X509VerificationFlags.NoFlag;
                    caChain.ChainPolicy.RevocationMode = X509RevocationMode.NoCheck;
                    caChain.Build(new X509Certificate2(certificate));
                    if (caChain.ChainStatus.Length == 0)
                        return false;
                    return caChain.ChainStatus[0].Status == X509ChainStatusFlags.NoError || caChain.ChainStatus[0].Status == X509ChainStatusFlags.PartialChain;
                }
                else
                    // In all other cases, return false.
                    return false;
            };
        }

        private static X509Certificate2 GetClientCertificate()
        {
            string clientCertPath = Config.CertFileName.TrimStart(new char[] { '/', '\\' });
            if (!Path.IsPathRooted(clientCertPath))
                clientCertPath = System.Web.Hosting.HostingEnvironment.MapPath("~") + clientCertPath;
            X509Certificate2 clientCert = new X509Certificate2(clientCertPath, keyStorePassword, X509KeyStorageFlags.DefaultKeySet);
            if (!clientCert.HasPrivateKey)
                throw new Exception("Certificate has no private key.");
            return clientCert;
        }

        public static object PostForObject(string url, object request, Type responseType)
        {
            HttpWebRequest req = (HttpWebRequest)HttpWebRequest.Create(url);
            req.Method = "POST";
            req.ClientCertificates.Add(GetClientCertificate());
            //the certificate is for groupAuth, work out the header
            if (Config.GroupAuth)
                req.Headers.Add("AS-Merchant-Token", Config.MerchantToken);
            req.ContentType = "application/json;charset=utf-8";
            string strRequest = JsonConvert.SerializeObject(request, Formatting.Indented, new JsonSerializerSettings() { NullValueHandling = NullValueHandling.Ignore });
            byte[] postData = System.Text.Encoding.UTF8.GetBytes(strRequest);
            req.ContentLength = postData.Length;
            using (Stream streamOut = req.GetRequestStream())
                streamOut.Write(postData, 0, postData.Length);
            string result = null;
            using (StreamReader streamIn = new StreamReader(req.GetResponse().GetResponseStream()))
                result = streamIn.ReadToEnd();
            return JsonConvert.DeserializeObject(result, responseType);
        }

        public static object GetForObject(string url, Type responseType)
        {
            try
            {
                HttpWebRequest req = (HttpWebRequest)HttpWebRequest.Create(url);
                req.Method = "GET";
                req.ClientCertificates.Add(GetClientCertificate());
                //the certificate is for groupAuth, work out the header
                if (Config.GroupAuth)
                    req.Headers.Add("AS-Merchant-Token", Config.MerchantToken);
                string result = null;
                using (StreamReader streamIn = new StreamReader(req.GetResponse().GetResponseStream()))
                    result = streamIn.ReadToEnd();
                return JsonConvert.DeserializeObject(result, responseType);
            }
            catch (Exception exp)
            {
                logger.Error(exp.Message, exp);
                throw exp;
            }
        }
    }
}