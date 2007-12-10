#!/usr/bin/python

import mebuild

spec = dict(
cldc    = '1.1',
midp    = '2.0',
name    = 'Javia Calculator',
icon    = 'a',
cls     = 'Calc',
version = '0.2.0',
vendor  = 'Mihai Preda',
infoUrl = 'http://calculator.javia.org/',
extralibs = [],
fileName  = 'calculator'
)

midlet = mebuild.Midlet(**spec)

midlet.build(mebuild.cmdLineOptions())
