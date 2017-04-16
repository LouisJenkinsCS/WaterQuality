/*
 Things to add:
 add a pop-up that confirms who is getting removed.
 add a confirmation once the server successfully removes the users.
 */

//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});
$.getScript("scripts/general.js", function () {});

function fillPageRemoveUser() {

    $('#Remove_User').append(
            '<div class=large_text>Select Users to Remove</div>'
            + '<input type="submit" id="input_submit_refresh_users" value="Refresh" onclick="refreshUsers()"><br><br/>'
            + '<table id="user_table">' +
            '<thead><tr><th>User Number</th><th>Login Name</th><th>First Name</th><th>Last Name</th><th>Email Address</th><th>Role</th></tr></thead>' +
            '</table>'
            + '<input type="submit" id="input_submit_remove_users" value="Remove Users" onclick="removeUsers()">'
            );
    $('#user_table').DataTable({
        columns: [
            {title: "User ID"},
            {title: "Login Name"},
            {title: "First Name"},
            {title: "Last Name"},
            {title: "EmailAddress"},
            {title: "Role"}
        ],
        select: 'multi'
    });

    var getUsersRequest = {action: 'getUserList'};

    post("AdminServlet", getUsersRequest, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert(resp.status);
            return;
        }

        var dataTable = $('#user_table').DataTable();

        dataTable.clear();

        var users = JSON.parse(resp)["users"];
        var htmlstring = '<thead><tr><th>User Number</th><th>Login Name</th><th>First Name</th><th>Last Name</th><th>Email Address</th><th>Role</th></tr></thead>';
        for (var i = 0; i < users.length; i++)
        {
            var item = users[i];
            dataTable.rows.add([[item["userNumber"], item["loginName"], item["firstName"], item["lastName"], item["emailAddress"], item["userRole"]]]);
        }
        dataTable.draw();

    });

}

function removeUsers() {

    var table = $('#user_table').DataTable();
    var selectedCells = table.rows('.selected').data();
    var userIDs = "";
    for (var i = 0; i < selectedCells.length; i++)
    {
        userIDs += selectedCells[i][0] + ",";
    }


    var removeUsers = {
        action: 'RemoveUser',
        userDeletionIDs: userIDs
    };

    post("AdminServlet", removeUsers, function (response) {
        var respData = JSON.parse(response);
        window.alert(respData["status"]);

        $("#user_table").DataTable().destroy();

        var getUsersRequest = {action: 'getUserList'};

        post("AdminServlet", getUsersRequest, function (resp) {
            if (resp.hasOwnProperty("status")) {
                window.alert(resp.status);
                return;
            }

            var dataTable = $('#user_table').DataTable();

            dataTable.clear();

            var users = JSON.parse(resp)["users"];
            var htmlstring = '<thead><tr><th>User Number</th><th>Login Name</th><th>First Name</th><th>Last Name</th><th>Email Address</th><th>Role</th></tr></thead>';
            for (var i = 0; i < users.length; i++)
            {
                var item = users[i];
                dataTable.rows.add([[item["userNumber"], item["loginName"], item["firstName"], item["lastName"], item["emailAddress"], item["userRole"]]]);
            }
            dataTable.draw();

        });
    });
}

function refreshUsers() {
    $("#data_table").DataTable().destroy();

    var getUsersRequest = {action: 'getUserList'};

    post("AdminServlet", getUsersRequest, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert(resp.status);
            return;
        }

        var dataTable = $('#user_table').DataTable();

        dataTable.clear();

        var users = JSON.parse(resp)["users"];
        var htmlstring = '<thead><tr><th>User Number</th><th>Login Name</th><th>First Name</th><th>Last Name</th><th>Email Address</th><th>Role</th></tr></thead>';
        for (var i = 0; i < users.length; i++)
        {
            var item = users[i];
            dataTable.rows.add([[item["userNumber"], item["loginName"], item["firstName"], item["lastName"], item["emailAddress"], item["userRole"]]]);
        }
        dataTable.draw();

    });
}
