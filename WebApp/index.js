'use strict';
var express = require('express');
var path = require('path');
var app = express();
var server = require('http').createServer(app);
var bodyParser = require('body-parser');
var database = require('./models');
var config = require('./config');

app.use(require('cookie-parser')());
app.use(require('express-session')({
  secret: 'perseus',
  resave: true, // resave - forces session to be saved even when unmodified. (default: true)
  saveUninitialized: true
}));

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.use(express.static(path.join(__dirname, 'images')));

app.use('/', require('./routes/index'));
app.use('/admin', require('./routes/admin'));
app.use('/glass', require('./routes/glass'));

// start sequelize
database.sequelize.sync({
  // if force is set, re-creates the database. All rows will be lost!
  force: process.argv.some(function(arg){
    return arg === "--force-create";
  })
}).complete(function(error){
  if(error){
    console.log(error);
  }else{
    server.listen(config.host.port);
  }
});
