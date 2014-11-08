'use strict';
module.exports = function(sequelize, DataTypes){
  var Listing = sequelize.define('Listing', {
    owner: {
      type: DataTypes.STRING,
      allowNull: false
    },
    name: {
      type: DataTypes.STRING,
      allowNull: false
    },
    description: {
      type: DataTypes.TEXT,
      allowNull: false
    },
    image: {
      type: DataTypes.STRING
    },
    qr: {
      type: DataTypes.STRING,
      unique: true
    },
    stock: {
      type: DataTypes.INTEGER,
      allowNull: false
    },
    remaining: {
      type: DataTypes.INTEGER,
      allowNull: false
    }
  });
  return Listing;
};