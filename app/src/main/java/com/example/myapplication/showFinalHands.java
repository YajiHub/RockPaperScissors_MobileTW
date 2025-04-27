    package com.example.myapplication;

    import android.content.Intent;
    import android.os.Bundle;
    import android.os.Handler;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    public class showFinalHands extends AppCompatActivity {
        static Player playerUser;
        static Computer_Player computerUser;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_show_final_hands);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });


//            Intent intent = getIntent();
            Player player = (Player) getIntent().getSerializableExtra("player");
            Computer_Player computer = (Computer_Player) getIntent().getSerializableExtra("computer");
            Russian_Roulette russianRoulette = (Russian_Roulette) getIntent().getSerializableExtra("russian_roulette");

            playerUser = player;
            computerUser = computer;

            switch(player.getFinalHand().getHandNumber()){
                case 1:
                    ((TextView)findViewById(R.id.playerFinal)).setText("Rock");
                    ((ImageView)findViewById(R.id.finalPlayerCard)).setImageResource(R.drawable.rock);
                    break;
                case 2:
                    ((TextView)findViewById(R.id.playerFinal)).setText("Paper");
                    ((ImageView)findViewById(R.id.finalPlayerCard)).setImageResource(R.drawable.paper);
                    break;
                case 3:
                    ((TextView)findViewById(R.id.playerFinal)).setText("Scissor");
                    ((ImageView)findViewById(R.id.finalPlayerCard)).setImageResource(R.drawable.scissor);
                    break;
            }

            switch(computer.getFinalHand().getHandNumber()){
                case 1:
                    ((TextView)findViewById(R.id.enemyFinal)).setText("Rock");
                    ((ImageView)findViewById(R.id.finalEnemyChosen)).setImageResource(R.drawable.rock);
                    break;
                case 2:
                    ((TextView)findViewById(R.id.enemyFinal)).setText("Paper");
                    ((ImageView)findViewById(R.id.finalEnemyChosen)).setImageResource(R.drawable.paper);
                    break;
                case 3:
                    ((TextView)findViewById(R.id.enemyFinal)).setText("Scissor");
                    ((ImageView)findViewById(R.id.finalEnemyChosen)).setImageResource(R.drawable.scissor);
                    break;
            }


            new Handler().postDelayed(() -> {
                int playerFinalHand = playerUser.getFinalHand().getHandNumber();
                int computerFinalHand = computerUser.getFinalHand().getHandNumber();

                int result = (playerFinalHand - computerFinalHand + 3) % 3;

                if (result == 0) {
                    ((TextView) findViewById(R.id.round)).setText(R.string.tie);

                    if (playerUser.isPlayerPlayingRussianRoulette) {
                        ((TextView) findViewById(R.id.TieWarning)).setVisibility(View.VISIBLE);

                        // Restart the same round after short delay
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(showFinalHands.this, NormalMode.class);
                            intent.putExtra("player", playerUser);
                            intent.putExtra("computer", computerUser);
                            intent.putExtra("russian_roulette", russianRoulette);
                            startActivity(intent);
                            finish();
                        }, 1300);
                    } else {
                        // Not in RR, just show buttons
                        showEndButtons();
                    }

                } else {
                    // Win or Lose
                    boolean didPlayerWin = (result == 1);
                    playerUser.didPlayerWin = didPlayerWin;

                    ((TextView) findViewById(R.id.round)).setText(didPlayerWin ? R.string.win : R.string.lose);

                    if (playerUser.isPlayerPlayingRussianRoulette) {
                        new Handler().postDelayed(() -> {
                            if (russianRoulette.fireRound()) {
                                Class<?> targetActivity = russianRoulette.isShotDeadly() ? LiveFireGun.class : blankFireGun.class;
                                Intent intent = new Intent(showFinalHands.this, targetActivity);
                                intent.putExtra("didWin", playerUser.didPlayerWin);
                                intent.putExtra("player", playerUser);
                                intent.putExtra("computer", computerUser);
                                intent.putExtra("russian_roulette", russianRoulette);
                                startActivity(intent);
                                finish();
                            } else {
                                // Handle no rounds left if needed (optional safety)
                                ((TextView) findViewById(R.id.round)).setText("No rounds left.");
                            }
                        }, 1300);
                    } else {
                        // Not playing RR, just show buttons
                        showEndButtons();
                    }
                }
            }, 1300);


//            new Handler().postDelayed(new Runnable(){
//                @Override
//                public void run() {
//                    //code
//                    int playerFinalHand = playerUser.getFinalHand().getHandNumber();
//                    int computerFinalHand = computerUser.getFinalHand().getHandNumber();
//
//                    int result = (playerFinalHand - computerFinalHand + 3) % 3;
//                    if(result == 0){
//                        ((TextView) findViewById(R.id.round)).setText(R.string.tie);
//
//                        //if its a tie while playing russian roulette then the geme will restart without increasing the round number
//                        if(playerUser.isPlayerPlayingRussianRoulette){
//                            ((TextView) findViewById(R.id.TieWarning)).setVisibility(TextView.VISIBLE);
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run(){
//                                    Intent intent = new Intent(showFinalHands.this, NormalMode.class);
//                                    intent.putExtra("player", playerUser);
//                                    intent.putExtra("computer", computerUser);
//                                    intent.putExtra("russian_roulette", russianRoulette);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            },1300);
//                        }
//
//                    }else if(result == 1){
//                        ((TextView) findViewById(R.id.round)).setText(R.string.win);
//                        playerUser.didPlayerWin = true;
//                    }else{
//                        ((TextView) findViewById(R.id.round)).setText(R.string.lose);
//                        playerUser.didPlayerWin = false;
//                    }
//
//
//
//                    if(!playerUser.isPlayerPlayingRussianRoulette){
//                        ((Button)findViewById(R.id.tryAgain)).setVisibility(Button.VISIBLE);
//                        ((Button)findViewById(R.id.playRR)).setVisibility(Button.VISIBLE);
//                        ((Button)findViewById(R.id.exit)).setVisibility(Button.VISIBLE);
//                    }else{
//                        if(russianRoulette.fireRound()){
//                            if(russianRoulette.isShotDeadly()){
//    //                            if(playerUser.didPlayerWin){
//    //                                //player wins the round
//    //                                //the computer is killed
//    //                            }else{
//    //                                //computer wins the round
//    //                                //the player dies
//    //                            }
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run(){
//                                        Intent intent = new Intent(showFinalHands.this, LiveFireGun.class);
//                                        intent.putExtra("player", playerUser);
//                                        intent.putExtra("computer", computerUser);
//                                        intent.putExtra("russian_roulette", russianRoulette);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                },1000);
//                            }else{
//                                //the shot is a blank and does not kill anyone round
//                                //continues the round
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run(){
//                                        Intent intent = new Intent(showFinalHands.this, blankFireGun.class);
//                                        intent.putExtra("player", playerUser);
//                                        intent.putExtra("computer", computerUser);
//                                        intent.putExtra("russian_roulette", russianRoulette);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                },1300);
//                            }
//                        }//else is for round is null pointer meaning no rounds left
//
//                    }
//
//                }
//            },1300);
        }


        private void showEndButtons() {
            findViewById(R.id.tryAgain).setVisibility(View.VISIBLE);
            findViewById(R.id.playRR).setVisibility(View.VISIBLE);
            findViewById(R.id.exit).setVisibility(View.VISIBLE);
        }


        public void exit(View view){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        public void tryAgain(View view){
            Intent intent = new Intent(this, LoadingScreenActivity.class);
            playerUser = null;
            computerUser = null;
            Player player = new Player();
            player.isPlayerPlayingRussianRoulette = false;
            intent.putExtra("player", player);
            startActivity(intent);
            finish();
        }

        public void playRR(View view){
            Intent intent = new Intent(this, RussianModeLoadingScreen.class);
    //        intent.putExtra("mode", "russian");
            Player player = new Player();
            player.isPlayerPlayingRussianRoulette = true;
            intent.putExtra("player", player);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }


        /*
        int result = (A - B + 3) % 3;

        if (result == 0) {
            // Tie
        } else if (result == 1) {
            // A wins
        } else {
            // B wins
        }
         */
    }