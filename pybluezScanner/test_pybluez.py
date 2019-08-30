from unittest import TestCase

from pybluez.pybluez import validate_environ


class PybluezTest(TestCase):
    def test_raises_if_url_is_missing(self):
        environ = {'RASPBERRY_ID': 'pi-1'}

        with self.assertRaises(RuntimeError):
            validate_environ(environ)

    def test_does_not_raise_if_all_found(self):
        environ = {'RASPBERRY_ID': 'pi-1', 'MQTT_URL': 'iot.example.com'}
        validate_environ(environ)
