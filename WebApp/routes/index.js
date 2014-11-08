'use strict';
var router = require('express').Router();

router.get('/', function(request, response){
  response.json({
    message: 'Hello World'
  });
});

module.exports = router;