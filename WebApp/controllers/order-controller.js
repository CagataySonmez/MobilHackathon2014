'use strict';

var OrderController = function(){
  this.database = require('../models');
};

OrderController.prototype.create = function(order){
  return this.database.Order.create(order);
};

module.exports = new OrderController();
