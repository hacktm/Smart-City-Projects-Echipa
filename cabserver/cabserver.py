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
			# - input: pr_dict['id_user']
			if not pr_dict.has_key('id_user'):
				resp_served = {'status': "error", 'details': "missing parameters: %s" %('id_user'), 'api_fn': command}
			else:
				sql_r = sql('SELECT * FROM trips WHERE id_user = %(id_user)s', id_user=pr_dict['id_user'])
				if type(sql_r) is ListType   and   len(sql_r) >= 1:
					resp_served['trips_list'] = []
					for item in sql_r:
						resp_served['trips_list'].append({	'id'          : item[0],
															'id_user'     : pr_dict['id_user'],
															'start_moment': str(item[2]),
															'stop_moment' : str(item[3]),
															'start_location': item[4],
															'stop_location': item[5],
															'distance'    : item[6],
															'idle'        : item[7],
															'price'       : item[8],
														})
						resp_served['status'] = "OK"
				else:
					resp_served = {'status': "WARN", 'details': "no records marched", 'api_fn': command}
		elif command == "get_positions":
			# TODO SQL
			# - input: pr_dict['trip_id']
			if not pr_dict.has_key('trip_id'):
				resp_served = {'status': "error", 'details': "missing parameters: %s" %('trip_id'), 'api_fn': command}
			else:
				sql_r = sql('SELECT * FROM positions WHERE id_trip = %(id_trip)s', id_trip=pr_dict['id_trip'])
				positions_list = [	{'lat':-1, 'lng': -1, 'time': time.time()}, 
									{'lat':-1, 'lng': -1, 'time': time.time()},
								]
				resp_served = {	'id_trip': pr_dict['trip_id'],
							'positions': positions_list,
							'status'      : "OK",
						}
		elif command == "login":
			if not pr_dict.has_key('user')   or   not pr_dict.has_key('pswd'):
				resp_served = {'status': "error", 'details': "missing one or both of the following parameters: %s" %('user or pswd'), 'api_fn': command}
			else:
				sql_r = sql("""SELECT id, name, email, phone, level FROM users WHERE name = %(user)s AND pswd = %(pswd)s""", user=pr_dict['user'], pswd=pr_dict['pswd'] )
				if type(sql_r) is ListType   and   len(sql_r) == 0:
					resp_served = {'status': "WARN", 'details': "no records matched", 'api_fn': command}
				if type(sql_r) is ListType   and   len(sql_r) == 1:
					resp_served['user_info'] = { 	'id_user':     sql_r[0][0],
													'name':    sql_r[0][1],
													'email':   sql_r[0][2],
													'phone':   sql_r[0][3],
													'level':   sql_r[0][4],
												}
					# also add prices:
					sql_r = sql("""SELECT day, night, city, hwy, stationary FROM tarrifs """)
					print("sql_r = ", sql_r)
					if type(sql_r) is ListType   and   len(sql_r) == 1:
						resp_served['tarrifs'] = { 	'day':     sql_r[0][0],
													'night':    sql_r[0][1],
													'cty':   sql_r[0][2],
													'hwy':   sql_r[0][3],
													'stationary':   sql_r[0][4],
												}
					resp_served['status'] = "OK"
				elif type(sql_r) is ListType   and   len(sql_r) > 1:
					resp_served = {'status': "error", 'details': "found more users with the same credentials", 'api_fn': command}
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
			pr_dict = dict( id_user   = self.get_argument('id_user', ''),
							timestamp = self.get_argument('timestamp', ''),
							lat       = self.get_argument('lat', ''),
							lng       = self.get_argument('lng', ''),
							)
		elif command == "add_event":
			pr_dict = dict( id_user   = self.get_argument('id_user', ''),
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


#*******************************************************************************
#
# setup psql
#
#*******************************************************************************

dbcon = configure(db_host='127.0.0.1', db_port='5432', db_name='cashacab', db_username='postgres', db_password='RasPi', autocommit=True)

#sql(statement, commit=False, conn_name='default', **kwargs)
# sql_r = sql("""INSERT INTO trips (id_user, start_moment, stop_moment, start_location, stop_location, distance, idle, price)
# 			VALUES (%(id_user)s, %(start_moment)s, %(stop_moment)s, %(start_location)s, %(stop_location)s, %(distance)s, %(idle)s, %(price)s) """,
# 			id_user=1, start_moment=time.strftime( "%Y-%m-%d %H:%M:%S" ), stop_moment=time.strftime( "%Y-%m-%d %H:%M:%S" ), start_location=[-37.81319, 144.96298], stop_location=[-31.95285, 115.85734], distance=2721560.85287, idle=10, price=200.99 )
# sql_r = sql("""INSERT INTO users (id, name, pswd, email, phone, level)
# 			VALUES ( %(id)s, %(name)s, %(pswd)s, %(email)s, %(phone)s, %(level)s )""",
# 			id=1, name='giglel', pswd='1234', email='g@y.ro', phone='+40256123456', level=1)
# sql_r = sql("""INSERT INTO tarrifs (day, night, city, hwy, stationary)
# 			VALUES ( %(day)s, %(night)s, %(city)s, %(hwy)s, %(stationary)s )""",
# 			day=1, night=3, city=1, hwy=2, stationary=0.50)
# print("sql_r = ", sql_r)
create_trips = """CREATE TABLE trips(
	id             bigserial  PRIMARY KEY, 
	id_user        bigserial, 
	start_moment   timestamp, 
	stop_moment    timestamp, 
	start_location float8[2],
	stop_location  float8[2],
	distance       bigint, 
	idle           integer,
	price          money
	);"""
create_users = """CREATE TABLE users(
	id             bigserial  PRIMARY KEY, 
	name           text, 
	pswd           text, 
	email          text, 
	phone          text, 
	level          smallint
	);"""
create_positions = """CREATE TABLE positions(
	id             bigserial  PRIMARY KEY,
	id_trip        bigserial,
	lat            float8,
	lng            float8,
	time           timestamp,
	city_hwy       bool
	);"""
create_tarrifs = """CREATE TABLE tarrifs(
	day	        money,
	night       money,
	city        money,
	hwy         money,
	stationary  money
	);"""
# KEEP IT COMMENTED !
# sql_r = sql(create_trips)
# sql_r = sql(create_users)
# sql_r = sql(create_positions)
# sql_r = sql(create_tarrifs)
# print("sql_r = ", sql_r)
# exit(0)

#sql_r = sql('SELECT * FROM trips')
#print("sql_r = ", sql_r)


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


