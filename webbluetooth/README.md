# Client usage

See installation.md for server setup.

Client example for fetching device bluetooth name: https://googlechrome.github.io/samples/web-bluetooth/write-descriptor-async-await.html

bluetooth service: device_info

bluetooth characteristic: gap.device_name

Select correct peripheral server server and submit, device name should be
visible in the demo output.

# Troubleshooting

### "NotFoundError" No Services matching UUID...

Implies that the GATT server is not running on the selected server.

### Raspberry mac address / name is not visible in the client bluetooth listing

Run `bluetoochtl` and enter command `advertise on`
