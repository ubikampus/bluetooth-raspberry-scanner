from unittest import TestCase
from unittest.mock import patch

from webbluetooth.bluetooth_hacks import current_bt_device


def mock_popen(stdout):
    class MockPopen:
        def __init__(self, *arg, **kwargs):
            pass

        def communicate(self):
            return stdout.encode(), b''

    return MockPopen


class BluetoothHackTest(TestCase):
    @patch('webbluetooth.bluetooth_hacks.Popen', new=mock_popen('invalid'))
    def test_regex_fails_parse(self):
        with self.assertRaisesRegex(RuntimeError, r'failed to parse'):
            current_bt_device()

    @patch('webbluetooth.bluetooth_hacks.Popen',
           new=mock_popen('94m[huawei-123]'))
    def test_regex_successful_parse(self):
        result = current_bt_device()

        self.assertEqual(result, 'huawei-123')
