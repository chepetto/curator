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

            curator.util.jsonCall('GET', '/curator/rest/feed/create?url={url}', {'{url}': field.val()}, null, function (response) {
                if(response==true) {
                    noty({text: 'New Feed added.', timeout: 2000});
                }
            });
        });

        var controller = $('<div></div>').append(field).append(checkButton);
        target.append(controller);

        curator.util.jsonCall('GET', '/curator/rest/feed/status/all', null, null, function (status) {

            count.text(status.totalFeedCount + ' under service');

        });
    }
});
