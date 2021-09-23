package cesarapps.newapp.com.instagram.helper;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import cesarapps.newapp.com.instagram.model.Usuario;

public class UsuarioFirebase {

    //MÉTODO PARA PEGAR USUÁRIO ATUAL(LOGADO)
    public static FirebaseUser getUsuarioAtual() {
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();

    }

    public static String getIdentificadorUsuario() {
        return getUsuarioAtual().getUid();
    }
    public static void atualizarNomeUsuario(String nome) {

        try{
            //USUÁRIO LOGADO NO APP
            FirebaseUser usuarioLogado = getUsuarioAtual();

            //CONFIGURAR OBJETO PARA ALTERAÇÃO DO PERFIL
            UserProfileChangeRequest profile = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(nome)
                    .build();
            usuarioLogado.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()) { //CASO NÃO TENHA DADO CERTO

                    }
                }
            });

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void atualizarFotoUsuario(Uri url) {

        try{
            //USUÁRIO LOGADO NO APP
            FirebaseUser usuarioLogado = getUsuarioAtual();

            //CONFIGURAR OBJETO PARA ALTERAÇÃO DO PERFIL
            UserProfileChangeRequest profile = new UserProfileChangeRequest
                    .Builder()
                    .setPhotoUri(url)
                    .build();
            usuarioLogado.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()) { //CASO NÃO TENHA DADO CERTO

                    }
                }
            });

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Usuario getDadosUsuarioLogado() {

        FirebaseUser firebaseUser = getUsuarioAtual();

        Usuario usuario = new Usuario();
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setNome(firebaseUser.getDisplayName());
        usuario.setId(firebaseUser.getUid());

        if(firebaseUser.getPhotoUrl() == null) {
            usuario.setCaminhoFoto("");
        }else {
            usuario.setCaminhoFoto(firebaseUser.getPhotoUrl().toString());
        }

        return usuario;
    }


}
