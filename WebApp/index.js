'use strict';
var express = require('express');
var path = require('path');
var app = express();
var bodyParser = require('body-parser');
var server = require('http').createServer(app);

app.use(require('cookie-parser')());
app.use(require('express-session')({
  secret: 'perseus',
  saveUninitialized: true
}));

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.use(express.static(path.join(__dirname, 'images')));

app.use('/', require('./routes/index'));

server.listen(3000);