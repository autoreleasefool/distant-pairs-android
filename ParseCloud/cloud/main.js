
// Use Parse.Cloud.define to define as many cloud functions as you want.
Parse.Cloud.define("requestAccountDeletionKey", function(request, response) {
  var keyGen = require('key.js');
  var key = keyGen.getRandomKey();
  var username = request.user.get("username");

  var accountDeleteKey = new Parse.Object("DeleteKey");
  accountDeleteKey.set("key", key);
  accountDeleteKey.set("username", username);
  accountDeleteKey.save().then(function(saved) {
    response.success(key);
  }, function(error) {
    response.error("no key");
  });
});

Parse.Cloud.define("deleteAccount", function(request, response) {
  Parse.Cloud.useMasterKey();
  var key = request.params.key;
  var username = request.params.username;

  var keyQuery = new Parse.Query("DeleteKey");
  query.equalTo("username", username);
  query.equalTo("key", key);
  query.find({
    success: function(results) {
      if (results.length > 0) {
        var query = new Parse.Query("User");
        query.equalTo("username", username);
        query.find({
          success: function(results) {
            if (results.length > 0) {
              results[0].destroy({
                success: function(destroyed) {
                  response.success("account deleted");
                },
                error: function(object, error) {
                  response.error("could not delete account");
                },
                useMasterKey: true
              });
            } else {
              response.error("could not find user");
            }
          },
          error: function(error) {
            response.error("could not find user");
          }
        });
      } else {
        response.error("invalid username or key");
      }
    },
    error: function(error) {
      response.error("invalid username or key");
    }
  });
});
