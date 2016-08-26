package bms.requisicaohttp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //noinspection ConstantConditions
        findViewById(R.id.btnFazReq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Requisicao().execute(Util.getURLMontada(Constantes.URL_BASE + Constantes.URI_PERSONAGEM));
            }
        });
    }

    private class Requisicao extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return Util.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
