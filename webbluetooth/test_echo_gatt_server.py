from array import array

from unittest.mock import patch, Mock
from unittest import TestCase

from webbluetooth.echo_gatt_server import (
    UserDescriptionDescriptor,
    DeviceNameChar,
    Characteristic,
)


def nil_constructor(*args):
    pass


class GattServerTest(TestCase):
    @patch.object(UserDescriptionDescriptor,
                  '__init__',
                  new=nil_constructor)
    @patch('webbluetooth.echo_gatt_server.current_bt_device')
    def test_user_description_fetches_bt_name(self, current_bt):
        current_bt.return_value = 'huawei'
        descriptor = UserDescriptionDescriptor()

        device_name = descriptor.ReadValue({})

        assert device_name == array('B', [104, 117, 97, 119, 101, 105])

    @patch.object(Characteristic, '__init__', new=nil_constructor)
    @patch('webbluetooth.echo_gatt_server.UserDescriptionDescriptor')
    def test_device_name_characteristic_adds_descriptor(self, user_info):
        add_descriptor = Mock()
        DeviceNameChar.add_descriptor = add_descriptor
        DeviceNameChar('bus', 1, None)

        assert add_descriptor.call_count == 1
