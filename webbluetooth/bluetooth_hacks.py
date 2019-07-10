import re
from subprocess import Popen, PIPE
import subprocess


def current_bt_device():
    """Hack to get currently connected bluetooth device name."""
    res = subprocess.run(
        'echo exit | bluetoothctl | grep exit',
        shell=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT
    )

    stdout = res.stdout

    match = re.search(r'94m\[([^\]]*)\]', stdout.decode('utf8'))

    if not match:
        error = 'failed to parse bluetoothctl output: {}, {}'.format(
            stdout,
            stderr,
        )

        raise RuntimeError(error)

    return match.group(1)
