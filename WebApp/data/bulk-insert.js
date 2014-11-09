'use strict';

var controllers = {
  user: require('../controllers/user-controller'),
  listing: require('../controllers/listing-controller'),
  order: require('../controllers/order-controller')
};

var data = {
  user: [
    {
      username: 'demo',
      password: 'demo'
    },{
      username: 'anil',
      password: 'anil'
    }
  ],
  listing: [
    {
      owner: "GittiGidiyor",
      name: "iPhone 6",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 50,
      price: 100
    },{
      owner: "HepsiBurada",
      name: "LG G2",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 10,
      price: 70
    },{
      owner: "GittiGidiyor",
      name: "LG G3",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 5,
      price: 90
    },{
      owner: "Trendyol",
      name: "Northface Yagmurluk",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 15,
      price: 50
    },{
      owner: "Trendyol",
      name: "Nike Ayakkabi",
      description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis metus ante, vel elementum velit pretium ut. Sed varius ex.",
      stock: 50,
      price: 55
    }
  ],
  order: [
    {
      UserId: 1,
      ListingId: 1,
      state: 'active'
    }
  ]
};

module.exports = {
  insert: function(){
    Object.keys(data).forEach(function(dataItem){
      data[dataItem].forEach(function(item){
        controllers[dataItem].create(item);
      });
    });
  }
};