var options_roles="";

function fillPageRegisterUser(){
    
    //options for role select should get a list of roles from the admin servlet
        //instead.
    options_roles="<option value=\"administrator\">Administrator</option>";
    
    //<form> tag might be removed when implementing AJAX (unsure).
    $('#Register_User').append(
            '<form method="POST" id="form_register"></form>'
            +'<div class="large_text">Enter New User Information<br></div>'
            +'<table id="table_register">'
            +'  <tr>'
            +'      <td>New Username: </td>'
            +'      <td><input type="text" name="username" form="form_register"></td>'
            +'  </tr>'
            +'  <tr>'
            +'      <td>New Password: </td>'
            +'      <td><input type="password" name="username" form="form_register"></td>'
            +'  </tr>'
            +'  <tr>'
            +'      <td>Confirm Password: </td>'
            +'      <td><input type="password" name="username" form="form_register"></td>'
            +'  </tr>'
            +'  <tr>'
            +'      <td>Role: </td>'
            //WARNING: code drift! fix soon?
            +'      <td><select type="text" name="username" form="form_register">'+options_roles+'</select></td>'
            +'  </tr>'
            +'  <tr>'
            +'      <td><br><input type="submit" value="Submit" form="form_register"></td>'
            +'  </tr>'
            +'</table>'
    );
};
