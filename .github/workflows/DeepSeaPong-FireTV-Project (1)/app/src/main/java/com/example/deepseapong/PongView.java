package com.example.deepseapong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.View;
import java.util.Random;

public class PongView extends View {
    private final Paint p = new Paint(3);
    private final Random random = new Random();
    private float w, h, ballX, ballY, vx, vy, playerY, cpuY;
    private float paddleH, paddleW, ballR;
    private int playerScore = 0, cpuScore = 0;
    private boolean started = false, gameOver = false, paused = false;
    private long lastTime = 0;
    private final float[] bubblesX = new float[32], bubblesY = new float[32], bubblesR = new float[32];

    public PongView(Context c) {
        super(c);
        setFocusable(true);
        for (int i=0;i<bubblesX.length;i++) {
            bubblesX[i] = random.nextFloat();
            bubblesY[i] = random.nextFloat();
            bubblesR[i] = 2 + random.nextFloat()*7;
        }
    }

    private void resetBall(float direction) {
        ballX = w/2; ballY = h/2;
        vx = direction * Math.max(260, w*0.32f);
        vy = (random.nextFloat() - .5f) * Math.max(260, h*.48f);
    }

    @Override protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        w=width; h=height;
        paddleW = Math.max(16, w*.018f);
        paddleH = h*.19f;
        ballR = Math.max(8, h*.018f);
        playerY = cpuY = h/2;
        resetBall(random.nextBoolean()?1:-1);
    }

    public boolean handleRemoteKey(int key) {
        float step = Math.max(30, h*.09f);
        if (key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_W) {
            playerY = Math.max(paddleH/2, playerY-step); invalidate(); return true;
        }
        if (key == KeyEvent.KEYCODE_DPAD_DOWN || key == KeyEvent.KEYCODE_S) {
            playerY = Math.min(h-paddleH/2, playerY+step); invalidate(); return true;
        }
        if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER ||
            key == KeyEvent.KEYCODE_BUTTON_A || key == KeyEvent.KEYCODE_SPACE) {
            if (gameOver) { playerScore=cpuScore=0; gameOver=false; }
            started = !started;
            lastTime = 0;
            invalidate();
            return true;
        }
        return false;
    }

    public void pauseGame() { paused=true; }
    public void resumeGame() { paused=false; lastTime=0; postInvalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (w <= 0 || h <= 0) { w=getWidth(); h=getHeight(); }
        c.drawColor(Color.rgb(2, 18, 49));
        // Deep-water glow
        p.setShader(new android.graphics.LinearGradient(0,0,w,h, new int[]{Color.rgb(2,32,73),Color.rgb(0,92,112),Color.rgb(2,19,50)}, null, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p); p.setShader(null);
        // Rays and drifting bubbles
        p.setColor(Color.argb(20,110,255,236));
        for(int i=0;i<5;i++) {
            float x=w*(.08f+i*.22f);
            c.drawOval(new RectF(x,-h*.2f,x+w*.08f,h*.75f),p);
        }
        p.setColor(Color.argb(80,150,246,255));
        for(int i=0;i<bubblesX.length;i++) {
            float x=bubblesX[i]*w, y=(bubblesY[i]*h);
            c.drawCircle(x,y,bubblesR[i],p);
            if(started && !paused) { bubblesY[i]-=.0017f; if(bubblesY[i]<0) bubblesY[i]=1; }
        }
        // Dotted center line
        p.setColor(Color.argb(110,135,255,239));
        for(float y=20;y<h;y+=h*.045f) c.drawRoundRect(w/2-2,y,w/2+2,y+h*.022f,4,4,p);
        // Seaweed/coral decoration
        p.setColor(Color.rgb(0,205,166));
        for(int i=0;i<7;i++) {
            float x=i*w/6f;
            c.drawOval(new RectF(x-9,h-45,x+9,h+12),p);
            c.drawOval(new RectF(x-22,h-34,x-7,h+8),p);
        }
        // paddles
        p.setShadowLayer(18,0,0,Color.CYAN);
        p.setColor(Color.rgb(53,245,221));
        c.drawRoundRect(new RectF(w*.055f,playerY-paddleH/2,w*.055f+paddleW,playerY+paddleH/2),paddleW,paddleW,p);
        p.setShadowLayer(18,0,0,Color.MAGENTA);
        p.setColor(Color.rgb(255,113,190));
        c.drawRoundRect(new RectF(w*.945f-paddleW,cpuY-paddleH/2,w*.945f,cpuY+paddleH/2),paddleW,paddleW,p);
        p.clearShadowLayer();
        // ball
        p.setColor(Color.WHITE); p.setShadowLayer(15,0,0,Color.CYAN);
        c.drawCircle(ballX,ballY,ballR,p); p.clearShadowLayer();
        // Score
        p.setTextAlign(Paint.Align.CENTER); p.setTypeface(android.graphics.Typeface.create("sans-serif",1));
        p.setTextSize(h*.105f); p.setColor(Color.WHITE);
        c.drawText(""+playerScore,w*.39f,h*.16f,p);
        c.drawText(""+cpuScore,w*.61f,h*.16f,p);
        p.setTextSize(h*.035f); p.setColor(Color.rgb(142,244,239));
        c.drawText("DEEP SEA PONG",w/2,h*.07f,p);
        if(!started || gameOver) {
            p.setTextSize(h*.055f); p.setColor(Color.WHITE);
            String msg = gameOver ? (playerScore>=7 ? "YOU WIN!" : "OCEAN WINS!") : "PRESS OK TO DIVE IN";
            c.drawText(msg,w/2,h*.77f,p);
            p.setTextSize(h*.027f); p.setColor(Color.rgb(173,226,240));
            c.drawText(gameOver ? "PRESS OK TO PLAY AGAIN" : "REMOTE: UP / DOWN TO MOVE",w/2,h*.84f,p);
        }
        if(started && !paused && !gameOver) {
            long now=System.nanoTime();
            float dt=lastTime==0?0:Math.min(.035f,(now-lastTime)/1_000_000_000f);
            lastTime=now;
            updateGame(dt);
            postInvalidateDelayed(16);
        } else if(!started) lastTime=0;
    }

    private void updateGame(float dt) {
        if(dt<=0) return;
        ballX+=vx*dt; ballY+=vy*dt;
        if(ballY<ballR || ballY>h-ballR) vy=-vy;
        float leftX=w*.055f+paddleW, rightX=w*.945f-paddleW;
        if(vx<0 && ballX-ballR<=leftX && ballX>leftX-paddleW*1.8f &&
           Math.abs(ballY-playerY)<paddleH/2+ballR) {
            vx=Math.abs(vx)*1.035f; vy += (ballY-playerY)*2.1f;
        }
        if(vx>0 && ballX+ballR>=rightX && ballX<rightX+paddleW*1.8f &&
           Math.abs(ballY-cpuY)<paddleH/2+ballR) {
            vx=-Math.abs(vx)*1.02f; vy += (ballY-cpuY)*1.6f;
        }
        float target=ballY;
        cpuY += Math.signum(target-cpuY)*Math.min(Math.abs(target-cpuY),h*.72f*dt);
        cpuY=Math.max(paddleH/2,Math.min(h-paddleH/2,cpuY));
        if(ballX<0) { cpuScore++; pointScored(-1); }
        if(ballX>w) { playerScore++; pointScored(1); }
        if(playerScore>=7 || cpuScore>=7) { gameOver=true; started=false; }
    }

    private void pointScored(float dir) { resetBall(dir); started=false; }
}
