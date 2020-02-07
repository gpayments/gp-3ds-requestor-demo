# Running the Project

Prerequisuite: 
* PHP cURL is needed to access curl functions
```bash
# In Ubuntu you can install with following command
sudo apt-get install php7.2-curl
```

```bash
# Install Composer
curl -sS https://getcomposer.org/installer | php
# Install the dependencies 
php composer.phar install
# Run the project
php -S localhost:8082
```