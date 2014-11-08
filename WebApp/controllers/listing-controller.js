'use strict';

var Promise = require('promise');
var QR = require('qr-image');
var fs = require('fs');

var ListingController = function(){
  this.database = require('../models');
};

ListingController.prototype.create = function(listingData){
  listingData.remaining = listingData.stock;
  return new Promise(function(resolve, reject){
    this.database.Listing.create(listingData).then(function(listing){
      var filename = listing.getDataValue('id') + '.png';
      var QRStream = QR.image("http://192.168.1.157:3000/glass/listing/" + listing.getDataValue('id'), {
        type: 'png'
      });
      QRStream.pipe(fs.createWriteStream('images/qr-' + filename));
      QRStream.on('end', function(){
        listing.setDataValue('qr', 'qr-' + filename);
        listing.setDataValue('image', 'image-' + filename);
        listing.save().then(function(){
          resolve(listing);
        });
      });
      QRStream.on('error', function(error){
        reject(error);
      });
    });
  }.bind(this));
};

ListingController.prototype.getListing = function(listingId){

};

module.exports = new ListingController();
