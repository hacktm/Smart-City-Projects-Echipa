#!/usr/bin/env python
# -*- coding: utf-8 -*-

""" webserver running on RaspberryPi machine
is used to served RaspiStarter tests to your browser
"""
#
# main
#
#  @author: Ovidiu Vatafu
#   @event: HackTM
#  Copyright 2014
#

from __future__ import print_function


import tornado.ioloop
import tornado.web
import os, sys, subprocess
import json
import logging, signal
import re
import time, datetime
from pprint import pformat as ppf
import traceback
#import ipdb			# 
from types import * 	# this module they say is explicitly safe for import *

color_magenta		= chr(27) + "[0;35m"
color_reset  		= chr(27) + "[0m"



def main():
	logging.basicConfig(filename=None, level=logging.INFO,
				format='%(asctime)s: %(levelname)s: %(message)s', datefmt='%Y-%m-%d %H:%M:%S')

	logging.info(" Starting RaspberryPi WebServer! ")
	application.listen(8080, '0.0.0.0')
	#ipdb.set_trace()	# definitely palce your debugging call BEFORE IOLoop !
	tornado.ioloop.IOLoop.instance().start()

if __name__ == "__main__":
	main()

