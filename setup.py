from setuptools import setup, find_packages

with open('requirements.txt') as req_file:
    requirements = req_file.read()


setup(
    name='ubikampus-webbluetooth',
    packages=find_packages('.'),
    install_requires=requirements,
)
