'use strict';
var UserController = require('../controllers/user-controller');
var OrderController = require('../controllers/order-controller');
var ListingController = require('../controllers/listing-controller');

var router = require('express').Router();

router.post('/user', function(request, response){
  UserController.create(request.body.user).then(function(user){
    response.json(user);
  }, function(error){
    response.status(500).json(error);
  });
});

router.post('/listing', function(request, response){
  ListingController.create(request.body.listing).then(function(listing){
    response.json(listing);
  }, function(error){
    response.status(500).json(error);
  });
});

router.post('/order', function(request, response){
  OrderController.create(request.body.order).then(function(order){
    response.json(order);
  }, function(error){
    response.status(500).json(error);
  });
});

module.exports = router;