'use strict';
var router = require('express').Router();
var auth = require('./auth');
var Promise = require('promise');
var config = require('../config');
var UserController = require('../controllers/user-controller');
var OrderController = require('../controllers/order-controller');
var QR = require('qr-image');

router.get('/login', function(request, response){
  UserController.login({
    username: request.query.username,
    password: request.query.password
  }).then(function(user){
    if(user){
      user.session = true;
      user.save().then(function(){
        response.json({
          username: user.username,
          token: user.glass
        });
      });
    }else{
      response.status(401).json("User not found");
    }
  }, function(error){
    response.status(500).json(error);
  });
});

router.get('/session', function(request, response){
  UserController.findById(1).then(function(user){
    if(user.session){
      response.json({
        username: user.username,
        token: user.glass
      });
    }else{
      response.status(401).end();
    }
  });
});

router.get('/logout', function(request, response){
  UserController.findById(1).then(function(user){
    if(user.session){
      user.session = false;
      user.save().then(function(){
        response.status(200).end();
      });
    }
  });
});

router.get('/pair', auth.token, function(request, response){
  if(request.query.token){
    response.type('png');
    var qrText = config.host.ip + ':' + config.host.port + '/glass/auth?token=' + request.query.token;
    QR.image(qrText, { type: 'png' }).pipe(response);
  }else{
    response.status(500);
  }
});

router.get('/orders', auth.token, function(request, response){
  UserController.findByToken(request.query.token).then(function(user){
    UserController.getUserOrders(user.id).then(function(orders){
      orders.Orders = orders.Orders.map(function(order){
        var listing = order.Listing;
        listing.dataValues.imageUrl = listing.getImageUrl();
        return listing.dataValues;
      });
      response.json(orders);
    }, function(error){
      response.status(500).json(error);
    });
  });
});

router.get('/order/:id', auth.token, function(request, response){
  OrderController.getOrder(request.params.id).then(function(order){
    response.json(order);
  }, function(error){
    response.status(500).json(error);
  });
});

router.get('/purchase/:id', auth.token, function(request, response){
  UserController.findByToken(request.query.token).then(function(user){
    OrderController.purchaseOrder(user.id, request.params.id).then(function(order){
      response.json(order);
    }, function(error){
      response.status(500).json(error);
    });
  });
});

router.get('/cancel/:id', auth.token, function(request, response){
  UserController.findByToken(request.query.token).then(function(user){
    OrderController.cancelOrder(user.id, request.params.id).then(function(order){
      response.json(order);
    }, function(error){
      response.status(500).json(error);
    });
  });
});

module.exports = router;