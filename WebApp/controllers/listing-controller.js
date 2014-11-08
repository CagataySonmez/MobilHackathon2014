'use strict';

var ListingController = function(){
  this.database = require('../models');
};

ListingController.prototype.create = function(listing){
  return this.database.Listing.create(listing);
};

module.exports = new ListingController();
