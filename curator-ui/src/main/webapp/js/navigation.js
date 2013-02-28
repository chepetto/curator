$.widget("curator.navigation", {

    options:{
        items:[
            {name:'Hot', link:'published.html'},
            {name:'Live', link:'live.html'},
            {name:'Post Article', fn:curator.util.dialogNewArticle},
            {name:'New Feed', fn:curator.util.dialogNewFeed}
        ]
    },

    _init:function () {

        var $this = this;

        var wrapper = $('<ul/>');
        for (var index in $this.options.items) {

            var menuItem = $this.options.items[index];

            var link;
            if (menuItem.link) {
                link = $('<a/>', {href:menuItem.link, text:menuItem.name});
            } else {
                link = $('<a/>', {href:'#', text:menuItem.name}).click(menuItem.fn);
            }

            var li = $('<li/>').append(link);

            wrapper.append(li);
        }

        $this.element.empty().append(wrapper);

    }

});
