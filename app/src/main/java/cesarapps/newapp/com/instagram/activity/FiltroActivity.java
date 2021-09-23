package cesarapps.newapp.com.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;
import cesarapps.newapp.com.instagram.helper.UsuarioFirebase;
import cesarapps.newapp.com.instagram.model.Postagem;
import cesarapps.newapp.com.instagram.model.Usuario;

public class FiltroActivity extends AppCompatActivity {

    private ImageView imageFotoEscolhida;
    private Bitmap imagem;
    private String idUsuarioLogado;
    private TextInputEditText textDescricaoFiltro;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private Usuario usuarioLogado;
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private DataSnapshot seguidoresSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        //CONFIGURAÇÕES INICIAIS
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");
        firebaseRef = ConfiguracaoFirebase.getFirebase();

        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);


        //RECUPERAR DADOS PARA UMA NOVA POSTAGEM
        recuperarDadosPostagem();



        //CONFIGURAR TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Filtros");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);


        //RECUPERA A IMAGEM ESCOLHIDA PELO USUÁRIO
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            byte[] dadosImagem = bundle.getByteArray("fotoEscolhida");
            imagem = BitmapFactory.decodeByteArray(dadosImagem, 0, dadosImagem.length);
            imageFotoEscolhida.setImageBitmap(imagem);

        }
    }


    private void recuperarDadosPostagem() {
        abrirDialogCarregamento("Carregando dados, aguarde");

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //RECUPERAR DADOS DE USUÁRIO LOGADO
                        usuarioLogado = snapshot.getValue(Usuario.class);

                        DatabaseReference seguidoresRef = firebaseRef
                                .child("seguidores")
                                .child(idUsuarioLogado);
                        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                seguidoresSnapshot = snapshot;
                                dialog.cancel();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });








                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


    }

    private void abrirDialogCarregamento(String titulo) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(titulo);//->passado como parâmetro
        alert.setCancelable(false);
        alert.setView(R.layout.carregamento);

        dialog = alert.create();
        dialog.show();


    }

    private void publicarPostagem() {
        abrirDialogCarregamento("Salvando Postagem");
        final Postagem postagem = new Postagem();
        postagem.setIdUsuario(idUsuarioLogado);
        postagem.setDescricao(textDescricaoFiltro.getText().toString());

        //Recuperarar dados da imagem para o firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dadosImagem = baos.toByteArray();

        //SALVAR IMAGEM NO FIREBASE
        StorageReference storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        final StorageReference imagemRef = storageRef
                .child("imagens")
                .child("postagens")
                .child(postagem.getId() + ".jpeg");

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FiltroActivity.this,
                        "Erro ao salvar a imagem",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //RECUPERAR LOCAL DA FOTO
                imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri url = task.getResult();
                        postagem.setCaminhoFoto(url.toString());

                        //ATUALIZAR A QUANTIDADE DE POSTAGENS
                        int qtdPostagem = usuarioLogado.getPostagens() + 1;
                        usuarioLogado.setPostagens(qtdPostagem);
                        usuarioLogado.atualizarQtdPostagem();

                        //Salvar Postagem
                        if(postagem.salvar(seguidoresSnapshot)) {
                            Toast.makeText(FiltroActivity.this,
                                    "Sucesso ao salvar postagem",
                                    Toast.LENGTH_LONG).show();
                            dialog.cancel();
                            finish();

                        }


                    }
                });


            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtro, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.ic_salvar_postagem:
                publicarPostagem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
