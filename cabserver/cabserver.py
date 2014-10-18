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

color_cyan_l  		= chr(27) + "[36m"
color_cyan_d  		= chr(27) + "[36;2m"
color_cyan_b  		= chr(27) + "[36;1m"
color_magenta		= chr(27) + "[0;35m"
color_reset  		= chr(27) + "[0m"

VERBOSE_S2 = True	# server details

TEMPLATES_PATH = os.path.join(os.path.dirname(__file__), "templates")
STATIC_PATH = os.path.join(os.path.dirname(__file__), "static")



class ProfileHandler( tornado.web.RequestHandler ):
	""" my custom RequestHandler-inherited class, that is the base class for all my Handler classes below """
	
	# A. init
	def initialize(self, user=""):
		""" hook for subclass initialization """
		self.user = user
		print( "\n"*8 + "NEW REQUEST -- rpiserver\n" + "/\\"*64 )
		if VERBOSE_S2: print("initialize / user = ", self.user)
	def prepare(self):
		""" called before the actual handler method """
		if VERBOSE_S2: print("prepare")
	def on_finish(self):
		""" called after the actual handler method """
		if VERBOSE_S2: print("on_finish")


class MainHandler( ProfileHandler ):
	""" handles requests from humans / web browser """
	def get(self):
		self.set_header('Content-Type', 'text/html')
		#self.write("Hello, world!")
		#raise tornado.web.HTTPError(403)
		items = ["Item 1", "Item 2", "Item 3"]
		self.render( "index.html", title="My title", items=items )
		
	
	def post(self):
		self.set_header('Content-Type', 'text/html')

class CabHandler( ProfileHandler ):
	""" handles requests from cab tablets """
	def get(self):
		self.set_header("Content-Type", "application/json")
	def post(self):
		self.set_header("Content-Type", "application/json")



#*******************************************************************************
#
# setup handlers
#
#*******************************************************************************

my_settings = {
				"static_path" : STATIC_PATH,
				"template_path": TEMPLATES_PATH,
				#"cookie_secret":"",
				"debug":True,
				}
my_handlers = [	(r"/", MainHandler),
				(r"/webservice/([0-9a-zA-Z_;]*)", CabHandler, dict()),
				#(r"/story/([0-9]+)", StoryHandler),
				]
class Application(tornado.web.Application):
	def __init__(self, *args, **kwargs):
		# your global server/tester status:
		# [idle, busy, testing, ... ]
		super(Application, self).__init__(*args, **kwargs)

application = Application( my_handlers, **my_settings )




#*******************************************************************************
#
# main
# & fire off the tornado server
#
#*******************************************************************************

# 	# install the signal handlers
def bye_handler(signal, frame):
	logging.info(" Stopping RaspberryPi WebServer! ")
	sys.exit(0)
#         jabber_client.info('interrupt signal received, shutting down...')
#         jabber_client.bye = True
#         jabber_client.do_disconnect()

def main():
	logging.basicConfig(filename=None, level=logging.INFO,
				format='%(asctime)s: %(levelname)s: %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
	
	signal.signal(signal.SIGINT, bye_handler)
	signal.signal(signal.SIGTERM, bye_handler)
	
	logging.info(" Starting RaspberryPi WebServer! ")
	application.listen(8080, '0.0.0.0')
	#ipdb.set_trace()	# definitely palce your debugging call BEFORE IOLoop !
	tornado.ioloop.IOLoop.instance().start()

if __name__ == "__main__":
	main()


