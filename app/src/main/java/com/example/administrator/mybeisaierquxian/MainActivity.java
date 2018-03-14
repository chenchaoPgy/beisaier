package com.example.administrator.mybeisaierquxian;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
    RelativeLayout mContainer = null;
    Button bt = null;
    ImageView shoppingCarImgV = null;
    PathMeasure mPathMeasure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mContainer = (RelativeLayout) findViewById(R.id.content_layout);
        bt = (Button) findViewById(R.id.bt);
        shoppingCarImgV = (ImageView) findViewById(R.id.shopping_car);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCar(bt);
            }
        });
    }

    private void addToCar(Button clickBt) {
        // 先把商品的要做抛物动画的View new出来
        final ImageView goods = new ImageView(MainActivity.this);
        goods.setImageResource(R.mipmap.ic_launcher);
        goods.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        mContainer.addView(goods, params);

        //得到点击按钮的坐标作为起始点,这个得到的是clickBt的中心点
        int startLoc[] = new int[2];
        clickBt.getLocationOnScreen(startLoc);

        //得到购物车图片的坐标(用于计算动画结束后的坐标)
        int endLoc[] = new int[2];
        shoppingCarImgV.getLocationOnScreen(endLoc);

        // 正式开始计算动画开始/结束的坐标
        // 从点击的那个按钮的中心点开始动画
        float startX = startLoc[0];
        float startY = startLoc[1];
        // 落入购物车的中心点，这样可能会导致做动画的图片底部会超出购物车图片的底部，因此要给toY减去购物车高度的一半
        float toX = endLoc[0];
        float toY = endLoc[1] - shoppingCarImgV.getHeight() / 2;

        // 计算中间动画的插值坐标（贝塞尔曲线）（其实就是用贝塞尔曲线来完成起终点的过程）
        //开始绘制贝塞尔曲线
        Path path = new Path();
        // 移动到起始点（贝塞尔曲线的起点）,其实就是从点击按钮的中点开始
        path.moveTo(startX, startY);
        // 使用贝塞尔曲线， 中间减200是把控制点稍微往上移，这样就会有抛物线的效果
        path.quadTo((startX + toX) / 2, startY - 200, toX, toY);
        //mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，如果是true，path会形成一个闭环
        mPathMeasure = new PathMeasure(path, false);

        // 用属性动画来实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        valueAnimator.setDuration(500);
        // 匀速线性插值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 当插值计算进行时，获取中间的每个值，
                // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
                float value = (Float) animation.getAnimatedValue();
                // ★★★★★获取当前点坐标封装到mCurrentPosition
                // boolean getPosTan(float distance, float[] pos, float[] tan) ：
                // 传入一个距离distance(0<=distance<=getLength())，会根据当前距离计算当前距离的坐标，保存到mCurrentPosition中
                float[] mCurrentPosition = new float[2];
                mPathMeasure.getPosTan(value, mCurrentPosition, null);//mCurrentPosition此时就是中间距离点的坐标值
                // 移动的商品图片（动画图片）的坐标设置为该中间点的坐标
                goods.setTranslationX(mCurrentPosition[0]);
                goods.setTranslationY(mCurrentPosition[1]);
            }
        });
        // 开始动画
        valueAnimator.start();

        //  动画结束后把做动画的图片移除
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            //当动画结束后：
            @Override
            public void onAnimationEnd(Animator animation) {
                // 把移动的图片imageview从父布局里移除
                mContainer.removeView(goods);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
