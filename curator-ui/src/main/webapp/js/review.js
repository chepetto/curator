$.widget("curator.review", {

    options: {
        articles: null
    },

    _init: function () {
        var $this = this;

        $this.element.empty();

        $this._menu();
        $this._table();
    },

    _menu: function () {
        // todo menu like "publish new article"

        var newArticle = $('<div/>', {text: 'Add Article' })
            .button({
                icons: {
                    primary: "ui-icon-triangle-1-s"
                }
            })
            .click(function () {

            });

        return $('<div/>').append(newArticle);

    },

    _table: function () {

        var $this = this;

        var table = $('<table/>');

        $this.element.append(table);

        curator.util.jsonCall('GET', '/curator/rest/article/list/review', null, null, function (response) {

            var data = [];
            for (var id in response.list) {
                var article = response.list[id];

                var _title = $('<a/>', {href: '/curator/rest/link/' + article.id, text: article.title}).wrap('<div>').parent().html();
                var _source = article.url.replace('http://', '').replace('https://', '').replace('www.', '').replace(/\/.*/g, '');
                var _quality = Math.max(0, parseInt(article.quality * 100));
                var _rating = $this._getRating(article);

                var _details;
//                if (article.published) {
//                    _published = $this._newDateField(article.publishedTime).html();
//                } else {
                _details = '<span class="details">Details</span>';
//                }


                data.push([article.id, _title, _source, _quality, $this._newDateField(article.date).html(), _rating.html(), _details]);
            }

            $this.oTable = table.dataTable({
                'aaData': data,
                'bStateSave': true,
                'iDisplayLength': 50,
                "aLengthMenu": [
                    [50, 100, -1],
                    [50, 100, 'All']
                ],
                'aoColumns': [
                    { 'sTitle': 'Id' },
                    { 'sTitle': 'Title' },
                    { 'sTitle': 'Source' },
                    { 'sTitle': 'Quality', 'sClass': 'center' },
                    { 'sTitle': 'Date', 'sWidth': '70px' },
                    { 'sTitle': 'Rating', 'sWidth': '150px' },
                    { 'sTitle': 'Details', 'sWidth': '40px' }
                ],
                'aaSorting': [
                    [4, 'desc'],
                    [5, 'desc']
                ],
                fnDrawCallback: function () {

                    table.find('.details').button();

                    // doku see http://wbotelhos.com/raty/
                    table.find('.rating').each(function () {

                        var el = $(this);
                        var articleId = el.attr('articleId');
                        var ratingsCount = parseInt(el.attr('ratingscount'));
                        var ratingsSum = parseInt(el.attr('ratingssum'));

                        el.raty({

                            score: ratingsCount == 0 ? 0 : parseInt(ratingsSum / ratingsCount),
                            noRatedMsg: 'anyone rated this product yet!',

                            click: function (score, evt) {
                                var params = {'{articleId}': articleId, '{rating}': score};
                                curator.util.jsonCall('POST', '/curator/rest/article/rate/{articleId}?rating={rating}', params, null, function (response) {
                                    noty({text: 'Thanks for rating!', timeout: 2000});
                                });
                            }
                        });

                    });
                }
            });

            $('.details').live('click', function (e) {

                var articleId = $(this).parent().parent().children().first().text();

                //$('#dialog-publish-template').clone().publish({articleId:articleId});
                $('#dialog-publish-template').clone().attr('id', '').publish({articleId: articleId});

            });

        });

    },

    _getRating: function (article) {
//        return $('<div/>').append('<div class="rating" ratingscount="' + article.ratingsCount + '" ratingssum="' + article.ratingsSum + '" articleid="' + article.id + '"></div>')
        return $('<div/>').append(
            $('<div/>', {
                class: 'rating',
                ratingscount: article.ratingsCount,
                ratingssum: article.ratingsSum,
                articleid: article.id
            }));
    },

    _newDateField: function (dateStr) {
        var date = curator.util.strToDate(dateStr);
        return $('<div/>')
            .append($('<span/>', {text: date.getTime()}).hide())
            .append($('<span/>', {text: $.timeago(date)}));
    }

});


$.widget("curator.publish", {

    options: {
        articleId: null
    },

    _create: function () {

    },

    _init: function () {
        var $this = this;

        var target = $this.element;

        curator.util.jsonCall('GET', '/curator/rest/article/{id}', {'{id}': $this.options.articleId}, null, function (article) {

            var link = $('<a/>', {href: article.url, text: article.url.replace(/http[s]?:\/\/[w.]?]/g, '')});

            if (article.published) {
                target.find('.orgignial-data').hide();
            } else {
                target.find('.orgignial-data').hide();
            }
            target.find('.org-link').empty().append(link);
            target.find('.org-title').text(article.title);
            target.find('.org-text').text(article.text);

            target.find('.custom-title').empty().val(article.customTitle);
            target.find('.custom-text').val(article.customText);

            target.find('.button').button();
            target.dialog({
                modal: true,
                resizable: false,
                closeOnEscape: true,
                sticky: true,
                width: 700,
                buttons: {
                    Publish: function (event, ui) {

                        var customText = target.find('.custom-text').val();
                        var customTitle = target.find('.custom-title').val();

                        curator.util.jsonCall('POST', '/curator/rest/article/publish/{id}?text={text}&title={title}', {'{id}': $this.options.articleId, '{text}': customText, '{title}': customTitle}, null, function (article) {
                            //$this.oTable.dataTable().fnUpdate(article.publishedTime, pos[0], pos[1]);
                            target.dialog('destroy').remove();
                        });
                    },
                    Cancel: function (event, ui) {
                        target.dialog('destroy').remove();
                    }
                }

            });
        });
    }
});