'use strict';
var express = require('express');
var path = require('path');
var app = express();
var server = require('http').createServer(app);

app.use('/', require('./routes/index'));

server.listen(3000);