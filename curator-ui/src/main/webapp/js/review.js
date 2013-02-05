$.widget("curator.review", {

    options:{
        articles:null
    },

    _init:function () {
        var $this = this;

        $this.element.empty();

        $this._menu();
        $this._table();
    },

    _menu:function () {
        // todo menu like "publish new article"

        var newArticle = $('<div>Add Article</div>')
            .button({
                icons:{
                    primary:"ui-icon-triangle-1-s"
                }
            })
            .click(function () {

            });

        return $('<div></div>').append(newArticle);

    },

    _table:function () {

        var $this = this;

        var table = $('<table></table>');

        $this.element.append(table);

        util.jsonCall('GET', '/curator/rest/article/list/review', null, null, function (response) {

            var data = [];
            for (var id in response.list) {
                var article = response.list[id];

                var _title = $('<a></a>').attr('href', '/curator/rest/link/' + article.id).text(article.title).wrap('<div>').parent().html();
                var _source = article.url.replace('http://', '').replace('https://', '').replace('www.', '').replace(/\/.*/g, '');
                var _quality = Math.max(0, parseInt(article.quality * 100));
                var _rating = $this._getRating(article);

                var _published;
                if (article.published) {
                    _published = $this._newDateField(article.publishedTime).html();
                } else {
                    _published = '<span class="publish">Publish</span>';
                }


                data.push([article.id, _title, _source, _quality, $this._newDateField(article.date).html(), _rating.html(), _published]);
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
                    { 'sTitle':'Date', 'sWidth':'70px' },
                    { 'sTitle':'Rating', 'sWidth':'150px' },
                    { 'sTitle':'Publish', 'sWidth':'40px' }
                ],
                'aaSorting':[
                    [4, 'desc'],
                    [5, 'desc']
                ],
                fnDrawCallback:function () {

                    table.find('.publish').button();

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
                                    console.log('success');
                                });
                            }
                        });

                    });
                }
            });

            $('.publish').live('click', function (e) {

                var articleId = $(this).parent().parent().children().first().text();

                //$('#dialog-publish-template').clone().publish({articleId:articleId});
                $('#dialog-publish-template').publish({articleId:articleId});

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

        util.jsonCall('GET', '/curator/rest/article/{id}', {'{id}':$this.options.articleId}, null, function (article) {

            var link = $('<a></a>').attr('href', article.url).text(article.url.replace(/http[s]?:\/\/[w.]?]/g, ''));

            target.find('.org-link').empty().append(link);
            target.find('.org-title').text(article.title);
            target.find('.org-text').text(article.text);

            target.find('.custom-title').empty().append(article.customtitle);
            target.find('.custom-text').text(article.customtext);

            target.find('.button').button();
            target.dialog({
                modal:true,
                resizable:false,
                closeOnEscape:true,
                sticky:true,
                width:700,
                buttons:{
                    Publish:function (event, ui) {

                        var customText = target.find('.custom-text').text();
                        var customTitle = target.find('.custom-title').text();

                        util.jsonCall('POST', '/curator/rest/article/publish/{id}?text={text}&title={title}', {'{id}':$this.options.articleId, '{text}':customText, '{title}':customTitle}, null, function (article) {
                            //$this.oTable.dataTable().fnUpdate(article.publishedTime, pos[0], pos[1]);
                            target.dialog('destroy');
                        });
                    },
                    Cancel:function (event, ui) {
                        target.dialog('destroy');
                    }
                }

            });
        });
    }
});