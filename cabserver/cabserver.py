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
import hashlib, random
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

class Index1( ProfileHandler ):
	def get(self):
		""" render the html template """
		self.set_header('Content-Type', 'text/html')
		self.render( "index-1.html", title="My title" )
class Index2( ProfileHandler ):
	def get(self):
		""" render the html template """
		self.set_header('Content-Type', 'text/html')
		self.render( "index-2.html", title="My title" )
class Index3( ProfileHandler ):
	def get(self):
		""" render the html template """
		self.set_header('Content-Type', 'text/html')
		self.render( "index-3.html", title="My title" )
class Index4( ProfileHandler ):
	def get(self):
		""" render the html template """
		self.set_header('Content-Type', 'text/html')
		self.render( "index-4.html", title="My title" )


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
		print("cabserver.py GET / CabHandler received: command: %s%s%s \t params: %s%s%s" \
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
															'start_location': {'lat': item[4][0], 'lng': item[4][1]},
															'stop_location':  {'lat': item[5][0], 'lng': item[5][1]},
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
			if not pr_dict.has_key('id_trip'):
				resp_served = {'status': "error", 'details': "missing parameters: %s" %('id_trip'), 'api_fn': command}
			else:
				sql_r = sql('SELECT id_trip, lat, lng, time, city_hwy FROM positions WHERE id_trip = %(id_trip)s', id_trip=pr_dict['id_trip'])
				if type(sql_r) is ListType   and   len(sql_r) >= 1:
					resp_served['positions_list'] = []
					for item in sql_r:
						resp_served['positions_list'].append({	'lat': item[1], 
															'lng': item[2], 
															'time': str(item[3]),
															'city_hwy': item[4],
														})
					resp_served['status'] = "OK"
				else:
					resp_served = {'status': "WARN", 'details': "no records marched", 'api_fn': command}
		elif command == "login":
			if not pr_dict.has_key('username')   or   not pr_dict.has_key('pswd'):
				resp_served = {'status': "error", 'details': "missing one or both of the following parameters: %s" %('username or pswd'), 'api_fn': command}
			else:
				sql_r = sql("""SELECT id, name, username, email, phone, level FROM users WHERE username = %(username)s AND pswd = %(pswd)s""", username=pr_dict['username'], pswd=pr_dict['pswd'] )
				if type(sql_r) is ListType   and   len(sql_r) == 0:
					resp_served = {'status': "WARN", 'details': "no records matched", 'api_fn': command}
				if type(sql_r) is ListType   and   len(sql_r) == 1:
					resp_served['user_info'] = { 	'id_user':     sql_r[0][0],
													'name':        sql_r[0][1],
													'username':    sql_r[0][1],
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
													'city':   sql_r[0][2],
													'hwy':   sql_r[0][3],
													'stationary':   sql_r[0][4],
												}
					resp_served['status'] = "OK"
				elif type(sql_r) is ListType   and   len(sql_r) > 1:
					resp_served = {'status': "error", 'details': "found more users with the same credentials", 'api_fn': command}
		
		elif command == "get_drivers":
				sql_r = sql("""SELECT id, name, username, email, phone, level FROM users """ )
				if type(sql_r) is ListType   and   len(sql_r) == 0:
					resp_served = {'status': "WARN", 'details': "no records matched", 'api_fn': command}
				if type(sql_r) is ListType   and   len(sql_r) >= 1:
					resp_served['user_list'] = []
					for item in sql_r:
						resp_served['user_list'].append({	'id_user': item[0],
															'name':        item[1],
															'username':    item[1],
															'email':       item[2],
															'phone':       item[3],
															'level':       item[4],
															})
					resp_served['status'] = "OK"
		
		else: # not defined
			resp_served = {'status': "error", 'details': "function not implemented", 'api_fn': command}
		
		print("cabserver.py GET / CabHandler served: %s%s%s" %(color_cyan_d, ppf(resp_served), color_reset) )
		self.write(resp_served)
	
	
	def post(self, command):
		self.set_header("Content-Type", "application/json")
		pr_dict = {'warn': 'for POST: you have to know you params by name'}
		print("cabserver.py POST / CabHandler received: command: %s%s%s \t params: %s%s%s" \
				%(color_cyan_b, command, color_reset, \
				color_cyan_l, pr_dict, color_reset) )
		resp_served = {}
		if command == "set_position":
			pr_dict = dict( id_trip   = self.get_argument('id_trip', ''),
							lat       = float(self.get_argument('lat', 0)),
							lng       = float(self.get_argument('lng', 0)),
							time      = self.get_argument('time', ''),
							city_hwy   = self.get_argument('city_hwy', ''),
							)
			sql_r = sql("""INSERT INTO positions (id_trip, lat, lng, time, city_hwy)
							VALUES ( %(id_trip)s, %(lat)s, %(lng)s, %(time)s, %(city_hwy)s ) """,
							id_trip = pr_dict['id_trip'], lat = pr_dict['lat'], lng = pr_dict['lng'], time = pr_dict['time'], city_hwy = pr_dict['city_hwy'] )
			if type(sql_r) is BooleanType and sql_r is False:
				resp_served = {'status': "ERROR", 'details': "record NOT saved", 'api_fn': command}
			else:
				resp_served = {'status': "OK", 'details': "record saved", 'api_fn': command}
			print("sql_r = ", sql_r)
		
		elif command == "add_event":
			pr_dict = dict( id_user   = self.get_argument('id_user', ''),
							time = self.get_argument('time', ''),
							type      = self.get_argument('type', ''),
							city_hwy  = self.get_argument('city_hwy', ''),
							distance  = self.get_argument('distance', ''),
							price     = self.get_argument('price', ''),
							start_moment   = self.get_argument('start_moment', ''),
							stop_moment    = self.get_argument('stop_moment', ''),
							start_location = self.get_argument('start_location', ''),
							stop_location  = self.get_argument('stop_location', ''),
						)
			if pr_dict['type'] == "stop":
				# add a trip
				sql_r = sql("""INSERT INTO trips (id_user, start_moment, stop_moment, start_location, stop_location, distance, idle, price)
							VALUES ( %(id_user)s, %(start_moment)s, %(stop_moment)s, %(start_location)s, %(stop_location)s, %(distance)s, %(idle)s, %(price)s ) """,
							id_user = pr_dict['id_user'], start_moment = pr_dict['start_moment'], stop_moment = pr_dict['stop_moment'], 
							start_location = pr_dict['start_location'], stop_location = pr_dict['stop_location'], 
							distance = pr_dict['distance'], idle = pr_dict['idle'], price = pr_dict['price'] )
				print("sql_r = ", sql_r)
			
			if pr_dict['type'] == "start":
				# hash a random sequence
				hash_o = hashlib.sha224() 		# sha224 is 56 chars long
				rnd_num = str(random.random())	# range: [0.0, 1.0)
				hash_o.update( rnd_num )
				hash_hex = hash_o.hexdigest()
			
		else: # not defined
			resp_served = {'status': "error", 'details': "function not implemented", 'api_fn': command}
		
		if VERBOSE_S2: print("params: %s%s%s" %(color_cyan_l, pr_dict, color_reset))
		
		print("cabserver.py POST / CabHandler served: %s%s%s" %(color_cyan_d, ppf(resp_served), color_reset) )
		self.write(resp_served)


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
my_handlers = [	(r"/index.html", MainHandler),
				(r"/index-1.html", Index1),
				(r"/index-2.html", Index2),
				(r"/index-3.html", Index3),
				(r"/index-4.html", Index4),
				#(r"/index-1.html", Register),
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

##sql(statement, commit=False, conn_name='default', **kwargs)
# sql_r = sql("""INSERT INTO trips (id_user, start_moment, stop_moment, start_location, stop_location, distance, idle, price)
# 			VALUES (%(id_user)s, %(start_moment)s, %(stop_moment)s, %(start_location)s, %(stop_location)s, %(distance)s, %(idle)s, %(price)s) """,
# 			id_user=1, start_moment=time.strftime( "%Y-%m-%d %H:%M:%S" ), stop_moment=time.strftime( "%Y-%m-%d %H:%M:%S" ), start_location=[-37.81319, 144.96298], stop_location=[-31.95285, 115.85734], distance=2721560.85287, idle=10, price=200.99 )
# sql_r = sql("""INSERT INTO users (id, name, username, pswd, email, phone, level)
# 			VALUES ( %(id)s, %(name)s, %(username)s, %(pswd)s, %(email)s, %(phone)s, %(level)s )""",
# 			id=2, name='Gig Lel', username='giglel', pswd='1234', email='g@y.ro', phone='+40256123456', level=1)
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
	username       text,
	pswd           text, 
	email          text, 
	phone          text, 
	level          smallint
	);"""
create_positions = """CREATE TABLE positions(
	id             bigserial  PRIMARY KEY,
	id_trip        text,
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
	application.listen(8081, '0.0.0.0')
	#ipdb.set_trace()	# definitely palce your debugging call BEFORE IOLoop !
	tornado.ioloop.IOLoop.instance().start()

if __name__ == "__main__":
	main()


