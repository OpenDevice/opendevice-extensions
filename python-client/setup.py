from distutils.core import setup

setup(
    # Application name:
    name="opendevice",

    # Version number (initial):
    version="0.1.3",

    # Application author details:
    author="Ricardo JL Rufino",
    author_email="ricardo.jl.rufino@gmail.com",

    # Packages
    packages=["opendevice", "opendevice.connection", "opendevice.util"],

    # Details
    url="http://opendevice.io",

    #
    # license="LICENSE.txt",
    description="Python Client for OpenDevice",

    # Dependent packages (distributions)
    install_requires=[
        # "flask",
    ],
)
