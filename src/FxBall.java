import java.awt.Color;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FxBall extends Ball {

    private Color color;
    private ConcurrentLinkedQueue<FxShadow> shadow = new ConcurrentLinkedQueue<>();

    public FxBall(double cx, double cy, double width, double height, Color color, double speed, double vx, double vy) {
        super(cx, cy, width, height, color, speed, vx, vy);
        this.color = color;
    }

    public void draw() {
        shadow.add(new FxShadow(super.getCx(), super.getCy(), super.getHeight(), color));
        drawShadow();
        GameLib.setColor(color);
        GameLib.fillRect(super.getCx(), super.getCy(), super.getHeight(), super.getWidth());

    }

    public void drawShadow(){
        shadow.forEach(element -> {
            element.draw();
            if(element.getSize() < 1){
                FxShadow nullable = this.shadow.poll();
                nullable = null;
            }
        });
    }
}