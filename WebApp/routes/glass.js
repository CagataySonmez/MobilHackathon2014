'use strict';

var router = require('express').Router();
var ListingController = require('../controllers/listing-controller');
var UserController = require('../controllers/user-controller');
var auth = require('./auth');

router.get('/auth', function(request, response){
  UserController.findByToken(request.query.token).then(function(user){
    if(user){
      response.json({
        type: 'auth',
        token: request.query.token
      });
    }else{
      response.status(500).json('Invalid token');
    }
  }, function(error){
    response.status(500).json(error);
  });
});

router.get('/test', function(request, response){
  response.json({
    userid: request.session.userid
  });
});

router.get('/listing/:id', function(request, response){
  ListingController.getListing(request.params.id).then(function(listing){
    listing.dataValues.imageUrl = listing.getImageUrl();
    listing.dataValues.type = "product";
    response.json(listing.dataValues);
  }, function(error){
    response.status(500).json(error);
  });
});

router.get('/order/:id', auth.token, function(request, response){
  UserController.findByToken(request.query.token).then(function(user){
    UserController.createOrder(user.id, request.params.id).then(function(order){
      response.json({
        type: "order",
        success: true
      });
    }, function(error){
      response.status(500).json(error);
    });
  });
});

module.exports = router;