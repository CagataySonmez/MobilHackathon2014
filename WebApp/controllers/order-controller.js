'use strict';

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

module.exports = new OrderController();
