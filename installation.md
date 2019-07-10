# Setting up and installing bluetooth-raspberry-scanner

## Raspberry setup
If you have already setup your raspberry's wifi and ssh, you can skip this

### 1. Go to raspberry preferences
 - Enable SSH
 - Set WiFi country
 - (Optional: change hostname)

### 2. Set up network and SSH
  - Connect the raspberry to a network
  - Add your public SSH-key to the raspberry. Easiest way is to use [ssh-copy-id](https://www.ssh.com/ssh/copy-id)
  - SSH will ask for password. The default password is probably `raspberry`
  - Now you should be able to SSH into the raspberry without password
  - For security, you should disable password authentication from `/etc/ssh/sshd_config`
  - The change will come to effect after you restart ssh: `service ssh restart`
  - If you don't, at least change the password


### 3. Install Node version manager and Node
  - Install nvm: `wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.34.0/install.sh | bash`
  - Use nvm to install Node 8: `nvm install 8`
  - Set Node 8 as default version: `nvm alias default 8`
  - Change root's Node version to Node 8:
  ```
    n=$(which node); \
    n=${n%/bin/node}; \
    chmod -R 755 $n/bin/*; \
    sudo cp -r $n/{bin,lib,share} /usr/local
 ```
 - Verify that both `node -v` and `sudo node -v` report version 8

 ## Installing the application

 ### 1. Clone the scanner project with git
 - `git clone https://github.com/ubikampus/bluetooth-raspberry-scanner.git`

 ### 2. Create .env file in project root directory
 The env file is used to configure enviromental variables. It needs two variables:
 - `mqttUrl=<url>` and `raspId=<unique_name>`
 - mqttUrl defines the address of the mqtt broker
 - raspId is used to identify raspberries in your system. It must be unique
 - Example:
 ```
 mqttUrl=iot.myserver.net
 raspId=rasp-1
 ```

 ### 3. Run the scanner
 (do these in project root folder)
 - Install dependencies:
 `npm install`
 - run program
 `sudo node src/app.js`
 (Bluetooth requires root access)
 - to run without sudo, give node permissions to Bluetooth
 ```sudo setcap cap_net_raw+eip $(eval readlink -f `which node`)```

## Web bluetooth GATT server setup

### Bluetoothd

Configure bluetoothd to run with `--experimental` flag,
`sudo systemctl edit bluetooth`, add section:

```
[Service]
ExecStart=
ExecStart=/usr/sbin/bluetoothd --experimental
```

`sudo systemctl reload bluetooth`

`bluetoothctl`

In the new shell:

`> advertise on` (leave the shell open!)

### GATT server

Install python dependencies: `python3 -m pip install --user
webbluetooth/requirements.txt`

Install the server: `python3 -m pip install bluetooth-raspberry-scanner`

Start the server: `python3 -m webbluetooth`

Server should be now ready to accept new web bluetooth connections.
