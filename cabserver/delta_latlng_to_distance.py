#!/usr/bin/env python
# -*- coding: utf-8 -*-

from __future__ import print_function
import math

def delta_latlng_to_distance(lat1, lng1, lat2, lng2):
    if (lat1 == lat2 and lng1 == lng2):
        return 0
    
    def deg2rad(deg):
        return deg * math.pi / 180

    def rad2deg(rad):
        return rad * 180 / math.pi

    def haversine(lat1, lng1, lat2, lng2):
        R = 6371
        dLat = deg2rad(lat2 - lat1)
        dLng = deg2rad(lng2 - lng1)
        a = math.sin(dLat / 2) * math.sin(dLat / 2) + math.cos(deg2rad(lat1)) * math.cos(deg2rad(lat2)) * math.sin(dLng / 2) * math.sin(dLng / 2)
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        d = R * c
        d = d * 0.62137119

        return d

    theta = lng1 - lng2
    dist = math.sin(deg2rad(lat1)) * math.sin(deg2rad(lat2)) + math.cos(deg2rad(lat1)) * math.cos(deg2rad(lat2)) * math.cos(deg2rad(theta))
    if dist > 1:
        dist = 1

    if dist < -1:
        dist = -1

    dist = math.acos(dist)
    dist = rad2deg(dist)
    dist = dist * 60 * 1.1515 * 1.609344 * 1000

    return dist # in meters


if __name__ == "__main__":
	#main()
	r = delta_latlng_to_distance(-37.81319, 144.96298,-31.95285, 115.85734)
	print("delta_latlng_to_distance[m] = %s [km] = %s" %(r, r/1000.0))
	r = delta_latlng_to_distance(45.759811, 21.218349,45.759930, 21.258604)
	print("delta_latlng_to_distance[m] = %s [km] = %s" %(r, r/1000.0))




