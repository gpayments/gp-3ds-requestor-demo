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
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"log"
)

type GPayments struct {
	AsAuthUrl     string `yaml:"asAuthUrl"`
	BaseUrl       string `yaml:"baseUrl"`
	CertFileName  string `yaml:"certFileName"`
	GroupAuth     bool   `yaml:"groupAuth"`
	MerchantToken string `yaml:"merchantToken"`
}

type Server struct {
	Port int `yaml:"port"`
}

type Config struct {
	GPayments GPayments
	Server    Server
}

func loadConf() (*Config, error) {
	source, err := ioutil.ReadFile("conf/application.yaml")
	if err != nil {
		return nil, err
	}

	var config Config

	err = yaml.Unmarshal(source, &config)
	if err != nil {
		return nil, err
	}

	log.Printf("loaded configuration %+v\n", config)

	return &config, nil
}
