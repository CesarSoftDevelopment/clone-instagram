package cesarapps.newapp.com.instagram.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import cesarapps.newapp.com.instagram.R;
import cesarapps.newapp.com.instagram.model.Comentario;
import de.hdodenhof.circleimageview.CircleImageView;
//1º
public class AdapterComentario extends RecyclerView.Adapter<AdapterComentario.MyViewHolder> {

    private List<Comentario> listaComentarios;
    private Context context;

    public AdapterComentario(List<Comentario> listaComentarios, Context context) {
        this.listaComentarios = listaComentarios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentario,
                parent, false);
        return new AdapterComentario.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Comentario comentario = listaComentarios.get(position);
        holder.nomeUsuario.setText(comentario.getNomeUsuario());
        holder.comentario.setText(comentario.getComentario());
        Glide.with(context).load(comentario.getCaminhoFoto()).into(holder.imagemPerfil);

    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    //2º
    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imagemPerfil;
        TextView nomeUsuario, comentario;


    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        imagemPerfil = itemView.findViewById(R.id.imageFotoComentario);
        nomeUsuario = itemView.findViewById(R.id.textNomeComentario);
        comentario = itemView.findViewById(R.id.textComentario);
    }
}
}
