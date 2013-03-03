$.widget("curator.live", {

    options:{
        url:null,
        class:null
    },

    _init:function () {
        var $this = this;

        if ($this.options.url == null) {
            throw 'url is null';
        }

        $this.element.empty().addClass($this.options.class);
        $this._table();
    },

    _table:function () {

        var $this = this;

        var list = $('<div/>');

        $this.element.append(list);

        curator.util.jsonCall('GET', $this.options.url, null, null, function (response) {

            for (var id in response.list) {
                var article = response.list[id];

                var _title = $('<a/>', {href:'/curator/rest/link/' + article.id, text:article.title}).wrap('<div>').parent().html();
                var _source = article.url.replace('http://', '').replace('https://', '').replace('www.', '').replace(/\/.*/g, '');
                var _quality = Math.max(0, parseInt(article.quality * 100));

                var _dateField;
                //if (_published) {
                //    _dateField = $this._newDateField(article.featuredTime);
                //} else {
                _dateField = $this._newDateField(article.date);
                //}

                var e_title = $('<div class="title" style="float: left;"/>').append(_title);
                var e_source = $('<div class="source" style="float: left;"/>').append('on ' + _source);
                var e_head = $('<div/>')
                        .append(e_title)
                        .append(e_source)
                    ;

                var e_abstract = $('<div class="abstract"/>').text(article.text);

                var e_time = $('<div class="time"/>').append(_dateField);
                var e_views = $('<div class="views"/>', {text:article.views});
                var e_votes = $('<div class="votes"/>').append($this._getRating(article));
                var e_stats = $('<div class="stats"/>')
                        .append(e_time)
                        .append(e_views)
                        .append(e_votes)
                    ;

                var e_menu = $('<div class="menu"/>');

                var e_article = $('<div class="article"/>')
                        .append(e_head)
                        .append('<div style="clear: both;"/>')
                        .append(e_stats)
                        .append(e_abstract)
                        .append('<div style="clear: both;"/>')
                        .append(e_menu)
                        .append('<div style="clear: both;"/>')
                    ;

                e_article.css('max-height', 80).click(function () {
                    // todo animate
                    $(this).css('max-height', '');
                });
                list.append(e_article);
            }
        });

    },

    _getRating:function (article) {
        var ratingsCount = article.ratingsCount;
        var ratingsSum = article.ratingsSum;
        return $('<div/>').raty({

            score:ratingsCount == 0 ? 0 : parseInt(ratingsSum / ratingsCount),
            noRatedMsg:'anyone rated this product yet!',

            click:function (score, evt) {
                var params = {'{articleId}':article.id, '{rating}':score};
                curator.util.jsonCall('POST', '/curator/rest/article/vote/{articleId}?rating={rating}', params, null, function (response) {
                    noty({text:'Thanks for voting!', timeout:2000});
                });
            }
        });
    },

    _newDateField:function (dateStr) {
        var date = curator.util.strToDate(dateStr);
        return $('<div/>')
            .append($('<span/>', {text:date.getTime()}).hide())
            .append($('<span/>', {text:$.timeago(date)}));
    }

});