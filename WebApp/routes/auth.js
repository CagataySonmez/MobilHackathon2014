'use strict';

var config = require('../config');
var UserController = require('../controllers/user-controller');

module.exports = {
  session: function(request, response, next){
    if(request.session.userid !== undefined){
      return next();
    }
    response.status(401).json('Authentication').end();
  },

  glass: function(request, response, next){
    if(request.session && request.session.userid){
      UserController.glassToken(request.session.userid).then(function(token){
        if(token && request.query.token === token.glass){
          return next();
        }
        response.status(401).json('Invalid token').end();
      }, function(){
        response.status(401).json('Cannot find token').end();
      });
    }else{
      response.status(401).json('Session undefined').end();
    }
  },

  admin: function(request, response, next){
    if(request.query.pass === config.admin.pass){
      return next();
    }
    response.status(401).json('Authentication').end();
  }
};

