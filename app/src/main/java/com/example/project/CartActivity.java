package com.example.project;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.Model.Cart;
import com.example.project.Prevalent.Prevalent;
import com.example.project.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;

public class CartActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProcessBtn;
    private TextView txtTotalAmount ,txtmsg1;

    private int overTotalPrice =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        recyclerView=findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessBtn=(Button)findViewById(R.id.next_process_button);
        txtTotalAmount=(TextView)findViewById(R.id.total_price);
        txtmsg1=(TextView)findViewById(R.id.msg1);



        NextProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)

            {
                txtTotalAmount.setText("Total price =Rs" + String.valueOf(overTotalPrice));




                Intent intent=new Intent(CartActivity.this,ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(overTotalPrice));
                startActivity(intent);
                finish();
            }
        });



    }

    @Override
    protected void onStart() {

        super.onStart();
        CheckorderState();

        final DatabaseReference cartListRef= FirebaseDatabase.getInstance("https://bookmart-b2ad7.firebaseio.com/").getReference().child("Cart List");

        FirebaseRecyclerOptions<Cart> options=new FirebaseRecyclerOptions.Builder<Cart>().setQuery(cartListRef.child("User View").child(Prevalent.CurrentOnlineUser.getPhone()).child("Products"),Cart.class).build();


        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter
                = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model)
            {
                holder.txtProductQuantity.setText(model.getQuantity());
                holder.txtProductPrice.setText(model.getPrice());
                holder.txtProductName.setText(model.getPname());

                int Onetypeproductprice =(((Integer.valueOf(model.getPrice())))* Integer.valueOf(model.getQuantity()));
                overTotalPrice= overTotalPrice+ Onetypeproductprice;

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        CharSequence options[]=new CharSequence[] {
                                "Edit",
                                "Remove"


                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(i==0)
                                {
                                    Intent intent= new Intent(CartActivity.this,ProductdetailsActivity.class);
                                    intent.putExtra("pid",model.getPid());
                                    startActivity(intent);
                                }
                                if(i==1){
                                    cartListRef.child("User View")
                                           // .child(Prevalent.CurrentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(model.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(CartActivity.this,"item removed",Toast.LENGTH_SHORT);

                                                        Intent intent= new Intent(CartActivity.this,HomeActivity.class);
                                                        startActivity(intent);


                                                    }
                                                }
                                            });



                                }
                            }
                        });
                        builder.show();

                    }
                });

            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout,parent,false);
                CartViewHolder holder=new CartViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void CheckorderState()
    {
        DatabaseReference ordersRef;
        ordersRef= FirebaseDatabase.getInstance("https://bookmart-b2ad7.firebaseio.com/").getReference().child("Orders").child(Prevalent.CurrentOnlineUser.getPhone());

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String shippingstate=dataSnapshot.child("state").getValue().toString();
                    String Username=dataSnapshot.child("name").getValue().toString();

                    if(shippingstate.equals("shipped"))
                    {
                        txtTotalAmount.setText("Dear +Username+ \nOrder is shipped successfully");
                        recyclerView.setVisibility(View.GONE);
                        txtmsg1.setVisibility(View.VISIBLE);

                        NextProcessBtn.setVisibility(View.GONE);

                        Toast.makeText(CartActivity.this,"You can purchase more items later",Toast.LENGTH_SHORT).show();


                    }
                    else if(shippingstate.equals("Not shipped"))

                    {
                        txtTotalAmount.setText("Shipping State= Not shipped");
                        recyclerView.setVisibility(View.GONE);
                        txtmsg1.setVisibility(View.VISIBLE);

                        NextProcessBtn.setVisibility(View.GONE);

                        Toast.makeText(CartActivity.this,"You can purchase more items",Toast.LENGTH_SHORT).show();


                    }

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


    }





}

