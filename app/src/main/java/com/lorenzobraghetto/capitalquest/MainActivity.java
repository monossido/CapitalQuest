package com.lorenzobraghetto.capitalquest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private static final String SQUADRA_0 = "Etiopia";
    private static final String SQUADRA_1 = "Uganda";
    private static final String SQUADRA_2 = "Kenya";
    private static final String SQUADRA_3 = "Mozambico";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private int idSquadra = -1;
    private int tappa = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final EditText nomeSquadra = (EditText) findViewById(R.id.editText1);
        Button ok = (Button) findViewById(R.id.button1);
        final TextView text = (TextView) findViewById(R.id.textView1);
        final TextView textCell = (TextView) findViewById(R.id.textViewCell);
        TextView text2 = (TextView) findViewById(R.id.textView2);
        final ImageView img = (ImageView) findViewById(R.id.imageView1);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        Intent currentIntent = getIntent();

        if (currentIntent.getExtras() == null && pref.getInt("Tappa", -1) == -1) {

            nomeSquadra.setHint("");
            long time = System.currentTimeMillis();

            Calendar calendar = new GregorianCalendar();

            calendar.set(Calendar.MONTH, Calendar.MAY);
            calendar.set(Calendar.DAY_OF_MONTH, 24);
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            calendar.set(Calendar.MINUTE, 0);

            long startTime = calendar.getTimeInMillis();

            if (startTime - time <= 0) {
                ok.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String squadra = nomeSquadra.getText().toString();
                        int idSquadra = -1;
                        if (squadra.equalsIgnoreCase(SQUADRA_0))
                            idSquadra = 0;
                        else if (squadra.equalsIgnoreCase(SQUADRA_1))
                            idSquadra = 1;
                        else if (squadra.equalsIgnoreCase(SQUADRA_2))
                            idSquadra = 2;
                        else if (squadra.equalsIgnoreCase(SQUADRA_3))
                            idSquadra = 3;

                        if (idSquadra != -1) {
                            Intent intent = getIntent();
                            intent.putExtra("Squadra", idSquadra);
                            intent.putExtra("Tappa", 0);
                            finish();
                            startActivity(intent);
                        } else {
                            text.setText("Li mortacci tua, hai sbagliato nome della squadra");
                        }
                    }
                });
            } else {
                text2.setVisibility(View.GONE);
                ok.setVisibility(View.GONE);
                nomeSquadra.setVisibility(View.GONE);
                img.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) textCell.getLayoutParams();
                lp.addRule(RelativeLayout.BELOW, R.id.imageView1);

                new CountDownTimer(startTime - time, 1000) {

                    public void onTick(long millisUntilFinished) {
                        int seconds = (int) (millisUntilFinished / 1000) % 60;
                        int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                        int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                        int days = (int) (millisUntilFinished / (1000 * 60 * 60 * 24));

                        text.setText("Nun c'avè fretta! Mancano ancora:\n\n" + days + " giorni " + hours + " ore " + minutes + " minuti " + seconds + " secondi\n\n" +
                                "\"African quest Roma, 24 Maggio 2015.\"\n");
                    }

                    public void onFinish() {
                        text.setText("Pronti, via (chiudi e riapri l'app :) )!");
                    }
                }.start();
            }
        } else {
            if (currentIntent.getExtras() != null) {
                idSquadra = currentIntent.getExtras().getInt("Squadra", -1);
                tappa = currentIntent.getExtras().getInt("Tappa");
            }

            int idSquadraPref = pref.getInt("Squadra", -1);
            int tappPref = pref.getInt("Tappa", -1);

            if (tappa < tappPref)
                tappa = tappPref;
            if (idSquadra == -1)
                idSquadra = idSquadraPref;

            String indovinello = getIndovinello(idSquadra, tappa);

            text.setText(getString(R.string.tappa_titolo) + " " + (int) (tappa + 1));
            text2.setText(indovinello);

            final int tappaFinal = tappa;
            ok.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String codice = nomeSquadra.getText().toString();
                    boolean isOk = checkCodice(codice, idSquadra, tappaFinal);
                    if (isOk) {
                        editor.putInt("Tappa", tappaFinal + 1);
                        editor.putInt("Squadra", idSquadra);
                        editor.commit();
                        Intent intent = getIntent();
                        intent.putExtra("Squadra", idSquadra);
                        intent.putExtra("Tappa", tappaFinal + 1);
                        finish();
                        startActivity(intent);
                    } else {
                        text.setText("Li mortacci tua, hai sbagliato codice");
                        text.setTextColor(Color.RED);
                    }
                }

            });

            if (tappa == 4) {
                text.setText(getString(R.string.tappa_titolo_finale) + "\n" + getString(R.string.daje));
                ok.setVisibility(View.GONE);
                nomeSquadra.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                editor.clear().commit();
                Intent intent = new Intent(this, MainActivity.class);
                finish();
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getIndovinello(int squadra, int tappa) {
        Log.v("AFRICAQUEST", "tappa=" + tappa);
        Log.v("AFRICAQUEST", "squadra=" + squadra);
        String risultato;
        switch (squadra) {
            case 0: //Etiopia
                switch (tappa) {
                    case 0:
                        risultato = getString(R.string.tappa_piazza_minerva);
                        return risultato;
                    case 1:
                        risultato = getString(R.string.tappa_ospedale_bambole);
                        return risultato;
                    case 2:
                        risultato = getString(R.string.tappa_piazza_pietra);
                        return risultato;
                    case 3:
                        risultato = getString(R.string.tappa_statua_babbuino);
                        return risultato;
                    case 4:
                        risultato = getString(R.string.tappa_auditorium);
                        return risultato;
                    default:
                        break;
                }
            case 1: //Uganda
                switch (tappa) {
                    case 0:
                        risultato = getString(R.string.tappa_statua_babbuino);
                        return risultato;
                    case 1:
                        risultato = getString(R.string.tappa_piazza_pietra);
                        return risultato;
                    case 2:
                        risultato = getString(R.string.tappa_ospedale_bambole);
                        return risultato;
                    case 3:
                        risultato = getString(R.string.tappa_piazza_minerva);
                        return risultato;
                    case 4:
                        risultato = getString(R.string.tappa_auditorium);
                        return risultato;
                    default:
                        break;
                }
            case 2: //Kenya
                switch (tappa) {
                    case 0:
                        risultato = getString(R.string.tappa_ospedale_bambole);
                        return risultato;
                    case 1:
                        risultato = getString(R.string.tappa_piazza_minerva);
                        return risultato;
                    case 2:
                        risultato = getString(R.string.tappa_statua_babbuino);
                        return risultato;
                    case 3:
                        risultato = getString(R.string.tappa_piazza_pietra);
                        return risultato;
                    case 4:
                        risultato = getString(R.string.tappa_auditorium);
                        return risultato;
                    default:
                        break;
                }
            case 3: //Mozambico
                switch (tappa) {
                    case 0:
                        risultato = getString(R.string.tappa_piazza_pietra);
                        return risultato;
                    case 1:
                        risultato = getString(R.string.tappa_statua_babbuino);
                        return risultato;
                    case 2:
                        risultato = getString(R.string.tappa_piazza_minerva);
                        return risultato;
                    case 3:
                        risultato = getString(R.string.tappa_ospedale_bambole);
                        return risultato;
                    case 4:
                        risultato = getString(R.string.tappa_auditorium);
                        return risultato;
                    default:
                        break;
                }
            default:
                break;
        }
        return null;
    }

    private boolean checkCodice(String codice, int squadra, int tappa) {
        switch (squadra) {
            case 0: //Etiopia
                if (tappa == 0 && codice.equalsIgnoreCase(getString(R.string.codice_minerva)))
                    return true;
                else if (tappa == 1 && codice.equalsIgnoreCase(getString(R.string.codice_bambole)))
                    return true;
                else if (tappa == 2 && codice.equalsIgnoreCase(getString(R.string.codice_pietra)))
                    return true;
                else if (tappa == 3 && codice.equalsIgnoreCase(getString(R.string.codice_statua_babbuino)))
                    return true;
                else
                    return false;
            case 1: //Uganda
                if (tappa == 0 && codice.equalsIgnoreCase(getString(R.string.codice_statua_babbuino)))
                    return true;
                else if (tappa == 1 && codice.equalsIgnoreCase(getString(R.string.codice_pietra)))
                    return true;
                else if (tappa == 2 && codice.equalsIgnoreCase(getString(R.string.codice_bambole)))
                    return true;
                else if (tappa == 3 && codice.equalsIgnoreCase(getString(R.string.codice_minerva)))
                    return true;
                else
                    return false;
            case 2: //Kenya
                if (tappa == 0 && codice.equalsIgnoreCase(getString(R.string.codice_bambole)))
                    return true;
                else if (tappa == 1 && codice.equalsIgnoreCase(getString(R.string.codice_minerva)))
                    return true;
                else if (tappa == 2 && codice.equalsIgnoreCase(getString(R.string.codice_statua_babbuino)))
                    return true;
                else if (tappa == 3 && codice.equalsIgnoreCase(getString(R.string.codice_pietra)))
                    return true;
                else
                    return false;
            case 3: //Mozambico
                if (tappa == 0 && codice.equalsIgnoreCase(getString(R.string.codice_pietra)))
                    return true;
                else if (tappa == 1 && codice.equalsIgnoreCase(getString(R.string.codice_statua_babbuino)))
                    return true;
                else if (tappa == 2 && codice.equalsIgnoreCase(getString(R.string.codice_minerva)))
                    return true;
                else if (tappa == 3 && codice.equalsIgnoreCase(getString(R.string.codice_bambole)))
                    return true;
                else
                    return false;

            default:
                return false;
        }

    }
}
