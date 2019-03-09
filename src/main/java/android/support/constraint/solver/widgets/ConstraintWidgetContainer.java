package android.support.constraint.solver.widgets;

import android.support.constraint.solver.LinearSystem;
import android.support.constraint.solver.Metrics;
import android.support.constraint.solver.widgets.ConstraintAnchor.Type;
import android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstraintWidgetContainer extends WidgetContainer {
    private static final boolean DEBUG = false;
    static final boolean DEBUG_GRAPH = false;
    private static final boolean DEBUG_LAYOUT = false;
    private static final int MAX_ITERATIONS = 8;
    private static final boolean USE_SNAPSHOT = true;
    int mDebugSolverPassCount;
    public boolean mGroupsWrapOptimized;
    private boolean mHeightMeasuredTooSmall;
    ChainHead[] mHorizontalChainsArray;
    int mHorizontalChainsSize;
    public boolean mHorizontalWrapOptimized;
    private boolean mIsRtl;
    private int mOptimizationLevel;
    int mPaddingBottom;
    int mPaddingLeft;
    int mPaddingRight;
    int mPaddingTop;
    public boolean mSkipSolver;
    private Snapshot mSnapshot;
    protected LinearSystem mSystem;
    ChainHead[] mVerticalChainsArray;
    int mVerticalChainsSize;
    public boolean mVerticalWrapOptimized;
    public List<ConstraintWidgetGroup> mWidgetGroups;
    private boolean mWidthMeasuredTooSmall;
    public int mWrapFixedHeight;
    public int mWrapFixedWidth;

    public String getType() {
        return "ConstraintLayout";
    }

    public boolean handlesInternalConstraints() {
        return false;
    }

    public void fillMetrics(Metrics metrics) {
        this.mSystem.fillMetrics(metrics);
    }

    public ConstraintWidgetContainer() {
        this.mIsRtl = false;
        this.mSystem = new LinearSystem();
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
        this.mVerticalChainsArray = new ChainHead[4];
        this.mHorizontalChainsArray = new ChainHead[4];
        this.mWidgetGroups = new ArrayList();
        this.mGroupsWrapOptimized = false;
        this.mHorizontalWrapOptimized = false;
        this.mVerticalWrapOptimized = false;
        this.mWrapFixedWidth = 0;
        this.mWrapFixedHeight = 0;
        this.mOptimizationLevel = 7;
        this.mSkipSolver = false;
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
        this.mDebugSolverPassCount = 0;
    }

    public ConstraintWidgetContainer(int i, int i2, int i3, int i4) {
        super(i, i2, i3, i4);
        this.mIsRtl = false;
        this.mSystem = new LinearSystem();
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
        this.mVerticalChainsArray = new ChainHead[4];
        this.mHorizontalChainsArray = new ChainHead[4];
        this.mWidgetGroups = new ArrayList();
        this.mGroupsWrapOptimized = false;
        this.mHorizontalWrapOptimized = false;
        this.mVerticalWrapOptimized = false;
        this.mWrapFixedWidth = 0;
        this.mWrapFixedHeight = 0;
        this.mOptimizationLevel = 7;
        this.mSkipSolver = false;
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
        this.mDebugSolverPassCount = 0;
    }

    public ConstraintWidgetContainer(int i, int i2) {
        super(i, i2);
        this.mIsRtl = false;
        this.mSystem = new LinearSystem();
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
        this.mVerticalChainsArray = new ChainHead[4];
        this.mHorizontalChainsArray = new ChainHead[4];
        this.mWidgetGroups = new ArrayList();
        this.mGroupsWrapOptimized = false;
        this.mHorizontalWrapOptimized = false;
        this.mVerticalWrapOptimized = false;
        this.mWrapFixedWidth = 0;
        this.mWrapFixedHeight = 0;
        this.mOptimizationLevel = 7;
        this.mSkipSolver = false;
        this.mWidthMeasuredTooSmall = false;
        this.mHeightMeasuredTooSmall = false;
        this.mDebugSolverPassCount = 0;
    }

    public void setOptimizationLevel(int i) {
        this.mOptimizationLevel = i;
    }

    public int getOptimizationLevel() {
        return this.mOptimizationLevel;
    }

    public boolean optimizeFor(int i) {
        return (this.mOptimizationLevel & i) == i;
    }

    public void reset() {
        this.mSystem.reset();
        this.mPaddingLeft = 0;
        this.mPaddingRight = 0;
        this.mPaddingTop = 0;
        this.mPaddingBottom = 0;
        this.mWidgetGroups.clear();
        this.mSkipSolver = false;
        super.reset();
    }

    public boolean isWidthMeasuredTooSmall() {
        return this.mWidthMeasuredTooSmall;
    }

    public boolean isHeightMeasuredTooSmall() {
        return this.mHeightMeasuredTooSmall;
    }

    public boolean addChildrenToSolver(LinearSystem linearSystem) {
        addToSolver(linearSystem);
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            if (constraintWidget instanceof ConstraintWidgetContainer) {
                DimensionBehaviour dimensionBehaviour = constraintWidget.mListDimensionBehaviors[0];
                DimensionBehaviour dimensionBehaviour2 = constraintWidget.mListDimensionBehaviors[1];
                if (dimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED);
                }
                if (dimensionBehaviour2 == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED);
                }
                constraintWidget.addToSolver(linearSystem);
                if (dimensionBehaviour == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setHorizontalDimensionBehaviour(dimensionBehaviour);
                }
                if (dimensionBehaviour2 == DimensionBehaviour.WRAP_CONTENT) {
                    constraintWidget.setVerticalDimensionBehaviour(dimensionBehaviour2);
                }
            } else {
                Optimizer.checkMatchParent(this, linearSystem, constraintWidget);
                constraintWidget.addToSolver(linearSystem);
            }
        }
        if (this.mHorizontalChainsSize > 0) {
            Chain.applyChainConstraints(this, linearSystem, 0);
        }
        if (this.mVerticalChainsSize > 0) {
            Chain.applyChainConstraints(this, linearSystem, 1);
        }
        return true;
    }

    public void updateChildrenFromSolver(LinearSystem linearSystem, boolean[] zArr) {
        zArr[2] = false;
        updateFromSolver(linearSystem);
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            constraintWidget.updateFromSolver(linearSystem);
            if (constraintWidget.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget.getWidth() < constraintWidget.getWrapWidth()) {
                zArr[2] = true;
            }
            if (constraintWidget.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget.getHeight() < constraintWidget.getWrapHeight()) {
                zArr[2] = true;
            }
        }
    }

    public void setPadding(int i, int i2, int i3, int i4) {
        this.mPaddingLeft = i;
        this.mPaddingTop = i2;
        this.mPaddingRight = i3;
        this.mPaddingBottom = i4;
    }

    public void setRtl(boolean z) {
        this.mIsRtl = z;
    }

    public boolean isRtl() {
        return this.mIsRtl;
    }

    public void analyze(int i) {
        super.analyze(i);
        int size = this.mChildren.size();
        for (int i2 = 0; i2 < size; i2++) {
            ((ConstraintWidget) this.mChildren.get(i2)).analyze(i);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x0191  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0188  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0292  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0285  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0295  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0188  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0191  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01dc  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0285  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0292  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0295  */
    public void layout() {
        /*
        r23 = this;
        r1 = r23;
        r2 = r1.mX;
        r3 = r1.mY;
        r0 = r23.getWidth();
        r4 = 0;
        r5 = java.lang.Math.max(r4, r0);
        r0 = r23.getHeight();
        r6 = java.lang.Math.max(r4, r0);
        r1.mWidthMeasuredTooSmall = r4;
        r1.mHeightMeasuredTooSmall = r4;
        r0 = r1.mParent;
        if (r0 == 0) goto L_0x0046;
    L_0x001f:
        r0 = r1.mSnapshot;
        if (r0 != 0) goto L_0x002a;
    L_0x0023:
        r0 = new android.support.constraint.solver.widgets.Snapshot;
        r0.<init>(r1);
        r1.mSnapshot = r0;
    L_0x002a:
        r0 = r1.mSnapshot;
        r0.updateFrom(r1);
        r0 = r1.mPaddingLeft;
        r1.setX(r0);
        r0 = r1.mPaddingTop;
        r1.setY(r0);
        r23.resetAnchors();
        r0 = r1.mSystem;
        r0 = r0.getCache();
        r1.resetSolverVariables(r0);
        goto L_0x004a;
    L_0x0046:
        r1.mX = r4;
        r1.mY = r4;
    L_0x004a:
        r0 = r1.mOptimizationLevel;
        r7 = 32;
        r8 = 8;
        r9 = 1;
        if (r0 == 0) goto L_0x006a;
    L_0x0053:
        r0 = r1.optimizeFor(r8);
        if (r0 != 0) goto L_0x005c;
    L_0x0059:
        r23.optimizeReset();
    L_0x005c:
        r0 = r1.optimizeFor(r7);
        if (r0 != 0) goto L_0x0065;
    L_0x0062:
        r23.optimize();
    L_0x0065:
        r0 = r1.mSystem;
        r0.graphOptimizer = r9;
        goto L_0x006e;
    L_0x006a:
        r0 = r1.mSystem;
        r0.graphOptimizer = r4;
    L_0x006e:
        r0 = r1.mListDimensionBehaviors;
        r10 = r0[r9];
        r0 = r1.mListDimensionBehaviors;
        r11 = r0[r4];
        r23.resetChains();
        r0 = r1.mWidgetGroups;
        r0 = r0.size();
        if (r0 != 0) goto L_0x0092;
    L_0x0081:
        r0 = r1.mWidgetGroups;
        r0.clear();
        r0 = r1.mWidgetGroups;
        r12 = new android.support.constraint.solver.widgets.ConstraintWidgetGroup;
        r13 = r1.mChildren;
        r12.<init>(r13);
        r0.add(r4, r12);
    L_0x0092:
        r0 = r1.mWidgetGroups;
        r12 = r0.size();
        r13 = r1.mChildren;
        r0 = r23.getHorizontalDimensionBehaviour();
        r14 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (r0 == r14) goto L_0x00ad;
    L_0x00a2:
        r0 = r23.getVerticalDimensionBehaviour();
        r14 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (r0 != r14) goto L_0x00ab;
    L_0x00aa:
        goto L_0x00ad;
    L_0x00ab:
        r14 = 0;
        goto L_0x00ae;
    L_0x00ad:
        r14 = 1;
    L_0x00ae:
        r0 = 0;
        r15 = 0;
    L_0x00b0:
        if (r15 >= r12) goto L_0x02f6;
    L_0x00b2:
        r8 = r1.mSkipSolver;
        if (r8 != 0) goto L_0x02f6;
    L_0x00b6:
        r8 = r1.mWidgetGroups;
        r8 = r8.get(r15);
        r8 = (android.support.constraint.solver.widgets.ConstraintWidgetGroup) r8;
        r8 = r8.mSkipSolver;
        if (r8 == 0) goto L_0x00c6;
    L_0x00c2:
        r21 = r12;
        goto L_0x02ea;
    L_0x00c6:
        r8 = r1.optimizeFor(r7);
        if (r8 == 0) goto L_0x00fb;
    L_0x00cc:
        r8 = r23.getHorizontalDimensionBehaviour();
        r7 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.FIXED;
        if (r8 != r7) goto L_0x00ed;
    L_0x00d4:
        r7 = r23.getVerticalDimensionBehaviour();
        r8 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.FIXED;
        if (r7 != r8) goto L_0x00ed;
    L_0x00dc:
        r7 = r1.mWidgetGroups;
        r7 = r7.get(r15);
        r7 = (android.support.constraint.solver.widgets.ConstraintWidgetGroup) r7;
        r7 = r7.getWidgetsToSolve();
        r7 = (java.util.ArrayList) r7;
        r1.mChildren = r7;
        goto L_0x00fb;
    L_0x00ed:
        r7 = r1.mWidgetGroups;
        r7 = r7.get(r15);
        r7 = (android.support.constraint.solver.widgets.ConstraintWidgetGroup) r7;
        r7 = r7.mConstrainedGroup;
        r7 = (java.util.ArrayList) r7;
        r1.mChildren = r7;
    L_0x00fb:
        r23.resetChains();
        r7 = r1.mChildren;
        r7 = r7.size();
        r8 = 0;
    L_0x0105:
        if (r8 >= r7) goto L_0x011d;
    L_0x0107:
        r4 = r1.mChildren;
        r4 = r4.get(r8);
        r4 = (android.support.constraint.solver.widgets.ConstraintWidget) r4;
        r9 = r4 instanceof android.support.constraint.solver.widgets.WidgetContainer;
        if (r9 == 0) goto L_0x0118;
    L_0x0113:
        r4 = (android.support.constraint.solver.widgets.WidgetContainer) r4;
        r4.layout();
    L_0x0118:
        r8 = r8 + 1;
        r4 = 0;
        r9 = 1;
        goto L_0x0105;
    L_0x011d:
        r9 = r0;
        r0 = 0;
        r4 = 1;
    L_0x0120:
        if (r4 == 0) goto L_0x02d9;
    L_0x0122:
        r18 = r4;
        r8 = 1;
        r4 = r0 + 1;
        r0 = r1.mSystem;	 Catch:{ Exception -> 0x0164 }
        r0.reset();	 Catch:{ Exception -> 0x0164 }
        r23.resetChains();	 Catch:{ Exception -> 0x0164 }
        r0 = r1.mSystem;	 Catch:{ Exception -> 0x0164 }
        r1.createObjectVariables(r0);	 Catch:{ Exception -> 0x0164 }
        r0 = 0;
    L_0x0135:
        if (r0 >= r7) goto L_0x014b;
    L_0x0137:
        r8 = r1.mChildren;	 Catch:{ Exception -> 0x0164 }
        r8 = r8.get(r0);	 Catch:{ Exception -> 0x0164 }
        r8 = (android.support.constraint.solver.widgets.ConstraintWidget) r8;	 Catch:{ Exception -> 0x0164 }
        r19 = r9;
        r9 = r1.mSystem;	 Catch:{ Exception -> 0x0162 }
        r8.createObjectVariables(r9);	 Catch:{ Exception -> 0x0162 }
        r0 = r0 + 1;
        r9 = r19;
        goto L_0x0135;
    L_0x014b:
        r19 = r9;
        r0 = r1.mSystem;	 Catch:{ Exception -> 0x0162 }
        r8 = r1.addChildrenToSolver(r0);	 Catch:{ Exception -> 0x0162 }
        if (r8 == 0) goto L_0x015d;
    L_0x0155:
        r0 = r1.mSystem;	 Catch:{ Exception -> 0x015b }
        r0.minimize();	 Catch:{ Exception -> 0x015b }
        goto L_0x015d;
    L_0x015b:
        r0 = move-exception;
        goto L_0x0169;
    L_0x015d:
        r20 = r8;
        r21 = r12;
        goto L_0x0186;
    L_0x0162:
        r0 = move-exception;
        goto L_0x0167;
    L_0x0164:
        r0 = move-exception;
        r19 = r9;
    L_0x0167:
        r8 = r18;
    L_0x0169:
        r0.printStackTrace();
        r9 = java.lang.System.out;
        r20 = r8;
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r21 = r12;
        r12 = "EXCEPTION : ";
        r8.append(r12);
        r8.append(r0);
        r0 = r8.toString();
        r9.println(r0);
    L_0x0186:
        if (r20 == 0) goto L_0x0191;
    L_0x0188:
        r8 = r1.mSystem;
        r9 = android.support.constraint.solver.widgets.Optimizer.flags;
        r1.updateChildrenFromSolver(r8, r9);
    L_0x018f:
        r9 = 2;
        goto L_0x01da;
    L_0x0191:
        r8 = r1.mSystem;
        r1.updateFromSolver(r8);
        r8 = 0;
    L_0x0197:
        if (r8 >= r7) goto L_0x018f;
    L_0x0199:
        r9 = r1.mChildren;
        r9 = r9.get(r8);
        r9 = (android.support.constraint.solver.widgets.ConstraintWidget) r9;
        r12 = r9.mListDimensionBehaviors;
        r17 = 0;
        r12 = r12[r17];
        r0 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r12 != r0) goto L_0x01bc;
    L_0x01ab:
        r0 = r9.getWidth();
        r12 = r9.getWrapWidth();
        if (r0 >= r12) goto L_0x01bc;
    L_0x01b5:
        r0 = android.support.constraint.solver.widgets.Optimizer.flags;
        r8 = 2;
        r12 = 1;
        r0[r8] = r12;
        goto L_0x018f;
    L_0x01bc:
        r12 = 1;
        r0 = r9.mListDimensionBehaviors;
        r0 = r0[r12];
        r12 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        if (r0 != r12) goto L_0x01d6;
    L_0x01c5:
        r0 = r9.getHeight();
        r9 = r9.getWrapHeight();
        if (r0 >= r9) goto L_0x01d6;
    L_0x01cf:
        r0 = android.support.constraint.solver.widgets.Optimizer.flags;
        r8 = 1;
        r9 = 2;
        r0[r9] = r8;
        goto L_0x01da;
    L_0x01d6:
        r9 = 2;
        r8 = r8 + 1;
        goto L_0x0197;
    L_0x01da:
        if (r14 == 0) goto L_0x0254;
    L_0x01dc:
        r8 = 8;
        if (r4 >= r8) goto L_0x0254;
    L_0x01e0:
        r0 = android.support.constraint.solver.widgets.Optimizer.flags;
        r0 = r0[r9];
        if (r0 == 0) goto L_0x0254;
    L_0x01e6:
        r0 = 0;
        r9 = 0;
        r12 = 0;
    L_0x01e9:
        if (r0 >= r7) goto L_0x0213;
    L_0x01eb:
        r8 = r1.mChildren;
        r8 = r8.get(r0);
        r8 = (android.support.constraint.solver.widgets.ConstraintWidget) r8;
        r22 = r4;
        r4 = r8.mX;
        r16 = r8.getWidth();
        r4 = r4 + r16;
        r9 = java.lang.Math.max(r9, r4);
        r4 = r8.mY;
        r8 = r8.getHeight();
        r4 = r4 + r8;
        r12 = java.lang.Math.max(r12, r4);
        r0 = r0 + 1;
        r4 = r22;
        r8 = 8;
        goto L_0x01e9;
    L_0x0213:
        r22 = r4;
        r0 = r1.mMinWidth;
        r0 = java.lang.Math.max(r0, r9);
        r4 = r1.mMinHeight;
        r4 = java.lang.Math.max(r4, r12);
        r8 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (r11 != r8) goto L_0x0239;
    L_0x0225:
        r8 = r23.getWidth();
        if (r8 >= r0) goto L_0x0239;
    L_0x022b:
        r1.setWidth(r0);
        r0 = r1.mListDimensionBehaviors;
        r8 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        r9 = 0;
        r0[r9] = r8;
        r0 = 1;
        r19 = 1;
        goto L_0x023a;
    L_0x0239:
        r0 = 0;
    L_0x023a:
        r8 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (r10 != r8) goto L_0x0251;
    L_0x023e:
        r8 = r23.getHeight();
        if (r8 >= r4) goto L_0x0251;
    L_0x0244:
        r1.setHeight(r4);
        r0 = r1.mListDimensionBehaviors;
        r4 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        r8 = 1;
        r0[r8] = r4;
        r0 = 1;
        r9 = 1;
        goto L_0x0259;
    L_0x0251:
        r9 = r19;
        goto L_0x0259;
    L_0x0254:
        r22 = r4;
        r9 = r19;
        r0 = 0;
    L_0x0259:
        r4 = r1.mMinWidth;
        r8 = r23.getWidth();
        r4 = java.lang.Math.max(r4, r8);
        r8 = r23.getWidth();
        if (r4 <= r8) goto L_0x0275;
    L_0x0269:
        r1.setWidth(r4);
        r0 = r1.mListDimensionBehaviors;
        r4 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.FIXED;
        r8 = 0;
        r0[r8] = r4;
        r0 = 1;
        r9 = 1;
    L_0x0275:
        r4 = r1.mMinHeight;
        r8 = r23.getHeight();
        r4 = java.lang.Math.max(r4, r8);
        r8 = r23.getHeight();
        if (r4 <= r8) goto L_0x0292;
    L_0x0285:
        r1.setHeight(r4);
        r0 = r1.mListDimensionBehaviors;
        r4 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.FIXED;
        r8 = 1;
        r0[r8] = r4;
        r0 = 1;
        r9 = 1;
        goto L_0x0293;
    L_0x0292:
        r8 = 1;
    L_0x0293:
        if (r9 != 0) goto L_0x02d2;
    L_0x0295:
        r4 = r1.mListDimensionBehaviors;
        r12 = 0;
        r4 = r4[r12];
        r12 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (r4 != r12) goto L_0x02b4;
    L_0x029e:
        if (r5 <= 0) goto L_0x02b4;
    L_0x02a0:
        r4 = r23.getWidth();
        if (r4 <= r5) goto L_0x02b4;
    L_0x02a6:
        r1.mWidthMeasuredTooSmall = r8;
        r0 = r1.mListDimensionBehaviors;
        r4 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.FIXED;
        r9 = 0;
        r0[r9] = r4;
        r1.setWidth(r5);
        r0 = 1;
        r9 = 1;
    L_0x02b4:
        r4 = r1.mListDimensionBehaviors;
        r4 = r4[r8];
        r12 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (r4 != r12) goto L_0x02d2;
    L_0x02bc:
        if (r6 <= 0) goto L_0x02d2;
    L_0x02be:
        r4 = r23.getHeight();
        if (r4 <= r6) goto L_0x02d2;
    L_0x02c4:
        r1.mHeightMeasuredTooSmall = r8;
        r0 = r1.mListDimensionBehaviors;
        r4 = android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour.FIXED;
        r0[r8] = r4;
        r1.setHeight(r6);
        r4 = 1;
        r9 = 1;
        goto L_0x02d3;
    L_0x02d2:
        r4 = r0;
    L_0x02d3:
        r12 = r21;
        r0 = r22;
        goto L_0x0120;
    L_0x02d9:
        r19 = r9;
        r21 = r12;
        r0 = r1.mWidgetGroups;
        r0 = r0.get(r15);
        r0 = (android.support.constraint.solver.widgets.ConstraintWidgetGroup) r0;
        r0.updateUnresolvedWidgets();
        r0 = r19;
    L_0x02ea:
        r15 = r15 + 1;
        r12 = r21;
        r4 = 0;
        r7 = 32;
        r8 = 8;
        r9 = 1;
        goto L_0x00b0;
    L_0x02f6:
        r13 = (java.util.ArrayList) r13;
        r1.mChildren = r13;
        r4 = r1.mParent;
        if (r4 == 0) goto L_0x032a;
    L_0x02fe:
        r2 = r1.mMinWidth;
        r3 = r23.getWidth();
        r2 = java.lang.Math.max(r2, r3);
        r3 = r1.mMinHeight;
        r4 = r23.getHeight();
        r3 = java.lang.Math.max(r3, r4);
        r4 = r1.mSnapshot;
        r4.applyTo(r1);
        r4 = r1.mPaddingLeft;
        r2 = r2 + r4;
        r4 = r1.mPaddingRight;
        r2 = r2 + r4;
        r1.setWidth(r2);
        r2 = r1.mPaddingTop;
        r3 = r3 + r2;
        r2 = r1.mPaddingBottom;
        r3 = r3 + r2;
        r1.setHeight(r3);
        goto L_0x032e;
    L_0x032a:
        r1.mX = r2;
        r1.mY = r3;
    L_0x032e:
        if (r0 == 0) goto L_0x033a;
    L_0x0330:
        r0 = r1.mListDimensionBehaviors;
        r2 = 0;
        r0[r2] = r11;
        r0 = r1.mListDimensionBehaviors;
        r2 = 1;
        r0[r2] = r10;
    L_0x033a:
        r0 = r1.mSystem;
        r0 = r0.getCache();
        r1.resetSolverVariables(r0);
        r0 = r23.getRootConstraintContainer();
        if (r1 != r0) goto L_0x034c;
    L_0x0349:
        r23.updateDrawPosition();
    L_0x034c:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.constraint.solver.widgets.ConstraintWidgetContainer.layout():void");
    }

    public void preOptimize() {
        optimizeReset();
        analyze(this.mOptimizationLevel);
    }

    public void solveGraph() {
        ResolutionAnchor resolutionNode = getAnchor(Type.LEFT).getResolutionNode();
        ResolutionAnchor resolutionNode2 = getAnchor(Type.TOP).getResolutionNode();
        resolutionNode.resolve(null, 0.0f);
        resolutionNode2.resolve(null, 0.0f);
    }

    public void resetGraph() {
        ResolutionAnchor resolutionNode = getAnchor(Type.LEFT).getResolutionNode();
        ResolutionAnchor resolutionNode2 = getAnchor(Type.TOP).getResolutionNode();
        resolutionNode.invalidateAnchors();
        resolutionNode2.invalidateAnchors();
        resolutionNode.resolve(null, 0.0f);
        resolutionNode2.resolve(null, 0.0f);
    }

    public void optimizeForDimensions(int i, int i2) {
        if (!(this.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT || this.mResolutionWidth == null)) {
            this.mResolutionWidth.resolve(i);
        }
        if (this.mListDimensionBehaviors[1] != DimensionBehaviour.WRAP_CONTENT && this.mResolutionHeight != null) {
            this.mResolutionHeight.resolve(i2);
        }
    }

    public void optimizeReset() {
        int size = this.mChildren.size();
        resetResolutionNodes();
        for (int i = 0; i < size; i++) {
            ((ConstraintWidget) this.mChildren.get(i)).resetResolutionNodes();
        }
    }

    public void optimize() {
        if (!optimizeFor(8)) {
            analyze(this.mOptimizationLevel);
        }
        solveGraph();
    }

    public ArrayList<Guideline> getVerticalGuidelines() {
        ArrayList arrayList = new ArrayList();
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            if (constraintWidget instanceof Guideline) {
                Guideline guideline = (Guideline) constraintWidget;
                if (guideline.getOrientation() == 1) {
                    arrayList.add(guideline);
                }
            }
        }
        return arrayList;
    }

    public ArrayList<Guideline> getHorizontalGuidelines() {
        ArrayList arrayList = new ArrayList();
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ConstraintWidget constraintWidget = (ConstraintWidget) this.mChildren.get(i);
            if (constraintWidget instanceof Guideline) {
                Guideline guideline = (Guideline) constraintWidget;
                if (guideline.getOrientation() == 0) {
                    arrayList.add(guideline);
                }
            }
        }
        return arrayList;
    }

    public LinearSystem getSystem() {
        return this.mSystem;
    }

    private void resetChains() {
        this.mHorizontalChainsSize = 0;
        this.mVerticalChainsSize = 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void addChain(ConstraintWidget constraintWidget, int i) {
        if (i == 0) {
            addHorizontalChain(constraintWidget);
        } else if (i == 1) {
            addVerticalChain(constraintWidget);
        }
    }

    private void addHorizontalChain(ConstraintWidget constraintWidget) {
        if (this.mHorizontalChainsSize + 1 >= this.mHorizontalChainsArray.length) {
            this.mHorizontalChainsArray = (ChainHead[]) Arrays.copyOf(this.mHorizontalChainsArray, this.mHorizontalChainsArray.length * 2);
        }
        this.mHorizontalChainsArray[this.mHorizontalChainsSize] = new ChainHead(constraintWidget, 0, isRtl());
        this.mHorizontalChainsSize++;
    }

    private void addVerticalChain(ConstraintWidget constraintWidget) {
        if (this.mVerticalChainsSize + 1 >= this.mVerticalChainsArray.length) {
            this.mVerticalChainsArray = (ChainHead[]) Arrays.copyOf(this.mVerticalChainsArray, this.mVerticalChainsArray.length * 2);
        }
        this.mVerticalChainsArray[this.mVerticalChainsSize] = new ChainHead(constraintWidget, 1, isRtl());
        this.mVerticalChainsSize++;
    }

    public List<ConstraintWidgetGroup> getWidgetGroups() {
        return this.mWidgetGroups;
    }
}
