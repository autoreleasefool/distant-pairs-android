
// Use Parse.Cloud.define to define as many cloud functions as you want.

// Default method
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});
