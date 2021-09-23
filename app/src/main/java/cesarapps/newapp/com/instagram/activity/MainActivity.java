package cesarapps.newapp.com.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.fragment.FeedFragment;
import cesarapps.newapp.com.instagram.fragment.PerfilFragment;
import cesarapps.newapp.com.instagram.fragment.PesquisaFragment;
import cesarapps.newapp.com.instagram.fragment.PostagemFragment;
import cesarapps.newapp.com.instagram.helper.ConfiguracaoFirebase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //CONFIGURAR OBJETOS PARA O FIREBASE
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //CONFIGURAR BOTTON NAVIGATION VIEW
        configurarBottomNavigationView(); //->CHAMANDO MÉTODO ABAIXO
        FragmentManager fragmentManager = getSupportFragmentManager(); //CHAMANDO A CLASSE
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();//VISUALIZAÇÃO

        //CONFIGURAR TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Instagram");
        setSupportActionBar(toolbar);
    }

    //MÉTODO RESPONSÁVEL POR CRIAR A BOTTOM NAVEGATION
    private void configurarBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
       habilitarNavegacao(bottomNavigationView); //->CHAMANDO O MÉTODO ABAIXO
}

    //MÉTODO PARA CRIAR OS FRAGMENTOS DENTRO DO BOTTOM NAVEGATION
    public void habilitarNavegacao(BottomNavigationView view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //CRIANDO OS LAYOUTS/ITENS
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.ic_home:
                        fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();
                        return true;
                    case R.id.ic_pesquisa:
                        fragmentTransaction.replace(R.id.viewPager, new PesquisaFragment()).commit();
                        return true;
                    case R.id.ic_postagem:
                        fragmentTransaction.replace(R.id.viewPager, new PostagemFragment()).commit();
                        return true;
                    case R.id.ic_perfil:
                        fragmentTransaction.replace(R.id.viewPager, new PerfilFragment()).commit();
                        return true;
                }

                return false; //-> CASO NÃO SEJA CARREGADO NENHUM FRAGMENTO!
            }
        });
    }




    //MÉTODO PARA CRIAR MENUS -> PEGAR O MENU!
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //TRATAMENTOS PARA OS MENUS
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sair:
                deslogarUsuario();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //MÉTODO PARA PASSAR DENTRO DO onOptionItemSelelect PARA DESLOGAR
    private void deslogarUsuario() {

        try {
            autenticacao.signOut();

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
