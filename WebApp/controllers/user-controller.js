'use strict';

var UserController = function(){
  this.database = require('../models');
};

UserController.prototype.create = function(user){
  return this.database.User.create(user);
};

module.exports = new UserController();
