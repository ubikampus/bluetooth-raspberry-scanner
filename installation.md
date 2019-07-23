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

### 3 Set up bluetoothd

Configure bluetoothd to run with `--experimental` flag,
`sudo systemctl edit bluetooth`, add section:

```
[Service]
ExecStart=
ExecStart=/usr/sbin/bluetoothd --experimental
```

Run `sudo systemctl reload bluetooth`

Run `bluetoothctl`

In the new shell:

`advertise on` (leave the shell open!)

### 4. Python native dependencies

`sudo apt-get install bluetooth libbluetooth-dev`

Install PyGObject dependency:
https://pygobject.readthedocs.io/en/latest/getting_started.html#ubuntu-getting-started

The project is tested on python 3.5 and python 3.6, verify python version by
running `python3 --version`.

## Installing the application

### 1. Clone the scanner project with git
- `git clone https://github.com/ubikampus/bluetooth-raspberry-scanner.git`

### 2. Install the package
- `python3 -m pip install --user --upgrade .`

Configure MQTT_URL (e.g. mqtt.example.com), RASPBERRY_ID and MQTT_PUB_TOPIC in
pybluez/pybluez.service file. Copy both /pybluez/pybluez.service and
/webbluetooth/webbluetooth.service files to raspberry pi's /etc/systemd/system
directory. Run `sudo systemctl daemon-reload`

### 3. Start the scanner and web bluetooth server

1. sudo systemctl enable pybluez
1. sudo systemctl start pybluez
1. sudo systemctl enable webbluetooth
1. sudo systemctl start webbluetooth

Verify service statuses with `systemctl status pybluez` or `systemctl status
webbluetooth`
