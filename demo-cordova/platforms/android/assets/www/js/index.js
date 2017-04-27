/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
        initialize: function() {
            document.getElementById("submitBtn").addEventListener("click", this.onStartConversation.bind(this));
            document.getElementById("btnHistory").addEventListener("click", this.openHistory.bind(this));
            document.getElementById("btnResetHistory").addEventListener("click", this.resetHistory.bind(this));
        },

        // Login
        onStartConversation: function() {
            var firstName = document.getElementById("first_name").value;
            var lastName = document.getElementById("last_name").value;
            var email = document.getElementById("email").value;
            if (firstName == "" || lastName == "" || email == "") {
                alert("Please fill in the fields", null, "", "OK");
                return;
            }

            ///
            /// Calling login
            ///
            cordova.exec(
                // Success callback
                function(params){
                    // Handle success callback here

                },
                // Failure callback
                function(err) {},
                // Plugin name
                "CDVChatCenter",
                // Method
                "presentChatView",
                // Arguments
                ["YOUR_ORG_UID", firstName, lastName, email, null, null, null, null, null, null, "DEVICE_TOKEN", null]
            );
        },


        // To show chat history
        openHistory: function() {
            cordova.exec(
                 // Success callback
                 function(){},
                 // Failure callback
                 function(err) {},
                 // Plugin name
                 "CDVChatCenter",
                 // Method
                 "presentHistoryView",
                 // Arguments
                 []
            );
        },

        // Logout
        resetHistory: function() {
            cordova.exec(
                 // Success callback
                 function(){},
                 // Failure callback
                 function(err) {},
                 // Plugin name
                 "CDVChatCenter",
                 // Method
                 "signOut",
                 // Arguments
                 ["DEVICE_TOKEN"]
             );
        }
};

app.initialize();
