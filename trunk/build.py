#!/usr/bin/python

import mebuild
import glob
import shutil
from os import path

spec = dict(
cldc    = '1.1',
midp    = '2.0',
name    = 'Calculator',
icon    = 'a',
cls     = 'C',
version = '0.1.3',
author  = 'Mihai Preda & Carlo Teubner',
infoUrl = 'http://calculator.javia.org/',
extralibs = []
)

midlet = mebuild.Midlet(**spec)

midlet.build(mebuild.cmdLineOptions())
