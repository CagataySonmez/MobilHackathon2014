'use strict';
var router = require('express').Router();
var auth = require('./auth');
var UserController = require('../controller/user-controller');
var OrderController = require('../controllers/order-controller');
var QR = require('qr-image');

router.get('/login', function(request, response){
  UserController.login({
    username: request.query.username,
    password: request.query.password
  }).then(function(user){
    request.session.userid = user.id;
    response.json(user);
  });
});

router.get('/logout', function(request, response){
  request.session.destroy();
  response.status(200).end();
});

router.get('/pair', auth.session, function(request, response){
  UserController.glassToken(request.session.userid).then(function(token){
    if(token){
      response.type('png');
      QR.image(token, { type: 'png' }).pipe(response);
    }else{
      response.status(500);
    }
  }, function(error){
    response.status(500).json(error);
  });
});

router.get('/orders', auth.session, function(request, response){
  OrderController.getOrders().then(function(orders){
    response.json(orders);
  });
});

router.get('/order/:id', auth.session, function(request, response){
  OrderController.getOrder(request.params.id).then(function(order){
    response.json(order);
  });
});