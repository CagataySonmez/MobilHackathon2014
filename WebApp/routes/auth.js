'use strict';

var config = require('../config');
var UserController = require('../controllers/user-controller');

module.exports = {

  token: function(request, response, next){
    if(request.query.token){
      UserController.findByToken(request.query.token).then(function(user){
        if(user){
          return next();
        }
        response.status(401).json('Invalid token').end();
      }, function(error){
        response.status(401).json(error).end();
      });
    }else{
      response.status(401).json('No token provided');
    }
  },

  admin: function(request, response, next){
    if(request.query.pass === config.admin.pass){
      return next();
    }
    response.status(401).json('Authentication').end();
  }
};

