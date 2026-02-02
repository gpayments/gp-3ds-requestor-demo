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

// All expiryDate fields use automatic extend date + 1 year for testing purposes
var cards = [
  {
    provider: "visa",
    label: "Visa",
    scenarios: [
      {
        name: "frictionless",
        description: "Frictionless authentication",
        label: "Frictionless",
        cardNumber: 4100000000000100,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "frictionlessWithTdsMethod",
        description: "Frictionless (with 3DS Method)",
        label: "Frictionless (with 3DS Method)",
        cardNumber: 4100000000600008,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "challenge",
        description: "Challenge authentication",
        label: "Challenge",
        cardNumber: 4100000000005000,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "attempt",
        description: "Authentication attempt",
        label: "Auth attempt",
        cardNumber: 4100000000100009,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "fail",
        description: "Authentication failed",
        label: "Auth failed",
        cardNumber: 4100000000300005,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "unavailable",
        description: "Authentication unavailable",
        label: "Auth unavailable",
        cardNumber: 4100000000400003,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "reject",
        description: "Authentication rejected",
        label: "Auth rejected",
        cardNumber: 4100000000500000,
        expiryDate: moment().add(1, 'year').format('YYMM')
      }
    ]
  },
  {
    provider: "mastercard",
    label: "Mastercard",
    scenarios: [
      {
        name: "frictionless",
        description: "Frictionless authentication",
        label: "Frictionless",
        cardNumber: 5100000000000107,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "frictionlessWithTdsMethod",
        description: "Frictionless (with 3DS Method)",
        label: "Frictionless (with 3DS Method)",
        cardNumber: 5100000000600005,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "challenge",
        description: "Challenge authentication",
        label: "Challenge",
        cardNumber: 5100000000005007,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "attempt",
        description: "Authentication attempt",
        label: "Auth attempt",
        cardNumber: 5100000000100006,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "fail",
        description: "Authentication failed",
        label: "Auth failed",
        cardNumber: 5100000000300002,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "unavailable",
        description: "Authentication unavailable",
        label: "Auth unavailable",
        cardNumber: 5100000000400000,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "reject",
        description: "Authentication rejected",
        label: "Auth rejected",
        cardNumber: 5100000000500007,
        expiryDate: moment().add(1, 'year').format('YYMM')
      }
    ]
  },
  {
    provider: "jcb",
    label: "JCB",
    scenarios: [
      {
        name: "frictionless",
        description: "Frictionless authentication",
        label: "Frictionless",
        cardNumber: 3528000000000106,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "frictionlessWithTdsMethod",
        description: "Frictionless (with 3DS Method)",
        label: "Frictionless (with 3DS Method)",
        cardNumber: 3528000000600004,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "challenge",
        description: "Challenge authentication",
        label: "Challenge",
        cardNumber: 3528000000005006,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "attempt",
        description: "Authentication attempt",
        label: "Auth attempt",
        cardNumber: 3528000000100005,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "fail",
        description: "Authentication failed",
        label: "Auth failed",
        cardNumber: 3528000000300001,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "unavailable",
        description: "Authentication unavailable",
        label: "Auth unavailable",
        cardNumber: 3528000000400009,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "reject",
        description: "Authentication rejected",
        label: "Auth rejected",
        cardNumber: 3528000000500006,
        expiryDate: moment().add(1, 'year').format('YYMM')
      }
    ]
  },
  {
    provider: "amex",
    label: "AMEX",
    scenarios: [
      {
        name: "frictionless",
        description: "Frictionless authentication",
        label: "Frictionless",
        cardNumber: 340000000000108,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "frictionlessWithTdsMethod",
        description: "Frictionless (with 3DS Method)",
        label: "Frictionless (with 3DS Method)",
        cardNumber: 340000000600006,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "challenge",
        description: "Challenge authentication",
        label: "Challenge",
        cardNumber: 340000000005008,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "attempt",
        description: "Authentication attempt",
        label: "Auth attempt",
        cardNumber: 340000000100007,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "fail",
        description: "Authentication failed",
        label: "Auth failed",
        cardNumber: 340000000300003,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "unavailable",
        description: "Authentication unavailable",
        label: "Auth unavailable",
        cardNumber: 340000000400001,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "reject",
        description: "Authentication rejected",
        label: "Auth rejected",
        cardNumber: 340000000500008,
        expiryDate: moment().add(1, 'year').format('YYMM')
      }
    ]
  },
  {
    provider: "discover",
    label: "Discover",
    scenarios: [
      {
        name: "frictionless",
        description: "Frictionless authentication",
        label: "Frictionless",
        cardNumber: 6440000000000104,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "frictionlessWithTdsMethod",
        description: "Frictionless (with 3DS Method)",
        label: "Frictionless (with 3DS Method)",
        cardNumber: 6440000000600002,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "challenge",
        description: "Challenge authentication",
        label: "Challenge",
        cardNumber: 6440000000005004,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "attempt",
        description: "Authentication attempt",
        label: "Auth attempt",
        cardNumber: 6440000000100003,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "fail",
        description: "Authentication failed",
        label: "Auth failed",
        cardNumber: 6440000000300009,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "unavailable",
        description: "Authentication unavailable",
        label: "Auth unavailable",
        cardNumber: 6440000000400007,
        expiryDate: moment().add(1, 'year').format('YYMM')
      },
      {
        name: "reject",
        description: "Authentication rejected",
        label: "Auth rejected",
        cardNumber: 6440000000500004,
        expiryDate: moment().add(1, 'year').format('YYMM')
      }
    ]
  }
];

//construct provider menu
cards.forEach(function (card) {

  $('#providerSelect').append(
      "<option value=\"" + card.provider + "\">" + card.label + "</option>"
  );
});

//show default scenario
showScenario(cards[0].scenarios);

function onChangeProvider() {

  var provider = $('#providerSelect').val();
  var card = cards.find(function (value) {
    return value.provider === provider;
  });
  showScenario(card.scenarios);
}

function showScenario(scenarios) {
  var select = $('#scenarioSelect');
  select.empty();
  scenarios.forEach(function (scenario) {
    select.append(
        "<option value=\"" + scenario.name + "\">" + scenario.description
        + "</option>"
    );
  });

  showCard();
}

function showCard() {
  var provider = $('#providerSelect').val();
  var card = cards.find(function (value) {
    return value.provider === provider;
  });

  var scenarioName = $('#scenarioSelect').val();
  var scenario = card.scenarios.find(function (value) {
    return value.name === scenarioName;
  });
  $('#acctNumber').val(scenario.cardNumber);
  $('#cardExpiryDate').val(scenario.expiryDate);
}
