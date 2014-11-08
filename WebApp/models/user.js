'use strict';
var crypto = require('crypto');

module.exports = function(sequelize, DataTypes){
  var User = sequelize.define('User', {
    username: {
      type: DataTypes.STRING,
      unique: true,
      allowNull: false
    },
    password: {
      type: DataTypes.STRING,
      allowNull: false,
      set: function(password){
        var shasum = crypto.createHash('sha1');
        shasum.update(password);
        return shasum.digest('hex');
      }
    },
    glass: {
      type: DataTypes.UUID,
      defaultValue: sequelize.UUIDV4
    }
  }, {
    classMethods: {
      associate: function(models){
        User.hasMany(models.Order);
      }
    }
  });
  return User;
};