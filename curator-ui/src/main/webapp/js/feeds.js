$.widget("curator.feeds", {

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

        util.jsonCall('GET', '/curator/rest/feed/list', null, null, function (response) {

            var data = [];
            for (var id in response.list) {
                var feed = response.list[id];

                var _title = $('<a></a>').attr('href', feed.url).text(feed.url).wrap('<div>').parent().html();
                var _triggerHarvest;
                if (feed.harvestRequired) {
                    _triggerHarvest = 'Scheduled';
                } else {
                    _triggerHarvest = '<span class="button harvest">Harvest</span>';
                }
                var _active;
                if (feed.active) {
                    _active = '<span class="button status">Deactivate</span>';
                } else {
                    _active = '<span class="button status activate">Activate</span>';
                }

                var lastHarvestTime = feed.lastHarvestTime == null ? '-' : $.timeago(util.strToDate(feed.lastHarvestTime));
                var lastArticleTime = feed.lastArticleTime == null ? '-' : $.timeago(util.strToDate(feed.lastArticleTime));

                data.push([feed.id, _title, feed.creationTime, feed.articlesCount, feed.reviewRequired, lastHarvestTime, lastArticleTime, _triggerHarvest, _active]);
            }

            $this.oTable = table.dataTable({
                'aaData':data,
                'bStateSave': true,
                'iDisplayLength': 50,
                "aLengthMenu": [[50, 100, -1], [50, 100, 'All']],
                'aoColumns':[
                    { 'sTitle':'Id' },
                    { 'sTitle':'Title' },
                    { 'sTitle':'Since' },
                    { 'sTitle':'Articles' },
                    { 'sTitle':'Errornous' },
                    { 'sTitle':'Last harvest' },
                    { 'sTitle':'Last article' },
                    { 'sTitle':'Harvest' },
                    { 'sTitle':'Status' }
                ],
                'aaSorting':[
                    [1, 'desc']
                ],
                fnDrawCallback:function () {
                    table.find('SPAN.button').button();
                }
            });
        });

        $('.button').live('click', function () {

            var pos = $this.oTable.dataTable().fnGetPosition($(this).parent()[0]);
            var feedId = $(this).parent().parent().children().first().text();

            if($(this).hasClass('status')) {
                var activate = $(this).hasClass('activate');
                util.jsonCall('POST', '/curator/rest/feed/status/{id}/'+activate, {'{id}':feedId}, null, function (feed) {
                    $this.oTable.dataTable().fnUpdate('<span class="button status">'+(activate?'Deactivate':'Activate')+'</span>', pos[0], pos[1]);
                })
            } else

            if($(this).hasClass('harvest')) {
                util.jsonCall('POST', '/curator/rest/feed/harvest/{id}/', {'{id}':feedId}, null, function (feed) {
                    $this.oTable.dataTable().fnUpdate('Scheduled', pos[0], pos[1]);
                })
            }
        });

    }

});
