package bms.requisicaohttp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

/**
 * Created by brunosilva on 29/07/16.
 */
public final class Util {

    /**
     * Faz a requisição na web no endereço informado que deverá retornar uma String JSON.
     *
     * @param url
     *          Endereço HTTP de requisição para obtenção da string JSON.
     * @return A String referente ao JSON requisitado.
     */
    public static String getJSONString(String url) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = null;
        InputStream inputStream = null;
        String aux;
        try{
            URL mURL = new URL(url);
            Log.i("TAG", "URL: " + url);
            httpURLConnection = (HttpURLConnection) mURL.openConnection();
            //Parametros para comunicação: setRequestMethod("GET"); Por default já é GET.
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");
            //Implementar: Aqui pegará a ETag gravada....
            //httpURLConnection.setRequestProperty("If-None-Match", "71b3dd4b68a5a59e6ac8400c3cab941b09c737be");
            Log.i("TAG","Requisição por .: "+httpURLConnection.getRequestMethod());
            Log.i("TAG","getDoInput = "+httpURLConnection.getDoInput());
            Log.i("TAG","getDoOutput = "+httpURLConnection.getDoOutput());
            Log.i("TAG","getHeaderFields.: "+httpURLConnection.getHeaderFields());
            switch(httpURLConnection.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    //Para o retorno da primeira conexao, caso use eTag
                    httpURLConnection.connect();
                    Log.i("TAG", "Conectado ao Servidor.");
                    inputStream = httpURLConnection.getInputStream();
                    Log.i("TAG", "InputStream obtido.");

                    if(httpURLConnection.getContentEncoding().equals("gzip")) {
                        Log.i("TAG", "Descompactando dados para leitura do servidor.");
                        /*
                        1. O inputStream traz os bytes compactados, devido a solicitação Accept-Encoding = gzip
                        2. O GZIPInputStream descompacta esses bytes recebidos
                        3. O InputStreamReader ler esses bytes já descompactados e os converte para caracteres no formato  UTF-8
                        4. O BufferedReader é alimentado com os caracteres
                         */
                        bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream), "UTF-8"));
                    }else{
                        Log.i("TAG", "Lendo dados do servidor.");
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    }

                    stringBuilder = new StringBuilder();
                    while ((aux = bufferedReader.readLine()) != null){
                        stringBuilder.append(aux);
                    }
                    Log.i("TAG", "Dados lidos do servidor.");
                    //Aqui compacta e quarda a requisicao, e a ETag.
                    break;
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    //Caso não haja modificações nos dados da requisicao trabalhar com cache.
                    //Aqui descompacta e ler
                    break;
            }
        }catch (IOException e) { e.printStackTrace(); }
        finally {
            try {
                if(bufferedReader != null) bufferedReader.close();
                if(inputStream != null) inputStream.close();
            } catch (IOException e) { e.printStackTrace(); }

            if(httpURLConnection != null) httpURLConnection.disconnect();
            Log.i("TAG", "Conexao fechada.");
        }
        aux = stringBuilder != null ? stringBuilder.toString() : null;
        Log.i("TAG", "String JSON: " + aux);
        return aux;
    }




    /**
     * @author brunomeloesilva
     *
     * @param tipoAlgoritmo
     *            Informe o tipo do algoritmo, podendo ser um MD5, "MD4",
     *            "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512",
     *            "RIPEMD128", "RIPEMD160", "RIPEMD256", "RIPEMD320", "Tiger",
     *            "DHA256" e "FORK256".
     * @param mensagem
     *            String que deverá ser criptografada conforme o tipoAlgoritmo
     *            informado
     * @return Uma representacao hexadecimal em string do retorno da execução do
     *         algotimo message digest, conforme tipoAlgoritmo informado
     *
     * @see Fontes:
     * 	 https://pt.wikipedia.org/wiki/MD5
     * , http://www.devmedia.com.br/como-funciona-a-criptografia-hash-em-java/31139
     */
    public static final String getMensagemCodificada(String tipoAlgoritmo, String mensagem) {
        StringBuilder hexString = null;
        try {
            // Obtém uma instancia do algoritmo de codificação informada (do tipo MD5,SSH1, etc..)
            MessageDigest algoritmo = MessageDigest.getInstance(tipoAlgoritmo);
            // Obtém o valor Hash (ex.: o valor criptografado conforme tipo setado acima)
            byte[] messageDigest = algoritmo.digest(mensagem.getBytes("UTF-8"));
            // Transformando o vetor de bytes (messageDigest) em um vetor de
            // hexadecimais (hexadecimal) representado numa string
            hexString = new StringBuilder();
            for (byte mbyte : messageDigest) {
                //Converte cada byte em hexadecimal com 2 casas completadas com zeros a esquerda
                hexString.append(String.format("%02x", mbyte));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }






    public static final String getURLMontada(String UrlBase) {
        //hora em milesimos de segundos
        Long hora = System.currentTimeMillis();
        String hashKey = getMensagemCodificada("MD5", hora + Constantes.CHAVE_PRIVADA + Constantes.CHAVE_PUBLICA);

        String urlMontada = UrlBase
                +"apikey=" + Constantes.CHAVE_PUBLICA
                +"&ts=" + hora.toString()
                +"&hash=" + hashKey;

        return urlMontada;
    }





}

