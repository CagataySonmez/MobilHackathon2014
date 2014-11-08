'use strict';
var Promise = require('promise');

var OrderController = function(){
  this.database = require('../models');
};

OrderController.prototype.create = function(order){
  return this.database.Order.create(order);
};

OrderController.prototype.getOrder = function(orderId){
  return this.database.Order.find({
    where: {
      id: orderId
    },
    include: [this.database.User, this.database.Listing]
  });
};

OrderController.prototype.cancelOrder = function(userId, orderId){
  return new Promise(function(resolve, reject){
    this.database.Order.find({
      where: {
        id: orderId
      },
      include: [this.database.User, this.database.Listing]
    }).then(function(order){
      if(order.User.id === userId){
        order.state = "cancelled";
        order.Listing.remaining = order.Listing.remaining + 1;
        order.Listing.save().success(function(){
          order.save().then(resolve);
        });
      }else{
        reject("Not your order!");
      }
    }, function(error){
      reject(error);
    });
  }.bind(this));
};

OrderController.prototype.purchaseOrder = function(userId, orderId){
  return new Promise(function(resolve, reject){
    this.database.Order.find({
      where: {
        id: orderId
      },
      include: [this.database.User]
    }).then(function(order){
      if(order.User.id === userId){
        order.state = 'purchased';
        order.save().then(resolve);
      }else{
        reject("Not your order!");
      }
    }, function(error){
      reject(error);
    });
  }.bind(this));
};

module.exports = new OrderController();