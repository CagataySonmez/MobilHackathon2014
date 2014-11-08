'use strict';

var router = require('express').Router();
var ListingController = require('../controllers/listing-controller');

router.get('/listing/:id', function(request, response){
  ListingController.getListing(request.params.id).then(function(listing){
    response.json(listing);
  }, function(error){
    response.status(500).json(error);
  });
});

module.exports = router;