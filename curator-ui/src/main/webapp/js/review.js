$.widget("curator.review", {

    options:{
        articles:null
    },

    _init:function () {
        var $this = this;

        $this.element.empty();

        $this._table();
    },

    _table:function () {

        var $this = this;

        var table = $('<table/>');

        $this.element.append(table);

        curator.util.jsonCall('GET', '/curator/rest/article/list/review', null, null, function (response) {

            var data = [];
            for (var id in response.list) {
                var article = response.list[id];

                var _title = $('<a/>', {href:'/curator/rest/link/' + article.id, text:article.title}).wrap('<div>').parent().html();
                var _source = article.url.replace('http://', '').replace('https://', '').replace('www.', '').replace(/\/.*/g, '');
                var _quality = Math.max(0, parseInt(article.quality * 100));
                var _rating = $this._getRating(article);

                var _details = '<span class="details">Details</span>';


                var _published = article.published;
                var _dateField;
                if (_published) {
                    _dateField = $this._newDateField(article.publishedTime);
                } else {
                    _dateField = $this._newDateField(article.date);
                }

                data.push([article.id, _title, _source, _quality, _rating.html(), _dateField.html(), _details, _published]);
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
                    { 'sTitle':'Rating', 'sWidth':'150px' },
                    { 'sTitle':'Date', 'sWidth':'70px' },
                    { 'sTitle':'Details', 'sWidth':'40px' }
                ],
                'aaSorting':[
                    [4, 'desc'],
                    [5, 'desc']
                ],

                fnRowCallback:function (nRow, aData, iDisplayIndex) {
                    nRow.className = iDisplayIndex % 2 == 0 ? 'even' : 'odd';
                    if (aData[aData.length - 1]) {
                        nRow.className += ' published';
                    }
                    return nRow;
                },

                fnDrawCallback:function () {

                    table.find('.details').button();

                    // doku see http://wbotelhos.com/raty/
                    table.find('.rating').each(function () {

                        var el = $(this);
                        var articleId = el.attr('articleId');
                        var ratingsCount = parseInt(el.attr('ratingscount'));
                        var ratingsSum = parseInt(el.attr('ratingssum'));

                        el.raty({

                            score:ratingsCount == 0 ? 0 : parseInt(ratingsSum / ratingsCount),
                            noRatedMsg:'anyone rated this product yet!',

                            click: function (score, evt) {
                                var params = {'{articleId}': articleId, '{rating}': score};
                                curator.util.jsonCall('POST', '/curator/rest/article/vote/{articleId}?rating={rating}', params, null, function (response) {
                                    noty({text: 'Thanks for voting!', timeout: 2000});
                                });
                            }
                        });

                    });
                }
            });

            $('.details').live('click', function (e) {

                var articleId = $(this).parent().parent().children().first().text();

                $('#dialog-publish-template').clone().removeAttr('id').publish({articleId:articleId});

            });

        });

    },

    _getRating:function (article) {
        return $('<div/>').append(
            $('<div/>', {
                class:'rating',
                ratingscount:article.ratingsCount,
                ratingssum:article.ratingsSum,
                articleid:article.id
            }));
    },

    _newDateField:function (dateStr) {
        var date = curator.util.strToDate(dateStr);
        return $('<div/>')
            .append($('<span/>', {text:date.getTime()}).hide())
            .append($('<span/>', {text:$.timeago(date)}));
    }

});


$.widget("curator.publish", {

    options:{
        articleId:null
    },

    _create:function () {
        if (this.options.articleId == null) {
            var err = 'articleId is null';
            console.error(err);
            throw err;
        }
    },

    _init:function () {
        var $this = this;

        var target = $this.element;

        curator.util.jsonCall('GET', '/curator/rest/article/{id}', {'{id}':$this.options.articleId}, null, function (article) {

            var link = $('<a/>', {href:article.url, text:article.url.replace(/http[s]?:\/\/[w.]?]/g, '')});

            if (article.published) {
                target.find('.orgignial-data').hide();
            } else {
                target.find('.orgignial-data').hide();
            }
            target.find('.org-link').empty().append(link);
            target.find('.org-title').text(article.title);
            target.find('.org-text').text(article.text);

            target.find('.custom-title').empty().val(article.customTitle);
            target.find('.custom-text').val(article.customTextMarkup);

            target.find('.button').button();
            target.dialog({
                modal:true,
                resizable:false,
                draggable:false,
                closeOnEscape:true,
                sticky:true,
                width:700,
                buttons:$this._buttons(target, article.published)
            });
        });
    },

    _buttons:function (target, isPublished) {

        var $this = this;

        var buttons = {};

        var publishBttName;
        if (isPublished) {
            publishBttName = 'Update';
        } else {
            publishBttName = 'Publish';
        }

        buttons[publishBttName] = function (event, ui) {

            var customText = target.find('.custom-text').val();
            var customTitle = target.find('.custom-title').val();

            curator.util.jsonCall('POST', '/curator/rest/article/publish/{id}?text={text}&title={title}', {'{id}':$this.options.articleId, '{text}':customText, '{title}':customTitle}, null, function (article) {
                noty({text:'Updated!', timeout:2000});
                target.dialog('destroy').remove();
            });
        };

        buttons['Cancel'] = function (event, ui) {
            target.dialog('destroy').remove();
        };

        return buttons;
    }
});