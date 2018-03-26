#!/bin/bash

SERVER="127.0.0.1:8080/cisco"
CURL_OUTPUT="\nHTTP CODE: %{http_code}\n\n"

#
#  POST /cars
# 
curl -s -w "$CURL_OUTPUT" -H "Content-Type: application/json" -X "POST" --noproxy "*" --data "@cars.json" "$SERVER/cars"


#
#  GET /cars
#
curl -s -w "$CURL_OUTPUT" -X "GET" --noproxy "*" "$SERVER/cars"


