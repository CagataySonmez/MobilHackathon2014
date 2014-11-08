'use strict';

var router = require('express').Router();
var ListingController = require('../controllers/listing-controller');
var auth = require('./auth');

router.get('/listing/:id', auth.glass, function(request, response){
  ListingController.getListing(request.params.id).then(function(listing){
    listing.dataValues.imageUrl = listing.getImageUrl();
    response.json(listing.dataValues);
  }, function(error){
    response.status(500).json(error);
  });
});


module.exports = router;