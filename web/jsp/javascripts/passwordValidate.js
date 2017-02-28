
function passwordValidate(id1, id2)
{
    var newPassword = document.getElementById(id1);
    var confirmpassword = document.getElementById(id2);

    if(newPassword.value.length < 8) {
        newPassword.setCustomValidity('Password must be at least 8 characters');
    }
    else {
        newPassword.setCustomValidity('');
    }

    if(newPassword.value === confirmpassword.value)  {
        confirmpassword.setCustomValidity('');
    }else{
        confirmpassword.setCustomValidity('Both passwords must be the same');
    }                
}


