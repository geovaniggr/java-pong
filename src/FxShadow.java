import java.awt.Color;
// public class Shadow {
  
//   private double cx;
//   private double cy;
//   private double size;

//   public Shadow(double cx, double cy, double size){
//     this.cx = cx;
//     this.cy = cy;
//     this.size = size;

//   }

//   public double getCx() {
//     return cx;
//   }

//   public void setCx(double cx) {
//     this.cx = cx;
//   }

//   public double getCy() {
//     return cy;
//   }

//   public void setCy(double cy) {
//     this.cy = cy;
//   }
//   public double getSize() {
//     return size;
//   }
//   public void setSize(double size) {
//     this.size = size;
//   }
// }

public class FxShadow {
  private double cx;
  private double cy;
  private double size;
  private Color color;

  public FxShadow(double cx, double cy, double size, Color color){
    this.cx = cx;
    this.cy = cy;
    this.size = size;
    this.color = color.darker().darker();
  }

  public void draw(){
    GameLib.setColor(color);
    GameLib.fillRect(cx, cy, size, size);
    size--;
  }

  public double getSize(){
    return size;
  }

}
