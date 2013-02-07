$.widget("curator.feedmanager", {

    options:{

    },

    _create:function () {

    },

    _init:function () {
        var $this = this;

        var target = $this.element.empty();

        var count = $('<div></div>');
        target.append(count);


        var field = $('<input type="text" name="feed-url" class="feed-url">');
        var checkButton = $('<span>Add</span>').button().click(function() {

            util.jsonCall('GET', '/curator/rest/feed/new?url={url}', {'{url}': field.val()}, null, function (response) {

            });

        });

        var controller = $('<div></div>').append(field).append(checkButton);
        var controller = $('<div></div>').append(field).append(checkButton);
        target.append(controller);


        util.jsonCall('GET', '/curator/rest/feed/status/all', null, null, function (status) {

            count.text(status.totalFeedCount + ' under service');

        });
    }

});
