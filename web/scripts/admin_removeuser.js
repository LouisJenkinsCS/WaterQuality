/*
 Things to add:
 add a pop-up that confirms who is getting removed.
 add a confirmation once the server successfully removes the users.
 */

var options_users = "";

function fillPageRemoveUser() {

    var sample_data = '<option value=user123>user123</option>'
            + '<option value=user234>user234</option>'
            + '<option value=user345>user345</option>';
    //maybe we could also display their role and date of creation.
    //this will have to be a formatted string with a set number of characters 
    //for each field so they appear to be in columns.

    options_users = sample_data;

    //<form> tag might be removed when implementing AJAX (unsure).
    $('#Remove_User').append(
            '<div class=large_text>Select Users to Remove</div><br>'
            + '<form method="POST" id="form_remove_user"></form>'
            + '<select name="user_list" id="select_user_list" form="form_remove_user">'
            + options_users
            + '</select><br><br>'
            + '<input type="submit" id="select_user_list" value="Remove Users" form="form_remove_user">'
            );
    var userList = document.getElementById("select_user_list");
    userList.multiple = "multiple";
    
    document.getElementByTagName('input[type="submit"]').addEventListener("click", sendMsg, false);
    setUpXHR();
}

function setUpXHR(){
    xhr=false;
}
