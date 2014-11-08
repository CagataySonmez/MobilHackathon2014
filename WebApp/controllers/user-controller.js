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

UserController.prototype.login = function(credentials){
  return this.database.User.find({
    where: {
      username: credentials.username,
      password: hash(credentials.password)
    }
  });
};

UserController.prototype.glassToken = function(userId){
  return this.database.User.find({
    where: {
      id: userId
    },
    attributes: ['glass']
  });
};

UserController.prototype.getUserOrders = function(userId){
  return this.database.User.find({
    where: {
      id: userId
    },
    include: [this.database.Order]
  });
};

UserController.prototype.findByToken = function(token){
  return this.database.User.find({
    where: {
      glass: token
    }
  });
};

UserController.prototype.orderItem = function(userId, listingId){

};

module.exports = new UserController();
