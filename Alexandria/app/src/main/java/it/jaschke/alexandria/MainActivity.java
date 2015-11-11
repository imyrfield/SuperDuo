package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;


public class MainActivity
        extends ActionBarActivity
        implements Callback {

    private       CharSequence title;
    private       String       dialog;
    public static boolean      IS_TABLET;

    private BroadcastReceiver messageReciever;
    public static final  String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final  String MESSAGE_KEY   = "MESSAGE_EXTRA";
    public static final  String DIALOG_ABOUT  = "about";
    public static final  String DIALOG_ADD    = "add";
    private static final String BOOKLIST_TAG  = "BLT";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        IS_TABLET = getResources().getBoolean( R.bool.large_layout );

        if (IS_TABLET) {
            setContentView( R.layout.activity_main_tablet );
        } else {
            setContentView( R.layout.activity_main );
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                                       .replace( R.id.container,
                                                 new ListOfBooks(),
                                                 BOOKLIST_TAG )
                                       .commit();
        }
        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter( MESSAGE_EVENT );
        LocalBroadcastManager.getInstance( this )
                             .registerReceiver( messageReciever, filter );
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState( savedInstanceState );
        if (IS_TABLET && findViewById( R.id.right_container ) !=  null) {
            getSupportFragmentManager().popBackStack( "detail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void setTitle (int titleId) {
        title = getString( titleId );
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case ( R.id.action_about ):
                dialog = DIALOG_ABOUT;
                createDialog( dialog, null );
                break;
            case ( R.id.action_add_book ):
                dialog = DIALOG_ADD;
                createDialog( dialog, null );
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onDestroy () {
        LocalBroadcastManager.getInstance( this ).unregisterReceiver( messageReciever );
        super.onDestroy();
    }

    @Override
    public void onItemSelected (String ean) {
        Bundle args = new Bundle();
        args.putString( BookDetail.EAN_KEY, ean );

        BookDetail fragment = new BookDetail();
        fragment.setArguments( args );

        int id = R.id.container;
        if (findViewById( R.id.right_container ) != null) {
            id = R.id.right_container;
        }
        getSupportFragmentManager().beginTransaction()
                                   .replace( id, fragment )
                                   .addToBackStack( "detail" )
                                   .commit();

    }

    private class MessageReciever
            extends BroadcastReceiver {
        @Override
        public void onReceive (Context context, Intent intent) {
            if (intent.getStringExtra( MESSAGE_KEY ) != null) {
                Toast.makeText( MainActivity.this,
                                intent.getStringExtra( MESSAGE_KEY ),
                                Toast.LENGTH_LONG ).show();
            }
        }
    }

    @Override
    public void onBackPressed () {
        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
    }

    public void createDialog (String dialog, Bundle args) {

        switch (dialog) {

            case ( DIALOG_ABOUT ):

                AboutDialog dialog_Fragment = new AboutDialog();

                dialog_Fragment.show( getFragmentManager(), "dialog" );
                break;

            case ( DIALOG_ADD ):

                ScanDialog dialogFragment = new ScanDialog();

                if (IS_TABLET) {
                    dialogFragment.show( getSupportFragmentManager(), "dialog" );
                } else {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace( R.id.container, dialogFragment, "dialog" )
                               .addToBackStack( null )
                               .commit();
                }

                break;
        }
    }
}