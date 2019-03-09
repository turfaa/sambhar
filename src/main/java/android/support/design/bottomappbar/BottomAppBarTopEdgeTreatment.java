package android.support.design.bottomappbar;

import android.support.design.shape.EdgeTreatment;
import android.support.design.shape.ShapePath;

public class BottomAppBarTopEdgeTreatment extends EdgeTreatment {
    private static final int ANGLE_LEFT = 180;
    private static final int ANGLE_UP = 270;
    private static final int ARC_HALF = 180;
    private static final int ARC_QUARTER = 90;
    private float cradleVerticalOffset;
    private float fabDiameter;
    private float fabMargin;
    private float horizontalOffset;
    private float roundedCornerRadius;

    public BottomAppBarTopEdgeTreatment(float f, float f2, float f3) {
        this.fabMargin = f;
        this.roundedCornerRadius = f2;
        this.cradleVerticalOffset = f3;
        if (f3 >= 0.0f) {
            this.horizontalOffset = 0.0f;
            return;
        }
        throw new IllegalArgumentException("cradleVerticalOffset must be positive.");
    }

    public void getEdgePath(float f, float f2, ShapePath shapePath) {
        float f3 = f;
        ShapePath shapePath2 = shapePath;
        if (this.fabDiameter == 0.0f) {
            shapePath2.lineTo(f3, 0.0f);
            return;
        }
        float f4 = ((this.fabMargin * 2.0f) + this.fabDiameter) / 2.0f;
        float f5 = f2 * this.roundedCornerRadius;
        float f6 = (f3 / 2.0f) + this.horizontalOffset;
        float f7 = (this.cradleVerticalOffset * f2) + ((1.0f - f2) * f4);
        if (f7 / f4 >= 1.0f) {
            shapePath2.lineTo(f3, 0.0f);
            return;
        }
        float f8 = f4 + f5;
        float f9 = f7 + f5;
        f8 = (float) Math.sqrt((double) ((f8 * f8) - (f9 * f9)));
        float f10 = f6 - f8;
        float f11 = f6 + f8;
        float toDegrees = (float) Math.toDegrees(Math.atan((double) (f8 / f9)));
        float f12 = 90.0f - toDegrees;
        f9 = f10 - f5;
        shapePath2.lineTo(f9, 0.0f);
        float f13 = f5 * 2.0f;
        float f14 = toDegrees;
        shapePath.addArc(f9, 0.0f, f10 + f5, f13, 270.0f, toDegrees);
        shapePath.addArc(f6 - f4, (-f4) - f7, f6 + f4, f4 - f7, 180.0f - f12, (f12 * 2.0f) - 180.0f);
        shapePath.addArc(f11 - f5, 0.0f, f11 + f5, f13, 270.0f - f14, f14);
        shapePath2.lineTo(f3, 0.0f);
    }

    /* Access modifiers changed, original: 0000 */
    public void setHorizontalOffset(float f) {
        this.horizontalOffset = f;
    }

    /* Access modifiers changed, original: 0000 */
    public float getHorizontalOffset() {
        return this.horizontalOffset;
    }

    /* Access modifiers changed, original: 0000 */
    public float getCradleVerticalOffset() {
        return this.cradleVerticalOffset;
    }

    /* Access modifiers changed, original: 0000 */
    public void setCradleVerticalOffset(float f) {
        this.cradleVerticalOffset = f;
    }

    /* Access modifiers changed, original: 0000 */
    public float getFabDiameter() {
        return this.fabDiameter;
    }

    /* Access modifiers changed, original: 0000 */
    public void setFabDiameter(float f) {
        this.fabDiameter = f;
    }

    /* Access modifiers changed, original: 0000 */
    public float getFabCradleMargin() {
        return this.fabMargin;
    }

    /* Access modifiers changed, original: 0000 */
    public void setFabCradleMargin(float f) {
        this.fabMargin = f;
    }

    /* Access modifiers changed, original: 0000 */
    public float getFabCradleRoundedCornerRadius() {
        return this.roundedCornerRadius;
    }

    /* Access modifiers changed, original: 0000 */
    public void setFabCradleRoundedCornerRadius(float f) {
        this.roundedCornerRadius = f;
    }
}
