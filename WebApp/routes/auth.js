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
    UserController.glassToken(request.session.userid).then(function(token){
      if(request.query.token === token){
        return next();
      }
      response.status(401).json('Authentication').end();
    }, function(){
      response.status(401).json('Authentication').end();
    });
  },

  admin: function(request, response, next){
    if(request.query.pass === config.admin.pass){
      return next();
    }
    response.status(401).json('Authentication').end();
  }
};

