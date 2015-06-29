// Generates a random one-time use key to delete a user's account.
// Saves the key to the database, along with the user's name and returns the key
Parse.Cloud.define("requestAccountDeletionKey", function(request, response) {
    var keyGen = require('cloud/key.js');
    var key = keyGen.getRandomKey();

    // Creates the key for the account that made the request
    var username = request.user.get("username");

    // Saves the key to the database
    var accountDeleteKey = new Parse.Object("DeleteKey");
    accountDeleteKey.set("key", key);
    accountDeleteKey.set("username", username);
    accountDeleteKey.save().then(function(saved) {
        response.success(key);
    }, function(error) {
        response.error("no key");
    });
});

// Deletes an account, provided the key and username are stored in
// the database.
Parse.Cloud.define("deleteAccount", function(request, response) {
    Parse.Cloud.useMasterKey();
    var key = request.params.key;
    var username = request.params.username;

    // Checks if the key provided is a valid deletion key for the username
    var keyQuery = new Parse.Query("DeleteKey");
    keyQuery.equalTo("username", username);
    keyQuery.equalTo("key", key);
    keyQuery.find({
        success: function(results) {
            if (results.length > 0) {
                var createdAt = results[0].createdAt;
                var timeToLive = 1000 * 60;
                var expirationTime = new Date(createdAt.getTime() + timeToLive);
                var currentTime = new Date();

                // A key is only valid for 30 seconds
                if (currentTime > expirationTime)
                {
                    response.error(4);
                    return;
                }

                // If the key is valid, the user is fetched and deleted
                results[0].destroy();
                var query = new Parse.Query("User");
                query.equalTo("username", username);
                query.find({
                    success: function(results) {
                        if (results.length > 0) {
                            results[0].destroy({
                                success: function(destroyed) {
                                    response.success(0);
                                },
                                error: function(object, error) {
                                    response.error(1);
                                },
                                useMasterKey: true
                            });
                        } else {
                            response.error(2);
                        }
                    },
                    error: function(error) {
                        response.error(2);
                    }
                });
            } else {
                response.error(3);
            }
        },
        error: function(error) {
            response.error(3);
        }
    });
});
