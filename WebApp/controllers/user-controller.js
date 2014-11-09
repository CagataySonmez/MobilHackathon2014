'use strict';

var Promise = require('promise');
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
    include: [{
      model: this.database.Order,
      include: [this.database.Listing]
    }]
  });
};

UserController.prototype.findByToken = function(token){
  return this.database.User.find({
    where: {
      glass: token
    }
  });
};

UserController.prototype.findById = function(userId){
  return this.database.User.find({
    where: {
      id: userId
    }
  });
};

UserController.prototype.createOrder = function(userId, listingId){
  return new Promise(function(resolve, reject){
    this.database.Order.create({
      UserId: userId,
      ListingId: listingId,
      state: 'active'
    }).then(function(order){
      this.database.Listing.find({
        where: { id: order.ListingId }
      }).then(function(listing){
        if(listing.remaining > 0){
          listing.remaining = listing.remaining - 1;
          listing.save().then(resolve, reject);
        }else{
          reject();
        }
      }, reject);
    }.bind(this), reject);
  }.bind(this));
};

module.exports = new UserController();
