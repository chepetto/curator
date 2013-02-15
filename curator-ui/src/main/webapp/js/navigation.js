$.widget("curator.navigation", {

    options: {
        items: [
            {name: 'Hot', link: 'published.html'},
            {name: 'Live', link: 'live.html'},
            {name: 'Post', fn: function () {
                //newArticleDialog
                var dialog = $('#dialog-add-article-template').clone().removeAttr('id').dialog({
                    modal: true,
                    resizable: false,
                    closeOnEscape: true,
                    sticky: true,
                    width: 700,
                    buttons: {
                        'Publish': function () {

                            var title = dialog.find('.custom-title').val();
                            var url = dialog.find('.custom-link').val();
                            var text = dialog.find('.custom-text').val();

                            // todo validate

                            var article = {title: title, url: url, text: text};

                            curator.util.jsonCall('POST', '/curator/rest/article', null, JSON.stringify(article), function (response) {
                                // todo
                                noty({text: 'Thanks for posting!', timeout: 2000});
                            });
                        }
                    }
                });
            }
            }
        ]
    },

    _init: function () {

        var $this = this;

        var wrapper = $('<ul/>');
        for (var index in $this.options.items) {

            var menuItem = $this.options.items[index];

            var link;
            if (menuItem.link) {
                link = $('<a/>', {href: menuItem.link, text: menuItem.name});
            } else {
                link = $('<a/>', {href: '#', text: menuItem.name}).click(menuItem.fn);
            }

            var li = $('<li/>').append(link);

            wrapper.append(li);
        }

        $this.element.empty().append(wrapper);

    }

});
