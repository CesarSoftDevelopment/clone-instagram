package cesarapps.newapp.com.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;
import cesarapps.newapp.com.instagram.helper.UsuarioFirebase;
import cesarapps.newapp.com.instagram.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Button botaoCadastrar;
    private ProgressBar progressBar;
    private Usuario usuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        //INICIALIZAÇÃO
        inicializarComponentes();

        //CADASTRO
        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //RECUPERAR OS DADOS E CONVERTER PARA STRING
                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                //VALIDAR OS COMPONENTES SE FORAM PREENCHIDOS
                if(!textoNome.isEmpty()) {
                    if(!textoEmail.isEmpty()) {
                        if(!textoSenha.isEmpty()) {
                           usuario = new Usuario();
                           usuario.setNome(textoNome);
                           usuario.setEmail(textoEmail);
                           usuario.setSenha(textoSenha);
                            cadastrar(usuario);
                        }else {
                            Toast.makeText(CadastroActivity.this, "preencha a senha", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(CadastroActivity.this, "Preencha o email", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(CadastroActivity.this, "Preencha o nome", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
    //MÉTODO RESPONSÁVEL POR CADASTRAR O USUÁRIO, EMAIL E SENHA
    public void cadastrar(final Usuario usuario) {
        //MOSTRAR A PROGRESSBAR
        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()

                //TRATAR OS ERROS!
        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            try {
                                progressBar.setVisibility(View.GONE);

                                //SALVAR DADOS NO FIREBASE
                                String idUsuario = task.getResult().getUser().getUid();
                                usuario.setId(idUsuario);
                                usuario.salvar();

                                //SALVAR DADOS NO PROFILE DO FIREBASE
                                UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());


                                Toast.makeText(CadastroActivity.this, "Cadastro com sucesso",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(CadastroActivity.this, MainActivity.class));
                                finish();

                            }catch (Exception e) {
                                e.printStackTrace();
                            }


                        }else {

                            progressBar.setVisibility(View.GONE);

                            String erroExcecao = "";
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e) {
                                erroExcecao = "Digite uma senha mais forte com letras e números";
                            }catch (FirebaseAuthInvalidCredentialsException e) {
                                erroExcecao = "Por favor, digite um e-mail válido";
                            }catch (FirebaseAuthUserCollisionException e) {
                                erroExcecao = "Esta conta já foi cadastrada";
                            }catch (Exception e) {
                                erroExcecao = "ao cadastrar o usuário: " + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroActivity.this, "Erro: " + erroExcecao,
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }
        );
    }

    //MÉTODO RESPONSÁVEL POR INICIALIZAR OS COMPONENTES DA INTERFACE
    public void inicializarComponentes()  {
        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
        botaoCadastrar = findViewById(R.id.buttonEntrar);
        progressBar = findViewById(R.id.progressCadastro);

        campoNome.requestFocus();

    }
}
