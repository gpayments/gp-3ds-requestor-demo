using GPayments.Requestor.TestLab.Models.dto.Threeds1;
using System;
using System.Collections.Generic;
using System.Dynamic;
using System.Linq;
using System.Web.Http;

namespace GPayments.Requestor.TestLab.Controllers.Threeds1
{
    /**
    * MainController class for 3DSecure 1.0.2 transactions.
    *
    * @author GPayments
    */
    public class Main3DS1Controller : ApiController
    {
        private static readonly log4net.ILog logger = log4net.LogManager.GetLogger(typeof(Main3DS1Controller));
        private ThreeDS1Service threeDS1Service = new ThreeDS1Service();

        /**
         * Return JSON response
         */
        [HttpPost, Route("3ds1/auth")]
        public ThreeDS1AuthResp auth([FromBody] ThreeDS1AuthReq req)
        {
            logger.Info(string.Format("3ds1 auth request received: {0}", req));
            return threeDS1Service.HandleAuthRequest(req);
        }

    }
}
