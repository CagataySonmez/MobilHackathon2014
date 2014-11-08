'use strict';
var router = require('express').Router();
var ListingController = require('../controllers/listing-controller');

router.get('/', function(request, response){
  response.json({
    message: 'Hello World'
  });
});

module.exports = router;