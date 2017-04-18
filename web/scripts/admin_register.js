$.getScript("scripts/AJAX_functions.js", function () {});
$.getScript("scripts/general.js", function () {});

var options_roles = "";

function fillPageRegisterUser() {

    var rolesRequest = {action: 'getRoles'};

//    get("AdminServlet", rolesRequest, function (response)
//    {
//        var parameter_names = JSON.parse(response)["data"];
//        for (var i = 0; i < parameter_names.length; i++)
//        {
//            options_roles += '<option>';
//            var item = parameter_names[i];
//            //console.log(item["name"]);
//            options_roles += item["name"];
//            options_roles += '</option>';
//        }
//    });

    get("AdminServlet", rolesRequest, function (resp)
    {
        var roles = JSON.parse(resp)["UserRoles"];
        for (var i = 0; i < roles.length; i++)
        {
            options_roles += '<option>';
            options_roles += roles[i]["UserRole"];
            options_roles += '</option>';
        }

        $('#Register_User').append(
                '<div class="large_text">Enter New User Information<br></div>'
                + '<table id="table_register">'
                + '  <tr>'
                + '      <td>First Name: </td>'
                + '      <td><input type="text" id="firstName"></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td>Last Name: </td>'
                + '      <td><input type="text" id="lastName"></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td>Email: </td>'
                + '      <td><input type="text" id="email"></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td>Username: </td>'
                + '      <td><input type="text" id="username"></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td>Password: </td>'
                + '      <td><input type="password" id="password"></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td>Confirm Password: </td>'
                + '      <td><input type="password" id="confirmpassword"></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td>Role: </td>'
                + '      <td><select id="userRole">' + options_roles + '</select></td>'
                + '  </tr>'
                + '</table>'
                + '<br>'
                + '<input type="submit" id="register_user" value="Register User" onclick="registerUser()">'
                );
    });

    //options for role select should get a list of roles from the admin servlet
    //instead.
    //options_roles="<option value=\"administrator\">Administrator</option>";

    //<form> tag might be removed when implementing AJAX (unsure).
};

function registerUser()
{
    if ($('#password').val() !== $('#confirmpassword').val())
    {
        window.alert("Your passwords do not match");
        return;
    }

    var registerUserRequest = {action: 'RegisterUser',
        username: $('#username').val(),
        password: $('#password').val(),
        firstName: $('#firstName').val(),
        lastName: $('#lastName').val(),
        email: $('#email').val(),
        userRole: $('#userRole').val()
    };

    post("AdminServlet", registerUserRequest, function (resp) {
        var respData = JSON.parse(resp);
        if (respData["status"] === "Success")
        {
            window.alert("User Register Successful");
            document.getElementById("username").value = "";
            document.getElementById("password").value = "";
            document.getElementById("confirmpassword").value = "";
            document.getElementById("firstName").value = "";
            document.getElementById("lastName").value = "";
            document.getElementById("email").value = "";
        }
        else if(respData["status"] === "Failed")
            window.alert("User Register Failed. Check Error Logs");
        else
            window.alert(respData["status"]);
    });
}
