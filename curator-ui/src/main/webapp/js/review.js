$.widget("curator.review", {

    options: {
        articles: null
    },

    _create:function() {

    },

    _init:function() {
        var $this = this;

        $this.element.empty();

        var table = $('<table></table>');

        $this.element.append(table);

        util.jsonCall('GET', '/curator/rest/article/list/best', null, null, function(response) {

            var data = [];
            for(var id in response.list) {
                var article = response.list[id];

                var _title = $('<a></a>').attr('href', '/curator/rest/link/'+article.id).text(article.title).wrap('<div>').parent().html();
                var _source = article.url.replace('http://','').replace('https://','').replace('www.','').replace(/\/.*/g,'');
                var _quality = Math.max(0, parseInt(article.quality * 100));
                var _published;
                if(article.published) {
                    _published = $this._newDateField(article.publishedTime).html();
                } else {
                    _published = '<span class="publish">Publish</span>';
                }
                data.push([article.id, article.mediaType, _title, _source, _quality, $this._newDateField(article.date).html(), _published]);
            }

            $this.oTable = table.dataTable( {
                'aaData': data,
                'bStateSave': true,
                'iDisplayLength': 50,
                "aLengthMenu": [[50, 100, -1], [50, 100, 'All']],
                'aoColumns': [
                    { 'sTitle': 'Id' },
                    { 'sTitle': 'Media', 'sWidth': '50px' },
                    { 'sTitle': 'Title' },
                    { 'sTitle': 'Source' },
                    { 'sTitle': 'Quality', 'sClass': 'center' },
                    { 'sTitle': 'Date', 'sWidth': '150px' },
                    { 'sTitle': 'Published', 'sWidth': '100px', 'sClass': 'center' }
                ],
                'aaSorting': [[4, 'desc'], [5, 'desc']],
                fnDrawCallback:function() {
                    table.find('.publish').button();
                }
            });

        });

        $('.publish').live('click', function(e) {

            var articleId = $(this).parent().parent().children().first().text();

            //$('#dialog-publish-template').clone().publish({articleId:articleId});
            $('#dialog-publish-template').publish({articleId:articleId});

        });

    },

    _newDateField:function(dateStr) {
        var date = util.strToDate(dateStr);
        return $('<div></div>')
            .append($('<span style="display: none"></span>').text(date.getTime()))
            .append($('<span></span>').text($.timeago(date)));
    }

});





$.widget("curator.publish", {

    options: {
        articleId: null
    },

    _create:function() {

    },

    _init:function() {
        var $this = this;

        var target = $this.element;
        var customText = $this.element.find('.custom-text').text();

        util.jsonCall('GET', '/curator/rest/article/{id}?custom={custom}', {'{id}':$this.options.articleId, '{custom}':customText}, null, function(article) {
            target.find('#text').text(article.text);
            target.find('.button').button();
            target.dialog({
                modal:true,
                resizable:false,
                closeOnEscape: true,
                width:700,
                buttons: {
                    Publish: function(event, ui) {
                        util.jsonCall('POST', '/curator/rest/article/publish/{id}', {'{id}': $this.options.articleId}, null, function(article) {
                            $this.oTable.dataTable().fnUpdate(article.publishedTime, pos[0], pos[1]);
                        });
                    },
                    Cancel: function(event, ui) {$(this).dialog('close');}
                }

            });
        });
    }
});