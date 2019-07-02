import re
from subprocess import Popen, PIPE


def current_bt_device():
    """Hack to get currently connected bluetooth device name."""
    res = Popen("./grab_name.sh", stdout=PIPE, stderr=PIPE)
    stdout, stderr = res.communicate()

    match = re.search(r'94m\[([^\]]*)\]', stdout.decode('utf8'))

    if not match:
        error = 'failed to parse bluetoothctl output: {}, {}'.format(
            stdout,
            stderr,
        )

        raise RuntimeError(error)

    return match.group(1)
