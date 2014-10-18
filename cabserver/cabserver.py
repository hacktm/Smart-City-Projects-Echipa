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

import logging, signal
logging.basicConfig(filename=None, level=logging.DEBUG, format='%(asctime)s: %(levelname)7s: [%(name)s] %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
from db_access import configure, sql
import tornado.ioloop
import tornado.web
import os, sys, subprocess
import json
import re
import time, datetime
from pprint import pformat as ppf
import urlparse
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
		print( "\n"*8 + "NEW REQUEST -- cabserver\n" + "/\\"*64 )
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
		""" render the html template """
		self.set_header('Content-Type', 'text/html')
		items = ["Item 1", "Item 2", "Item 3"]
		self.render( "index.html", title="My title", items=items )
		
	
	def post(self):
		""""  not much """
		pass


class CabHandler( ProfileHandler ):
	""" handles requests from cab tablets """
	def web_arg_to_py_obj(self, arg_name):
		"""
		web arguments (from GET & POST) seem to come in as strings --> objectify them
		"""
		a1 = self.get_argument(arg_name, {'status': 'No data received'})
		a2 = a1.replace("'", '"').replace('u"','"').replace('False','false').replace('True','true')
		try:
			a3 = json.loads(a2)
		except ValueError:
			a3 = {'status': 'web arg could not be decoded into valid python obj'}
		#print("a1 = ", a1 ); print("a2 = ", a2 ); print("a3 = ", a3 )
		return a3
	
	def get(self, command):
		self.set_header("Content-Type", "application/json")
		pr_dict = dict(urlparse.parse_qsl(self.request.query[:-1], keep_blank_values=True))
		print("cabserver.py GET / CommandHandler received: command: %s%s%s \t params: %s%s%s" \
						%(color_cyan_b, command, color_reset, \
						color_cyan_l, pr_dict, color_reset) )
		
		resp_served = {}
		if command == "get_trips":
			# TODO SQL 
			# - input: pr_dict['user_id']
			if not pr_dict.has_key('user_id'):
				resp_served = {'status': "error", 'details': "missing parameters: %s" %('user_id'), 'api_fn': command}
			else:
				resp_served = {	'id'          : -001,
							'id_user'     : pr_dict['user_id'],
							'start_moment': time.time(),
							'stop_moment' : time.time(),
							'distance'    : -1,
							'idle'        : -1,
							'price'       : -1,
							'status'      : "OK",
							}
		elif command == "get_positions":
			# TODO SQL
			# - input: pr_dict['trip_id']
			if not pr_dict.has_key('trip_id'):
				resp_served = {'status': "error", 'details': "missing parameters: %s" %('trip_id'), 'api_fn': command}
			else:
				positions_list = [	{'lat':-1, 'lng': -1, 'time': time.time()}, 
									{'lat':-1, 'lng': -1, 'time': time.time()},
								]
				resp_served = {	'id_trip': pr_dict['trip_id'],
							'positions': positions_list,
							'status'      : "OK",
						}
		else: # not defined
			resp_served = {'status': "error", 'details': "function not implemented", 'api_fn': command}
		
		self.write(resp_served)
	
	
	def post(self, command):
		self.set_header("Content-Type", "application/json")
		pr_dict = {'warn': 'for POST: you have to know you params by name'}
		print("cabserver.py POST / CommandHandler received: command: %s%s%s \t params: %s%s%s" \
				%(color_cyan_b, command, color_reset, \
				color_cyan_l, pr_dict, color_reset) )
		if command == "set_position":
			pr_dict = dict( user_id   = self.get_argument('user_id', ''),
							timestamp = self.get_argument('timestamp', ''),
							lat       = self.get_argument('lat', ''),
							lng       = self.get_argument('lng', ''),
							)
		elif command == "add_event":
			pr_dict = dict( user_id   = self.get_argument('user_id', ''),
							timestamp = self.get_argument('timestamp', ''),
							type      = self.get_argument('type', ''),
							city_hwy  = self.get_argument('city_hwy', ''),
							distance  = self.get_argument('distance', ''),
							price     = self.get_argument('price', ''),
						)
		else: # not defined
			resp_served = {'status': "error", 'details': "function not implemented", 'api_fn': command}
		
		if VERBOSE_S2: print("params: %s%s%s" %(color_cyan_l, pr_dict, color_reset))


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
				(r"/webservice/([0-9a-zA-Z_;]*)", CabHandler),
				#(r"/story/([0-9]+)", StoryHandler),
				]
class Application(tornado.web.Application):
	def __init__(self, *args, **kwargs):
		# your global server/tester status:
		# [idle, busy, testing, ... ]
		super(Application, self).__init__(*args, **kwargs)

application = Application( my_handlers, **my_settings )

dbcon = configure(db_host='127.0.0.1', db_port='5432', db_name='cashacab', db_username='postgres', db_password='RasPi', autocommit=True)

#sql(statement, commit=False, conn_name='default', **kwargs)
# sql_r = sql("""INSERT INTO trips (id_user, start_moment, stop_moment, distance, idle, price)
# 			VALUES (%(id_user)s, %(start_moment)s, %(stop_moment)s, %(distance)s, %(idle)s, %(price)s) """,
# 			id_user=1, start_moment=time.strftime( "%Y-%m-%d %H:%M:%S" ), stop_moment=time.strftime( "%Y-%m-%d %H:%M:%S" ), distance=10000, idle=10, price=20.99 )
# print("sql_r = ", sql_r)
sql_r = sql('SELECT * FROM trips')
print("sql_r = ", sql_r)


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
	#logging.basicConfig(filename=None, level=logging.INFO,
	#			format='%(asctime)s: %(levelname)s: %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
	
	
	signal.signal(signal.SIGINT, bye_handler)
	signal.signal(signal.SIGTERM, bye_handler)
	
	logging.info(" Starting RaspberryPi WebServer! ")
	application.listen(8080, '0.0.0.0')
	#ipdb.set_trace()	# definitely palce your debugging call BEFORE IOLoop !
	tornado.ioloop.IOLoop.instance().start()

if __name__ == "__main__":
	main()


