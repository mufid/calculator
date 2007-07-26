#!/usr/bin/python

import mebuild

spec = dict(
cldc    = '1.1',
midp    = '2.0',
name    = 'Javia Calculator',
icon    = 'a',
cls     = 'C',
version = '0.1.2',
author  = 'Mihai Preda & Carlo Teubner',
infoUrl = 'http://calculator.javia.org/',
extralibs = [],
fileName  = 'calculator'
)

midlet = mebuild.Midlet(**spec)

midlet.build(mebuild.cmdLineOptions())
