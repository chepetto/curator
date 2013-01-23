$.widget("curator.review", {

    options:{
        articles:null
    },

    _create:function () {

    },

    _init:function () {
        var $this = this;

        $this.element.empty();

        var table = $('<table></table>');

        $this.element.append(table);

        util.jsonCall('GET', '/curator/rest/article/list/suggest', null, null, function (response) {

            var data = [];
            for (var id in response.list) {
                var article = response.list[id];

                var _title = $('<a></a>').attr('href', '/curator/rest/link/' + article.id).text(article.title).wrap('<div>').parent().html();
                var _source = article.url.replace('http://', '').replace('https://', '').replace('www.', '').replace(/\/.*/g, '');
                var _quality = Math.max(0, parseInt(article.quality * 100));
                var _rating = $this._getRating(article);

                data.push([article.id, _title, _source, _quality, $this._newDateField(article.date).html(), _rating.html()]);
            }

            $this.oTable = table.dataTable({
                'aaData':data,
                'bStateSave':true,
                'iDisplayLength':50,
                "aLengthMenu":[
                    [50, 100, -1],
                    [50, 100, 'All']
                ],
                'aoColumns':[
                    { 'sTitle':'Id' },
                    { 'sTitle':'Title' },
                    { 'sTitle':'Source' },
                    { 'sTitle':'Quality', 'sClass':'center' },
                    { 'sTitle':'Date', 'sWidth':'150px' },
                    { 'sTitle':'Rating', 'sWidth':'150px' }
                ],
                'aaSorting':[
                    [4, 'desc'],
                    [5, 'desc']
                ],
                fnDrawCallback:function () {
                    // doku see http://wbotelhos.com/raty/
                    table.find('.rating').each(function () {

                        var el = $(this);
                        var articleId = el.attr('articleId');
                        var ratingsCount = parseInt(el.attr('ratingscount'));
                        var ratingsSum = parseInt(el.attr('ratingssum'));

                        el.raty({

                            score:ratingsCount == 0 ? 0 : parseInt(ratingsSum / ratingsCount),
                            noRatedMsg:'anyone rated this product yet!',

                            click:function (score, evt) {
                                var params = {'{articleId}':articleId, '{rating}':score};
                                util.jsonCall('POST', '/curator/rest/article/rate/{articleId}?rating={rating}', params, null, function (response) {
                                    alert('success');
                                });
                            }
                        });

                    });
                }
            });

        });

    },

    _getRating:function (article) {
        return $('<div></div>').append('<div class="rating" ratingscount="' + article.ratingsCount + '" ratingssum="' + article.ratingsSum + '" articleid="' + article.id + '"></div>')
    },

    _newDateField:function (dateStr) {
        var date = util.strToDate(dateStr);
        return $('<div></div>')
            .append($('<span style="display: none"></span>').text(date.getTime()))
            .append($('<span></span>').text($.timeago(date)));
    }

});


$.widget("curator.publish", {

    options:{
        articleId:null
    },

    _create:function () {

    },

    _init:function () {
        var $this = this;

        var target = $this.element;
        var customText = $this.element.find('.custom-text').text();

        util.jsonCall('GET', '/curator/rest/article/{id}?custom={custom}', {'{id}':$this.options.articleId, '{custom}':customText}, null, function (article) {
            target.find('#text').text(article.text);
            target.find('.button').button();
            target.dialog({
                modal:true,
                resizable:false,
                closeOnEscape:true,
                width:700,
                buttons:{
                    Publish:function (event, ui) {
                        util.jsonCall('POST', '/curator/rest/article/publish/{id}', {'{id}':$this.options.articleId}, null, function (article) {
                            $this.oTable.dataTable().fnUpdate(article.publishedTime, pos[0], pos[1]);
                        });
                    },
                    Cancel:function (event, ui) {
                        $(this).dialog('close');
                    }
                }

            });
        });
    }
});