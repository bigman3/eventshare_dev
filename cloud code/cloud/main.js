
var MessageClass = Parse.Object.extend("Message");
var GroupClass = Parse.Object.extend("ChatGroups");
var GroupMembershipClass = Parse.Object.extend("GroupMembership");
var SessionClass = Parse.Object.extend("Session");
var InstallationClass = Parse.Object.extend("Installation");
var UserClass = Parse.Object.extend("User");
var MessagesStatusClass = Parse.Object.extend("MessagesStatus");
var ChannelsClass = Parse.Object.extend("Channels");

var Image = require("parse-image");



Parse.Cloud.beforeSave(Parse.Installation, function(request, response) {
	
	Parse.Cloud.useMasterKey();
	console.log("invoked beforeSave Installation");

	var userId = request.object.get("userId") ;
	var installationId = request.object.get("installationId") ;
	var currTime = new Date();

	var prevInstallsQuery = new Parse.Query(Parse.Installation);
	prevInstallsQuery.equalTo("userId", userId);
	prevInstallsQuery.notEqualTo("installationId", installationId);
	prevInstallsQuery.lessThan("createdAt", currTime);
	
	prevInstallsQuery.find().then(function(installations) {
		if (typeof installations !== "undefined") {
			Parse.Object.destroyAll(installations, function(){
				response.success();
			});
		} else {
			response.success();
		}
	});
});


Parse.Cloud.beforeSave("Message", function(request, response) {
	console.log("invoked beforeSave Message");

	if(request.object.get("newMessage") == false) {
		response.error("should not change current messages");
	}
	
	request.object.set("newMessage", false);
	if(request.object.get("isPhotoAttached") !== true) {
	    response.success();
		return;
	}
	
		console.log("message is with attached image.");

		var parseImageFile = request.object.get("attachedImage");		
		originalImageName = parseImageFile.name();
		console.log("original image name: " + originalImageName);

	Parse.Cloud.httpRequest({
		url: parseImageFile.url()

	  }).then(function(httpRequestResponse) {
	  //console.log("getDownSampledImage() -  RECEIVED ORIGINAL IMAGE");
		var image = new Image();
		return image.setData(httpRequestResponse.buffer);

	  }).then(function(image) {
		// Crop the image to the smaller of width or height.
		var size = Math.min(image.width(), image.height());
		return image.crop({
		  left: (image.width() - size) / 2,
		  top: (image.height() - size) / 2,
		  width: size,
		  height: size
		});

	  }).then(function(image) {
		  console.log("getDownSampledImage() -  RESIZING");

		// Resize the image to 
		return image.scale({
		  width: 50,
		  height: 50
		});

	  }).then(function(image) {
		// Make sure it's a JPEG to save disk space and bandwidth.
		return image.setFormat("JPEG");

	  }).then(function(image) {
		return image.data();
	
		}).then(function(buffer) {
			return buffer.toString("base64");
		}).then(function(buffer) {
		// Attach the image file to the original object.
		request.object.set("attachedImageSmall", buffer);

	  }).then(function(result) {
	  	console.log("RETURN SUCCESS  * ** * ");

		response.success();
	  }, function(error) {
		response.error(error);
	  });

});


//delete the file if errorvar image = result.get("imageFile").url();    
/*
Parse.Cloud.httpRequest({
        method: 'DELETE',
        url: image.substring(image.lastIndexOf("/")+1),
        headers: {
            "X-Parse-Application-Id": "YOUR_APP_ID
            "X-Parse-REST-API-Key" : YOUR_API_KEY"
        }
    );
	
*/
	
Parse.Cloud.afterSave("Message", function(request) {

	console.log("invoked afterSave Message");

	var msg = request.object;
	var msgId = msg.id;
	var channel = msg.get("groupId");
	var body = msg.get("body");
	var sender = msg.get("userId");
	var isPhotoAttached = msg.get("isPhotoAttached");


    var channelQuery = new Parse.Query(ChannelsClass);
    channelQuery.equalTo("groupId", channel);

    channelQuery.first().then(function(channelResult) {

        var pushQuery = new Parse.Query(Parse.Installation);
        pushQuery.containedIn("objectId", channelResult.get("installationObjectId"));
        pushQuery.notEqualTo("userId", sender);

        Parse.Push.send({
            where : pushQuery,
              data: {
                tag: "MSG_PUSH",
                objectId: msgId,
                groupId: channel,
                userId: sender,
                photoAttached: isPhotoAttached,
              }
            }, {
          success: function() {
            console.log("push message on new post sent. body:" + body + "\n. target group:" + channel + "\n");
          },
          error: function(error) {
            console.log("error sending push message on new post. body:" + body + "\n. target group:" + channel + "\n");
          }
        });

    });




/*
	var ackQuery = new Parse.Query(Parse.Installation);
	ackQuery.equalTo("userId", sender);
	
	Parse.Push.send({
		where : ackQuery,
		  data: {
			tag: "MSG_ACK_PUSH",
			objectId: msgId,
			groupId: channel,
			userId: sender,
		  }
		}, {
	  success: function() {
		console.log("ack on new post sent.");
	  },
	  error: function(error) {
	  	console.log("error sending ack on new post.");
	  }
	});
			*/
	
});



var AddFbUsersToGroupAndSendPush = function(group, membersFbIds) {

    console.log("enter AddFbUsersToGroupAndSendPush()");

    var promise = new Parse.Promise.as();
    promise.then(function() {
        var changeType = group.get("objectStatus");
        console.log("membersFbIds :   " + membersFbIds);
        var query = new Parse.Query(Parse.User);
        query.containedIn("fbId", membersFbIds);

        console.log("********** here 1 ***************");
        //Parse.Cloud.useMasterKey();

        query.find().then(function(users) {

            console.log("users :   " + users);
        console.log("********** here 2 ***************");

            var memberships = [];
            var installationIds = [];
            for (var i in users){

                //create membership for group
                var newMembership = new GroupMembershipClass();
                newMembership.set("groupId", group.id);
                newMembership.set("user", users[i]);

                //newMembership.set("installationObjectId", installationId);
                memberships.push(newMembership);

                var installationId = users[i].get("installation").id;
                        console.log("installationId[i] :   " + installationId);

                installationIds.push(installationId);
            }
        console.log("********** here 3 ***************");

           // console.log("installationIds :   " + installationIds);

            var sender = Parse.User.current().id;
            var channel = group.id;
            var channelQuery = new Parse.Query(ChannelsClass);
            channelQuery.equalTo("groupId", channel);

            channelQuery.first().then(function(channelResult) {
                console.log("********** here 4 ***************");



                if (typeof channelResult == "undefined") {
                    console.log("channel doesn't exist. Creating it..");
                    channelResult = new ChannelsClass();
                    channelResult.set("groupId", channel);
                } else {
                    console.log("channel already exists.");
                }

                for (var i in installationIds){
                   // console.log("**************** instId  " + installationIds[i]);
                    channelResult.addUnique("installationObjectId", installationIds[i]);
                }


                memberships.push(channelResult);

                Parse.Object.saveAll(memberships).then(function(mmbships){
                    console.log("saved memberships.");

                    var pushQuery = new Parse.Query(Parse.Installation);
                    //pushQuery.equalTo("channels", channel);
                    pushQuery.containedIn("objectId", channelResult.get("installationObjectId"));
                    pushQuery.notEqualTo("userId", sender);

                   // pushQuery.find().then(function(resInst) {console.log("sending push to " + resInst );});
                    Parse.Push.send({
                        where : pushQuery,
                          data: {
                            tag: "GROUP_PUSH",
                            changeType: changeType,
                            objectId: channel,
                            groupName: group.get("groupName"),
                            userId: sender,
                          }
                        }, {
                      success: function() {
                        console.log("Push message on group change ("+ changeType + ") sent. target group:" + channel + "\n");
                       // return promise;
                      },
                      error: function(error) {
                        console.log("Error sending push message on group change ("+ changeType + "). target group:" + channel + "\n");
                        //return Parse.Promise.error();
                      }
                    });
                });
            });
        });

    });
     console.log(" * ********* HERE 20 *********");
    return promise;
}


var FUNC = function(group, membersFbIds) {

    console.log("enter FUNC()");

    var promise = new Parse.Promise.as();

    var mmbsh1 = new GroupMembershipClass();
    mmbsh1.set("groupId", group.id);
    mmbsh1.save().then(
        function(obj){
            console.log("saved dummy obj");
            Parse.Promise.as();
        }, function error(){
            console.log("failed saving dummy obj");
            Parse.Promise.error();
        }
    );

    return promise;
}

Parse.Cloud.beforeSave("ChatGroups", function(request, response) {
	console.log("invoked beforeSave ChatGroups");

   if ( typeof request.object.get("members") == "undefined") {
    	console.log("members is undefined. clearing it");

        request.object.set("members") = [];
    }

    var updateType = request.object.get("objectStatus");


	var eventDateTimeString = request.object.get("eventDate");
	var eventDateTimeObj = new Date(eventDateTimeString);

	if (updateType !== "GROUP_REMOVE_MEMBER" && updateType !== "GROUP_NEW_MEMBERS") {
	    request.object.set("takingPlaceOn", eventDateTimeObj);
	}

	var group = request.object;
	var membersFbIds = group.get("members");


    console.log("iterating over membersFbIds: (length is " + membersFbIds.length + ")");
    for (var i in membersFbIds) {
        console.log("membersFbIds["+i+"]" + membersFbIds[i]);
    }


	var query = new Parse.Query(Parse.User);
	query.containedIn("fbId", membersFbIds);

	//console.log("search for fbIds : " + membersFbIds);


	query.find({ useMasterKey: true }).then(function(users) {
	console.log("search results for users from fbId : " + users);
		//Parse.Cloud.useMasterKey();

        if (updateType == "GROUP_NEW_MEMBERS" && group.existed()) {
            console.log("GROUP_NEW_MEMBERS update.");
            response.success();
        } else {

            for (var i in users){
                var groupACL = group.getACL();
                var userId = users[i].id;

                groupACL.setReadAccess(userId, true);
                groupACL.setWriteAccess(userId, true);
                console.log("search for fbIds  result[i]: " + users[i].get("fullName"));
            }

            console.log("TRYING TO CHANGE ACL.");

            group.setACL(groupACL);

            var parseImageFile = request.object.get("groupPic");
                originalImageName = parseImageFile.name();
                //console.log("original image name: " + originalImageName);

            Parse.Cloud.httpRequest({
                url: parseImageFile.url()

              }).then(function(httpRequestResponse) {
              //console.log("getDownSampledImage() -  RECEIVED ORIGINAL IMAGE");
                var image = new Image();
                return image.setData(httpRequestResponse.buffer);



              //}).then(function(image) {
                // Crop the image to the smaller of width or height.
               // var size = Math.min(image.width(), image.height());
               // return image.crop({
               //   left: (image.width() - size) / 2,
               //   top: (image.height() - size) / 2,
               //   width: size,
              //    height: size
              //  });

              }).then(function(image) {
                  console.log("getDownSampledImage() -  RESIZING");

                // Resize the image to
                return image.scale({
                  width: 150,
                  height: 150
                });

              }).then(function(image) {
                // Make sure it's a JPEG to save disk space and bandwidth.
                return image.setFormat("PNG");

              }).then(function(image) {
                return image.data();

                }).then(function(buffer) {
                    return buffer.toString("base64");
                }).then(function(buffer) {
                // Attach the image file to the original object.
                request.object.set("groupPicSmall", buffer);

              }).then(function(result) {
                console.log("RETURN SUCCESS  * ** * ");

                response.success();
              }, function(error) {
                response.error(error);
              });

            //response.success();
        }
    });
});


Parse.Cloud.afterSave("ChatGroups", function(request) {
	Parse.Cloud.useMasterKey();
	console.log("** invoked afterSave ChatGroups **");
	
	var fb_auth = request.user.get('authData')['facebook'];

	var group = request.object;
	var channel = group.id;
	var sender = group.get("adminId");
	var membersFbIds = group.get("members");
	var changeType = group.get("objectStatus");

	if (typeof changeType == "undefined") {
	    changeType = "GROUP_CHANGE_UNDEFINED";
	}

	var senderFbId = fb_auth['id'];

	if (changeType == "GROUP_NEW") {
	    membersFbIds.push(senderFbId);
	}

    AddFbUsersToGroupAndSendPush(group, membersFbIds).then(function(){});

});	

			
Parse.Cloud.define("markSeenAllMessagesUpToNow", function(request, response) {
	var channel = request.params.groupId;
	var seenBy = request.params.seenBy;
	var seenOnTime = request.params.seenOnTime;
	var lastMsgId = request.params.lastMsgId;

	var seenOnTimeObj = new Date(seenOnTime);
	
	var query = new Parse.Query(MessagesStatusClass);
	query.equalTo("groupId", channel);
	query.equalTo("targetId", seenBy);
	query.doesNotExist("seenOnTime");
	query.lessThanOrEqualTo("sentOn", seenOnTimeObj);
	query.descending("sentOn");

	query.find().then(function(ms) {
		console.log("************ query returned " + ms.length + " objects");
		
		for (var i in ms){
			ms[i].set("seenOnTime", seenOnTimeObj);
		}

		Parse.Object.saveAll(ms,{success: function(list) {

		    var channelQuery = new Parse.Query(ChannelsClass);
            channelQuery.equalTo("groupId", channel);
            channelQuery.first().then(function(channelResult) {

                var pushQuery = new Parse.Query(Parse.Installation);
                pushQuery.containedIn("objectId", channelResult.get("installationObjectId"));
                pushQuery.notEqualTo("userId", seenBy);

                Parse.Push.send({
                    where : pushQuery,
                      data: {
                        tag: "SEEN_PUSH",
                        groupId: channel,
                        seenBy: seenBy,
                        seenOnTime: seenOnTime,
                        lastMsgId: lastMsgId
                      }
                    }, {
                  success: function() {
                    console.log("push seen by sent.  target group:" + channel + "\n");
                    response.success("sending push seen by successful.  target group:" + channel + "\n");

                  },
                  error: function(error) {
                    console.log("error: sending push seen by.  target group:" + channel + "\n");
                    response.error(error);
                  }
                });
	        });
		},
		error: function(error) {
			console.log("error: saving message statuses.  target group:" + channel + "\n");
		},
	  });
	 });
	
}, function(error) {
		response.error(error);
	}
);

	

