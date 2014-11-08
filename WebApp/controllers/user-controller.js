'use strict';

var crypto = require('crypto');

var hash = function(password){
  var shasum = crypto.createHash('sha1');
  shasum.update(password);
  return shasum.digest('hex');
};

var UserController = function(){
  this.database = require('../models');
};

UserController.prototype.create = function(user){
  user.password = hash(user.password);
  return this.database.User.create(user);
};

module.exports = new UserController();
