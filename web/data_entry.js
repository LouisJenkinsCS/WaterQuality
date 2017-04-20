/* 
 * Started 2/26/2017
 * Author: Brandon Meglathery
 * 
 * Next task: correct code to match admin content/vars
 */
var $data = $('#data_entry'); //variable $data holds contents of div id=data_entry
var $name = $('#name'); //selector puts the object with id='name' into $name

$.ajax({
    type: 'GET', //Gets a collection objects and passes them to $friends
    url: 'http://rest.learncode.academy/api/Brandons/friends',
    success: function (data_objs) {
        $.each(data_objs, function (obj) {
            addObj(obj);
        });
    }
});

$('#add-friend').on('click', function () {

    var friend = {
        name: $name.val(),
    };

    $.ajax({
        type: 'POST',
        url: 'http://rest.learncode.academy/api/Brandons/friends',
        data: friend,
        success: function (newFriend) {
            addFriend(newFriend);
        },
        error: function () {
            alert('error making friend');
        }
    });

});

$friends.delegate('.remove', 'click', function () {
    var $li = $(this).closest('li');

    $.ajax({
        type: 'DELETE',
        url: 'http://rest.learncode.academy/api/Brandons/friends/' +
                $(this).attr('data-id'),
        success: function () {
            $li.fadeOut(600, function () {
                $li.remove();
//                console.log('Friend deleted successfully :(');
            });
        },
        failure: function () {
//            console.log('Error, id: ' + $li.data("id"));
        }
    });
});

function addFriend(friend)
{
    $friends.append('<li><p>Friend: ' + friend.name + '</p>'
            + '<button data-id="' + friend.id + '" class = "remove">X</button></li>');
}


