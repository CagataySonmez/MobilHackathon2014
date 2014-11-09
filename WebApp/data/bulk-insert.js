'use strict';
var UserController = require('../controllers/user-controller');
var ListingController = require('../controllers/listing-controller');

var data = {
  users: [
    {
      username: 'demo',
      password: 'demo'
    },{
      username: 'anil',
      password: 'anil'
    }
  ],
  listings: [
    {
      owner: "GittiGidiyor",
      name: "iPhone 6",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 50,
      price: 100
    },{
      owner: "HepsiBurada",
      name: "LG G3",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 10,
      price: 100
    },{
      owner: "GittiGidiyor",
      name: "LG G3",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 5,
      price: 90
    }
  ]
};

module.exports = {
  insert: function(){
    data.users.forEach(function(user){
      UserController.create(user);
    });

    data.listings.forEach(function(listing){
      ListingController.create(listing);
    });
  }
};