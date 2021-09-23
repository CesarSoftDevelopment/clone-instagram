package cesarapps.newapp.com.instagram.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    //Este método tem como objetivo verificas as permissões que o usuário tem e solicitar as permissões
    public static boolean validaPermissoes(String[] permissoes, Activity activity, int requestCode) {

        if(Build.VERSION.SDK_INT >= 23) {

            List<String> listaPermissoes = new ArrayList<String>();

            /*
            Percorrer as permissões uma a uma para se já tem a permissão liberada.
             */
            for(String permissao: permissoes) {

                //verifica se você tem a permissão necessária
                Boolean validaPermissao = ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
                if(!validaPermissao) listaPermissoes.add(permissao);
            }

            /*Caso a lista esteja vazia, não é necessário solicitar permissão*/
            if(listaPermissoes.isEmpty()) return true;

            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissoes);

            //Solicitar permissão -> código próprio de onde foi requisitado essas permissões
            ActivityCompat.requestPermissions(activity, novasPermissoes, 1);

        }
        return true;
    }
}
