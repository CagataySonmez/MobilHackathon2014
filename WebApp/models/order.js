'use strict';
module.exports = function(sequelize, DataTypes){
  var Order = sequelize.define('Order', {
    state: {
      type: DataTypes.ENUM('active', 'purchased', 'cancelled')
    }
  }, {
    classMethods: {
      associate: function(models){
        Order.belongsTo(models.User);
        Order.belongsTo(models.Listing);
      }
    }
  });
  return Order;
};