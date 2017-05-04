/*
    This has been changed to Manage Users, just didn't update file name.

    Allows you to lock, unlock, or delete multiple users at the same time
    by selecting rows via clicking.
 */

//This function simply pulls the AJAX_functions.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_functions.js", function () {});
$.getScript("scripts/general.js", function () {});

/*
 * Fills the pag with the table, buttons, and text, and initializes the table
 * Refresh button is needed if a user is added as the table is generated when
 * the admin page is visited, not each individual tab
 */
function fillPageRemoveUser() {

    $('#Remove_User').append(
            '<div class=large_text>Select Users to Remove</div>'
            + '<input type="submit" id="input_submit_refresh_users" value="Refresh" onclick="refreshUsers()"><br><br/>'
            + '<table id="user_table">' +
            '<thead><tr><th>User Number</th><th>Login Name</th><th>Name</th><th>Email Address</th><th>Role</th><th>Locked</th></tr></thead>' +
            '</table>'
            + '<input type="submit" id="input_submit_remove_users" value="Remove Users" onclick="removeUsers()">'
            + '<input type="submit" id="input_submit_lock_users" value="Lock Users" onclick="lockUsers()">'
            + '<input type="submit" id="input_submit_unlock_users" value="Unlock Users" onclick="unlockUsers()">'
            );
    $('#user_table').DataTable({
        columns: [
            {title: "User ID"},
            {title: "Login Name"},
            {title: "Name"},
            {title: "EmailAddress"},
            {title: "Role"},
            {title: "Locked"}
        ],
        select: 'multi'
    });

    var getUsersRequest = {action: 'getUserList'};
    
    /*
     * getUserList returns a JSON holding an array of users
     */
    post("AdminServlet", getUsersRequest, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert(resp.status);//status indicates an error occured
            return;
        }

        var dataTable = $('#user_table').DataTable();

        dataTable.clear();

        var users = JSON.parse(resp)["users"];//splits the JSON into an array

        for (var i = 0; i < users.length; i++)
        {
            var item = users[i];
            var locked = item["locked"];
            if (locked === "0")
                locked = "False";//0s and 1s for booleans don't look nice
            else
                locked = "True";
            dataTable.rows.add([[item["userNumber"], item["loginName"], item["firstName"] + " " + item["lastName"], item["emailAddress"], item["userRole"], locked]]);
        }
        dataTable.draw();

    });

}


/*
 * Deletes all selected users
 * Self deleting not allowed
 */
function removeUsers() {
    
    //Concatenates all selected user ids into a string
    var table = $('#user_table').DataTable();
    var selectedCells = table.rows('.selected').data();
    var userIDs = "";
    for (var i = 0; i < selectedCells.length; i++)
    {
        userIDs += selectedCells[i][0] + ",";
    }
    //asks the user if they're sure before deleting
    if (confirm("Are you sure you'd like to delete " + selectedCells.length + " user(s)?"))
    {

        var removeUsers = {
            action: 'RemoveUser',
            userDeletionIDs: userIDs
        };

        post("AdminServlet", removeUsers, function (response) {
            var respData = JSON.parse(response);
            window.alert(respData["status"]);

            refreshUsers();//refreshes the users after deleting some
        });
    }
}

/*
 * Locks all selected users
 * Self locking not allowed
 */
function lockUsers() {
    //Concatenates all selected user ids into a string
    var table = $('#user_table').DataTable();
    var selectedCells = table.rows('.selected').data();
    var userIDs = "";
    for (var i = 0; i < selectedCells.length; i++)
    {
        userIDs += selectedCells[i][0] + ",";
    }


    var removeUsers = {
        action: 'LockUser',
        userLockIDs: userIDs
    };

    post("AdminServlet", removeUsers, function (response) {
        var respData = JSON.parse(response);
        window.alert(respData["status"]);

        refreshUsers();
    });
}

/*
 * Unlocks all selected users
 */
function unlockUsers() {
    //Concatenates all selected user ids into a string
    var table = $('#user_table').DataTable();
    var selectedCells = table.rows('.selected').data();
    var userIDs = "";
    for (var i = 0; i < selectedCells.length; i++)
    {
        userIDs += selectedCells[i][0] + ",";
    }


    var removeUsers = {
        action: 'UnlockUser',
        userUnlockIDs: userIDs
    };

    post("AdminServlet", removeUsers, function (response) {
        var respData = JSON.parse(response);
        window.alert(respData["status"]);

        refreshUsers();
    });
}

/*
 * Refreshes the table.
 */
function refreshUsers() {

    var getUsersRequest = {action: 'getUserList'};

    post("AdminServlet", getUsersRequest, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert(resp.status);
            return;
        }

        var dataTable = $('#user_table').DataTable();

        dataTable.clear();

        var users = JSON.parse(resp)["users"];
        for (var i = 0; i < users.length; i++)
        {
            var item = users[i];
            var locked = item["locked"];
            if (locked === "0")
                locked = "False";
            else
                locked = "True";
            dataTable.rows.add([[item["userNumber"], item["loginName"], item["firstName"] + " " + item["lastName"], item["emailAddress"], item["userRole"], locked]]);
        }
        dataTable.draw();

    });
}
