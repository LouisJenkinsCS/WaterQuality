/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function fillPageNotes(){
    
    
    
    $('#Notes').append(
                '<section class="section_edit_notes" style="float:left;">' +
                '<div class="large_text">Notes</div>' +
                '<textarea name="notes" id="textarea_notes" form="form_edit_notes">' +
                '</textarea><br>' +
                '<button type="button" onclick="editNotes()">Submit Changes</button>' +
                '<button type="button" onclick="showNotesPreview()" style="margin-left:10px;">Preview</button>'+
                '</section>'+
                '<section style="width: 95%;" class="section_edit_notes">' +
                '<div style="height:100%; width: 100%; border: 1px solid black;" class="modal" name="notes_preview" id="notes_modal_preview">' +
                '<div id="notes_preview" style="position: absolute; top: 1%; left: 0%; resize: none; width: 49.5%;">'+
                '<textarea style="resize:none; width:100%;" name="notes" id="textarea_notes_preview">' +
                '</textarea>'+
                '</div>'+
                '<div style="background-color:white; position: absolute; left: 50.5%; top: 1%; max-width: 49.5%; border: 1px solid black;"'+
                'name="notes_preview" class="markdown-preview" data-use-github-style id="notes_textarea_preview">' +
                '</div><br><br>' +
                '</section>'
                );
    document.getElementById('textarea_notes').innerHTML = "# Notes\n" +
            "The server is still in early development! What you are seeing is liable to change and is not a final representation of the product!" +
            "\nNote: The sensor may or may not be down right now, as of 4/26/2017";
}

function editNotes(){
    
}

function showNotesPreview(){
    $(document.getElementById('textarea_notes_preview')).val($(document.getElementById('textarea_notes')).val());
    $(document.getElementById('notes_textarea_preview')).html(marked($(document.getElementById('textarea_notes')).val()));
    $(document.getElementById("notes_modal_preview")).show("slow", () => {
        var modal = $(document.getElementById('notes_modal_preview'));
        var preview = $(document.getElementById('notes_textarea_preview'));
        var textarea = $(document.getElementById('textarea_notes_preview'));
        var previewPaddTop = parseInt(preview.css('padding-top').substring(0, 2));
        var textareaPaddTop = parseInt(textarea.css('padding-top').substring(0, 2));
        var previewPaddBott = parseInt(preview.css('padding-bottom').substring(0, 2));
        var textareaPaddBott = parseInt(textarea.css('padding-bottom').substring(0, 2))
        var previewPadding = previewPaddTop + previewPaddBott;
        var textareaPadding = textareaPaddTop + textareaPaddBott;
        $(document.getElementById('textarea_notes_preview')).css('height', ($(document.getElementById('notes_modal_preview')).outerHeight() - textareaPadding)+ 'px');
        $(document.getElementById('textarea_notes_preview')).css('max-height', ($(document.getElementById('notes_modal_preview')).outerHeight() - textareaPadding) + 'px');
        $(document.getElementById('notes_textarea_preview')).css('height', ($(document.getElementById('notes_modal_preview')).outerHeight() - previewPadding) + 'px');
        $(document.getElementById('notes_textarea_preview')).css('max-height', ($(document.getElementById('notes_modal_preview')).outerHeight() - previewPadding) + 'px');
    });
    
    
    $('#textarea_notes_preview').on('input propertychange paste', () => {
            $('#notes_textarea_preview').html(marked($('#textarea_notes_preview').val()));
    });
    
    $(document).mouseup(e => {
        var container = $("#notes_modal_preview");

        if (!container.is(e.target) // if the target of the click isn't the container...
            && container.has(e.target).length === 0) // ... nor a descendant of the container
        {
            $(document.getElementById('textarea_notes')).val($(document.getElementById('textarea_notes_preview')).val());
            container.hide("slow");
        }
    });
}