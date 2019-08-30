# Beacon scanner for Raspberry Pi 3 computers using [noble](https://github.com/noble/noble)

### Requirements
* Node 8

### Setup:

#### Raspberry Preferences menu:
* (Optional: Set raspberry name)
* Enable SSH
* Set wifi country
* (Optional: Set keyboard language)

#### Raspberry Preferences menu:
* Connect the raspberry to a wifi network
* Add at least one ssh key to raspberry, for example with ssh-copy-id
* Make sure that the ssh key works
* Disable password authentication from /etc/ssh/sshd_config (remember to use sudo)
* Restart ssh (shell command: service ssh restart)

#### Install nvm and update Node to version 8 if it is not already installed:
* wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.34.0/install.sh | bash
* nvm install 8
* nvm alias default 8
* Change root node version to node 8:
    		n=$(which node); \
    		n=${n%/bin/node}; \
    		chmod -R 755 $n/bin/*; \
    		sudo cp -r $n/{bin,lib,share} /usr/local  
* verify that both ‘which node’ and ‘sudo which node’ report the right version

#### Clone the scanner

#### Create .env file in project root directory:
For instance with command : ```echo -e "mqttUrl=iot.ubikampus.net\nraspId=rasp-1" > .env```
The .env file needs two options: mqttUrl and raspId. mqttUrl is the address of the mqtt broker.
raspId specifies a name for the rasp. This must be unique in your system of raspberries. mqttTopic has to be 
changed in source code. 

#### Running the scanner:
* install dependencies: ```npm install```
* run program with: ```sudo node src/app.js```
* to run without sudo, give node permissions to bluetooth:```sudo setcap cap_net_raw+eip $(eval readlink -f `which node`)```



