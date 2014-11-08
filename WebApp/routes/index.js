'use strict';
var router = require('express').Router();
var ListingController = require('../controllers/listing-controller');

router.get('/', function(request, response){
  response.json({
    message: 'Hello World'
  });
});

router.get('/listing/:id', function(request, response){
  ListingController.getListing(request.params.id).then(function(listing){
    response.render()
  });
});

module.exports = router;