package github.orient33.demo.videodemo;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 垂直移动View
 */
public class TouchListen implements View.OnTouchListener {
    final ViewGroup follow;

    TouchListen(View t) {
        follow = (ViewGroup) t.getParent().getParent();
        t.setOnTouchListener(this);
    }

    int eventY;
    float downY;

    public boolean onTouch(View v, MotionEvent event) {
        int ey = (int) event.getY();
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downY = follow.getY();
                eventY = ey;
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = ey - eventY;
                follow.setY(downY + dy);
                break;
            case MotionEvent.ACTION_UP:
                downY = 0;
                break;
            default:
                return false;
        }
        return true;
    }
}
