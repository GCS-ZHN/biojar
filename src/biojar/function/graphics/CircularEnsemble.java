package biojar.function.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.Graphics2D;
import java.io.IOException;

import com.lowagie.text.DocumentException;

public class CircularEnsemble {
	private int width = 3600;
	private int height = 3600;
	private Graphics2D graphics2d = null;
	private int[] nums = {14, 20, 32};
	private Color[] colorSets = {
			new Color(169, 209, 142),//green
			new Color(237, 125, 40),//orange
			new Color(157, 195, 230)//blue
	};
	private String[] labelSets = {"DT", "SVM", "Bayes"};
	public CircularEnsemble(int width, int height, String outputfile) throws DocumentException, IOException, Exception {
		String[] tmp = outputfile.split("\\.");
		this.width = width;
		this.height = height;
		CreateGraphics cGraphics = new CreateGraphics(this.width, this.height, tmp[tmp.length - 1], outputfile);
		graphics2d = cGraphics.getGraphics2D();
		drawCircule();
		cGraphics.saveToFlie();
		
	}
	public void drawCircule() throws Exception {
		double center_x = width/2;
		double center_y = height/2;
		double d = width /9;
		double inner_r = width/7;
		double step = 3*d/12;
		for (int i = 0; i < 3; i++) {
			drawCircule(center_x, center_y, inner_r + d, inner_r, nums[i], colorSets[i], labelSets[i]);
			inner_r = inner_r + d - step;
		}
	}
	private void drawCircule(double center_x, double center_y, double out_r, double inner_r, int num, Color color, String label) throws Exception {
		if (graphics2d == null) return;
		double stepAngle = 2*Math.PI/num;
		for (double currentAngle = 0.0; currentAngle < 2*Math.PI; currentAngle+=stepAngle) {
			double d = out_r - inner_r;
			double locat_x = center_x + (out_r + inner_r)/2 * Math.cos(currentAngle) - d/2;
			double locat_y = center_y - (out_r + inner_r)/2 * Math.sin(currentAngle) -d/2;
			Ellipse2D ellipse2d = new Ellipse2D.Double(locat_x, locat_y, d, d);
			Color oldColor = graphics2d.getColor();
			graphics2d.setColor(color);
			graphics2d.fill(ellipse2d);
			graphics2d.setColor(oldColor);
			graphics2d.setFont(new Font("Courier New", Font.BOLD, (int) (0.17*d)));
			double rotated =(currentAngle >= Math.PI/2 && currentAngle <=3*Math.PI/2)?180 -currentAngle*180/Math.PI: -currentAngle*180/Math.PI;
			drawSimpleLabel(label, locat_x + d/2, locat_y +d/2, rotated, 0, "m","m", Color.WHITE);
		}
	}
	private void drawSimpleLabel (String label, double rotateCenter_x, double rotateCenter_y, double rotateDegree, double rotateR, String h_mode, String v_mode, Color FontColor) throws Exception {
		if (graphics2d == null) return;
		FontMetrics metrics = graphics2d.getFontMetrics();
		float baseline_x;
		float baseline_y;
		int locat_x = (int) (rotateCenter_x - rotateR), locat_y = (int) rotateCenter_y;
		if (h_mode.equals("l")) {
			locat_x = (int) (rotateCenter_x + rotateR);
		}
		switch	 (v_mode) {//垂直对齐方式
			case "m":{baseline_y =locat_y*1f - metrics.getHeight()/2f + metrics.getAscent();break;}
			case "u":{baseline_y =locat_y + metrics.getAscent();break;}
			case "d":{baseline_y =locat_y - metrics.getHeight() + metrics.getAscent();break;}
			default: throw new Exception("Ilegal mode symbol: "+v_mode);
		}
		switch (h_mode) {//水平对齐方式
			case "m":{baseline_x =locat_x*1f - metrics.stringWidth(label)/2f;break;}
			case "l":{baseline_x =locat_x;break;}
			case "r": {baseline_x =locat_x - metrics.stringWidth(label);break;}
			default: throw new Exception("Ilegal mode symbol: "+h_mode);
		}
		graphics2d.setColor(FontColor);//设置字体颜色
		graphics2d.rotate(rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
		graphics2d.drawString(label, baseline_x, baseline_y);
		graphics2d.rotate(-rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
	}
	public static void main(String[] args) {
			try {
				new CircularEnsemble(3600, 3600, "ensemble learning.png");
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
