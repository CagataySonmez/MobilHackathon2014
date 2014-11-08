'use strict';
var UserController = require('../controllers/user-controller');
var OrderController = require('../controllers/order-controller');
var ListingController = require('../controllers/listing-controller');

module.exports = {
  insert: function(){
    UserController.create({
      username: 'demo',
      password: 'demo'
    });

    UserController.create({
      username: 'anil',
      password: 'anil'
    });

    ListingController.create({
      owner: "GittiGidiyor",
      name: "LG G2",
      description: "",
      stock: 10,
      price: 50
    });

    ListingController.create({
      owner: "GittiGidiyor",
      name: "LG G2",
      description: "Best smartphone",
      stock: 10,
      price: 50
    });
  }
};