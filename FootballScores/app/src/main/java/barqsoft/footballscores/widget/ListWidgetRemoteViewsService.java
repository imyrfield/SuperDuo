package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by Ian on 10/22/2015.
 */
public class ListWidgetRemoteViewsService extends RemoteViewsService {


    static final int INDEX_SCORES_DATE = 1;
    static final int INDEX_SCORES_MATCH_TIME = 2;
    static final int INDEX_SCORES_HOME_NAME = 3;
    static final int INDEX_SCORES_AWAY_NAME = 4;
    static final int INDEX_SCORES_LEAGUE = 5;
    static final int INDEX_SCORES_HOME_GOALS = 6;
    static final int INDEX_SCORES_AWAY_GOALS = 7;
    static final int INDEX_SCORES_ID = 8;
    static final int INDEX_SCORES_MATCH_DAY = 9;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {

                if (data != null){
                    data.close();
                }

                // Get today's date
                Date today = new Date(System.currentTimeMillis());
                SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
                String[] date = { mFormat.format(today) };

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(
                        uri,
                        null,
                        null,
                        date,
                        null
                );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if(position == AdapterView.INVALID_POSITION
                        || data == null
                        || !data.moveToPosition(position)){
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);
                String homeTeam = data.getString(INDEX_SCORES_HOME_NAME);
                String awayTeam = data.getString(INDEX_SCORES_AWAY_NAME);
                String time = data.getString(INDEX_SCORES_MATCH_TIME);

                int homeScore = data.getInt(INDEX_SCORES_HOME_GOALS);
                int awayScore = data.getInt(INDEX_SCORES_AWAY_GOALS);
                String score = Utilities.getScores( homeScore, awayScore );

                views.setTextViewText(R.id.home_name, homeTeam);
                views.setTextViewText(R.id.away_name, awayTeam);
                views.setTextViewText(R.id.score_textview, score);
                views.setTextViewText(R.id.data_textview, time);

                final Intent fillInIntent = new Intent(getApplicationContext(), MainActivity.class);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(data.moveToPosition(position)){
                    return data.getLong(INDEX_SCORES_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
