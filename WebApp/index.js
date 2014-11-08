'use strict';
var express = require('express');
var path = require('path');
var app = express();
var bodyParser = require('body-parser');
var server = require('http').createServer(app);
var database = require('./models');

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
app.use('/admin', require('./routes/admin'));

// start sequelize
database.sequelize.sync({
  // if force is set, re-creates the database. All rows will be lost!
  force: process.argv.some(function(arg){
    return arg === "--force-create";
  })
}).complete(function(error){
  if(error) throw error[0];
  else{
    server.listen(3100);
  }
});