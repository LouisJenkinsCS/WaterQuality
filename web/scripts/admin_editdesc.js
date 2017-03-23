/*things to add:
    add a pop-up warning when changing params with unsaved text edits.
    add a confirmation once the server successfully changes the desc.
 */

function fillPageEditDesc(){
    var sample_desc="Retrieved description goes here.";
    var sample_params="<option>param1</option><option>param2</option><option>param3</option>";
    //<form> tag might be removed when implementing AJAX (unsure).
    $('#Edit_Description').append(
            '<form method="POST" id="form_edit_desc"></form>'
            //left side
            +'<section class="section_edit_desc">'
            +'  <div class=large_text>Parameter List</div><br>'
            +'  <select name="param_list" id="select_param_list" form=form_edit_desc>'
                    +sample_params
            +'  </select>'
            +'</section>'
            //right side
            +'<section class="section_edit_desc">'
            +'  <div class=large_text>Description</div><br>'
            +'  <textarea name="desc" id="textarea_desc" form="form_edit_desc">'
                +sample_desc
            +'  </textarea><br><br>'
            +'  <input type="submit" value="Save Changes" form="form_edit_desc">'
            +'</section>'
            
//            +'<table id="table_edit_desc">'
//            //headers
//            +'<tr>'
//            +'  <td><div class=large_text>Parameter List</div><br></td>'
//            +'  <td><div class=large_text>Description</div><br></td>'
//            +'</tr>'
//            //content
//            +'<tr>'
//                //left
//            +'  <td><select name="param_list" id="select_param_list" form=form_edit_desc>'
//                    +sample_params
//            +'      </select>'
//            +'  </td>'
//                //right
//            +'  <td>'
//            +'      <textarea name="desc" id="textareaDesc" form="form_edit_desc">'
//                        +sample_desc+'</textarea><br><br>'
//            +'      <input type="submit" value="Save Changes" form="form_edit_desc">'
//            +'  </td>'
//            +'</tr>'
//            +'</table>'
    );
    var selectParamList = document.getElementById("select_param_list");
    selectParamList.multiple="multiple";
};
