Parse.Cloud.job("filterThoughts", function(request, status) {
    var timeToLive = 1000 * 60 * 60 * 24;
    var currentTime = new Date();
    Parse.Cloud.useMasterKey();

    var query = new Parse.Query("Thought");
    query.each(function(thought) {
        var read = thought.get("timeRead");
        if (read == 0) {
            thought.put("readTime", new Date().getTime());
            return thought.save();
        }

        var timeRead = thought.get("timeRead");
        var expirationTime = new Date(timeRead + timeToLive);
        if (currentTime > expirationTime) {
            return thought.destroy();
        }
        else {
            return;
        }
    }).then(function() {
        status.success("thoughts filtered successfully");
    }, function(error) {
        status.error("failed to filter thoughts");
    });
});
