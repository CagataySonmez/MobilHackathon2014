'use strict';
var fs = require('fs'),
    path = require('path'),
    db = require('../config').database,
    Sequelize = require('sequelize'),
    sequelize = new Sequelize(db.name, db.user, db.password),
    Database = {};

// traverses the models folder and initiates all models into sequelize
// we can accept sync because server is not up yet at this point;
fs.readdirSync(__dirname).filter(function(file){
  return (file.indexOf('.') !== 0) && (file !== 'index.js');
}).forEach(function(file){
  var model = sequelize.import(path.join(__dirname, file));
  Database[model.name] = model;
});

Object.keys(Database).forEach(function(model){
  if('associate' in Database[model]){
    Database[model].associate(Database);
    module.exports[model] = Database[model];
  }
});

module.exports.sequelize = sequelize;
module.exports.Sequelize = Sequelize;