package cesarapps.newapp.com.instagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;
import cesarapps.newapp.com.instagram.helper.Permissao;
import cesarapps.newapp.com.instagram.helper.UsuarioFirebase;
import cesarapps.newapp.com.instagram.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private CircleImageView imagemEditarPerfil;
    private TextView textAlterarFoto;
    private TextInputEditText editNomePerfil, editNomeEmail;
    private Button buttonSalvarAlteracoes;
    private Usuario usuarioLogado;
    //REQUEST CODE
    private static final int SECAO_GALERIA = 200;
    private StorageReference storageRef;
    private String identificadorUsuario;

    private String[] permissoesNecessarias = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //VALIDAR PERMISSÕES
        Permissao.validaPermissoes(permissoesNecessarias, this, 1);

        //CONFIGURAÇÕES INICIAIS
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();


        //CONFIGURAR TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Editar perfil");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //INICIALIZAR COMPONENTES
        inicializarComponentes();

        //RECUPERAR DADOS DO USUÁRIO
        FirebaseUser usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        editNomePerfil.setText(usuarioPerfil.getDisplayName().toUpperCase());
        editNomeEmail.setText(usuarioPerfil.getEmail());

        Uri url = usuarioPerfil.getPhotoUrl();
        if (url != null) {
            Glide.with(EditarPerfilActivity.this)
                    .load(url)
                    .into(imagemEditarPerfil);
        }else {
            imagemEditarPerfil.setImageResource(R.drawable.avatar);
        }

        //SALVAR ALTERAÇÕES DO NOME
        buttonSalvarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RECUPERAR O NOME DIGITADO!
                String nomeAtualizado = editNomePerfil.getText().toString();

                //ATUALIZAR NOME NO PERFIL DO FIREBASE
                UsuarioFirebase.atualizarNomeUsuario(nomeAtualizado); //ATUALIZAR NOME DO USUÁRIO LOGADO


                //ATUALIZAR NOME DIRETO NO BANCO DE DADOS
                usuarioLogado.setNome(nomeAtualizado);
                usuarioLogado.atualizar();

                Toast.makeText(EditarPerfilActivity.this,
                        "Dados alterados com sucesso",
                        Toast.LENGTH_SHORT).show();


            }
        });

        //Alterar foto do usuário
        textAlterarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i,SECAO_GALERIA );

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {
                //SELEÇÃO APENAS DA GALERIA
                switch (requestCode){
                    case SECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;

                }

                //CASO TENHA SIDO ESCOLHIDO UMA IMAGEM
                if (imagem != null) {
                    //CONFIGURAR IMAGEM NA TELA DO USUÁRIO
                    imagemEditarPerfil.setImageBitmap(imagem);

                    //RECUPERAR DADOS DA IMAGEM PARA O FIREBASE
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //SALVAR IMAGEM NO FIREBASE
                    final StorageReference imagemRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario + ".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Erro ao fazer o upload da imagem",
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
                                    atualizarFotoUsuario(url);
                                }
                            });

                            Toast.makeText(EditarPerfilActivity.this,
                                    "Sucesso ao fazer o upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void atualizarFotoUsuario(Uri url) {

        //ATUALIZAR FOTO NO PERFIL
        UsuarioFirebase.atualizarFotoUsuario(url);

        //ATUALIZAR FOTO NO FIREBASE
        usuarioLogado.setCaminhoFoto(url.toString());
        usuarioLogado.atualizar();

        Toast.makeText(EditarPerfilActivity.this, "Sua foto foi atualizada",
                Toast.LENGTH_SHORT).show();
    }

    public void inicializarComponentes() {
        imagemEditarPerfil = findViewById(R.id.imageEditarPerfil);
        textAlterarFoto = findViewById(R.id.alterarFoto);
        editNomePerfil = findViewById(R.id.editNomePerfil);
        editNomeEmail = findViewById(R.id.editEmailPerfil);
        buttonSalvarAlteracoes = findViewById(R.id.buttonSalvarAlteracoes);
        editNomeEmail.setFocusable(false);


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return false;
    }
}
