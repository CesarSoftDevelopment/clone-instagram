package cesarapps.newapp.com.instagram.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/*
   @classe responsável por pegar as instancias do firebase como:
   firebase-database, firebase-auth
    */
public class ConfiguracaoFirebase {

    private static DatabaseReference referenciaFirebase;
    private static FirebaseAuth referenciaAutenticacao;
    private static StorageReference storage;

    //MÉTODO PARA RETORNAR A INSTÂNCIA DO DATABASE
    public static DatabaseReference getFirebase() {
        if(referenciaFirebase == null) {
            referenciaFirebase = FirebaseDatabase.getInstance().getReference();
        }
        return referenciaFirebase;
    }

    //MÉTODO PARA RETORNAR A INSTÂNCIA DO FIREBASEAUTH
    public static FirebaseAuth getFirebaseAutenticacao() {
        if (referenciaAutenticacao == null) {
            referenciaAutenticacao = FirebaseAuth.getInstance();
        }
        return referenciaAutenticacao;
    }

    public static StorageReference getFirebaseStorage() {
        if(storage == null) {
            storage = FirebaseStorage.getInstance().getReference();
        }
        return storage;
    }



}
