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
        console.log("resp: " + resp);
        options_roles += '<option>';
        options_roles += resp.toString();
        options_roles += '</option>';
        console.log("options_roles: " + options_roles);
        buildForm(options_roles);
    });

    //options for role select should get a list of roles from the admin servlet
    //instead.
    //options_roles="<option value=\"administrator\">Administrator</option>";

    //<form> tag might be removed when implementing AJAX (unsure).
    function buildForm(options)
    {
        $('#Register_User').append(
                '<div class="large_text">Enter New User Information<br></div>'
                + '<table id="table_register">'
                + '  <tr>'
                + '      <td>First Name: </td>'
                + '      <td><input type="text" id="firstname"></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td>Last Name: </td>'
                + '      <td><input type="text" id="lastname"></td>'
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
                + '      <td><select type="text" id="role">' + options + '</select></td>'
                + '  </tr>'
                + '  <tr>'
                + '      <td><br><button type="button" onclick="registerUser()">Submit</td>'
                + '  </tr>'
                + '</table>'
                );
    }
}
;
