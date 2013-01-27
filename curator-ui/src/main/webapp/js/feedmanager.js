$.widget("curator.feedmanager", {

    options:{

    },

    _create:function () {

    },

    _init:function () {
        var $this = this;

        $this.element.empty();

        util.jsonCall('GET', '/curator/rest/feed/status', null, null, function (status) {
            var count = $('<div></div>').text(status.totalFeedCount + ' under service');
            $this.element.append(count);

            var field = $('<input type="text" name="feed-url" class="feed-url">');
            var addButton = $('<span>Add Feed</span>').button();

            var controller = $('<div></div>').append(field).append(addButton);
            $this.element.append(controller);
        });
    }

});
