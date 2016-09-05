package virginia.hc.philips.com.virginiademoapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;
import it.gmariotti.cardslib.library.view.CardViewNative;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settings = getSharedPreferences("ServerSettings", 0);

        ArrayList<Card> cards = new ArrayList<Card>();

        MaterialLargeImageCard card =
                MaterialLargeImageCard.with(this)
                        .setTextOverImage("Patient Registration")
                        .setTitle("Register a new patient today")
                        .setSubTitle("\"You treat a disease, you win, you lose. You treat a person, I guarantee you, you’ll win, no matter what the outcome.\"\n" +
                                "- Patch Adams")
                        .useDrawableId(R.drawable.ic_action_register)
                        .build();
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent intent = new Intent(MainActivity.this, PatientActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        cards.add(card);

        card = MaterialLargeImageCard.with(this)
                        .setTextOverImage("Patient Encounter")
                        .setTitle("Record a new encounter with your patient")
                        .setSubTitle("\"They may forget your name, but they will never forget how you made them feel.\"\n" +
                                "- Maya Angelou")
                        .useDrawableId(R.drawable.ic_action_medical)
                        .build();
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent intent = new Intent(MainActivity.this, EncounterActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        cards.add(card);

        card = MaterialLargeImageCard.with(this)
                .setTextOverImage("Patient Lookup")
                .setTitle("Find a patient record")
                .setSubTitle("\"Your customer doesn’t care how much you know until they know how much you care.\"\n" +
                        "- Damon Richards")
                .useDrawableId(R.drawable.ic_action_directory)
                .build();
        cards.add(card);

        card = MaterialLargeImageCard.with(this)
                .setTextOverImage("Settings")
                .setTitle("Configure application settings")
                .setSubTitle("\"To improve is to change; to be perfect is to change often.\"\n- Winston Churchill")
                .useDrawableId(R.drawable.ic_action_change)
                .build();
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                final Dialog dialog = new Dialog(MainActivity.this);

                dialog.setContentView(R.layout.server_url);
                //dialog.setTitle("Custom Alert Dialog");

                final EditText editText = (EditText) dialog.findViewById(R.id.editText);
                Button btnSave          = (Button) dialog.findViewById(R.id.button);

                editText.setText(settings.getString("ServerURL",""));

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("ServerURL",editText.getText().toString());
                        editor.commit();
                        dialog.cancel();
                    }
                });

                dialog.show();
            }
        });
        cards.add(card);

        CardArrayRecyclerViewAdapter mCardArrayAdapter = new CardArrayRecyclerViewAdapter(this, cards);
        CardRecyclerView mRecyclerView = (CardRecyclerView) this.findViewById(R.id.cardList);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mCardArrayAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
