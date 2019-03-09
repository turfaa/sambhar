package android.support.design.shape;

import android.support.design.internal.Experimental;

@Experimental("The shapes API is currently experimental and subject to change")
public class CutCornerTreatment extends CornerTreatment {
    private final float size;

    public CutCornerTreatment(float f) {
        this.size = f;
    }

    public void getCornerPath(float f, float f2, ShapePath shapePath) {
        shapePath.reset(0.0f, this.size * f2);
        double d = (double) f;
        double sin = Math.sin(d);
        double d2 = (double) this.size;
        Double.isNaN(d2);
        sin *= d2;
        double d3 = (double) f2;
        Double.isNaN(d3);
        float f3 = (float) (sin * d3);
        d = Math.cos(d);
        double d4 = (double) this.size;
        Double.isNaN(d4);
        d *= d4;
        Double.isNaN(d3);
        shapePath.lineTo(f3, (float) (d * d3));
    }
}
